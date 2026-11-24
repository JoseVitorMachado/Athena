package br.ufla.athena.bff.gestao.docs;

import io.swagger.v3.oas.annotations.media.Schema;

/** Entrada para alteração de papel de membro. */
public record MembroPatchDoc(
        @Schema(description = "Papel do membro.", requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {"Leitor", "Repórter", "Editor", "Aprovador", "Mantenedor", "Owner"})
        String papel
) {
}
