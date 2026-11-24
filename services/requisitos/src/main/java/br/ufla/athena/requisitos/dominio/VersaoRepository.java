package br.ufla.athena.requisitos.dominio;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VersaoRepository extends JpaRepository<Versao, UUID> {

    Page<Versao> findByRequisito_Id(UUID requisitoId, Pageable paginacao);

    Optional<Versao> findByIdAndRequisito_IdAndRequisito_ProjetoId(UUID id, UUID requisitoId, UUID projetoId);
}
