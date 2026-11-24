package br.ufla.athena.bff.gestao.docs;

import io.swagger.v3.oas.annotations.media.Schema;

public record RequisitoEntradaDoc(
        @Schema(description = "Identificador do documento.", example = "11111111-1111-4111-8111-111111111111", requiredMode = Schema.RequiredMode.REQUIRED)
        java.util.UUID documentoId,
        @Schema(description = "Identificador do épico.", example = "11111111-1111-4111-8111-111111111111")
        java.util.UUID epicoId,
        @Schema(description = "Identificador do responsável.", example = "11111111-1111-4111-8111-111111111111")
        java.util.UUID responsavelId,
        @Schema(description = "Catálogo não definido: x-decidir da operação.", example = "FUNCIONAL")
        String tipo,
        @Schema(description = "Titulo.", example = "Exportar documento de requisitos.", requiredMode = Schema.RequiredMode.REQUIRED)
        String titulo,
        @Schema(description = "Conteudo.", example = "O sistema deve permitir exportar o documento.", requiredMode = Schema.RequiredMode.REQUIRED)
        String conteudo,
        @Schema(description = "Critérios de aceite.", example = "[\"Exportação preserva requisitos.\"]", requiredMode = Schema.RequiredMode.REQUIRED)
        java.util.List<String> criteriosAceite
) {
}
