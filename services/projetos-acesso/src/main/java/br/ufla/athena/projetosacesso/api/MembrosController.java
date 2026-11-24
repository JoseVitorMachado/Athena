package br.ufla.athena.projetosacesso.api;

import br.ufla.athena.projetosacesso.dominio.Projeto;
import br.ufla.athena.projetosacesso.mensageria.EmissorMensagens;
import br.ufla.athena.projetosacesso.notificacoes.CaixaNotificacoes;
import br.ufla.athena.projetosacesso.notificacoes.MensagensNotificacao;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** Gestão de membros, convites e propriedade de projetos. */
@RestController
@Validated
public class MembrosController {
    private final AcessoRecursos acesso;
    private final IdentidadeController identidade;
    private final EmissorMensagens emissor;
    private final EntityManager entidades;
    private final CaixaNotificacoes caixa;

    public MembrosController(AcessoRecursos acesso, IdentidadeController identidade, EmissorMensagens emissor, EntityManager entidades, CaixaNotificacoes caixa) {
        this.acesso = acesso; this.identidade = identidade; this.emissor = emissor; this.entidades = entidades; this.caixa = caixa;
    }
    record PapelEntrada(@NotBlank String papel) { }
    record ConviteEntrada(@NotBlank @Email @Size(max = 254) String email, @NotBlank String papel) {
        ConviteEntrada { if (email != null) email = email.trim().toLowerCase(Locale.ROOT); }
    }
    record TransferenciaEntrada(@NotNull UUID destinatarioId, Boolean confirmacao) { }
    record Membro(UUID membroId, UUID usuarioId, String nome, String email, String papel, Instant atualizadoEm) { }
    @JsonInclude(JsonInclude.Include.NON_NULL)
    record Historico(UUID autor, UUID membroId, String papelAnterior, String papelNovo, Instant data) { }
    @JsonInclude(JsonInclude.Include.NON_NULL)
    record Convite(UUID conviteId, UUID projetoId, String email, String papel, boolean aceito, Instant atualizadoEm, String projetoNome) { }
    @JsonInclude(JsonInclude.Include.NON_NULL)
    record Transferencia(UUID transferenciaId, UUID projetoId, UUID ownerAtualId, UUID destinatarioId, boolean aceita, Instant atualizadoEm, String projetoNome) { }
    record ConviteEstado(UUID id, UUID projeto, String email, PoliticaMembros.Papel papel, UUID autor, Instant expira, Instant aceito, Instant cancelado) { }
    record TransferenciaEstado(UUID id, UUID projeto, UUID owner, UUID destinatario, Instant aceita, Instant cancelada) { }

