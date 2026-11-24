package br.ufla.athena.bff.gestao.docs;

import io.swagger.v3.oas.annotations.media.Schema;

/** Alterações de comentário. */
public record ComentarioPatchDoc(
        @Schema(description = "Texto do comentário.") String texto,
        @Schema(description = "Indica se o comentário está resolvido.") Boolean resolvido
) {
}
