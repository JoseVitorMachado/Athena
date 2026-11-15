package br.ufla.athena.rastreabilidade.api;

import br.ufla.athena.rastreabilidade.dominio.TipoRelacao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/** Schema RelacaoEntrada de docs/api/rastreabilidade.yaml. */
public record RelacaoEntrada(
        @NotNull UUID origemId,
        @NotNull UUID destinoId,
        @NotNull TipoRelacao tipo,
        @Size(max = 1000) String justificativa) {
}
