package br.ufla.athena.bff.gestao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class CaminhosTest {

    @Test
    void traduzColecoesERecursos() {
        assertEquals("/api/v1/gestao/workspaces", Caminhos.paraGestao("/api/v1/workspaces"));
        assertEquals("/api/v1/gestao/workspaces/w", Caminhos.paraGestao("http://interno/api/v1/workspaces/w"));
        assertEquals("/api/v1/gestao/workspaces?page=1", Caminhos.paraGestao("/api/v1/workspaces?page=1"));
        assertEquals("/api/v1/gestao/projetos/p", Caminhos.paraGestao("/api/v1/projetos/p"));
        assertEquals("/api/v1/gestao/projetos?page=1&sort=nome%2Casc",
                Caminhos.paraGestao("http://interno/api/v1/projetos?page=1&sort=nome%2Casc"));
        assertEquals("/api/v1/gestao/projetos/p/revisoes/l", Caminhos.paraGestao("/api/v1/projetos/p/lotes/l"));
        assertEquals("/api/v1/gestao/projetos/p/membros/m", Caminhos.paraGestao("/api/v1/projetos/p/membros/m"));
        assertEquals("/api/v1/gestao/projetos/p/convites/c", Caminhos.paraGestao("/api/v1/projetos/p/convites/c"));
        assertEquals("/api/v1/gestao/projetos/p/transferencias-propriedade/t", Caminhos.paraGestao("/api/v1/projetos/p/transferencias-propriedade/t"));
        assertEquals("/api/v1/gestao/notificacoes?page=1", Caminhos.paraGestao("/api/v1/notificacoes?page=1"));
        assertEquals("/api/v1/gestao/notificacoes/n", Caminhos.paraGestao("https://interno/api/v1/notificacoes/n"));
        assertEquals("/api/v1/notificacoes-outras", Caminhos.paraGestao("/api/v1/notificacoes-outras"));
        assertEquals("/api/v1/gestao/convites?page=1", Caminhos.paraGestao("/api/v1/convites?page=1"));
        assertEquals("/api/v1/gestao/convites/c/aceite", Caminhos.paraGestao("https://interno/api/v1/convites/c/aceite"));
        assertEquals("/api/v1/gestao/transferencias-propriedade", Caminhos.paraGestao("/api/v1/transferencias-propriedade"));
        assertEquals("/api/v1/gestao/transferencias-propriedade/t/aceite?x=1", Caminhos.paraGestao("http://interno/api/v1/transferencias-propriedade/t/aceite?x=1"));
        assertEquals("/api/v1/convites-outros", Caminhos.paraGestao("/api/v1/convites-outros"));
        assertEquals("/api/v1/workspaces-outros", Caminhos.paraGestao("/api/v1/workspaces-outros"));
    }

    @Test
    void traduzLinksEInstance() throws Exception {
        var corpo = new ObjectMapper().readTree("""
                {"_links":{"self":{"href":"/api/v1/workspaces?page=1"}},
                 "items":[{"_links":{"self":{"href":"/api/v1/projetos/p"},"notificacao":{"href":"/api/v1/notificacoes/n"},"convite":{"href":"/api/v1/convites/c"},"transferencia":{"href":"/api/v1/transferencias-propriedade/t"}}}],
                 "instance":"/api/v1/workspaces/w"}
                """);
        Caminhos.reescreverLinks(corpo);
        assertEquals("/api/v1/gestao/workspaces?page=1", corpo.path("_links").path("self").path("href").asText());
        assertEquals("/api/v1/gestao/projetos/p", corpo.path("items").get(0).path("_links").path("self").path("href").asText());
        assertEquals("/api/v1/gestao/notificacoes/n", corpo.path("items").get(0).path("_links").path("notificacao").path("href").asText());
        assertEquals("/api/v1/gestao/convites/c", corpo.path("items").get(0).path("_links").path("convite").path("href").asText());
        assertEquals("/api/v1/gestao/transferencias-propriedade/t", corpo.path("items").get(0).path("_links").path("transferencia").path("href").asText());
        assertEquals("/api/v1/gestao/workspaces/w", corpo.path("instance").asText());
    }
}
