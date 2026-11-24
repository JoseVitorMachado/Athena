package br.ufla.athena.bff.gestao.docs;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProjetoPatchDoc(
        @Schema(description = "nome da operação.")
        String nome,
        @Schema(description = "descricao da operação.")
        String descricao
) {
}
