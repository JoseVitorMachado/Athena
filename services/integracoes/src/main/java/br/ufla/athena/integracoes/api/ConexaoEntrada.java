package br.ufla.athena.integracoes.api;

import br.ufla.athena.integracoes.dominio.Provedor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Schema ConexaoEntrada de docs/api/integracoes.yaml. */
public record ConexaoEntrada(
        @NotNull Provedor provedor,
        @NotBlank @Size(max = 120) String nome,
        @Valid Configuracao configuracao,
        @Valid Credenciais credenciais) {

    public record Configuracao(@Size(max = 500) String urlBase, @Size(max = 200) String projetoExternoId) {
    }

    /** Somente escrita no contrato: nunca volta em resposta alguma. */
    public record Credenciais(@NotBlank String segredo) {
    }
}
