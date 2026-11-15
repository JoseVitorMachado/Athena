package br.ufla.athena.rastreabilidade.aplicacao;

import br.ufla.athena.rastreabilidade.api.Problema;
import br.ufla.athena.rastreabilidade.api.RelacaoEntrada;
import br.ufla.athena.rastreabilidade.dominio.Relacao;
import br.ufla.athena.rastreabilidade.dominio.RelacaoRepository;
import br.ufla.athena.rastreabilidade.dominio.TipoRelacao;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RelacaoService {

    private final RelacaoRepository repositorio;

    public RelacaoService(RelacaoRepository repositorio) {
        this.repositorio = repositorio;
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

    /**
     * Cria uma relação oficial aplicando as regras do livro §5.4 já cobertas nesta
     * etapa: sem autorrelação, RELACIONADO_A em ordem canônica e sem duplicata.
     * A trava por projeto e a detecção de ciclos entram na Parte 4.
     */
    @Transactional
    public Relacao criar(UUID projetoId, RelacaoEntrada entrada) {
        UUID origem = entrada.origemId();
        UUID destino = entrada.destinoId();
        if (origem.equals(destino)) {
            throw new Problema(HttpStatus.BAD_REQUEST, "AUTORRELACAO",
                    "Um requisito não pode se relacionar com ele mesmo.");
        }
        // Mesma ordem usada pelo PostgreSQL para uuid (bytes sem sinal), que é a da
        // representação textual; UUID.compareTo usa outra ordem.
        if (entrada.tipo() == TipoRelacao.RELACIONADO_A && origem.toString().compareTo(destino.toString()) > 0) {
            UUID troca = origem;
            origem = destino;
            destino = troca;
        }
        if (repositorio.existsByProjetoIdAndOrigemIdAndDestinoIdAndTipo(projetoId, origem, destino, entrada.tipo())) {
            throw new Problema(HttpStatus.CONFLICT, "RELACAO_DUPLICADA", "Conflito: relação duplicada.");
        }
        return repositorio.saveAndFlush(
                Relacao.nova(projetoId, origem, destino, entrada.tipo(), entrada.justificativa()));
    }
}
