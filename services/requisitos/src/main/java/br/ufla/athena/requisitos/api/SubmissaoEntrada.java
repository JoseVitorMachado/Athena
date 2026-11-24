package br.ufla.athena.requisitos.api;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SubmissaoEntrada(@NotNull UUID versaoId) { }
