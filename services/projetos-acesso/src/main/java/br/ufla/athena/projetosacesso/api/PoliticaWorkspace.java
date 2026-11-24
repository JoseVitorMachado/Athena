package br.ufla.athena.projetosacesso.api;

import java.util.Collection;
import java.util.UUID;

final class PoliticaWorkspace {
    private PoliticaWorkspace() { }

    static boolean owner(UUID usuario, UUID owner) { return usuario.equals(owner); }
    static boolean visivel(UUID usuario, UUID owner, Collection<String> papeis) {
        return owner(usuario, owner) || !papeis.isEmpty();
    }
    static boolean mantenedor(String papel) {
        return "OWNER".equals(papel) || "MANTENEDOR".equals(papel);
    }
    static boolean podeCriarProjeto(UUID usuario, UUID owner, Collection<String> papeis) {
        // Decisão ATHENA para o x-decidir do contrato: proprietário do workspace ou mantenedor de projeto existente.
        return owner(usuario, owner) || papeis.stream().anyMatch(PoliticaWorkspace::mantenedor);
    }
    static boolean confirmacaoValida(String nome, String confirmacao) {
        return confirmacao != null && nome.equals(confirmacao.trim());
    }
}
