package br.ufla.athena.projetosacesso.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/** Schema ProjetoEntrada de docs/api/projetos-acesso.yaml. */
public record ProjetoEntrada(
        @NotNull UUID workspaceId,
        @NotBlank @Size(max = 120) String nome,
        @Size(max = 1000) String descricao) {
}
