package br.ufla.athena.requisitos.dominio;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RequisitoRepository extends JpaRepository<Requisito, UUID> {

    Page<Requisito> findByProjetoIdAndSituacao(UUID projetoId, SituacaoRequisito situacao, Pageable pageable);

    Optional<Requisito> findByIdAndProjetoId(UUID id, UUID projetoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Requisito r where r.id = :id and r.projetoId = :projetoId")
    Optional<Requisito> buscarParaEdicao(@Param("id") UUID id, @Param("projetoId") UUID projetoId);

    /**
     * Reserva o próximo número do código REQ-n do projeto. O upsert é atômico:
     * duas criações simultâneas no mesmo projeto nunca recebem o mesmo número.
     */
    @Query(nativeQuery = true, value = """
            INSERT INTO sequencias_codigo (projeto_id, ultimo) VALUES (:projetoId, 1)
            ON CONFLICT (projeto_id) DO UPDATE SET ultimo = sequencias_codigo.ultimo + 1
            RETURNING ultimo
            """)
    int reservarProximoNumero(@Param("projetoId") UUID projetoId);
}
