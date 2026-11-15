package br.ufla.athena.revisoes.dominio;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LoteRepository extends JpaRepository<Lote, UUID>, JpaSpecificationExecutor<Lote> {

    Optional<Lote> findByIdAndProjetoId(UUID id, UUID projetoId);
}
