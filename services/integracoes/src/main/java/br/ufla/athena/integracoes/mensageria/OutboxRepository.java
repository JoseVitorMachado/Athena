package br.ufla.athena.integracoes.mensageria;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxRepository extends JpaRepository<MensagemOutbox, UUID> {
    @Query(value = "SELECT * FROM outbox WHERE publicado_em IS NULL AND proxima_tentativa <= now() "
            + "ORDER BY criado_em LIMIT :limite FOR UPDATE SKIP LOCKED", nativeQuery = true)
    List<MensagemOutbox> buscarPendentes(@Param("limite") int limite);
}
