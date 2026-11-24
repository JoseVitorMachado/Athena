package br.ufla.athena.projetosacesso.docs;

import io.swagger.v3.oas.annotations.media.Schema;

public record CadastroEntradaDoc(
        @Schema(description = "Nome.", example = "Fernando", requiredMode = Schema.RequiredMode.REQUIRED)
        String nome,
        @Schema(description = "Email.", example = "fernando@example.invalid", requiredMode = Schema.RequiredMode.REQUIRED)
        String email,
        @Schema(description = "Senha.", example = "senha-apenas-ilustrativa", requiredMode = Schema.RequiredMode.REQUIRED)
        String senha
) {
}
