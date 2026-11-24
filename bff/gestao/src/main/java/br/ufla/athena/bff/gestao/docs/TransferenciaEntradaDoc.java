package br.ufla.athena.bff.gestao.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/** Entrada para transferência de propriedade. */
public record TransferenciaEntradaDoc(
        @Schema(description = "Identificador do destinatário.", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID destinatarioId,
        @Schema(description = "Confirmação da transferência.", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean confirmacao
) {
}
