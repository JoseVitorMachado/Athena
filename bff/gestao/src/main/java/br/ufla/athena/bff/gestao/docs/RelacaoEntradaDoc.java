package br.ufla.athena.bff.gestao.docs;

import io.swagger.v3.oas.annotations.media.Schema;

public record RelacaoEntradaDoc(
        @Schema(description = "OrigemId.", example = "11111111-1111-4111-8111-111111111111", requiredMode = Schema.RequiredMode.REQUIRED)
        java.util.UUID origemId,
        @Schema(description = "DestinoId.", example = "33333333-3333-4333-8333-333333333333", requiredMode = Schema.RequiredMode.REQUIRED)
        java.util.UUID destinoId,
        @Schema(description = "Tipo.", example = "DEPENDE_DE", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"DEPENDE_DE", "BLOQUEIA", "RELACIONADO_A"})
        String tipo,
        @Schema(description = "Justificativa.", example = "Dependência funcional")
        String justificativa
) {
}
