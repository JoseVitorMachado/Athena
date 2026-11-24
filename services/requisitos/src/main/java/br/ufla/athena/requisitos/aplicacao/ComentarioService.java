package br.ufla.athena.requisitos.aplicacao;

import br.ufla.athena.requisitos.api.ComentarioResposta;
import br.ufla.athena.requisitos.api.Problema;
import br.ufla.athena.requisitos.dominio.Requisito;
import br.ufla.athena.requisitos.dominio.RequisitoRepository;
import br.ufla.athena.requisitos.dominio.SituacaoRequisito;
import br.ufla.athena.requisitos.dominio.Versao;
import br.ufla.athena.requisitos.dominio.VersaoRepository;
import br.ufla.athena.requisitos.mensageria.EmissorMensagens;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Gerencia comentários, histórico e suas notificações. */
@Service
public class ComentarioService {
    private final JdbcTemplate banco;
    private final RequisitoRepository requisitos;
    private final VersaoRepository versoes;
    private final UsuarioAtual usuario;
    private final PapelAtual papel;
    private final EmissorMensagens emissor;
    private final ObjectMapper json;

    public ComentarioService(JdbcTemplate banco, RequisitoRepository requisitos, VersaoRepository versoes,
            UsuarioAtual usuario, PapelAtual papel, EmissorMensagens emissor, ObjectMapper json) {
        this.banco = banco; this.requisitos = requisitos; this.versoes = versoes;
        this.usuario = usuario; this.papel = papel; this.emissor = emissor; this.json = json;
    }

    /** Lista comentários do requisito. */
    @Transactional(readOnly = true)
    public Page<ComentarioResposta> listar(UUID projeto, UUID requisito, Boolean resolvido, Pageable pagina) {
        obterRequisito(projeto, requisito, false);
        String filtro = resolvido == null ? "" : " AND resolvido=" + (resolvido ? "true" : "false");
        Long total = banco.queryForObject("SELECT count(*) FROM comentarios WHERE projeto_id=? AND requisito_id=?" + filtro, Long.class, projeto, requisito);
        List<ComentarioResposta> itens = banco.query("SELECT * FROM comentarios WHERE projeto_id=? AND requisito_id=?" + filtro
                + " ORDER BY criado_em, id LIMIT ? OFFSET ?", (rs, n) -> ler(rs), projeto, requisito, pagina.getPageSize(), pagina.getOffset());
        return new PageImpl<>(itens, pagina, total == null ? 0 : total);
    }

