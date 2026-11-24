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
        if (caminhoDeDominio.startsWith("http://") || caminhoDeDominio.startsWith("https://")) {
            java.net.URI uri = java.net.URI.create(caminhoDeDominio);
            String caminho = uri.getRawPath();
            if (uri.getRawQuery() != null) {
                caminho += "?" + uri.getRawQuery();
            }
            if (corresponde(caminho, "/api/v1/projetos") || corresponde(caminho, "/api/v1/workspaces")) {
                return paraGestao(caminho);
            }
        }
        if (corresponde(caminhoDeDominio, "/api/v1/workspaces")) {
            return "/api/v1/gestao/workspaces" + caminhoDeDominio.substring("/api/v1/workspaces".length());
        }
        if (caminhoDeDominio.equals("/api/v1/projetos") || caminhoDeDominio.startsWith("/api/v1/projetos?")) {
            return "/api/v1/gestao/projetos" + caminhoDeDominio.substring("/api/v1/projetos".length());
        }
        if (!caminhoDeDominio.startsWith(DOMINIO)) {
            return caminhoDeDominio;
        }
        String restante = caminhoDeDominio.substring(DOMINIO.length());
        int separador = restante.indexOf('/');
        if (separador >= 0) {
            String projeto = restante.substring(0, separador);
            String recurso = restante.substring(separador);
            if (recurso.equals("/lotes") || recurso.startsWith("/lotes/") || recurso.startsWith("/lotes?")) {
                restante = projeto + "/revisoes" + recurso.substring("/lotes".length());
            }
        }
        return GESTAO + restante;
    }

    private static boolean corresponde(String caminho, String base) {
        return caminho.equals(base) || caminho.startsWith(base + "/") || caminho.startsWith(base + "?");
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
