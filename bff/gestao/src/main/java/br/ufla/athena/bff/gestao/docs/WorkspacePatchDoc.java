package br.ufla.athena.bff.gestao.docs;

import io.swagger.v3.oas.annotations.media.Schema;

public record WorkspacePatchDoc(
        @Schema(description = "nome da operação.")
        String nome
) {
}