    /** Cria comentário e evento na mesma transação. */
    @Transactional
    public ComentarioResposta criar(UUID projeto, UUID requisitoId, JsonNode entrada) {
        Requisito requisito = obterRequisito(projeto, requisitoId, true);
        if (!RegrasComentario.podeCriar(papel.obter())) throw problema(HttpStatus.FORBIDDEN, "PAPEL_INSUFICIENTE", "Papel insuficiente para comentar.");
        ativo(requisito);
        campos(entrada, Set.of("versaoId", "texto", "ancora", "mencoes"));
        UUID versaoId;
        try {
            versaoId = id(entrada.get("versaoId"));
        } catch (Problema erro) {
            throw problema(HttpStatus.BAD_REQUEST, "VERSAO_INVALIDA", "Versão inválida para o requisito.");
        }
        Versao versao = versoes.findByIdAndRequisito_IdAndRequisito_ProjetoId(versaoId, requisitoId, projeto)
                .orElseThrow(() -> problema(HttpStatus.BAD_REQUEST, "VERSAO_INVALIDA", "Versão inválida para o requisito."));
        String texto = texto(entrada.get("texto"), 5000);
        String selecao = null;
        String situacao = null;
        if (entrada.has("ancora")) {
            JsonNode ancora = entrada.get("ancora");
            campos(ancora, Set.of("selecao"));
            JsonNode valorSelecao = ancora.get("selecao");
            if (valorSelecao == null || !valorSelecao.isTextual() || valorSelecao.textValue().isBlank()
                    || valorSelecao.textValue().length() > 1000) throw invalida();
            selecao = valorSelecao.textValue();
            situacao = localizada(versao, selecao) ? "LOCALIZADA" : "NAO_LOCALIZADA";
        }
        UUID autor = usuario.obter();
        List<UUID> mencoes = new ArrayList<>();
        if (entrada.has("mencoes")) {
            JsonNode lista = entrada.get("mencoes");
            if (!lista.isArray() || lista.size() > 20) throw invalida();
            Set<UUID> unicos = new HashSet<>();
            for (JsonNode item : lista) {
                UUID mencao = id(item);
                if (!unicos.add(mencao)) throw invalida();
                if (!mencao.equals(autor)) mencoes.add(mencao);
            }
        }
        UUID comentario = UUID.randomUUID();
        Historico.registrar(banco, projeto, requisitoId, versaoId, "COMENTARIO_CRIADO", autor, null);
        banco.update("INSERT INTO comentarios (id,projeto_id,requisito_id,versao_id,autor_id,texto,ancora_selecao,ancora_situacao,mencoes) VALUES (?,?,?,?,?,?,?,?,?::jsonb)",
                comentario, projeto, requisitoId, versaoId, autor, texto, selecao, situacao, escrever(mencoes));
        emissor.evento("ComentarioCriado", projeto, null, null, comentario, null,
                Map.of("projetoId", projeto, "requisitoId", requisitoId, "codigo", requisito.getCodigo(),
                        "comentarioId", comentario, "versaoId", versaoId, "autorId", autor,
                        "mencoes", mencoes, "trecho", texto.substring(0, Math.min(120, texto.length()))));
        return buscar(projeto, requisitoId, comentario, false);
    }

    /** Altera o comentário e registra os valores anteriores. */
    @Transactional
    public ComentarioResposta alterar(UUID projeto, UUID requisitoId, UUID comentarioId, JsonNode entrada) {
        Requisito requisito = obterRequisito(projeto, requisitoId, true);
        ComentarioResposta anterior = buscar(projeto, requisitoId, comentarioId, true);
        ativo(requisito);
        campos(entrada, Set.of("texto", "resolvido"));
        if (entrada.isEmpty()) throw invalida();
        UUID autorMudanca = usuario.obter();
        String texto = anterior.texto();
        boolean resolvido = anterior.resolvido();
        if (entrada.has("texto")) {
            if (!RegrasComentario.podeEditarTexto(anterior.autor(), autorMudanca))
                throw problema(HttpStatus.FORBIDDEN, "COMENTARIO_DE_OUTRO_AUTOR", "Somente o autor pode editar o texto.");
            texto = texto(entrada.get("texto"), 5000);
        }
        if (entrada.has("resolvido")) {
            if (!RegrasComentario.podeResolver(papel.obter(), anterior.autor(), autorMudanca))
                throw problema(HttpStatus.FORBIDDEN, "PAPEL_INSUFICIENTE", "Papel insuficiente para alterar a resolução.");
            if (!entrada.get("resolvido").isBoolean()) throw invalida();
            resolvido = entrada.get("resolvido").booleanValue();
        }
        banco.update("INSERT INTO comentarios_historico (id,comentario_id,autor_id,texto_anterior,resolvido_anterior) VALUES (?,?,?,?,?)",
                UUID.randomUUID(), comentarioId, autorMudanca, anterior.texto(), anterior.resolvido());
        if (entrada.has("resolvido") && resolvido != anterior.resolvido()) {
            banco.update("UPDATE comentarios SET texto=?, resolvido=?, resolvido_por=?, resolvido_em=CASE WHEN ? THEN now() ELSE NULL END, revisao=revisao+1, atualizado_em=now() WHERE id=?",
                    texto, resolvido, resolvido ? autorMudanca : null, resolvido, comentarioId);
        } else {
            banco.update("UPDATE comentarios SET texto=?, revisao=revisao+1, atualizado_em=now() WHERE id=?", texto, comentarioId);
        }
        return buscar(projeto, requisitoId, comentarioId, false);
    }

