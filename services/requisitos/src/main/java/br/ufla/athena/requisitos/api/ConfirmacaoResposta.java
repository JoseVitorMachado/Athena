package br.ufla.athena.requisitos.api;

import java.time.Instant;
import java.util.UUID;

public record ConfirmacaoResposta(UUID confirmacaoId, String comprovanteHumano, Instant expiraEm) { }
