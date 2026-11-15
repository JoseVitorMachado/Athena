package br.ufla.athena.requisitos.api;

import br.ufla.athena.requisitos.dominio.EstadoVersao;
import br.ufla.athena.requisitos.dominio.Versao;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Schema Versao de docs/api/requisitos.yaml. */
public record VersaoResposta(
        UUID versaoId,
        UUID requisitoId,
        int numero,
        EstadoVersao estadoVersao,
        String titulo,
        String conteudo,
        List<String> criteriosAceite,
        String hashConteudo,
        Instant atualizadoEm) {

    public static VersaoResposta de(Versao versao) {
        return new VersaoResposta(versao.getId(), versao.getRequisito().getId(), versao.getNumero(),
                versao.getEstadoVersao(), versao.getTitulo(), versao.getConteudo(),
                List.copyOf(versao.getCriteriosAceite()), versao.getHashConteudo(), versao.getAtualizadoEm());
    }
}
