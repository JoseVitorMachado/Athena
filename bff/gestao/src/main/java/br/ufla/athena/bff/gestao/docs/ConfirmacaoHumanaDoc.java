package br.ufla.athena.bff.gestao.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record ConfirmacaoHumanaDoc(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID versaoId,
        @Schema(description = "SHA-256 em hexadecimal minúsculo.", pattern = "^[a-f0-9]{64}$", requiredMode = Schema.RequiredMode.REQUIRED) String hashAcao,
        @Schema(description = "Deve ser true.", requiredMode = Schema.RequiredMode.REQUIRED) Boolean confirmacao,
        @Schema(description = "Senha do usuário autenticado.", format = "password", requiredMode = Schema.RequiredMode.REQUIRED) String senha) {}
