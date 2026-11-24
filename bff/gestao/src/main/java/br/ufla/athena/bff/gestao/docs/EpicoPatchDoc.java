package br.ufla.athena.bff.gestao.docs;

import io.swagger.v3.oas.annotations.media.Schema;

/** Alterações de épico. */
public record EpicoPatchDoc(
        @Schema(description = "Título do épico.") String titulo,
        @Schema(description = "Descrição do épico.") String descricao
) {
}
