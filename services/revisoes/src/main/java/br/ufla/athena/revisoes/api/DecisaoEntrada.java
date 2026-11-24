package br.ufla.athena.revisoes.api;

import br.ufla.athena.revisoes.dominio.TipoDecisao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record DecisaoEntrada(
        @NotNull TipoDecisao decisao,
        @Size(max = 2000) String justificativa,
        @NotNull UUID submissaoId,
        @NotNull UUID versaoId,
        @NotNull @Pattern(regexp = "^[a-f0-9]{64}$") String hashConteudo,
        String comprovanteHumano) {
    public DecisaoEntrada(TipoDecisao decisao, String justificativa, UUID submissaoId, UUID versaoId, String hashConteudo) {
        this(decisao, justificativa, submissaoId, versaoId, hashConteudo, null);
    }
}
