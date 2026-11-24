package br.ufla.athena.projetosacesso.notificacoes;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Seleciona destinatários a partir dos vínculos atuais de um projeto. */
public final class SelecaoDestinatarios {
    private SelecaoDestinatarios() { }
    public record Membro(UUID id, String papel) { }
    /** Seleciona membros mencionados, exceto o autor. */
    public static List<UUID> mencoes(List<Membro> membros, List<UUID> mencoes, UUID autor) {
        return membros.stream().map(Membro::id).filter(id -> !id.equals(autor) && mencoes.contains(id)).distinct().toList();
    }
    /** Seleciona aprovadores e papéis superiores, exceto o submissor. */
    public static List<UUID> revisores(List<Membro> membros, UUID submissor) {
        return membros.stream().filter(m -> Set.of("APROVADOR", "MANTENEDOR", "OWNER").contains(m.papel()))
                .map(Membro::id).filter(id -> !id.equals(submissor)).distinct().toList();
    }
}
