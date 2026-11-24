package br.ufla.athena.bff.gestao.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

/** Entrada para criação de comentário. */
public record ComentarioEntradaDoc(
        @Schema(description = "Identificador da versão.", requiredMode = Schema.RequiredMode.REQUIRED) UUID versaoId,
        @Schema(description = "Texto do comentário.", requiredMode = Schema.RequiredMode.REQUIRED) String texto,
        @Schema(description = "Âncora do comentário.") AncoraDoc ancora,
        @Schema(description = "Identificadores dos membros mencionados; máximo de 20.") List<UUID> mencoes
) {
    /** Seleção de texto que ancora o comentário. */
    public record AncoraDoc(@Schema(description = "Texto selecionado.") String selecao) {
    }
}
