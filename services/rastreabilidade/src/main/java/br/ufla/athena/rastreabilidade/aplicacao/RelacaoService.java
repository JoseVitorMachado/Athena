package br.ufla.athena.rastreabilidade.aplicacao;

import br.ufla.athena.rastreabilidade.api.Problema;
import br.ufla.athena.rastreabilidade.api.RelacaoEntrada;
import br.ufla.athena.rastreabilidade.dominio.Relacao;
import br.ufla.athena.rastreabilidade.dominio.RelacaoRepository;
import br.ufla.athena.rastreabilidade.dominio.TipoRelacao;
import java.util.UUID;
import java.util.LinkedHashMap;
import java.util.Map;
import br.ufla.athena.rastreabilidade.dominio.GrafoPrecedencias;
import br.ufla.athena.rastreabilidade.mensageria.EmissorMensagens;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RelacaoService {

    private final RelacaoRepository repositorio;
    private final JdbcTemplate banco;
    private final ClienteRequisitos requisitos;
    private final EmissorMensagens emissor;

    public RelacaoService(RelacaoRepository repositorio, JdbcTemplate banco, ClienteRequisitos requisitos, EmissorMensagens emissor) {
        this.repositorio = repositorio;
        this.banco = banco;
        this.requisitos = requisitos;
        this.emissor = emissor;
    }

    @Transactional(readOnly = true)
    public Page<Relacao> listar(UUID projetoId, UUID requisitoId, TipoRelacao tipo, Pageable paginacao) {
        Specification<Relacao> filtro = (relacao, consulta, cb) -> cb.equal(relacao.get("projetoId"), projetoId);
        if (tipo != null) {
            filtro = filtro.and((relacao, consulta, cb) -> cb.equal(relacao.get("tipo"), tipo));
        }
        if (requisitoId != null) {
            filtro = filtro.and((relacao, consulta, cb) -> cb.or(
                    cb.equal(relacao.get("origemId"), requisitoId),
                    cb.equal(relacao.get("destinoId"), requisitoId)));
        }
        return repositorio.findAll(filtro, paginacao);
    }

    @Transactional
    public Relacao criar(UUID projetoId, RelacaoEntrada entrada) {
        travar(projetoId);
        Relacao relacao = Relacao.nova(projetoId, entrada.origemId(), entrada.destinoId(), entrada.tipo(), entrada.justificativa());
        validar(relacao, null);
        repositorio.saveAndFlush(relacao);
        emitir("RelacaoCriada", relacao, relacao.getRevisao());
        return relacao;
    }

    @Transactional
    public Relacao editar(UUID projetoId, UUID id, String ifMatch, Map<String, Object> patch) {
        travar(projetoId);
        Relacao relacao = obter(projetoId, id, ifMatch);
        if (patch.isEmpty() || !java.util.Set.of("origemId", "destinoId", "tipo", "justificativa").containsAll(patch.keySet())) {
            throw new Problema(HttpStatus.BAD_REQUEST, "PATCH_INVALIDO", "Informe campos válidos para alteração.");
        }
        try {
            UUID origem = patch.containsKey("origemId") ? UUID.fromString(patch.get("origemId").toString()) : relacao.getOrigemId();
            UUID destino = patch.containsKey("destinoId") ? UUID.fromString(patch.get("destinoId").toString()) : relacao.getDestinoId();
            TipoRelacao tipo = patch.containsKey("tipo") ? TipoRelacao.valueOf(patch.get("tipo").toString()) : relacao.getTipo();
            String justificativa = patch.containsKey("justificativa") ? (String) patch.get("justificativa") : relacao.getJustificativa();
            if (justificativa != null && justificativa.length() > 1000) {
                throw new IllegalArgumentException();
            }
            relacao.alterar(origem, destino, tipo, justificativa);
        } catch (IllegalArgumentException | NullPointerException | ClassCastException e) {
            throw new Problema(HttpStatus.BAD_REQUEST, "PATCH_INVALIDO", "Campos de alteração inválidos.");
        }
        validar(relacao, id);
        repositorio.saveAndFlush(relacao);
        emitir("RelacaoAlterada", relacao, relacao.getRevisao());
        return relacao;
    }

    @Transactional
    public void remover(UUID projetoId, UUID id, String ifMatch) {
        travar(projetoId);
        Relacao relacao = obter(projetoId, id, ifMatch);
        long revisao = relacao.getRevisao() + 1;
        repositorio.delete(relacao);
        repositorio.flush();
        emitir("RelacaoRemovida", relacao, revisao);
    }

    private void travar(UUID projetoId) {
        banco.query("SELECT pg_advisory_xact_lock(hashtext(?))", rs -> { }, projetoId.toString());
    }

    private Relacao obter(UUID projetoId, UUID id, String ifMatch) {
        if (ifMatch == null || ifMatch.isBlank()) {
            throw new Problema(HttpStatus.PRECONDITION_REQUIRED, "PRECONDICAO_AUSENTE", "Informe If-Match.");
        }
        Relacao relacao = repositorio.findByIdAndProjetoId(id, projetoId).orElseThrow(() ->
                new Problema(HttpStatus.NOT_FOUND, "RELACAO_NAO_ENCONTRADA", "Relação não encontrada."));
        if (!ifMatch.equals("\"" + relacao.getRevisao() + "\"")) {
            throw new Problema(HttpStatus.PRECONDITION_FAILED, "REVISAO_DESATUALIZADA", "Revisão desatualizada.");
        }
        return relacao;
    }

    private void validar(Relacao relacao, UUID ignorar) {
        UUID origem = relacao.getOrigemId();
        UUID destino = relacao.getDestinoId();
        if (origem.equals(destino)) {
            throw new Problema(HttpStatus.BAD_REQUEST, "AUTORRELACAO", "Um requisito não pode se relacionar com ele mesmo.");
        }
        if (relacao.getTipo() == TipoRelacao.RELACIONADO_A && origem.toString().compareTo(destino.toString()) > 0) {
            relacao.alterar(destino, origem, relacao.getTipo(), relacao.getJustificativa());
            origem = relacao.getOrigemId();
            destino = relacao.getDestinoId();
        }
        requisitos.validar(relacao.getProjetoId(), origem);
        requisitos.validar(relacao.getProjetoId(), destino);
        GrafoPrecedencias grafo = new GrafoPrecedencias();
        for (Relacao existente : repositorio.findAllByProjetoId(relacao.getProjetoId())) {
            if (existente.getId().equals(ignorar)) {
                continue;
            }
            if (existente.getOrigemId().equals(origem) && existente.getDestinoId().equals(destino)
                    && existente.getTipo() == relacao.getTipo()) {
                throw new Problema(HttpStatus.CONFLICT, "RELACAO_DUPLICADA", "Conflito: relação duplicada.");
            }
            grafo.adicionar(existente.getOrigemId(), existente.getDestinoId(), existente.getTipo());
        }
        if (grafo.criaCiclo(origem, destino, relacao.getTipo())) {
            throw new Problema(HttpStatus.CONFLICT, "RELACAO_CICLICA", "Conflito: relação cíclica.");
        }
    }

    private void emitir(String tipo, Relacao relacao, long revisao) {
        Long posicao = banco.queryForObject("INSERT INTO sequencias_escrita (projeto_id, posicao) VALUES (?, 1) "
                + "ON CONFLICT (projeto_id) DO UPDATE SET posicao = sequencias_escrita.posicao + 1 RETURNING posicao",
                Long.class, relacao.getProjetoId());
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("relacaoId", relacao.getId());
        payload.put("projetoId", relacao.getProjetoId());
        if (!tipo.equals("RelacaoRemovida")) {
            payload.put("origemId", relacao.getOrigemId());
            payload.put("destinoId", relacao.getDestinoId());
            payload.put("tipo", relacao.getTipo());
            payload.put("justificativa", relacao.getJustificativa());
        }
        payload.put("revisao", revisao);
        payload.put("posicao", posicao);
        emissor.evento(tipo, relacao.getProjetoId(), null, null, UUID.randomUUID(), null, payload);
    }
}