    @GetMapping("/api/v1/projetos/{p}/membros")
    @Transactional(readOnly = true)
    public Pagina<Membro> membros(@RequestHeader(value="Authorization", required=false) String token, @PathVariable UUID p,
            @RequestParam(defaultValue="0") @Min(0) int page, @RequestParam(defaultValue="20") @Min(1) @Max(100) int size) {
        acesso.projeto(p, identidade.usuarioAutenticado(token), false);
        var lista = acesso.banco.query("SELECT m.usuario_id,u.nome,u.email,m.papel,m.atualizado_em FROM membros m JOIN usuarios u ON u.id=m.usuario_id WHERE m.projeto_id=? ORDER BY CASE m.papel WHEN 'OWNER' THEN 0 WHEN 'MANTENEDOR' THEN 1 WHEN 'APROVADOR' THEN 2 WHEN 'EDITOR' THEN 3 WHEN 'REPORTER' THEN 4 ELSE 5 END,u.nome,m.usuario_id", (rs,n) -> membro(rs), p);
        return pagina(lista, page, size, "/api/v1/projetos/" + p + "/membros");
    }
    @PatchMapping("/api/v1/projetos/{p}/membros/{m}")
    @Transactional
    public Membro alterar(@RequestHeader(value="Authorization", required=false) String token, @PathVariable UUID p, @PathVariable UUID m, @Valid @RequestBody PapelEntrada entrada) {
        UUID ator = identidade.usuarioAutenticado(token); projetoEscrita(p, ator);
        var atual = papelObrigatorio(p, m); var novo = papelEntrada(entrada.papel());
        if (!PoliticaMembros.podeGerenciar(papelObrigatorio(p, ator), atual, novo)) throw AcessoRecursos.insuficiente();
        if (atual != novo) {
            acesso.banco.update("UPDATE membros SET papel=?,atualizado_em=now() WHERE projeto_id=? AND usuario_id=?", novo.name(), p, m);
            historico(p, m, ator, atual, novo);
            caixa.notificar(m,p,"PAPEL_ALTERADO",MensagensNotificacao.papel(nomeProjeto(p),contrato(novo.name())),null,null);
            evento("PapelMembroAlterado", p, Map.of("projetoId",p,"usuarioId",m,"papelAnterior",atual.name(),"papelNovo",novo.name(),"autorId",ator));
        }
        return membro(p, m);
    }
    @DeleteMapping("/api/v1/projetos/{p}/membros/{m}")
    @Transactional
    public ResponseEntity<Void> remover(@RequestHeader(value="Authorization", required=false) String token, @PathVariable UUID p, @PathVariable UUID m) {
        UUID ator = identidade.usuarioAutenticado(token); projetoEscrita(p, ator);
        var alvo = papelObrigatorio(p, m);
        if (!PoliticaMembros.podeRemover(papelObrigatorio(p, ator), alvo, ator.equals(m))) throw AcessoRecursos.insuficiente();
        acesso.banco.update("DELETE FROM membros WHERE projeto_id=? AND usuario_id=?", p, m);
        acesso.banco.update("UPDATE transferencias_propriedade SET cancelada_em=now(),atualizado_em=now() WHERE projeto_id=? AND destinatario_id=? AND aceita_em IS NULL AND cancelada_em IS NULL", p, m);
        historico(p, m, ator, alvo, null);
        if (!ator.equals(m)) caixa.notificar(m,p,"MEMBRO_REMOVIDO",MensagensNotificacao.removido(nomeProjeto(p)),null,null);
        evento("MembroRemovido", p, Map.of("projetoId",p,"usuarioId",m,"autorId",ator));
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/api/v1/projetos/{p}/historico-membros")
    @Transactional(readOnly=true)
    public Pagina<Historico> historico(@RequestHeader(value="Authorization", required=false) String token, @PathVariable UUID p,
            @RequestParam(defaultValue="0") @Min(0) int page, @RequestParam(defaultValue="20") @Min(1) @Max(100) int size) {
        acesso.projeto(p, identidade.usuarioAutenticado(token), false);
        var lista = acesso.banco.query("SELECT autor_id,membro_id,papel_anterior,papel_novo,data FROM historico_membros WHERE projeto_id=? ORDER BY data DESC,id", (rs,n) -> new Historico(uuid(rs,1),uuid(rs,2),contrato(rs.getString(3)),contrato(rs.getString(4)),instante(rs,5)), p);
        return pagina(lista,page,size,"/api/v1/projetos/"+p+"/historico-membros");
    }
    @GetMapping("/api/v1/projetos/{p}/convites")
    @Transactional(readOnly=true)
    public Pagina<Convite> convitesProjeto(@RequestHeader(value="Authorization", required=false) String token, @PathVariable UUID p,
            @RequestParam(defaultValue="0") @Min(0) int page, @RequestParam(defaultValue="20") @Min(1) @Max(100) int size) {
        UUID ator=identidade.usuarioAutenticado(token); acesso.projeto(p,ator,false); exigirMantenedor(p,ator);
        var lista=acesso.banco.query("SELECT c.id,c.projeto_id,c.email,c.papel,c.aceito_em,c.atualizado_em,NULL AS projeto_nome FROM convites c WHERE c.projeto_id=? AND c.cancelado_em IS NULL AND (c.aceito_em IS NOT NULL OR c.expira_em>now()) ORDER BY c.criado_em DESC,c.id", (rs,n)->convite(rs),p);
        return pagina(lista,page,size,"/api/v1/projetos/"+p+"/convites");
    }
    @PostMapping("/api/v1/projetos/{p}/convites")
    @Transactional
    public ResponseEntity<Convite> convidar(@RequestHeader(value="Authorization", required=false) String token, @PathVariable UUID p, @Valid @RequestBody ConviteEntrada entrada) {
        UUID ator=identidade.usuarioAutenticado(token); projetoEscrita(p,ator);
        var papel=papelEntrada(entrada.papel());
        if (!PoliticaMembros.podeConvidar(papelObrigatorio(p,ator),papel)) throw AcessoRecursos.insuficiente();
        String email=entrada.email().trim().toLowerCase(Locale.ROOT);
        if (existe("SELECT count(*) FROM membros m JOIN usuarios u ON u.id=m.usuario_id WHERE m.projeto_id=? AND u.email=?",p,email)) throw erro(HttpStatus.CONFLICT,"JA_MEMBRO");
        if (existe("SELECT count(*) FROM convites WHERE projeto_id=? AND email=? AND aceito_em IS NULL AND cancelado_em IS NULL AND expira_em>now()",p,email)) throw erro(HttpStatus.CONFLICT,"CONVITE_PENDENTE");
        acesso.banco.update("UPDATE convites SET cancelado_em=now(),atualizado_em=now() WHERE projeto_id=? AND email=? AND aceito_em IS NULL AND cancelado_em IS NULL",p,email);
        UUID id=UUID.randomUUID();
        acesso.banco.update("INSERT INTO convites(id,projeto_id,email,papel,criado_por,expira_em) VALUES (?,?,?,?,?,now()+interval '7 days')",id,p,email,papel.name(),ator);
        acesso.banco.query("SELECT id FROM usuarios WHERE email=?",(rs,n)->rs.getObject(1,UUID.class),email).forEach(destinatario -> caixa.notificar(destinatario,p,"CONVITE",MensagensNotificacao.convite(nomeProjeto(p),contrato(papel.name())),"/convites","convite:"+id));
        evento("ConviteCriado",p,Map.of("projetoId",p,"conviteId",id,"email",email,"papel",papel.name(),"autorId",ator));
        return ResponseEntity.created(URI.create("/api/v1/projetos/"+p+"/convites/"+id)).body(convite(id));
    }
    @DeleteMapping("/api/v1/projetos/{p}/convites/{c}")
    @Transactional
    public ResponseEntity<Void> cancelarConvite(@RequestHeader(value="Authorization", required=false) String token, @PathVariable UUID p, @PathVariable UUID c) {
        UUID ator=identidade.usuarioAutenticado(token); projetoEscrita(p,ator); exigirMantenedor(p,ator);
        ConviteEstado convite=estadoConvite(c);
        if (!convite.projeto().equals(p)) throw erro(HttpStatus.NOT_FOUND,"CONVITE_NAO_ENCONTRADO");
        if (!PoliticaMembros.podeConvidar(papelObrigatorio(p,ator),convite.papel())) throw AcessoRecursos.insuficiente();
        if (convite.aceito()!=null) throw erro(HttpStatus.CONFLICT,"CONVITE_ACEITO");
        if (convite.cancelado()==null) {
            acesso.banco.update("UPDATE convites SET cancelado_em=now(),atualizado_em=now() WHERE id=?",c);
            evento("ConviteCancelado",p,Map.of("projetoId",p,"conviteId",c,"autorId",ator));
        }
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/api/v1/convites")
    @Transactional(readOnly=true)
    public Pagina<Convite> meusConvites(@RequestHeader(value="Authorization", required=false) String token,
            @RequestParam(defaultValue="0") @Min(0) int page, @RequestParam(defaultValue="20") @Min(1) @Max(100) int size) {
        UUID usuario=identidade.usuarioAutenticado(token);
        var lista=acesso.banco.query("SELECT c.id,c.projeto_id,c.email,c.papel,c.aceito_em,c.atualizado_em,p.nome FROM convites c JOIN projetos p ON p.id=c.projeto_id JOIN usuarios u ON u.email=c.email WHERE u.id=? AND p.excluido_em IS NULL AND c.aceito_em IS NULL AND c.cancelado_em IS NULL AND c.expira_em>now() ORDER BY c.criado_em DESC,c.id",(rs,n)->convite(rs),usuario);
        return pagina(lista,page,size,"/api/v1/convites");
    }
    @PostMapping("/api/v1/convites/{c}/aceite")
    @Transactional
    public ResponseEntity<Membro> aceitarConvite(@RequestHeader(value="Authorization", required=false) String token, @PathVariable UUID c) {
        UUID usuario=identidade.usuarioAutenticado(token); ConviteEstado inicial=estadoConvite(c);
        if (!email(usuario).equals(inicial.email())) throw erro(HttpStatus.NOT_FOUND,"CONVITE_NAO_ENCONTRADO");
        bloquearProjeto(inicial.projeto()); ConviteEstado convite=estadoConvite(c); UUID p=convite.projeto();
        if (convite.aceito()!=null) throw erro(HttpStatus.CONFLICT,"CONVITE_ACEITO");
        if (convite.cancelado()!=null || !convite.expira().isAfter(Instant.now())) throw erro(HttpStatus.CONFLICT,"CONVITE_EXPIRADO");
        if (papel(p,usuario)!=null) throw erro(HttpStatus.CONFLICT,"JA_MEMBRO");
        if (!PoliticaMembros.podeConvidar(papel(p,convite.autor()),convite.papel())) throw erro(HttpStatus.CONFLICT,"CONVITE_INVALIDO");
        acesso.banco.update("INSERT INTO membros(projeto_id,usuario_id,papel) VALUES (?,?,?)",p,usuario,convite.papel().name());
        historico(p,usuario,usuario,null,convite.papel());
        acesso.banco.update("UPDATE convites SET aceito_em=now(),aceito_por=?,atualizado_em=now() WHERE id=?",usuario,c);
        caixa.notificar(convite.autor(),p,"CONVITE_ACEITO",MensagensNotificacao.conviteAceito(nomeUsuario(usuario),nomeProjeto(p)),null,"aceite-convite:"+c);
        evento("MembroAdicionado",p,Map.of("projetoId",p,"usuarioId",usuario,"papel",convite.papel().name(),"autorId",usuario));
        return ResponseEntity.created(URI.create("/api/v1/projetos/"+p+"/membros/"+usuario)).body(membro(p,usuario));
    }
    @PostMapping("/api/v1/projetos/{p}/transferencias-propriedade")
    @Transactional
    public ResponseEntity<Transferencia> solicitar(@RequestHeader(value="Authorization", required=false) String token, @PathVariable UUID p, @Valid @RequestBody TransferenciaEntrada entrada) {
        UUID ator=identidade.usuarioAutenticado(token); Projeto projeto=projetoEscrita(p,ator);
        if (papelObrigatorio(p,ator)!=PoliticaMembros.Papel.OWNER || !projeto.getOwnerId().equals(ator)) throw AcessoRecursos.insuficiente();
        if (!Boolean.TRUE.equals(entrada.confirmacao())) throw erro(HttpStatus.BAD_REQUEST,"CONFIRMACAO_OBRIGATORIA");
        UUID destinatario=entrada.destinatarioId();
        if (ator.equals(destinatario) || papel(p,destinatario)==null) throw erro(HttpStatus.CONFLICT,"DESTINATARIO_INVALIDO");
        if (existe("SELECT count(*) FROM transferencias_propriedade WHERE projeto_id=? AND aceita_em IS NULL AND cancelada_em IS NULL",p)) throw erro(HttpStatus.CONFLICT,"TRANSFERENCIA_PENDENTE");
        UUID id=UUID.randomUUID();
        acesso.banco.update("INSERT INTO transferencias_propriedade(id,projeto_id,owner_atual_id,destinatario_id) VALUES (?,?,?,?)",id,p,ator,destinatario);
        caixa.notificar(destinatario,p,"TRANSFERENCIA_PROPRIEDADE",MensagensNotificacao.transferencia(nomeUsuario(ator),nomeProjeto(p)),"/transferencias","transferencia:"+id);
        evento("TransferenciaPropriedadeSolicitada",p,Map.of("projetoId",p,"transferenciaId",id,"ownerAtualId",ator,"destinatarioId",destinatario));
        return ResponseEntity.created(URI.create("/api/v1/projetos/"+p+"/transferencias-propriedade/"+id)).body(transferencia(id));
    }
    @GetMapping("/api/v1/transferencias-propriedade")
    @Transactional(readOnly=true)
    public Pagina<Transferencia> minhasTransferencias(@RequestHeader(value="Authorization", required=false) String token,
            @RequestParam(defaultValue="0") @Min(0) int page, @RequestParam(defaultValue="20") @Min(1) @Max(100) int size) {
        UUID usuario=identidade.usuarioAutenticado(token);
        var lista=acesso.banco.query("SELECT t.id,t.projeto_id,t.owner_atual_id,t.destinatario_id,t.aceita_em,t.atualizado_em,p.nome FROM transferencias_propriedade t JOIN projetos p ON p.id=t.projeto_id WHERE t.destinatario_id=? AND p.excluido_em IS NULL AND t.aceita_em IS NULL AND t.cancelada_em IS NULL ORDER BY t.criado_em DESC,t.id",(rs,n)->transferencia(rs),usuario);
        return pagina(lista,page,size,"/api/v1/transferencias-propriedade");
    }
    @PostMapping("/api/v1/transferencias-propriedade/{t}/aceite")
    @Transactional(noRollbackFor=Problema.class)
    public ResponseEntity<Transferencia> aceitarTransferencia(@RequestHeader(value="Authorization", required=false) String token, @PathVariable UUID t) {
        UUID usuario=identidade.usuarioAutenticado(token); TransferenciaEstado inicial=estadoTransferencia(t);
        if (!inicial.destinatario().equals(usuario)) throw erro(HttpStatus.NOT_FOUND,"TRANSFERENCIA_NAO_ENCONTRADA");
        Projeto projeto=bloquearProjeto(inicial.projeto()); TransferenciaEstado estado=estadoTransferencia(t); UUID p=estado.projeto();
        if (estado.aceita()!=null || estado.cancelada()!=null) throw erro(HttpStatus.CONFLICT,"TRANSFERENCIA_ENCERRADA");
        var anterior=papel(p,usuario);
        if (anterior==null || !projeto.getOwnerId().equals(estado.owner()) || papel(p,estado.owner())!=PoliticaMembros.Papel.OWNER) {
            acesso.banco.update("UPDATE transferencias_propriedade SET cancelada_em=now(),atualizado_em=now() WHERE id=?",t);
            throw erro(HttpStatus.CONFLICT,"TRANSFERENCIA_INVALIDA");
        }
        acesso.banco.update("UPDATE membros SET papel='MANTENEDOR',atualizado_em=now() WHERE projeto_id=? AND usuario_id=?",p,estado.owner());
        acesso.banco.update("UPDATE membros SET papel='OWNER',atualizado_em=now() WHERE projeto_id=? AND usuario_id=?",p,usuario);
        historico(p,estado.owner(),usuario,PoliticaMembros.Papel.OWNER,PoliticaMembros.Papel.MANTENEDOR);
        historico(p,usuario,usuario,anterior,PoliticaMembros.Papel.OWNER);
        projeto.transferirPropriedade(usuario); acesso.projetos.flush();
        acesso.banco.update("UPDATE transferencias_propriedade SET aceita_em=now(),atualizado_em=now() WHERE id=?",t);
        caixa.notificar(estado.owner(),p,"PROPRIEDADE_TRANSFERIDA",MensagensNotificacao.propriedadeTransferida(nomeUsuario(usuario),nomeProjeto(p)),null,"transferencia-aceita:"+t);
        evento("PropriedadeTransferida",p,Map.of("projetoId",p,"transferenciaId",t,"ownerAnteriorId",estado.owner(),"ownerNovoId",usuario));
        return ResponseEntity.created(URI.create("/api/v1/projetos/"+p+"/transferencias-propriedade/"+t)).body(transferencia(t));
    }
    private Projeto projetoEscrita(UUID p, UUID usuario) {
        Projeto projeto=acesso.projeto(p,usuario,false);
        acesso.workspaces.buscarParaAlteracao(projeto.getWorkspaceId()).orElseThrow(AcessoRecursos::projetoNaoEncontrado);
        entidades.refresh(projeto,LockModeType.PESSIMISTIC_WRITE);
        projeto=acesso.projeto(p,usuario,false);
        if (projeto.isArquivado()) throw erro(HttpStatus.CONFLICT,"PROJETO_ARQUIVADO");
        return projeto;
    }
    private Projeto bloquearProjeto(UUID p) {
        Projeto projeto=acesso.projetos.findById(p).filter(x->x.getExcluidoEm()==null).orElseThrow(AcessoRecursos::projetoNaoEncontrado);
        acesso.workspaces.buscarParaAlteracao(projeto.getWorkspaceId()).orElseThrow(AcessoRecursos::projetoNaoEncontrado);
        entidades.refresh(projeto,LockModeType.PESSIMISTIC_WRITE);
        if (projeto.getExcluidoEm()!=null) throw AcessoRecursos.projetoNaoEncontrado();
        if (projeto.isArquivado()) throw erro(HttpStatus.CONFLICT,"PROJETO_ARQUIVADO");
        return projeto;
    }
    private PoliticaMembros.Papel papel(UUID p, UUID usuario) {
        return acesso.banco.query("SELECT papel FROM membros WHERE projeto_id=? AND usuario_id=?",(rs,n)->PoliticaMembros.Papel.valueOf(rs.getString(1)),p,usuario).stream().findFirst().orElse(null);
    }
    private PoliticaMembros.Papel papelObrigatorio(UUID p, UUID usuario) {
        var papel=papel(p,usuario); if (papel==null) throw erro(HttpStatus.NOT_FOUND,"MEMBRO_NAO_ENCONTRADO"); return papel;
    }
    private void exigirMantenedor(UUID p, UUID usuario) {
        if (!PoliticaWorkspace.mantenedor(papelObrigatorio(p,usuario).name())) throw AcessoRecursos.insuficiente();
    }
    private PoliticaMembros.Papel papelEntrada(String valor) {
        try { return PoliticaMembros.deContrato(valor); } catch (IllegalArgumentException e) { throw erro(HttpStatus.BAD_REQUEST,"REQUISICAO_INVALIDA"); }
    }
    private void historico(UUID p, UUID membro, UUID autor, PoliticaMembros.Papel anterior, PoliticaMembros.Papel novo) {
        acesso.banco.update("INSERT INTO historico_membros(id,projeto_id,membro_id,autor_id,papel_anterior,papel_novo) VALUES (?,?,?,?,?,?)",UUID.randomUUID(),p,membro,autor,anterior==null?null:anterior.name(),novo==null?null:novo.name());
    }
    private void evento(String tipo, UUID p, Map<String,Object> payload) { emissor.evento(tipo,p,null,null,UUID.randomUUID(),null,payload); }
    private boolean existe(String sql, Object... args) { Long total=acesso.banco.queryForObject(sql,Long.class,args); return total!=null && total>0; }
    private String nomeProjeto(UUID projeto) { return acesso.banco.queryForObject("SELECT nome FROM projetos WHERE id=?",String.class,projeto); }
    private String nomeUsuario(UUID usuario) { return acesso.banco.queryForObject("SELECT nome FROM usuarios WHERE id=?",String.class,usuario); }
    private String email(UUID usuario) { return acesso.banco.queryForObject("SELECT email FROM usuarios WHERE id=?",String.class,usuario); }
    private Membro membro(UUID p, UUID usuario) {
        return acesso.banco.query("SELECT m.usuario_id,u.nome,u.email,m.papel,m.atualizado_em FROM membros m JOIN usuarios u ON u.id=m.usuario_id WHERE m.projeto_id=? AND m.usuario_id=?",(rs,n)->membro(rs),p,usuario).stream().findFirst().orElseThrow(()->erro(HttpStatus.NOT_FOUND,"MEMBRO_NAO_ENCONTRADO"));
    }
    private Membro membro(ResultSet rs) throws SQLException { UUID id=uuid(rs,1); return new Membro(id,id,rs.getString(2),rs.getString(3),contrato(rs.getString(4)),instante(rs,5)); }
    private Convite convite(UUID id) { return acesso.banco.query("SELECT id,projeto_id,email,papel,aceito_em,atualizado_em,NULL FROM convites WHERE id=?",(rs,n)->convite(rs),id).get(0); }
    private Convite convite(ResultSet rs) throws SQLException { return new Convite(uuid(rs,1),uuid(rs,2),rs.getString(3),contrato(rs.getString(4)),instante(rs,5)!=null,instante(rs,6),rs.getString(7)); }
    private ConviteEstado estadoConvite(UUID id) {
        return acesso.banco.query("SELECT id,projeto_id,email,papel,criado_por,expira_em,aceito_em,cancelado_em FROM convites WHERE id=?",(rs,n)->new ConviteEstado(uuid(rs,1),uuid(rs,2),rs.getString(3),PoliticaMembros.Papel.valueOf(rs.getString(4)),uuid(rs,5),instante(rs,6),instante(rs,7),instante(rs,8)),id).stream().findFirst().orElseThrow(()->erro(HttpStatus.NOT_FOUND,"CONVITE_NAO_ENCONTRADO"));
    }
    private Transferencia transferencia(UUID id) { return acesso.banco.query("SELECT id,projeto_id,owner_atual_id,destinatario_id,aceita_em,atualizado_em,NULL FROM transferencias_propriedade WHERE id=?",(rs,n)->transferencia(rs),id).get(0); }
    private Transferencia transferencia(ResultSet rs) throws SQLException { return new Transferencia(uuid(rs,1),uuid(rs,2),uuid(rs,3),uuid(rs,4),instante(rs,5)!=null,instante(rs,6),rs.getString(7)); }
    private TransferenciaEstado estadoTransferencia(UUID id) {
        return acesso.banco.query("SELECT id,projeto_id,owner_atual_id,destinatario_id,aceita_em,cancelada_em FROM transferencias_propriedade WHERE id=?",(rs,n)->new TransferenciaEstado(uuid(rs,1),uuid(rs,2),uuid(rs,3),uuid(rs,4),instante(rs,5),instante(rs,6)),id).stream().findFirst().orElseThrow(()->erro(HttpStatus.NOT_FOUND,"TRANSFERENCIA_NAO_ENCONTRADA"));
    }
    private static UUID uuid(ResultSet rs,int coluna) throws SQLException { return rs.getObject(coluna,UUID.class); }
    private static Instant instante(ResultSet rs,int coluna) throws SQLException { var valor=rs.getTimestamp(coluna); return valor==null?null:valor.toInstant(); }
    private static String contrato(String papel) { return papel==null?null:PoliticaMembros.paraContrato(PoliticaMembros.Papel.valueOf(papel)); }
    private static Problema erro(HttpStatus status,String codigo) { return new Problema(status,codigo,MENSAGENS.getOrDefault(codigo,"Operação não permitida.")); }
    private static final Map<String,String> MENSAGENS = Map.ofEntries(
            Map.entry("JA_MEMBRO","A pessoa já é membro do projeto."),
            Map.entry("CONVITE_PENDENTE","Já existe convite pendente para este e-mail."),
            Map.entry("CONVITE_NAO_ENCONTRADO","Convite não encontrado."),
            Map.entry("CONVITE_ACEITO","O convite já foi aceito."),
            Map.entry("CONVITE_EXPIRADO","O convite expirou ou foi cancelado."),
            Map.entry("CONVITE_INVALIDO","Quem convidou não pode mais conceder este papel."),
            Map.entry("MEMBRO_NAO_ENCONTRADO","Membro não encontrado."),
            Map.entry("PROJETO_ARQUIVADO","Projeto arquivado permite apenas leitura."),
            Map.entry("REQUISICAO_INVALIDA","Papel inválido."),
            Map.entry("CONFIRMACAO_OBRIGATORIA","Confirme a transferência de propriedade."),
            Map.entry("DESTINATARIO_INVALIDO","O destinatário deve ser outro membro ativo do projeto."),
            Map.entry("TRANSFERENCIA_PENDENTE","Já existe transferência de propriedade pendente."),
            Map.entry("TRANSFERENCIA_NAO_ENCONTRADA","Transferência não encontrada."),
            Map.entry("TRANSFERENCIA_ENCERRADA","A transferência já foi encerrada."),
            Map.entry("TRANSFERENCIA_INVALIDA","A transferência deixou de ser válida e foi cancelada."));
    private static <T> Pagina<T> pagina(List<T> lista,int page,int size,String caminho) {
        int inicio=(int)Math.min((long)page*size,lista.size()); int fim=Math.min(inicio+size,lista.size());
        return Pagina.de(new PageImpl<>(lista.subList(inicio,fim),PageRequest.of(page,size),lista.size()),caminho+"?page="+page+"&size="+size);
    }
}
