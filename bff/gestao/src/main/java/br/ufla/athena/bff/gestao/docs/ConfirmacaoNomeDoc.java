package br.ufla.athena.bff.gestao.docs;

import io.swagger.v3.oas.annotations.media.Schema;

public record ConfirmacaoNomeDoc(
        @Schema(description = "confirmacaoNome da operação.", requiredMode = Schema.RequiredMode.REQUIRED)
        String confirmacaoNome
) {
}
