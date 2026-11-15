package br.ufla.athena.requisitos.dominio;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicacaoRepository extends JpaRepository<Publicacao, UUID> {

    Page<Publicacao> findByRequisitoId(UUID requisitoId, Pageable pageable);
}
