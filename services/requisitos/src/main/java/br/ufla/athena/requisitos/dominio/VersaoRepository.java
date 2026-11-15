package br.ufla.athena.requisitos.dominio;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VersaoRepository extends JpaRepository<Versao, UUID> {

    Optional<Versao> findByIdAndRequisito_IdAndRequisito_ProjetoId(UUID id, UUID requisitoId, UUID projetoId);
}
