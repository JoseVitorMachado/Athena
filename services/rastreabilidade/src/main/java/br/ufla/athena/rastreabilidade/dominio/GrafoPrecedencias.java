package br.ufla.athena.rastreabilidade.dominio;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Grafo combinado de precedências, independente da persistência. */
public final class GrafoPrecedencias {
    private final Map<UUID, Set<UUID>> adjacencias = new HashMap<>();

    public void adicionar(UUID origem, UUID destino, TipoRelacao tipo) {
        if (tipo == TipoRelacao.RELACIONADO_A) {
            return;
        }
        UUID inicio = origem;
        UUID fim = destino;
        if (tipo == TipoRelacao.DEPENDE_DE) {
            inicio = destino;
            fim = origem;
        }
        adjacencias.computeIfAbsent(inicio, chave -> new HashSet<>()).add(fim);
    }

    public boolean criaCiclo(UUID origem, UUID destino, TipoRelacao tipo) {
        if (tipo == TipoRelacao.RELACIONADO_A) {
            return false;
        }
        UUID inicio = origem;
        UUID fim = destino;
        if (tipo == TipoRelacao.DEPENDE_DE) {
            inicio = destino;
            fim = origem;
        }
        var pendentes = new ArrayDeque<UUID>();
        var visitados = new HashSet<UUID>();
        pendentes.add(fim);
        while (!pendentes.isEmpty()) {
            UUID atual = pendentes.remove();
            if (atual.equals(inicio)) {
                return true;
            }
            if (visitados.add(atual)) {
                pendentes.addAll(adjacencias.getOrDefault(atual, Set.of()));
            }
        }
        return false;
    }
}
