package br.ufla.athena.projetosacesso.api;

import br.ufla.athena.projetosacesso.dominio.Projeto;
import java.time.Instant;
import java.util.UUID;

/** Schema Projeto de docs/api/projetos-acesso.yaml. */
public record ProjetoResposta(
        UUID projetoId,
        UUID workspaceId,
        String nome,
        String descricao,
        boolean arquivado,
        UUID ownerId,
        Instant atualizadoEm) {

    public static ProjetoResposta de(Projeto projeto) {
        return new ProjetoResposta(projeto.getId(), projeto.getWorkspaceId(), projeto.getNome(),
                projeto.getDescricao(), projeto.isArquivado(), projeto.getOwnerId(), projeto.getAtualizadoEm());
    }
}
