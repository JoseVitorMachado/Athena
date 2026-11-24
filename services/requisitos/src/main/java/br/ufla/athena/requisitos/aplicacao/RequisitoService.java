package br.ufla.athena.requisitos.aplicacao;

import br.ufla.athena.requisitos.api.Pagina;
import br.ufla.athena.requisitos.api.CandidataPatch;
import br.ufla.athena.requisitos.api.Problema;
import br.ufla.athena.requisitos.api.VersaoResposta;
import br.ufla.athena.requisitos.dominio.Versao;
import br.ufla.athena.requisitos.dominio.EstadoVersao;
import org.springframework.http.HttpStatus;
import br.ufla.athena.requisitos.api.RequisitoEntrada;
import br.ufla.athena.requisitos.api.RequisitoResposta;
import br.ufla.athena.requisitos.dominio.Requisito;
import br.ufla.athena.requisitos.dominio.RequisitoRepository;
import br.ufla.athena.requisitos.dominio.SituacaoRequisito;
import br.ufla.athena.requisitos.mensageria.EmissorMensagens;
import java.util.Map;
import java.util.List;
import java.time.Instant;
import java.sql.Timestamp;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.jdbc.core.JdbcTemplate;
import br.ufla.athena.requisitos.dominio.EpicoRepository;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RequisitoService {

    private final RequisitoRepository repositorio;
    private final EmissorMensagens emissor;
    private final EpicoRepository epicos;
    private final JdbcTemplate banco;
    private final UsuarioAtual usuario;

    public RequisitoService(RequisitoRepository repositorio, EmissorMensagens emissor, EpicoRepository epicos, JdbcTemplate banco, UsuarioAtual usuario) {
        this.repositorio = repositorio;
        this.emissor = emissor;
        this.epicos = epicos;
        this.banco = banco;
        this.usuario = usuario;
    }

    @Transactional(readOnly = true)
    public Pagina<RequisitoResposta> listar(br.ufla.athena.requisitos.dominio.FiltroRequisitos filtro,
                                            Pageable paginacao, String linkSelf) {
        Page<Requisito> pagina = repositorio.findAll(filtro.especificacao(), paginacao);
        return Pagina.de(pagina.map(RequisitoResposta::de), linkSelf);
    }

    @Transactional(readOnly = true)
    public Optional<Encontrado> buscar(UUID projetoId, UUID requisitoId) {
        return repositorio.findByIdAndProjetoId(requisitoId, projetoId)
                .map(requisito -> new Encontrado(RequisitoResposta.de(requisito), requisito.getRevisao()));
    }

    @Transactional
    public Encontrado criar(UUID projetoId, RequisitoEntrada entrada) {
        travarProjeto(projetoId);
        validarEpico(projetoId, entrada.epicoId());
        int numero = repositorio.reservarProximoNumero(projetoId);
        Requisito requisito = Requisito.novo(projetoId, numero, entrada.documentoId(), entrada.epicoId(),
                entrada.responsavelId(), entrada.tipo(), entrada.titulo(), entrada.conteudo(),
                entrada.criteriosAceite());
        Requisito salvo = repositorio.saveAndFlush(requisito);
        emitirRequisitoAlterado(salvo);
        return new Encontrado(RequisitoResposta.de(salvo), salvo.getRevisao());
    }

    @Transactional(readOnly = true)
    public CandidataEncontrada obterCandidata(UUID projetoId, UUID requisitoId) {
        Requisito requisito = repositorio.findByIdAndProjetoId(requisitoId, projetoId)
                .orElseThrow(RequisitoService::naoEncontrado);
        Versao candidata = requisito.candidata().orElseThrow(() ->
                new Problema(HttpStatus.NOT_FOUND, "SEM_CANDIDATA", "Não há candidata aberta."));
        return new CandidataEncontrada(VersaoResposta.de(candidata), candidata.getRevisao());
    }

    @Transactional
    public CandidataEncontrada editarCandidata(UUID projetoId, UUID requisitoId, String ifMatch,
                                               CandidataPatch patch) {
        Requisito requisito = repositorio.buscarParaEdicao(requisitoId, projetoId)
                .orElseThrow(RequisitoService::naoEncontrado);
        if (requisito.getSituacao() == SituacaoRequisito.CANCELADO) {
            throw new Problema(HttpStatus.CONFLICT, "REQUISITO_CANCELADO", "Requisito cancelado.");
        }
        if (patch.titulo() == null && patch.conteudo() == null && patch.criteriosAceite() == null) {
            throw new Problema(HttpStatus.BAD_REQUEST, "REQUISICAO_INVALIDA", "Informe ao menos um campo.");
        }
        Versao versao = requisito.candidata().orElseGet(() -> requisito.versaoPublicada()
                .orElseThrow(() -> new Problema(HttpStatus.NOT_FOUND, "SEM_CANDIDATA", "Não há versão disponível.")));
        if (ifMatch == null || ifMatch.isBlank()) {
            throw new Problema(HttpStatus.PRECONDITION_REQUIRED, "PRECONDICAO_AUSENTE", "Informe If-Match.");
        }
        if (!ifMatch.trim().equals("\"" + versao.getRevisao() + "\"")) {
            throw new Problema(HttpStatus.PRECONDITION_FAILED, "REVISAO_DESATUALIZADA", "A versão foi alterada.");
        }
        if (versao.getEstadoVersao() == EstadoVersao.EM_SUBMISSAO
                || versao.getEstadoVersao() == EstadoVersao.AGUARDANDO_REVISAO) {
            throw new Problema(HttpStatus.CONFLICT, "CANDIDATA_BLOQUEADA", "A candidata está bloqueada.");
        }
        if (versao.getEstadoVersao() == EstadoVersao.PUBLICADA) {
            versao = requisito.criarCandidataDaPublicada(versao);
        }
        String tituloAnterior = requisito.getTitulo();
        requisito.editarCandidata(versao, patch.titulo() == null ? versao.getTitulo() : patch.titulo(),
                patch.conteudo() == null ? versao.getConteudo() : patch.conteudo(),
                patch.criteriosAceite() == null ? versao.getCriteriosAceite() : patch.criteriosAceite());
        repositorio.saveAndFlush(requisito);
        List<Map<String, Object>> ancoras = banco.queryForList(
                "SELECT id, ancora_selecao FROM comentarios WHERE versao_id=? AND ancora_situacao='LOCALIZADA' FOR UPDATE", versao.getId());
        for (Map<String, Object> ancora : ancoras) {
            if (!ComentarioService.localizada(versao, (String) ancora.get("ancora_selecao"))) {
                banco.update("UPDATE comentarios SET ancora_situacao='NAO_LOCALIZADA', revisao=revisao+1, atualizado_em=now() WHERE id=?", ancora.get("id"));
            }
        }
        if (!Objects.equals(tituloAnterior, requisito.getTitulo())) {
            emitirRequisitoAlterado(requisito);
        }
        return new CandidataEncontrada(VersaoResposta.de(versao), versao.getRevisao());
    }

    /** Altera somente os metadados informados. */
    @Transactional
    public Encontrado alterarMetadados(UUID projeto, UUID id, String ifMatch, JsonNode corpo) {
        travarProjeto(projeto);
        Requisito r = repositorio.buscarParaEdicao(id, projeto).orElseThrow(RequisitoService::naoEncontrado);
        Precondicao.verificar(ifMatch, r.getRevisao());
        if (r.getSituacao() == SituacaoRequisito.CANCELADO)
            throw new Problema(HttpStatus.CONFLICT, "REQUISITO_CANCELADO", "Requisito cancelado.");
        if (!corpo.isObject() || corpo.isEmpty()) throw dadosInvalidos();
        corpo.fieldNames().forEachRemaining(c -> {
            if (!List.of("responsavelId", "epicoId", "tipo").contains(c)) throw dadosInvalidos();
        });
        UUID responsavel = corpo.has("responsavelId") ? uuid(corpo.get("responsavelId")) : r.getResponsavelId();
        UUID epico = corpo.has("epicoId") ? uuid(corpo.get("epicoId")) : r.getEpicoId();
        String tipo = r.getTipo();
        if (corpo.has("tipo")) {
            JsonNode valor = corpo.get("tipo");
            if (!valor.isTextual() || valor.asText().length() > 40) throw dadosInvalidos();
            tipo = valor.asText();
        }
        if (corpo.has("epicoId")) validarEpico(projeto, epico);
        r.alterarMetadados(responsavel, epico, tipo);
        repositorio.saveAndFlush(r);
        emitirRequisitoAlterado(r);
        return new Encontrado(RequisitoResposta.de(r), r.getRevisao());
    }

    /** Cancela ou reativa o requisito com registro idempotente. */
    @Transactional
    public AlteracaoEncontrada alterarSituacao(UUID projeto, UUID id, String ifMatch, String chave,
                                                SituacaoRequisito destino, boolean confirmado) {
        if (chave != null && chave.length() > 200) throw dadosInvalidos();
        if (destino == SituacaoRequisito.CANCELADO && !confirmado)
            throw new Problema(HttpStatus.BAD_REQUEST, "CONFIRMACAO_OBRIGATORIA", "Confirme o cancelamento.");
        travarProjeto(projeto);
        Requisito r = repositorio.buscarParaEdicao(id, projeto).orElseThrow(RequisitoService::naoEncontrado);
        if (chave != null) {
            List<Alteracao> anteriores = banco.query("SELECT * FROM alteracoes_situacao WHERE requisito_id=? AND situacao=? AND chave_idempotencia=?",
                    (rs, n) -> new Alteracao(rs.getObject("id", UUID.class), id, SituacaoRequisito.valueOf(rs.getString("situacao")),
                            rs.getObject("candidata_id", UUID.class), rs.getObject("autor_id", UUID.class), rs.getTimestamp("criado_em").toInstant()),
                    id, destino.name(), chave);
            if (!anteriores.isEmpty()) return new AlteracaoEncontrada(anteriores.get(0), r.getRevisao());
        }
        Precondicao.verificar(ifMatch, r.getRevisao());
        String erro;
        if (destino == SituacaoRequisito.CANCELADO) {
            List<String> sagas = banco.queryForList("SELECT estado_saga FROM submissoes WHERE requisito_id=?", String.class, id);
            erro = RegrasSituacao.erroCancelamento(r.getSituacao(), sagas,
                    r.candidata().map(Versao::getEstadoVersao).orElse(null));
        } else {
            erro = RegrasSituacao.erroReativacao(r.getSituacao());
        }
        if (erro != null) throw new Problema(HttpStatus.CONFLICT, erro, "A situação não pode ser alterada.");
        UUID candidata = null;
        if (destino == SituacaoRequisito.CANCELADO) r.cancelar();
        else candidata = r.reativar().getId();
        repositorio.saveAndFlush(r);
        Instant agora = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MICROS);
        Alteracao alteracao = new Alteracao(UUID.randomUUID(), id, destino, candidata, usuario.obter(), agora);
        banco.update("INSERT INTO alteracoes_situacao (id,projeto_id,requisito_id,situacao,candidata_id,autor_id,chave_idempotencia,criado_em) VALUES (?,?,?,?,?,?,?,?)",
                alteracao.operacaoId(), projeto, id, destino.name(), candidata, alteracao.autor(), chave, Timestamp.from(agora));
        emitirRequisitoAlterado(r);
        return new AlteracaoEncontrada(alteracao, r.getRevisao());
    }

    private void travarProjeto(UUID projeto) {
        banco.queryForObject("SELECT pg_advisory_xact_lock(hashtextextended(?, 0))", (rs, n) -> 0, "epicos:" + projeto);
    }
    private void validarEpico(UUID projeto, UUID id) {
        if (id != null && epicos.buscarParaEdicao(id, projeto).isEmpty())
            throw new Problema(HttpStatus.BAD_REQUEST, "EPICO_INVALIDO", "Épico inválido para o projeto.");
    }
    private static UUID uuid(JsonNode valor) {
        if (valor.isNull()) return null;
        if (!valor.isTextual()) throw dadosInvalidos();
        try { return UUID.fromString(valor.asText()); } catch (IllegalArgumentException e) { throw dadosInvalidos(); }
    }
    private static Problema dadosInvalidos() {
        return new Problema(HttpStatus.BAD_REQUEST, "REQUISICAO_INVALIDA", "Dados inválidos.");
    }
    public record Alteracao(UUID operacaoId, UUID requisitoId, SituacaoRequisito situacao, UUID candidataId, UUID autor, Instant data) { }
    public record AlteracaoEncontrada(Alteracao alteracao, long revisao) { }

    private void emitirRequisitoAlterado(Requisito requisito) {
        emissor.evento("RequisitoAlterado", requisito.getProjetoId(), null, null, UUID.randomUUID(), null,
                payloadRequisitoAlterado(requisito));
    }

    static Map<String, Object> payloadRequisitoAlterado(Requisito requisito) {
        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("requisitoId", requisito.getId());
        payload.put("projetoId", requisito.getProjetoId());
        payload.put("codigo", requisito.getCodigo());
        payload.put("titulo", requisito.getTitulo());
        payload.put("situacao", requisito.getSituacao().name());
        payload.put("revisao", requisito.getRevisao());
        // Metadados opcionais alimentam os nós do mapa (US14); ausentes quando não definidos.
        if (requisito.getTipo() != null) payload.put("tipo", requisito.getTipo());
        if (requisito.getEpicoId() != null) payload.put("epicoId", requisito.getEpicoId());
        if (requisito.getResponsavelId() != null) payload.put("responsavelId", requisito.getResponsavelId());
        return payload;
    }

    private static Problema naoEncontrado() {
        return new Problema(HttpStatus.NOT_FOUND, "REQUISITO_NAO_ENCONTRADO", "Requisito não encontrado.");
    }

    public record CandidataEncontrada(VersaoResposta versao, long revisao) {
    }

    /** Representação do requisito junto com a revisão técnica usada no ETag. */
    public record Encontrado(RequisitoResposta requisito, long revisao) {
    }
}
