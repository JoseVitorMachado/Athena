package br.ufla.athena.bff.gestao.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

/** Entrada para criação de baseline. */
public record BaselineEntradaDoc(
        @Schema(description = "Nome da baseline, com até 120 caracteres.", requiredMode = Schema.RequiredMode.REQUIRED) String nome,
        @Schema(description = "Versões publicadas; lista vazia captura o Livro corrente.", requiredMode = Schema.RequiredMode.REQUIRED) List<Item> versoes) {
    public record Item(UUID requisitoId, UUID versaoId, UUID retratoId) { }
}
