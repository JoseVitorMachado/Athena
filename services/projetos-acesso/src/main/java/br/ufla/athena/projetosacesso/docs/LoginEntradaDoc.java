package br.ufla.athena.projetosacesso.docs;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginEntradaDoc(
        @Schema(description = "Email.", example = "fernando@example.invalid", requiredMode = Schema.RequiredMode.REQUIRED)
        String email,
        @Schema(description = "Senha.", example = "senha-apenas-ilustrativa", requiredMode = Schema.RequiredMode.REQUIRED)
        String senha,
        @Schema(description = "LembrarDispositivo.", example = "false")
        Boolean lembrarDispositivo
) {
}
