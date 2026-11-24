package br.ufla.athena.requisitos.aplicacao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Validação dos vínculos informados para uma baseline. */
public final class RegrasBaseline {
    private RegrasBaseline() { }

    public record Item(UUID requisitoId, UUID versaoId, UUID retratoId) { }
    public record Vinculo(UUID requisitoId, UUID versaoId, UUID retratoId, boolean publicada) { }

    /** Retorna o primeiro requisito inválido ou repetido, ou null quando todos são válidos. */
    public static UUID primeiroInvalido(List<Item> itens, List<Vinculo> vinculos) {
        Set<UUID> vistos = new HashSet<>();
        for (Item item : itens) {
            if (!vistos.add(item.requisitoId()) || vinculos.stream().noneMatch(v -> v.publicada()
                    && v.requisitoId().equals(item.requisitoId()) && v.versaoId().equals(item.versaoId())
                    && v.retratoId().equals(item.retratoId()))) return item.requisitoId();
        }
        return null;
    }
}
