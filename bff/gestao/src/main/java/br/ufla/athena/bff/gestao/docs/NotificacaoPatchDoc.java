package br.ufla.athena.bff.gestao.docs;

import io.swagger.v3.oas.annotations.media.Schema;

/** Alteração de leitura da notificação. */
public record NotificacaoPatchDoc(
        @Schema(description = "Indica se a notificação foi lida.", requiredMode = Schema.RequiredMode.REQUIRED) Boolean lida
) {
}
