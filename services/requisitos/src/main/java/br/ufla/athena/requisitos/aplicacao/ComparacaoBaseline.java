package br.ufla.athena.requisitos.aplicacao;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Classifica os itens congelados em relação ao Livro corrente. */
public final class ComparacaoBaseline {
    private ComparacaoBaseline() { }
    public enum Situacao { INALTERADO, ALTERADO, FORA_DO_LIVRO, NOVO }
    public record Conteudo(UUID requisitoId, UUID versaoId, String codigo, String titulo, int numero) { }
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Item(UUID requisitoId, String codigo, String titulo, Situacao situacao,
                       Integer numeroBaseline, Integer numeroAtual) { }
    public record Resultado(List<Item> itens, Map<Situacao, Integer> contagens) { }

    public static Resultado comparar(List<Conteudo> baseline, List<Conteudo> livro) {
        Map<UUID, Conteudo> atuais = new LinkedHashMap<>();
        livro.forEach(c -> atuais.put(c.requisitoId(), c));
        List<Item> itens = new ArrayList<>();
        for (Conteudo anterior : baseline) {
            Conteudo atual = atuais.remove(anterior.requisitoId());
            Situacao situacao = atual == null ? Situacao.FORA_DO_LIVRO
                    : atual.versaoId().equals(anterior.versaoId()) ? Situacao.INALTERADO : Situacao.ALTERADO;
            Conteudo exibido = atual == null ? anterior : atual;
            itens.add(new Item(exibido.requisitoId(), exibido.codigo(), exibido.titulo(), situacao,
                    situacao == Situacao.ALTERADO ? anterior.numero() : null,
                    situacao == Situacao.ALTERADO ? atual.numero() : null));
        }
        atuais.values().forEach(c -> itens.add(new Item(c.requisitoId(), c.codigo(), c.titulo(), Situacao.NOVO, null, null)));
        Map<Situacao, Integer> contagens = new EnumMap<>(Situacao.class);
        for (Situacao situacao : Situacao.values()) contagens.put(situacao, 0);
        itens.forEach(i -> contagens.merge(i.situacao(), 1, Integer::sum));
        return new Resultado(List.copyOf(itens), contagens);
    }
}
