package br.ufla.athena.requisitos.aplicacao;

import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;

/** Registro transacional das ações do requisito. */
public final class Historico {
    private Historico() { }
    public static void registrar(JdbcTemplate banco, UUID projeto, UUID requisito, UUID versao,
                                 String acao, UUID autor, String detalhe) {
        banco.update("INSERT INTO historico_requisitos (id,projeto_id,requisito_id,versao_id,acao,autor_id,detalhe) VALUES (?,?,?,?,?,?,?)",
                UUID.randomUUID(), projeto, requisito, versao, acao, autor, detalhe);
    }
    public static void editar(JdbcTemplate banco, UUID projeto, UUID requisito, UUID versao, UUID autor) {
        int atualizadas = banco.update("UPDATE historico_requisitos SET data=now() WHERE id=(SELECT id FROM historico_requisitos WHERE requisito_id=? ORDER BY data DESC, id DESC LIMIT 1) AND acao='CANDIDATA_EDITADA' AND versao_id=? AND autor_id IS NOT DISTINCT FROM ? AND data > now()-interval '5 minutes'", requisito, versao, autor);
        if (atualizadas == 0) registrar(banco, projeto, requisito, versao, "CANDIDATA_EDITADA", autor, null);
    }
}
