package br.ufla.athena.projetosacesso.notificacoes;

/** Montagem das mensagens exibidas na caixa de notificações. */
public final class MensagensNotificacao {
    private MensagensNotificacao() { }
    public static String convite(String projeto, String papel) { return "Você foi convidado para o projeto " + projeto + " como " + papel + "."; }
    public static String transferencia(String owner, String projeto) { return owner + " quer transferir a propriedade do projeto " + projeto + " para você."; }
    public static String papel(String projeto, String papel) { return "Seu papel no projeto " + projeto + " agora é " + papel + "."; }
    public static String removido(String projeto) { return "Você foi removido do projeto " + projeto + "."; }
    public static String conviteAceito(String nome, String projeto) { return nome + " aceitou o convite para o projeto " + projeto + "."; }
    public static String propriedadeTransferida(String nome, String projeto) { return nome + " aceitou a transferência de propriedade do projeto " + projeto + "."; }
    public static String mencao(String autor, String codigo, String trecho) { return autor + " mencionou você em " + codigo + ": " + trecho; }
    public static String revisao(String codigo, String titulo) { return "Revisão disponível: " + codigo + " — " + titulo + "."; }
    /** Mensagem para quem submeteu, conforme a decisão aplicada. */
    public static String decisao(String codigo, String decisao) {
        if ("APROVAR".equals(decisao)) return "Sua alteração em " + codigo + " foi aprovada e pode ser publicada.";
        if ("REJEITAR".equals(decisao)) return "Sua alteração em " + codigo + " foi rejeitada e voltou para rascunho.";
        if ("SOLICITAR_CORRECOES".equals(decisao)) return "Foram solicitadas correções em " + codigo + ".";
        return "A revisão de " + codigo + " foi concluída.";
    }
}
