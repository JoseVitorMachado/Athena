package br.ufla.athena.bff.gestao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Iterator;
import java.util.Map;

/**
 * Tradução entre os caminhos dos serviços de domínio (/api/v1/projetos/...) e os
 * caminhos que o cliente Gestão enxerga (/api/v1/gestao/projetos/...).
 */
final class Caminhos {

    static final String DOMINIO = "/api/v1/projetos/";
    static final String GESTAO = "/api/v1/gestao/projetos/";

    private Caminhos() {
    }

    static String paraGestao(String caminhoDeDominio) {
        return caminhoDeDominio.startsWith(DOMINIO)
                ? GESTAO + caminhoDeDominio.substring(DOMINIO.length())
                : caminhoDeDominio;
    }

    /** Reescreve os href de _links para que o cliente continue navegando pelo BFF. */
    static void reescreverLinks(JsonNode corpo) {
        if (corpo == null || !corpo.has("_links")) {
            return;
        }
        Iterator<Map.Entry<String, JsonNode>> links = corpo.get("_links").fields();
        while (links.hasNext()) {
            JsonNode link = links.next().getValue();
            if (link instanceof ObjectNode objeto && link.hasNonNull("href")) {
                objeto.put("href", paraGestao(link.get("href").asText()));
            }
        }
    }
}
