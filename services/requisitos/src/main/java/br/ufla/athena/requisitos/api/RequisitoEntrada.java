package br.ufla.athena.requisitos.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

/** Corpo do POST de requisito: schema RequisitoEntrada de docs/api/requisitos.yaml. */
public record RequisitoEntrada(
        @NotNull UUID documentoId,
        UUID epicoId,
        UUID responsavelId,
        @Size(max = 40) String tipo,
        @NotBlank @Size(max = 300) String titulo,
        @NotBlank String conteudo,
        @NotNull List<@NotBlank String> criteriosAceite) {
}
