package br.ufla.athena.revisoes.api;

import br.ufla.athena.revisoes.dominio.TipoDecisao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Schema DecisaoEntrada de docs/api/revisoes.yaml. O campo comprovanteHumano
 * (decisão via agente MCP) entra junto com a autenticação, na Parte 4.
 */
public record DecisaoEntrada(
        @NotNull TipoDecisao decisao,
        @Size(max = 2000) String justificativa,
        @NotNull UUID submissaoId,
        @NotNull UUID versaoId,
        @NotNull @Pattern(regexp = "^[a-f0-9]{64}$") String hashConteudo) {
}
