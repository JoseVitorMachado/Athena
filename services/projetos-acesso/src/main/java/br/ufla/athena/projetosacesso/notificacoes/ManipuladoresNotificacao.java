package br.ufla.athena.projetosacesso.notificacoes;

import br.ufla.athena.projetosacesso.mensageria.ManipuladorMensagem;
import br.ufla.athena.projetosacesso.mensageria.MensagemInvalidaException;
import java.util.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/** Registra os consumidores de eventos que alimentam a caixa pessoal. */
@Configuration
public class ManipuladoresNotificacao {
    private final JdbcTemplate banco;
    private final CaixaNotificacoes caixa;
    public ManipuladoresNotificacao(JdbcTemplate banco, CaixaNotificacoes caixa) { this.banco=banco; this.caixa=caixa; }
    @Bean ManipuladorMensagem comentarioCriado() { return manipulador("ComentarioCriado"); }
    @Bean ManipuladorMensagem submissaoConcluida() { return manipulador("SubmissaoConcluida"); }
    @Bean ManipuladorMensagem decisaoRevisaoAplicada() { return manipulador("DecisaoRevisaoAplicada"); }
    private ManipuladorMensagem manipulador(String tipo) {
        return new ManipuladorMensagem() {
            public String messageType() { return tipo; }
            public void tratar(Map<String,Object> envelope) { processar(tipo,envelope); }
        };
    }
    private void processar(String tipo, Map<String,Object> envelope) {
        if (!(envelope.get("payload") instanceof Map<?,?> dados)) throw new MensagemInvalidaException("payload inválido");
        // Decisão sem autor conhecido (submissões antigas) ou descartada não gera aviso.
        if (tipo.equals("DecisaoRevisaoAplicada")
                && (dados.get("submissorId") == null || !"APLICADA".equals(texto(dados,"resultado")))) {
            return;
        }
        UUID projeto=dados.get("projetoId") == null ? id(envelope,"projetoId") : id(dados,"projetoId");
        // Decisões antigas não trazem requisitoId; nesse caso o link aponta para as revisões.
        UUID requisito=dados.get("requisitoId") == null && tipo.equals("DecisaoRevisaoAplicada") ? null : id(dados,"requisitoId");
        String codigo=dados.get("codigo") instanceof String c && !c.isBlank() ? c : "o requisito";
        String origem=id(envelope,"messageId").toString();
        String link=requisito == null ? "/projetos/"+projeto+"/revisoes" : "/projetos/"+projeto+"/requisitos/"+requisito;
        List<SelecaoDestinatarios.Membro> membros=banco.query("SELECT m.usuario_id,m.papel FROM membros m JOIN projetos p ON p.id=m.projeto_id WHERE m.projeto_id=? AND p.excluido_em IS NULL",(rs,n)->new SelecaoDestinatarios.Membro(rs.getObject(1,UUID.class),rs.getString(2)),projeto);
        if (tipo.equals("ComentarioCriado")) {
            UUID autor=id(dados,"autorId"); id(dados,"comentarioId"); id(dados,"versaoId");
            String trecho=texto(dados,"trecho");
            if (trecho.length()>120 || !(dados.get("mencoes") instanceof List<?> lista)) throw new MensagemInvalidaException("menções ou trecho inválidos");
            List<UUID> mencoes=new ArrayList<>();
            for (Object valor : lista) mencoes.add(uuid(valor));
            var nomes=banco.query("SELECT nome FROM usuarios WHERE id=?",(rs,n)->rs.getString(1),autor);
            if (nomes.isEmpty()) throw new MensagemInvalidaException("autor não encontrado");
            for (UUID destinatario : SelecaoDestinatarios.mencoes(membros,mencoes,autor))
                caixa.notificar(destinatario,projeto,"MENCAO",MensagensNotificacao.mencao(nomes.get(0),codigo,trecho),link,origem);
        } else if (tipo.equals("SubmissaoConcluida")) {
            // Submissões anteriores ao registro do autor chegam sem submissorId.
            UUID submissor=dados.get("submissorId") == null ? null : id(dados,"submissorId");
            UUID lote=id(dados,"loteId"); id(dados,"submissaoId");
            String titulo=texto(dados,"titulo");
            for (UUID destinatario : SelecaoDestinatarios.revisores(membros,submissor))
                caixa.notificar(destinatario,projeto,"REVISAO_DISPONIVEL",MensagensNotificacao.revisao(codigo,titulo),"/projetos/"+projeto+"/revisoes/"+lote,origem);
        } else {
            String decisao=dados.get("decisao") instanceof String d ? d : null;
            Object valor=dados.get("submissorId");
            if (valor != null) {
                UUID submissor=uuid(valor);
                if (membros.stream().anyMatch(m -> m.id().equals(submissor)))
                    caixa.notificar(submissor,projeto,"DECISAO_REVISAO",MensagensNotificacao.decisao(codigo,decisao),link,origem);
            }
        }
    }
    private static String texto(Map<?,?> dados,String chave) {
        if (!(dados.get(chave) instanceof String valor) || valor.isBlank()) throw new MensagemInvalidaException("campo inválido: "+chave);
        return valor;
    }
    private static UUID id(Map<?,?> dados,String chave) { return uuid(dados.get(chave)); }
    private static UUID uuid(Object valor) {
        try { if (!(valor instanceof String)) throw new IllegalArgumentException(); return UUID.fromString((String)valor); }
        catch (IllegalArgumentException erro) { throw new MensagemInvalidaException("identificador inválido"); }
    }
}
