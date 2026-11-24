package br.ufla.athena.projetosacesso.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EnvioEmail {
    private static final Logger LOG = LoggerFactory.getLogger(EnvioEmail.class);
    private final ObjectProvider<JavaMailSender> remetente;
    private final String host;
    private final String endereco;

    public EnvioEmail(ObjectProvider<JavaMailSender> remetente, @Value("${spring.mail.host:}") String host,
                      @Value("${athena.email.remetente:nao-responda@athena.local}") String endereco) {
        this.remetente = remetente;
        this.host = host;
        this.endereco = endereco;
    }

    public void enviar(String email, String codigo) {
        if (host.isBlank()) {
            LOG.info("Envio de e-mail desativado.");
            return;
        }
        try {
            SimpleMailMessage mensagem = new SimpleMailMessage();
            mensagem.setFrom(endereco);
            mensagem.setTo(email);
            mensagem.setSubject("ATHENA — código de recuperação");
            mensagem.setText("Seu código de recuperação é " + codigo + ", válido por 10 minutos; se não foi você, ignore este e-mail.");
            remetente.getObject().send(mensagem);
        } catch (org.springframework.mail.MailException e) {
            LOG.warn("Não foi possível enviar o e-mail de recuperação.");
        }
    }
}
