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

    public RequisitoService(RequisitoRepository repositorio, EmissorMensagens emissor) {
        this.repositorio = repositorio;
        this.emissor = emissor;
    }

    @Transactional(readOnly = true)
    public Pagina<RequisitoResposta> listar(UUID projetoId, SituacaoRequisito situacao, Pageable paginacao,
                                            String linkSelf) {
        Page<Requisito> pagina = repositorio.findByProjetoIdAndSituacao(projetoId, situacao, paginacao);
        return Pagina.de(pagina.map(RequisitoResposta::de), linkSelf);
    }

    @Transactional(readOnly = true)
    public Optional<Encontrado> buscar(UUID projetoId, UUID requisitoId) {
        return repositorio.findByIdAndProjetoId(requisitoId, projetoId)
                .map(requisito -> new Encontrado(RequisitoResposta.de(requisito), requisito.getRevisao()));
    }

    @Transactional
    public Encontrado criar(UUID projetoId, RequisitoEntrada entrada) {
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
        if (!Objects.equals(tituloAnterior, requisito.getTitulo())) {
            emitirRequisitoAlterado(requisito);
        }
        return new CandidataEncontrada(VersaoResposta.de(versao), versao.getRevisao());
    }

    private void emitirRequisitoAlterado(Requisito requisito) {
        emissor.evento("RequisitoAlterado", requisito.getProjetoId(), null, null, UUID.randomUUID(), null,
                payloadRequisitoAlterado(requisito));
    }

    static Map<String, Object> payloadRequisitoAlterado(Requisito requisito) {
        return Map.of(
                "requisitoId", requisito.getId(),
                "projetoId", requisito.getProjetoId(),
                "codigo", requisito.getCodigo(),
                "titulo", requisito.getTitulo(),
                "situacao", requisito.getSituacao().name(),
                "revisao", requisito.getRevisao());
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
