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

    /**
     * Reescreve os href de _links para que o cliente continue navegando pelo BFF.
     * Também troca o "instance" dos Problem Details: o caminho interno do domínio
     * não deve aparecer para o cliente.
     */
    static void reescreverLinks(JsonNode corpo) {
        if (corpo == null) {
            return;
        }
        if (corpo.isArray()) {
            corpo.forEach(Caminhos::reescreverLinks);
            return;
        }
        if (!corpo.isObject()) {
            return;
        }
        if (corpo instanceof ObjectNode objeto && corpo.path("instance").isTextual()) {
            objeto.put("instance", paraGestao(corpo.get("instance").asText()));
        }
        JsonNode links = corpo.get("_links");
        if (links != null && links.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> entradas = links.fields();
            while (entradas.hasNext()) {
                JsonNode link = entradas.next().getValue();
                if (link instanceof ObjectNode objeto && link.hasNonNull("href")) {
                    objeto.put("href", paraGestao(link.get("href").asText()));
                }
            }
        }
        corpo.elements().forEachRemaining(Caminhos::reescreverLinks);
    }
}
