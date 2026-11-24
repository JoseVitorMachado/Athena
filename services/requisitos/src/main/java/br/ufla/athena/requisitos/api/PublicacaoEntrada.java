package br.ufla.athena.requisitos.api;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PublicacaoEntrada(@NotNull UUID versaoId, @NotNull UUID submissaoId, String comprovanteHumano) {
}