    /** Verifica se a seleção ainda aparece na versão. */
    public static boolean localizada(Versao versao, String selecao) {
        return versao.getTitulo().contains(selecao) || versao.getConteudo().contains(selecao)
                || versao.getCriteriosAceite().stream().anyMatch(criterio -> criterio.contains(selecao));
    }

    private Requisito obterRequisito(UUID projeto, UUID requisito, boolean travar) {
        return (travar ? requisitos.buscarParaEdicao(requisito, projeto) : requisitos.findByIdAndProjetoId(requisito, projeto))
                .orElseThrow(() -> problema(HttpStatus.NOT_FOUND, "REQUISITO_NAO_ENCONTRADO", "Requisito não encontrado."));
    }
    private static void ativo(Requisito requisito) {
        if (requisito.getSituacao() == SituacaoRequisito.CANCELADO) throw problema(HttpStatus.CONFLICT, "REQUISITO_CANCELADO", "Requisito cancelado.");
    }
    private ComentarioResposta buscar(UUID projeto, UUID requisito, UUID comentario, boolean travar) {
        List<ComentarioResposta> itens = banco.query("SELECT * FROM comentarios WHERE projeto_id=? AND requisito_id=? AND id=?" + (travar ? " FOR UPDATE" : ""),
                (rs, n) -> ler(rs), projeto, requisito, comentario);
        if (itens.isEmpty()) throw problema(HttpStatus.NOT_FOUND, "COMENTARIO_NAO_ENCONTRADO", "Comentário não encontrado.");
        return itens.get(0);
    }
    private ComentarioResposta ler(ResultSet rs) throws SQLException {
        String selecao = rs.getString("ancora_selecao");
        try {
            List<UUID> mencoes = json.readValue(rs.getString("mencoes"), new TypeReference<List<UUID>>() { });
            return new ComentarioResposta(rs.getObject("id", UUID.class), rs.getObject("requisito_id", UUID.class),
                    rs.getObject("versao_id", UUID.class), rs.getObject("autor_id", UUID.class), rs.getString("texto"),
                    selecao == null ? null : new ComentarioResposta.Ancora(selecao, rs.getString("ancora_situacao")),
                    mencoes, rs.getBoolean("resolvido"), rs.getTimestamp("atualizado_em").toInstant());
        } catch (JsonProcessingException e) { throw new IllegalStateException(e); }
    }
    private String escrever(Object valor) {
        try { return json.writeValueAsString(valor); } catch (JsonProcessingException e) { throw new IllegalStateException(e); }
    }
    private static void campos(JsonNode objeto, Set<String> permitidos) {
        if (objeto == null || !objeto.isObject()) throw invalida();
        objeto.fieldNames().forEachRemaining(campo -> { if (!permitidos.contains(campo)) throw invalida(); });
    }
    private static String texto(JsonNode valor, int maximo) {
        if (valor == null || !valor.isTextual()) throw invalida();
        String texto = valor.textValue().trim();
        if (texto.isBlank() || texto.length() > maximo) throw invalida();
        return texto;
    }
    private static UUID id(JsonNode valor) {
        try {
            if (valor == null || !valor.isTextual()) throw invalida();
            UUID id = UUID.fromString(valor.textValue());
            if (!id.toString().equalsIgnoreCase(valor.textValue())) throw invalida();
            return id;
        } catch (IllegalArgumentException e) { throw invalida(); }
    }
    private static Problema invalida() { return problema(HttpStatus.BAD_REQUEST, "REQUISICAO_INVALIDA", "Dados do comentário inválidos."); }
    private static Problema problema(HttpStatus status, String codigo, String detalhe) { return new Problema(status, codigo, detalhe); }
}
