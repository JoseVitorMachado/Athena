package br.ufla.athena.bff.gestao.docs;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProjetoEntradaDoc(
        @Schema(description = "workspaceId da operação.", requiredMode = Schema.RequiredMode.REQUIRED)
        java.util.UUID workspaceId,
        @Schema(description = "nome da operação.", requiredMode = Schema.RequiredMode.REQUIRED)
        String nome,
        @Schema(description = "descricao da operação.")
        String descricao
) {
}
