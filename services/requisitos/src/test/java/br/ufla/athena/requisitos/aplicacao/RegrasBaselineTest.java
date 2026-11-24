package br.ufla.athena.requisitos.aplicacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RegrasBaselineTest {
    @Test
    void validaPublicacaoRetratoERepeticao() {
        UUID requisito = UUID.randomUUID(), versao = UUID.randomUUID(), retrato = UUID.randomUUID();
        var item = new RegrasBaseline.Item(requisito, versao, retrato);
        var vinculo = new RegrasBaseline.Vinculo(requisito, versao, retrato, true);
        assertNull(RegrasBaseline.primeiroInvalido(List.of(item), List.of(vinculo)));
        assertEquals(requisito, RegrasBaseline.primeiroInvalido(List.of(item, item), List.of(vinculo)));
        assertEquals(requisito, RegrasBaseline.primeiroInvalido(List.of(item), List.of(new RegrasBaseline.Vinculo(requisito, versao, retrato, false))));
        assertEquals(requisito, RegrasBaseline.primeiroInvalido(List.of(item), List.of(new RegrasBaseline.Vinculo(requisito, versao, UUID.randomUUID(), true))));
        assertEquals(requisito, RegrasBaseline.primeiroInvalido(List.of(item), List.of(new RegrasBaseline.Vinculo(UUID.randomUUID(), versao, retrato, true))));
        assertEquals(requisito, RegrasBaseline.primeiroInvalido(List.of(item), List.of(new RegrasBaseline.Vinculo(requisito, UUID.randomUUID(), retrato, true))));
    }
}
