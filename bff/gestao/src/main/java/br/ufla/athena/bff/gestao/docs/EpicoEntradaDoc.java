package br.ufla.athena.bff.gestao.docs;

import io.swagger.v3.oas.annotations.media.Schema;

/** Entrada para criação de épico. */
public record EpicoEntradaDoc(
        @Schema(description = "Título do épico.", requiredMode = Schema.RequiredMode.REQUIRED) String titulo,
        @Schema(description = "Descrição do épico.") String descricao
) {
}
