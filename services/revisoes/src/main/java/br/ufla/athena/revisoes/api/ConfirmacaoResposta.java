package br.ufla.athena.revisoes.api;

import java.time.Instant;
import java.util.UUID;

public record ConfirmacaoResposta(UUID confirmacaoId, String comprovanteHumano, Instant expiraEm) { }
