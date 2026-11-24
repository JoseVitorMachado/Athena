package br.ufla.athena.bff.gestao.docs;

import io.swagger.v3.oas.annotations.media.Schema;

public record DecisaoEntradaDoc(
        @Schema(description = "Decisao.", example = "APROVAR", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"APROVAR", "REJEITAR", "SOLICITAR_CORRECOES"})
        String decisao,
        @Schema(description = "Justificativa.", example = "Critérios atendidos.")
        String justificativa,
        @Schema(description = "Identificador da submissão.", example = "11111111-1111-4111-8111-111111111111", requiredMode = Schema.RequiredMode.REQUIRED)
        java.util.UUID submissaoId,
        @Schema(description = "Identificador da versão.", example = "33333333-3333-4333-8333-333333333333", requiredMode = Schema.RequiredMode.REQUIRED)
        java.util.UUID versaoId,
        @Schema(description = "Hash SHA-256 do conteúdo.", example = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", requiredMode = Schema.RequiredMode.REQUIRED)
        String hashConteudo,
        @Schema(description = "Comprovante de confirmação humana, exigido em ações delegadas.", example = "comprovante-opaco")
        String comprovanteHumano
) {
}
