package br.ufla.athena.bff.gestao.docs;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detalhes do problema; os campos efetivamente presentes variam conforme a origem do erro.")
public record ProblemaDoc(
        @Schema(description = "Tipo do problema.", example = "about:blank")
        String type,
        @Schema(description = "Título do problema.", example = "Não autorizado")
        String title,
        @Schema(description = "Código HTTP.", example = "401")
        Integer status,
        @Schema(description = "Detalhes do problema.", example = "Sessão encerrada.")
        String detail,
        @Schema(description = "Caminho da requisição.", example = "/api/v1/usuarios/me")
        String instance,
        @Schema(description = "Código da aplicação.", example = "SESSAO_ENCERRADA")
        String codigo
) {
}
