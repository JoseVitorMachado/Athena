package br.ufla.athena.bff.gestao.docs;

import io.swagger.v3.oas.annotations.media.Schema;

public record SubmissaoEntradaDoc(
        @Schema(description = "Identificador da versão.", example = "33333333-3333-4333-8333-333333333333", requiredMode = Schema.RequiredMode.REQUIRED)
        java.util.UUID versaoId
) {
}
