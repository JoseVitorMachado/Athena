package br.ufla.athena.requisitos.dominio;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Filtros da listagem de requisitos (US18). Todos são opcionais e combinados com E.
 *
 * O estado da versão de um requisito é o da candidata aberta; sem candidata, é
 * PUBLICADA quando existe versão publicada.
 */
public record FiltroRequisitos(UUID projetoId, SituacaoRequisito situacao, EstadoVersao estadoVersao,
                               String tipo, UUID epicoId, UUID responsavelId, String q) {

    public Specification<Requisito> especificacao() {
        return (raiz, consulta, cb) -> {
            List<Predicate> condicoes = new ArrayList<>();
            condicoes.add(cb.equal(raiz.get("projetoId"), projetoId));
            condicoes.add(cb.equal(raiz.get("situacao"), situacao));
            if (tipo != null && !tipo.isBlank()) {
                condicoes.add(cb.equal(raiz.get("tipo"), tipo.trim()));
            }
            if (epicoId != null) {
                condicoes.add(cb.equal(raiz.get("epicoId"), epicoId));
            }
            if (responsavelId != null) {
                condicoes.add(cb.equal(raiz.get("responsavelId"), responsavelId));
            }
            if (q != null && !q.isBlank()) {
                String padrao = "%" + escapar(q.trim().toLowerCase(Locale.ROOT)) + "%";
                condicoes.add(cb.or(
                        cb.like(cb.lower(raiz.get("titulo")), padrao, '\\'),
                        cb.like(cb.lower(raiz.get("codigo")), padrao, '\\')));
            }
            if (estadoVersao != null) {
                if (estadoVersao == EstadoVersao.PUBLICADA) {
                    condicoes.add(cb.not(cb.exists(versoesNoEstado(raiz, consulta, cb, null))));
                    condicoes.add(cb.exists(versoesNoEstado(raiz, consulta, cb, EstadoVersao.PUBLICADA)));
                } else {
                    condicoes.add(cb.exists(versoesNoEstado(raiz, consulta, cb, estadoVersao)));
                }
            }
            return cb.and(condicoes.toArray(Predicate[]::new));
        };
    }

    /** Versões do requisito no estado dado; com estado nulo, qualquer candidata aberta (não publicada). */
    private static Subquery<UUID> versoesNoEstado(Root<Requisito> raiz,
                                                  jakarta.persistence.criteria.CriteriaQuery<?> consulta,
                                                  jakarta.persistence.criteria.CriteriaBuilder cb,
                                                  EstadoVersao estado) {
        Subquery<UUID> sub = consulta.subquery(UUID.class);
        Root<Versao> versao = sub.from(Versao.class);
        Predicate doRequisito = cb.equal(versao.get("requisito"), raiz);
        Predicate noEstado = estado == null
                ? cb.notEqual(versao.get("estadoVersao"), EstadoVersao.PUBLICADA)
                : cb.equal(versao.get("estadoVersao"), estado);
        return sub.select(versao.get("id")).where(doRequisito, noEstado);
    }

    private static String escapar(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
