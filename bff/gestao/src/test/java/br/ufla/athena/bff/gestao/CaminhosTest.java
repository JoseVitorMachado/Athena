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
        assertEquals("/api/v1/workspaces-outros", Caminhos.paraGestao("/api/v1/workspaces-outros"));
    }

    @Test
    void traduzLinksEInstance() throws Exception {
        var corpo = new ObjectMapper().readTree("""
                {"_links":{"self":{"href":"/api/v1/workspaces?page=1"}},
                 "items":[{"_links":{"self":{"href":"/api/v1/projetos/p"}}}],
                 "instance":"/api/v1/workspaces/w"}
                """);
        Caminhos.reescreverLinks(corpo);
        assertEquals("/api/v1/gestao/workspaces?page=1", corpo.path("_links").path("self").path("href").asText());
        assertEquals("/api/v1/gestao/projetos/p", corpo.path("items").get(0).path("_links").path("self").path("href").asText());
        assertEquals("/api/v1/gestao/workspaces/w", corpo.path("instance").asText());
    }
}
