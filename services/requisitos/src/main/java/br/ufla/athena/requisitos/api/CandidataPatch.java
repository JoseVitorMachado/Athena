package br.ufla.athena.requisitos.api;

import java.util.List;

public record CandidataPatch(String titulo, String conteudo, List<String> criteriosAceite) {
}
