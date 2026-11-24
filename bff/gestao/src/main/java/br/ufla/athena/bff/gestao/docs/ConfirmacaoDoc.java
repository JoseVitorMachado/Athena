package br.ufla.athena.bff.gestao.docs;

import io.swagger.v3.oas.annotations.media.Schema;

/** Confirmação do cancelamento. */
public record ConfirmacaoDoc(
        @Schema(description = "Confirmação do cancelamento.", example = "true", requiredMode = Schema.RequiredMode.REQUIRED) Boolean confirmacao
) {
}
