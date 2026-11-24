package br.ufla.athena.integracoes.mensageria;

import java.util.Map;

public interface ManipuladorMensagem {
    String messageType();
    void tratar(Map<String, Object> envelope);
}
