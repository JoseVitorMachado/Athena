package br.ufla.athena.requisitos.aplicacao;

import br.ufla.athena.requisitos.api.Pagina;
import br.ufla.athena.requisitos.api.RequisitoEntrada;
import br.ufla.athena.requisitos.api.RequisitoResposta;
import br.ufla.athena.requisitos.dominio.Requisito;
import br.ufla.athena.requisitos.dominio.RequisitoRepository;
import br.ufla.athena.requisitos.dominio.SituacaoRequisito;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RequisitoService {

    private final RequisitoRepository repositorio;

    public RequisitoService(RequisitoRepository repositorio) {
        this.repositorio = repositorio;
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
        return new Encontrado(RequisitoResposta.de(salvo), salvo.getRevisao());
    }

    /** Representação do requisito junto com a revisão técnica usada no ETag. */
    public record Encontrado(RequisitoResposta requisito, long revisao) {
    }
}
