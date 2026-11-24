package br.ufla.athena.requisitos.dominio;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Persistência e trava de épicos. */
public interface EpicoRepository extends JpaRepository<Epico, UUID> {
    Page<Epico> findByProjetoId(UUID projetoId, Pageable pagina);
    Optional<Epico> findByIdAndProjetoId(UUID id, UUID projetoId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Epico e where e.id = :id and e.projetoId = :projeto")
    Optional<Epico> buscarParaEdicao(@Param("id") UUID id, @Param("projeto") UUID projeto);
}
