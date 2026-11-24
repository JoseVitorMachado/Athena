package br.ufla.athena.projetosacesso.api;

import br.ufla.athena.projetosacesso.mensageria.EmissorMensagens;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicoExclusao {
    private static final Logger log = LoggerFactory.getLogger(ServicoExclusao.class);
    private final JdbcTemplate banco;
    private final EmissorMensagens emissor;
    private final List<String> participantes;
    private final Duration intervalo;
    public ServicoExclusao(JdbcTemplate banco, EmissorMensagens emissor,
            @Value("${athena.exclusao.participantes:requisitos,rastreabilidade,revisoes,integracoes,impacto-mcp}") List<String> participantes,
            @Value("${athena.exclusao.reenvio:15s}") Duration intervalo) {
        this.banco = banco; this.emissor = emissor; this.participantes = List.copyOf(participantes); this.intervalo = intervalo;
        if (participantes.isEmpty() || participantes.stream().anyMatch(String::isBlank) || new HashSet<>(participantes).size() != participantes.size()) throw new IllegalArgumentException("Participantes inválidos");
    }
    public record Confirmacao(String servico, boolean quiescenciaConfirmada, boolean limpezaConfirmada) { }
    public record Exclusao(UUID exclusaoId, UUID projetoId, String estado, List<Confirmacao> confirmacoes, Instant atualizadoEm) { }
    private record Projeto(String nome, UUID owner, boolean arquivado, long revisao, boolean excluido) { }
    private record Registro(UUID id, UUID projeto, String estado) { }

    @Transactional
    public Exclusao criar(UUID projeto, UUID usuario, String ifMatch, String chave, String nome) {
        if (ifMatch == null || ifMatch.isBlank()) throw new Problema(HttpStatus.PRECONDITION_REQUIRED, "PRECONDICAO_OBRIGATORIA", "Informe If-Match.");
        if (chave == null || chave.isBlank()) throw new Problema(HttpStatus.PRECONDITION_REQUIRED, "PRECONDICAO_OBRIGATORIA", "Informe Idempotency-Key.");
        if (chave.length() > 200) throw new Problema(HttpStatus.BAD_REQUEST, "REQUISICAO_INVALIDA", "Chave de idempotência inválida.");
        var projetos = banco.query("SELECT nome,owner_id,arquivado,revisao,excluido_em IS NOT NULL FROM projetos WHERE id=? FOR UPDATE",
                (rs,n) -> new Projeto(rs.getString(1), rs.getObject(2,UUID.class), rs.getBoolean(3), rs.getLong(4), rs.getBoolean(5)), projeto);
        if (projetos.isEmpty()) throw AcessoRecursos.projetoNaoEncontrado();
        Projeto p = projetos.get(0);
        var anteriores = banco.query("SELECT id FROM exclusoes_projeto WHERE projeto_id=? AND chave_idempotencia=? AND solicitante_id=? ORDER BY criada_em DESC LIMIT 1", (rs,n) -> rs.getObject(1,UUID.class), projeto, chave, usuario);
        if (!anteriores.isEmpty()) { AcessoRecursos.revisao(ifMatch, p.revisao()); return carregar(projeto, anteriores.get(0)); }
        if (p.excluido()) throw AcessoRecursos.projetoNaoEncontrado();
        Integer membro = banco.queryForObject("SELECT count(*) FROM membros WHERE projeto_id=? AND usuario_id=? AND papel='OWNER'", Integer.class, projeto, usuario);
        if (!p.owner().equals(usuario) || membro == null || membro == 0) throw AcessoRecursos.insuficiente();
        var ativas = banco.queryForList("SELECT id FROM exclusoes_projeto WHERE projeto_id=? AND estado<>'CONCLUIDA'", UUID.class, projeto);
        if (!ativas.isEmpty()) throw new Problema(HttpStatus.CONFLICT, "EXCLUSAO_EM_ANDAMENTO", "Exclusão em andamento.");
        if (!p.arquivado()) throw new Problema(HttpStatus.CONFLICT, "PROJETO_NAO_ARQUIVADO", "Projeto não arquivado.");
        AcessoRecursos.revisao(ifMatch, p.revisao());
        if (!Objects.equals(p.nome(), nome)) throw new Problema(HttpStatus.BAD_REQUEST, "CONFIRMACAO_INVALIDA", "Nome de confirmação inválido.");
        UUID id = UUID.randomUUID();
        banco.update("INSERT INTO exclusoes_projeto(id,projeto_id,estado,solicitante_id,chave_idempotencia) VALUES (?,?,'FECHANDO',?,?)", id, projeto, usuario, chave);
        banco.update("UPDATE projetos SET em_exclusao=true WHERE id=?", projeto);
        for (String servico : participantes) {
            banco.update("INSERT INTO exclusao_confirmacoes(exclusao_id,servico) VALUES (?,?)", id, servico);
            enviar(id, projeto, servico, "FecharProjeto");
        }
        return carregar(projeto, id);
    }
    @Transactional(readOnly = true)
    public Exclusao obter(UUID projeto, UUID id, UUID usuario) {
        // Decisão do x-decidir: solicitante ou owner persistido acompanha mesmo após o bloqueio e a limpeza dos membros.
        Integer total = banco.queryForObject("SELECT count(*) FROM exclusoes_projeto e JOIN projetos p ON p.id=e.projeto_id WHERE e.id=? AND e.projeto_id=? AND (e.solicitante_id=? OR p.owner_id=?)", Integer.class, id, projeto, usuario, usuario);
        if (total == null || total == 0) throw naoEncontrada();
        return carregar(projeto, id);
    }
    private Exclusao carregar(UUID projeto, UUID id) {
        var registros = banco.query("SELECT estado,atualizada_em FROM exclusoes_projeto WHERE id=? AND projeto_id=?", (rs,n) -> new Object[]{rs.getString(1), rs.getTimestamp(2).toInstant()}, id, projeto);
        if (registros.isEmpty()) throw naoEncontrada();
        var confirmacoes = banco.query("SELECT servico,quiescencia_em IS NOT NULL,limpeza_em IS NOT NULL FROM exclusao_confirmacoes WHERE exclusao_id=? ORDER BY servico", (rs,n) -> new Confirmacao(rs.getString(1),rs.getBoolean(2),rs.getBoolean(3)), id);
        return new Exclusao(id, projeto, (String) registros.get(0)[0], confirmacoes, (Instant) registros.get(0)[1]);
    }
    @Transactional
    public void confirmar(UUID id, UUID projeto, String servico, boolean limpeza) {
        var registros = banco.query("SELECT id,projeto_id,estado FROM exclusoes_projeto WHERE id=? AND projeto_id=? FOR UPDATE", (rs,n) -> new Registro(rs.getObject(1,UUID.class),rs.getObject(2,UUID.class),rs.getString(3)), id, projeto);
        if (registros.isEmpty()) return;
        Registro r = registros.get(0);
        if (!(limpeza ? r.estado().equals("EXCLUINDO") : r.estado().equals("FECHANDO"))) return;
        int alterados = banco.update("UPDATE exclusao_confirmacoes SET " + (limpeza ? "limpeza_em" : "quiescencia_em") + "=COALESCE(" + (limpeza ? "limpeza_em" : "quiescencia_em") + ",now()) WHERE exclusao_id=? AND servico=?", id, servico);
        if (alterados == 0) return;
        banco.update("UPDATE exclusoes_projeto SET atualizada_em=now() WHERE id=?", id);
        String proximo = MaquinaExclusao.proximo(r.estado(), confirmacoes(id));
        if (proximo.equals(r.estado())) return;
        banco.update("UPDATE exclusoes_projeto SET estado=?,atualizada_em=now(),concluida_em=CASE WHEN ?='CONCLUIDA' THEN now() ELSE concluida_em END WHERE id=?", proximo, proximo, id);
        log.info("Exclusão {} do projeto {}: {} -> {}", id, projeto, r.estado(), proximo);
        if (proximo.equals("EXCLUINDO")) {
            for (var c : confirmacoes(id)) enviar(id, projeto, c.servico(), "LimparProjeto");
        } else {
            for (String tabela : List.of("notificacoes", "historico_membros", "convites", "transferencias_propriedade", "membros")) banco.update("DELETE FROM " + tabela + " WHERE projeto_id=?", projeto);
            banco.update("UPDATE projetos SET nome='Projeto excluído',descricao=NULL,excluido_em=now(),atualizado_em=now() WHERE id=?", projeto);
            emissor.evento("ProjetoExcluido", projeto, null, null, UUID.randomUUID(), null, Map.of("projetoId", projeto, "exclusaoId", id));
        }
    }
    @Scheduled(fixedDelayString = "${athena.exclusao.reenvio:15s}")
    @Transactional
    public void reenviar() {
        var registros = banco.query("SELECT id,projeto_id,estado FROM exclusoes_projeto WHERE estado IN ('FECHANDO','EXCLUINDO') FOR UPDATE SKIP LOCKED", (rs,n) -> new Registro(rs.getObject(1,UUID.class),rs.getObject(2,UUID.class),rs.getString(3)));
        for (Registro r : registros) for (String servico : MaquinaExclusao.reenviar(r.estado(), confirmacoes(r.id()), Instant.now(), intervalo))
            enviar(r.id(), r.projeto(), servico, r.estado().equals("FECHANDO") ? "FecharProjeto" : "LimparProjeto");
    }
    private List<MaquinaExclusao.Confirmacao> confirmacoes(UUID id) {
        return banco.query("SELECT servico,quiescencia_em,limpeza_em,ultimo_envio_em FROM exclusao_confirmacoes WHERE exclusao_id=?", (rs,n) -> new MaquinaExclusao.Confirmacao(rs.getString(1), instante(rs.getTimestamp(2)), instante(rs.getTimestamp(3)), instante(rs.getTimestamp(4))), id);
    }
    private static Instant instante(java.sql.Timestamp valor) { return valor == null ? null : valor.toInstant(); }
    private void enviar(UUID id, UUID projeto, String servico, String tipo) {
        emissor.comando(servico, tipo, projeto, null, UUID.randomUUID(), UUID.randomUUID(), null, Map.of("exclusaoId", id, "projetoId", projeto));
        banco.update("UPDATE exclusao_confirmacoes SET ultimo_envio_em=now() WHERE exclusao_id=? AND servico=?", id, servico);
    }
    private static Problema naoEncontrada() { return new Problema(HttpStatus.NOT_FOUND, "EXCLUSAO_NAO_ENCONTRADA", "Exclusão não encontrada."); }
}
