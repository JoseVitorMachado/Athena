package br.ufla.athena.projetosacesso.dominio;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select w from Workspace w where w.id = :id")
    java.util.Optional<Workspace> buscarParaAlteracao(@org.springframework.data.repository.query.Param("id") UUID id);
}
