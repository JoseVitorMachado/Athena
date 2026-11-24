package br.ufla.athena.projetosacesso.dominio;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjetoRepository extends JpaRepository<Projeto, UUID> {

    java.util.List<Projeto> findByWorkspaceIdAndExcluidoEmIsNull(UUID workspaceId);

    /** Projetos arquivados ficam fora das listagens de projetos ativos (livro §5.6). */
    Page<Projeto> findByArquivadoFalse(Pageable pageable);

    Page<Projeto> findByWorkspaceIdAndArquivadoFalse(UUID workspaceId, Pageable pageable);
}
