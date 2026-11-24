package br.ufla.athena.requisitos.api;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public record ConfirmacaoEntrada(@NotNull UUID versaoId,
        @NotNull @Pattern(regexp = "^[a-f0-9]{64}$") String hashAcao,
        @NotNull @AssertTrue Boolean confirmacao) { }
