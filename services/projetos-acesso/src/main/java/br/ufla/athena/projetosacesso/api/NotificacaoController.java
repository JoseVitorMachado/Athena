package br.ufla.athena.projetosacesso.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** Consulta e atualização da caixa pessoal de notificações. */
@RestController
@Validated
public class NotificacaoController {
    private final JdbcTemplate banco;
    private final IdentidadeController identidade;
    public NotificacaoController(JdbcTemplate banco, IdentidadeController identidade) { this.banco=banco; this.identidade=identidade; }
    @JsonInclude(JsonInclude.Include.NON_NULL)
    record Notificacao(UUID notificacaoId, UUID destinatarioId, UUID projetoId, String tipo, String mensagem, String link, boolean lida, Instant atualizadoEm) { }
    private static final String COLUNAS = "n.id,n.destinatario_id,n.projeto_id,n.tipo,n.mensagem,n.link,n.lida,n.atualizado_em";
    private static final String VISIVEL = " FROM notificacoes n WHERE n.destinatario_id=? AND (n.projeto_id IS NULL OR EXISTS (SELECT 1 FROM projetos p WHERE p.id=n.projeto_id AND p.excluido_em IS NULL))";
    private Notificacao mapear(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new Notificacao(rs.getObject(1,UUID.class),rs.getObject(2,UUID.class),rs.getObject(3,UUID.class),rs.getString(4),rs.getString(5),rs.getString(6),rs.getBoolean(7),rs.getTimestamp(8).toInstant());
    }
    /** Lista as notificações da pessoa autenticada. */
    @GetMapping("/api/v1/notificacoes")
    @Transactional(readOnly=true)
    public Pagina<Notificacao> listar(@RequestHeader(value="Authorization",required=false) String token,
            @RequestParam(defaultValue="0") @Min(0) int page, @RequestParam(defaultValue="20") @Min(1) @Max(100) int size,
            @RequestParam(required=false) Boolean lida) {
        UUID usuario=identidade.usuarioAutenticado(token);
        String filtro=lida==null ? "" : " AND n.lida=" + (lida ? "true" : "false");
        Long total=banco.queryForObject("SELECT count(*)"+VISIVEL+filtro,Long.class,usuario);
        var itens=banco.query("SELECT "+COLUNAS+VISIVEL+filtro+" ORDER BY n.criada_em DESC,n.id DESC LIMIT ? OFFSET ?",(rs,n)->mapear(rs),usuario,size,(long)page*size);
        String self="/api/v1/notificacoes?page="+page+"&size="+size+(lida==null?"":"&lida="+lida);
        return Pagina.de(new PageImpl<>(itens,PageRequest.of(page,size),total == null ? 0 : total),self);
    }
    /** Altera o estado de leitura de uma notificação própria. */
    @PatchMapping("/api/v1/notificacoes/{n}")
    @Transactional
    public Notificacao alterar(@RequestHeader(value="Authorization",required=false) String token, @PathVariable UUID n, @RequestBody JsonNode entrada) {
        UUID usuario=identidade.usuarioAutenticado(token);
        if (entrada==null || !entrada.isObject() || entrada.size()!=1 || !entrada.has("lida") || !entrada.get("lida").isBoolean())
            throw new Problema(HttpStatus.BAD_REQUEST,"REQUISICAO_INVALIDA","Informe somente o campo lida como booleano.");
        int alteradas=banco.update("UPDATE notificacoes SET lida=?, atualizado_em=now() WHERE id=? AND destinatario_id=?",entrada.get("lida").booleanValue(),n,usuario);
        if (alteradas==0) throw new Problema(HttpStatus.NOT_FOUND,"NOTIFICACAO_NAO_ENCONTRADA","Notificação não encontrada.");
        return banco.query("SELECT "+COLUNAS+" FROM notificacoes n WHERE n.id=? AND n.destinatario_id=?",(rs,i)->mapear(rs),n,usuario).get(0);
    }
}
