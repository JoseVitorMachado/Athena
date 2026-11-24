package br.ufla.athena.bff.gestao.docs;

import io.swagger.v3.oas.annotations.media.Schema;

/** Entrada para criação de convite. */
public record ConviteEntradaDoc(
        @Schema(description = "E-mail do destinatário.", requiredMode = Schema.RequiredMode.REQUIRED)
        String email,
        @Schema(description = "Papel oferecido.", requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {"Leitor", "Repórter", "Editor", "Aprovador", "Mantenedor"})
        String papel
) {
}
