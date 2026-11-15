package br.ufla.athena.revisoes.dominio;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DecisaoRepository extends JpaRepository<Decisao, UUID> {

    Optional<Decisao> findByLoteId(UUID loteId);
}
