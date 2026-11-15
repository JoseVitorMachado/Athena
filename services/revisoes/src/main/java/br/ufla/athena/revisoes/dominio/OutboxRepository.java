package br.ufla.athena.revisoes.dominio;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxRepository extends JpaRepository<MensagemOutbox, UUID> {
}
