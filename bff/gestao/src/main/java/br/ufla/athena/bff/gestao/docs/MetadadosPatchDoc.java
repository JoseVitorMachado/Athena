package br.ufla.athena.bff.gestao.docs;

import io.swagger.v3.oas.annotations.media.Schema;

/** Alterações de metadados do requisito. */
public record MetadadosPatchDoc(
        @Schema(description = "Identificador do responsável.") java.util.UUID responsavelId,
        @Schema(description = "Identificador do épico.") java.util.UUID epicoId,
        @Schema(description = "Tipo do requisito.") String tipo
) {
}
