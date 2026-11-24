package br.ufla.athena.requisitos.aplicacao;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class IdempotenciaSubmissaoTest {
    @Test
    void comparaPedidoPorRequisitoEVersao() {
        UUID requisito = UUID.randomUUID();
        UUID versao = UUID.randomUUID();
        String primeira = IdempotenciaSubmissao.impressao(requisito, versao);
        assertTrue(IdempotenciaSubmissao.mesmoPedido(primeira, IdempotenciaSubmissao.impressao(requisito, versao)));
        assertFalse(IdempotenciaSubmissao.mesmoPedido(primeira, IdempotenciaSubmissao.impressao(requisito, UUID.randomUUID())));
        assertFalse(IdempotenciaSubmissao.mesmoPedido(primeira, IdempotenciaSubmissao.impressao(UUID.randomUUID(), versao)));
    }
}
