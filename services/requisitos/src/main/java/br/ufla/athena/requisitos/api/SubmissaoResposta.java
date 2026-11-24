package br.ufla.athena.requisitos.api;

import java.time.Instant;
import java.util.UUID;

public record SubmissaoResposta(UUID submissaoId, UUID sagaId, UUID projetoId, UUID requisitoId,
                                UUID versaoId, String estadoSaga, String etapaSaga, int tentativas,
                                Instant prazo, String hashConteudo, UUID retratoId, UUID loteId,
                                Instant atualizadoEm) { }
