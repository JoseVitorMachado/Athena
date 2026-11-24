package br.ufla.athena.bff.gestao.docs;

import io.swagger.v3.oas.annotations.media.Schema;

public record CandidataPatchDoc(
        @Schema(description = "Titulo.", example = "Exportar documento de requisitos.")
        String titulo,
        @Schema(description = "Conteudo.", example = "O sistema deve permitir exportar o documento.")
        String conteudo,
        @Schema(description = "Critérios de aceite.", example = "[\"Exportação preserva requisitos.\"]")
        java.util.List<String> criteriosAceite
) {
}
