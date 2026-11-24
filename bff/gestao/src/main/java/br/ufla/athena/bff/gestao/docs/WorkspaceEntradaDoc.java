package br.ufla.athena.bff.gestao.docs;

import io.swagger.v3.oas.annotations.media.Schema;

public record WorkspaceEntradaDoc(
        @Schema(description = "nome da operação.", requiredMode = Schema.RequiredMode.REQUIRED)
        String nome
) {
}
