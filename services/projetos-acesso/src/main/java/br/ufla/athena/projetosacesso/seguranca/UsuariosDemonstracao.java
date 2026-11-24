package br.ufla.athena.projetosacesso.seguranca;

import java.util.UUID;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "athena.usuarios-demo", havingValue = "true")
public class UsuariosDemonstracao implements ApplicationRunner {
    private final JdbcTemplate banco;

    public UsuariosDemonstracao(JdbcTemplate banco) {
        this.banco = banco;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String[][] usuarios = {
                {"55555555-5555-4555-8555-555555555555", "Olívia Owner", "owner@athena.dev", "OWNER"},
                {"55555555-5555-4555-8555-555555555551", "Marcos Mantenedor", "mantenedor@athena.dev", "MANTENEDOR"},
                {"55555555-5555-4555-8555-555555555552", "Ana Aprovadora", "aprovador@athena.dev", "APROVADOR"},
                {"55555555-5555-4555-8555-555555555553", "Eduardo Editor", "editor@athena.dev", "EDITOR"},
                {"55555555-5555-4555-8555-555555555554", "Rita Repórter", "reporter@athena.dev", "REPORTER"},
                {"55555555-5555-4555-8555-555555555556", "Lucas Leitor", "leitor@athena.dev", "LEITOR"}
        };
        for (String[] usuario : usuarios) {
            UUID id = UUID.fromString(usuario[0]);
            Integer existe = banco.queryForObject("SELECT count(*) FROM usuarios WHERE id=? OR email=?", Integer.class, id, usuario[2]);
            if (existe != null && existe == 0) {
                String hash = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8().encode("Athena@2026");
                banco.update("INSERT INTO usuarios(id,nome,email,senha_hash) VALUES (?,?,?,?)", id, usuario[1], usuario[2], hash);
            }
            banco.update("INSERT INTO membros(projeto_id,usuario_id,papel) SELECT p.id,u.id,? FROM projetos p JOIN usuarios u ON u.id=? AND u.email=? WHERE p.id=? ON CONFLICT DO NOTHING", usuario[3], id, usuario[2], UUID.fromString("22222222-2222-4222-8222-222222222222"));
        }
    }
}
