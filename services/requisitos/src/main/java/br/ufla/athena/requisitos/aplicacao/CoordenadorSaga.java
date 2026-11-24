package br.ufla.athena.requisitos.aplicacao;

import br.ufla.athena.requisitos.api.Problema;
import br.ufla.athena.requisitos.api.SubmissaoResposta;
import br.ufla.athena.requisitos.dominio.EstadoVersao;
import br.ufla.athena.requisitos.dominio.Requisito;
import br.ufla.athena.requisitos.dominio.RequisitoRepository;
import br.ufla.athena.requisitos.dominio.SituacaoRequisito;
import br.ufla.athena.requisitos.dominio.Versao;
import br.ufla.athena.requisitos.dominio.VersaoRepository;
import br.ufla.athena.requisitos.mensageria.EmissorMensagens;
import io.micrometer.core.instrument.MeterRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CoordenadorSaga {
    private static final Logger log = LoggerFactory.getLogger(CoordenadorSaga.class);
    private static final String OPERACAO = "criarSubmissao";
    private final MeterRegistry metricas;
    private final JdbcTemplate banco;
    private final RequisitoRepository requisitos;
    private final VersaoRepository versoes;
    private final EmissorMensagens emissor;
    private final ObjectMapper json;
    private final UsuarioAtual usuario;
    private final Duration prazoTotal;
    private final Duration espera;
    private final List<Duration> intervalos;
    private final Duration repeticao;
    private final int maximo;
    private final MaquinaSaga maquina = new MaquinaSaga();

    public CoordenadorSaga(MeterRegistry metricas, JdbcTemplate banco, RequisitoRepository requisitos, VersaoRepository versoes,
                           EmissorMensagens emissor, ObjectMapper json, UsuarioAtual usuario,
                           @Value("${athena.saga.prazo-total:90s}") Duration prazoTotal,
                           @Value("${athena.saga.espera-por-tentativa:10s}") Duration espera,
                           @Value("${athena.saga.intervalos:2s,4s}") List<Duration> intervalos,
                           @Value("${athena.saga.repeticao-compensacao-pendente:60s}") Duration repeticao,
                           @Value("${athena.saga.tentativas:3}") int maximo) {
        this.metricas = metricas;
        this.banco = banco;
        this.requisitos = requisitos;
        this.versoes = versoes;
        this.emissor = emissor;
        this.json = json;
        this.usuario = usuario;
        this.prazoTotal = prazoTotal;
        this.espera = espera;
        this.intervalos = intervalos;
        this.repeticao = repeticao;
        this.maximo = maximo;
    }

    @Transactional
    public SubmissaoResposta criar(UUID projeto, UUID requisitoId, UUID versaoId, String chave, String ifMatch) {
        if (chave == null || chave.isBlank()) throw problema(HttpStatus.PRECONDITION_REQUIRED, "IDEMPOTENCY_KEY_AUSENTE", "Informe Idempotency-Key.");
        if (chave.length() > 200) throw problema(HttpStatus.BAD_REQUEST, "REQUISICAO_INVALIDA", "Chave muito longa.");
        Requisito requisito = requisitos.buscarParaEdicao(requisitoId, projeto)
                .orElseThrow(() -> problema(HttpStatus.NOT_FOUND, "REQUISITO_NAO_ENCONTRADO", "Requisito não encontrado."));
        if (requisito.getSituacao() != SituacaoRequisito.ATIVO) throw problema(HttpStatus.CONFLICT, "REQUISITO_CANCELADO", "Requisito cancelado.");
        String impressao = IdempotenciaSubmissao.impressao(requisitoId, versaoId);
        banco.queryForObject("SELECT pg_advisory_xact_lock(hashtextextended(?, 0))", (rs, n) -> 0,
                projeto + ":" + OPERACAO + ":" + chave);
        banco.update("DELETE FROM chaves_idempotencia WHERE projeto_id=? AND operacao=? AND chave=? AND criado_em < now() - interval '24 hours'", projeto, OPERACAO, chave);
        List<Map<String, Object>> anteriores = banco.queryForList("SELECT impressao, recurso_id FROM chaves_idempotencia WHERE projeto_id=? AND operacao=? AND chave=?", projeto, OPERACAO, chave);
        if (!anteriores.isEmpty()) {
            Map<String, Object> anterior = anteriores.get(0);
            if (!IdempotenciaSubmissao.mesmoPedido((String) anterior.get("impressao"), impressao)) {
                throw problema(HttpStatus.CONFLICT, "IDEMPOTENCY_KEY_REUTILIZADA", "A chave já foi usada em outro pedido.");
            }
            return buscar(projeto, requisitoId, (UUID) anterior.get("recurso_id"));
        }
        Versao candidata = requisito.candidata().orElseThrow(() -> problema(HttpStatus.CONFLICT, "VERSAO_NAO_E_CANDIDATA", "Não há candidata."));
        if (ifMatch == null || ifMatch.isBlank()) throw problema(HttpStatus.PRECONDITION_REQUIRED, "PRECONDICAO_AUSENTE", "Informe If-Match.");
        if (!ifMatch.trim().equals("\"" + candidata.getRevisao() + "\"")) throw problema(HttpStatus.PRECONDITION_FAILED, "REVISAO_DESATUALIZADA", "A candidata foi alterada.");
        if (!candidata.getId().equals(versaoId)) throw problema(HttpStatus.CONFLICT, "VERSAO_NAO_E_CANDIDATA", "A versão não é a candidata.");
        Integer ativas = banco.queryForObject("SELECT count(*) FROM submissoes WHERE requisito_id=? AND estado_saga IN ('EM_ANDAMENTO','COMPENSANDO','COMPENSACAO_PENDENTE')", Integer.class, requisitoId);
        if (ativas != null && ativas > 0) throw problema(HttpStatus.CONFLICT, "SUBMISSAO_ATIVA", "Já existe submissão ativa.");
        if (candidata.getEstadoVersao() != EstadoVersao.RASCUNHO) throw problema(HttpStatus.CONFLICT, "CANDIDATA_FORA_DE_RASCUNHO", "A candidata não está em rascunho.");
        Instant agora = Instant.now();
        UUID id = UUID.randomUUID();
        UUID saga = UUID.randomUUID();
        UUID comando = UUID.randomUUID();
        Map<String, Object> snapshot = Map.of("titulo", candidata.getTitulo(), "conteudo", candidata.getConteudo(), "criteriosAceite", candidata.getCriteriosAceite());
        int inseridas = banco.update("INSERT INTO submissoes (id,saga_id,projeto_id,requisito_id,versao_id,hash_conteudo,snapshot,estado_saga,etapa_saga,comando_esperado_id,comando_retrato_id,tentativas,proxima_acao_em,prazo,criado_em,atualizado_em,autor_id) VALUES (?,?,?,?,?,?,?::jsonb,'EM_ANDAMENTO','AGUARDANDO_RETRATO',?,?,1,?,?,?,?,?) ON CONFLICT (requisito_id) WHERE estado_saga IN ('EM_ANDAMENTO','COMPENSANDO','COMPENSACAO_PENDENTE') DO NOTHING",
                id, saga, projeto, requisitoId, versaoId, candidata.getHashConteudo(), escrever(snapshot), comando, comando,
                Timestamp.from(proxima(agora, 1, agora.plus(prazoTotal))), Timestamp.from(agora.plus(prazoTotal)), Timestamp.from(agora), Timestamp.from(agora), usuario.obter());
        if (inseridas == 0) throw problema(HttpStatus.CONFLICT, "SUBMISSAO_ATIVA", "Já existe submissão ativa.");
        Historico.registrar(banco, projeto, requisitoId, versaoId, "SUBMETIDA", usuario.obter(), null);
        banco.update("INSERT INTO chaves_idempotencia VALUES (?,?,?,?,?,?)", chave, OPERACAO, projeto, impressao, id, Timestamp.from(agora));
        candidata.alterarEstado(EstadoVersao.EM_SUBMISSAO);
        Registro registro = carregar(id, false);
        enviar(registro, null);
        log.info("SAGA {} {}/{} -> {}/{} ({})", saga, null, null, registro.estado, registro.etapa, "submissão criada");
        return resposta(registro);
    }

    @Transactional(readOnly = true)
    public SubmissaoResposta buscar(UUID projeto, UUID requisito, UUID id) {
        Registro registro = carregar(id, false);
        if (registro == null || !registro.projeto.equals(projeto) || !registro.requisito.equals(requisito)) {
            throw problema(HttpStatus.NOT_FOUND, "SUBMISSAO_NAO_ENCONTRADA", "Submissão não encontrada.");
        }
        return resposta(registro);
    }

    @Transactional(readOnly = true)
    public Page<SubmissaoResposta> listar(UUID projeto, UUID requisito, Pageable pagina) {
        if (requisitos.findByIdAndProjetoId(requisito, projeto).isEmpty()) throw problema(HttpStatus.NOT_FOUND, "REQUISITO_NAO_ENCONTRADO", "Requisito não encontrado.");
        Long total = banco.queryForObject("SELECT count(*) FROM submissoes WHERE projeto_id=? AND requisito_id=?", Long.class, projeto, requisito);
        List<SubmissaoResposta> itens = banco.query("SELECT * FROM submissoes WHERE projeto_id=? AND requisito_id=? ORDER BY criado_em DESC, id LIMIT ? OFFSET ?", (rs, n) -> resposta(ler(rs)), projeto, requisito, pagina.getPageSize(), pagina.getOffset());
        return new PageImpl<>(itens, pagina, total == null ? 0 : total);
    }

    @Transactional
    public void receber(Map<String, Object> envelope) {
        UUID saga = uuid(envelope.get("sagaId"));
        if (saga == null) return;
        List<Registro> encontrados = banco.query("SELECT * FROM submissoes WHERE saga_id=? FOR UPDATE", (rs, n) -> ler(rs), saga);
        if (encontrados.isEmpty()) return;
        Registro r = encontrados.get(0);
        if (!r.projeto.equals(uuid(envelope.get("projetoId"))) || !r.id.equals(uuid(envelope.get("correlationId")))) return;
        @SuppressWarnings("unchecked") Map<String, Object> payload = (Map<String, Object>) envelope.get("payload");
        if (payload == null || !r.id.equals(uuid(payload.get("submissaoId")))) return;
        if ("EM_ANDAMENTO".equals(r.estado) && !Instant.now().isBefore(r.prazo)) {
            r.motivo = "Prazo total esgotado";
            mudarEtapa(r, "COMPENSANDO", "CANCELANDO_LOTE", uuid(envelope.get("messageId")), r.motivo);
            salvar(r);
            return;
        }
        String tipo = String.valueOf(envelope.get("messageType"));
        UUID comando = uuid(envelope.get("commandId"));
        if (comando == null) return;
        MaquinaSaga.Acao acao = maquina.resposta(estado(r), tipo, comando, Boolean.TRUE.equals(payload.get("retryable")));
        if (acao == MaquinaSaga.Acao.IGNORAR) {
            log.info("resposta fora de etapa: saga {} tipo {}", saga, tipo);
            return;
        }
        if (acao == MaquinaSaga.Acao.AGUARDAR) return;
        UUID causa = uuid(envelope.get("messageId"));
        switch (acao) {
            case PROXIMO_LOTE -> {
                r.retrato = uuid(payload.get("retratoId"));
                r.relacionados = payload.get("relacionados");
                if (r.retrato == null || !(r.relacionados instanceof List<?>)) throw new IllegalArgumentException("Retrato inválido");
                mudarEtapa(r, "EM_ANDAMENTO", "AGUARDANDO_LOTE", causa, "retrato registrado");
            }
            case CONCLUIR -> {
                r.lote = uuid(payload.get("loteId"));
                if (r.lote == null) throw new IllegalArgumentException("Lote inválido");
                transicao(r, "CONCLUIDA", null, "lote aberto");
                Historico.registrar(banco, r.projeto, r.requisito, r.versao, "SAGA_CONCLUIDA", uuid(payload.get("autorId")), null);
                r.comando = null;
                r.proxima = null;
                mudarVersao(r, EstadoVersao.AGUARDANDO_REVISAO);
                Requisito requisito = requisitos.findByIdAndProjetoId(r.requisito, r.projeto)
                        .orElseThrow(() -> new IllegalStateException("Requisito da submissão não encontrado"));
                Map<String, Object> evento = new LinkedHashMap<>();
                evento.put("projetoId", r.projeto.toString());
                evento.put("requisitoId", r.requisito.toString());
                evento.put("codigo", requisito.getCodigo());
                evento.put("titulo", requisito.getTitulo());
                evento.put("submissaoId", r.id.toString());
                evento.put("loteId", r.lote.toString());
                if (r.autor != null) evento.put("submissorId", r.autor.toString());
                emissor.evento("SubmissaoConcluida", r.projeto, r.saga, null, r.id, causa, evento);
                emissor.comando("revisoes", "AtivarLoteRevisao", r.projeto, r.saga, UUID.randomUUID(), r.id, causa,
                        Map.of("submissaoId", r.id.toString(), "loteId", r.lote.toString()));
            }
            case INICIAR_COMPENSACAO -> {
                r.motivo = String.valueOf(payload.get("code")) + ": " + String.valueOf(payload.get("detail"));
                mudarEtapa(r, "COMPENSANDO", "CANCELANDO_LOTE", causa, "recusa definitiva");
            }
            case CANCELAR_RETRATO -> mudarEtapa(r, "COMPENSANDO", "CANCELANDO_RETRATO", causa, "lote cancelado");
            case COMPENSAR -> {
                transicao(r, "COMPENSADA", null, "retrato cancelado");
                Historico.registrar(banco, r.projeto, r.requisito, r.versao, "SAGA_COMPENSADA", uuid(payload.get("autorId")), null);
                r.comando = null;
                r.proxima = null;
                mudarVersao(r, EstadoVersao.RASCUNHO);
            }
            default -> { }
        }
        salvar(r);
    }

    @Transactional
    public void processarAtrasadas() {
        List<Registro> atrasadas = banco.query("SELECT * FROM submissoes WHERE estado_saga IN ('EM_ANDAMENTO','COMPENSANDO','COMPENSACAO_PENDENTE') AND proxima_acao_em <= now() ORDER BY proxima_acao_em FOR UPDATE SKIP LOCKED LIMIT 50", (rs, n) -> ler(rs));
        for (Registro r : atrasadas) {
            Instant agora = Instant.now();
            MaquinaSaga.Acao acao = maquina.tempo(estado(r), agora, maximo);
            if (acao == MaquinaSaga.Acao.INICIAR_COMPENSACAO) {
                r.motivo = "Prazo ou tentativas esgotados";
                mudarEtapa(r, "COMPENSANDO", "CANCELANDO_LOTE", null, r.motivo);
            } else if (acao == MaquinaSaga.Acao.PENDENTE) {
                transicao(r, "COMPENSACAO_PENDENTE", r.etapa, "cancelamento sem confirmação");
                r.proxima = agora.plus(repeticao);
            } else if (acao == MaquinaSaga.Acao.REENVIAR) {
                if (!"COMPENSACAO_PENDENTE".equals(r.estado)) r.tentativas++;
                enviar(r, null);
                r.proxima = "COMPENSACAO_PENDENTE".equals(r.estado) ? agora.plus(repeticao) : proxima(agora, r.tentativas, "EM_ANDAMENTO".equals(r.estado) ? r.prazo : null);
            }
            salvar(r);
        }
    }

    private void mudarEtapa(Registro r, String estado, String etapa, UUID causa, String motivo) {
        transicao(r, estado, etapa, motivo);
        r.comando = UUID.randomUUID();
        switch (etapa) {
            case "AGUARDANDO_LOTE" -> r.comandoLote = r.comando;
            case "CANCELANDO_LOTE" -> r.comandoCancelarLote = r.comando;
            case "CANCELANDO_RETRATO" -> r.comandoCancelarRetrato = r.comando;
            default -> throw new IllegalStateException(etapa);
        }
        r.tentativas = 1;
        r.proxima = proxima(Instant.now(), 1, "EM_ANDAMENTO".equals(estado) ? r.prazo : null);
        enviar(r, causa);
    }

    private void transicao(Registro r, String estado, String etapa, String motivo) {
        log.info("SAGA {} {}/{} -> {}/{} ({})", r.saga, r.estado, r.etapa, estado, etapa, motivo);
        metricas.counter("athena.saga.transicoes", "de", r.estado, "para", estado).increment();
        r.estado = estado;
        r.etapa = etapa;
    }

    private void enviar(Registro r, UUID causa) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("submissaoId", r.id.toString());
        String destino;
        String tipo;
        switch (r.etapa) {
            case "AGUARDANDO_RETRATO", "AGUARDANDO_LOTE" -> {
                payload.put("requisitoId", r.requisito.toString());
                payload.put("versaoId", r.versao.toString());
                payload.put("hashConteudo", r.hash);
                payload.put("deadline", r.prazo.toString());
                if ("AGUARDANDO_RETRATO".equals(r.etapa)) {
                    destino = "rastreabilidade";
                    tipo = "RegistrarRetratoRastreabilidade";
                } else {
                    destino = "revisoes";
                    tipo = "AbrirLoteRevisao";
                    payload.put("retratoId", r.retrato.toString());
                    payload.put("relacionados", r.relacionados);
                }
            }
            case "CANCELANDO_LOTE" -> {
                destino = "revisoes";
                tipo = "CancelarLoteRevisao";
            }
            case "CANCELANDO_RETRATO" -> {
                destino = "rastreabilidade";
                tipo = "CancelarRetratoRastreabilidade";
            }
            default -> throw new IllegalStateException("Etapa inválida: " + r.etapa);
        }
        emissor.comando(destino, tipo, r.projeto, r.saga, r.comando, r.id, causa, payload);
    }

    private void mudarVersao(Registro r, EstadoVersao estado) {
        Versao versao = versoes.findByIdAndRequisito_IdAndRequisito_ProjetoId(r.versao, r.requisito, r.projeto)
                .orElseThrow(() -> new IllegalStateException("Versão da submissão não encontrada"));
        versao.alterarEstado(estado);
    }

    private Instant proxima(Instant agora, int tentativa, Instant prazo) {
        Instant proxima = agora.plus(MaquinaSaga.espera(espera, intervalos, tentativa));
        return prazo != null && prazo.isBefore(proxima) ? prazo : proxima;
    }

    private void salvar(Registro r) {
        r.atualizado = Instant.now();
        banco.update("UPDATE submissoes SET estado_saga=?, etapa_saga=?, comando_esperado_id=?, comando_lote_id=?, comando_cancelar_lote_id=?, comando_cancelar_retrato_id=?, tentativas=?, proxima_acao_em=?, retrato_id=?, lote_id=?, relacionados=?::jsonb, motivo_falha=?, revisao=revisao+1, atualizado_em=? WHERE id=?",
                r.estado, r.etapa, r.comando, r.comandoLote, r.comandoCancelarLote, r.comandoCancelarRetrato,
                r.tentativas, r.proxima == null ? null : Timestamp.from(r.proxima), r.retrato, r.lote,
                r.relacionados == null ? null : escrever(r.relacionados), r.motivo == null ? null : r.motivo.substring(0, Math.min(1000, r.motivo.length())), Timestamp.from(r.atualizado), r.id);
    }

    private Registro carregar(UUID id, boolean travar) {
        List<Registro> encontrados = banco.query("SELECT * FROM submissoes WHERE id=?" + (travar ? " FOR UPDATE" : ""), (rs, n) -> ler(rs), id);
        return encontrados.isEmpty() ? null : encontrados.get(0);
    }

    private Registro ler(ResultSet rs) throws SQLException {
        Registro r = new Registro();
        r.id = rs.getObject("id", UUID.class);
        r.saga = rs.getObject("saga_id", UUID.class);
        r.projeto = rs.getObject("projeto_id", UUID.class);
        r.requisito = rs.getObject("requisito_id", UUID.class);
        r.versao = rs.getObject("versao_id", UUID.class);
        r.autor = rs.getObject("autor_id", UUID.class);
        r.hash = rs.getString("hash_conteudo");
        r.estado = rs.getString("estado_saga");
        r.etapa = rs.getString("etapa_saga");
        r.comando = rs.getObject("comando_esperado_id", UUID.class);
        r.comandoLote = rs.getObject("comando_lote_id", UUID.class);
        r.comandoCancelarLote = rs.getObject("comando_cancelar_lote_id", UUID.class);
        r.comandoCancelarRetrato = rs.getObject("comando_cancelar_retrato_id", UUID.class);
        r.tentativas = rs.getInt("tentativas");
        Timestamp proxima = rs.getTimestamp("proxima_acao_em");
        r.proxima = proxima == null ? null : proxima.toInstant();
        r.prazo = rs.getTimestamp("prazo").toInstant();
        r.retrato = rs.getObject("retrato_id", UUID.class);
        r.lote = rs.getObject("lote_id", UUID.class);
        String relacionados = rs.getString("relacionados");
        if (relacionados != null) {
            try {
                r.relacionados = json.readValue(relacionados, new TypeReference<List<Map<String, Object>>>() { });
            } catch (JsonProcessingException e) {
                throw new IllegalStateException(e);
            }
        }
        r.motivo = rs.getString("motivo_falha");
        r.atualizado = rs.getTimestamp("atualizado_em").toInstant();
        return r;
    }

    private String escrever(Object objeto) {
        try {
            return json.writeValueAsString(objeto);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static UUID uuid(Object valor) {
        return valor == null ? null : UUID.fromString(valor.toString());
    }

    private static MaquinaSaga.Estado estado(Registro r) {
        return new MaquinaSaga.Estado(r.estado, r.etapa, r.comando, r.tentativas, r.prazo, r.proxima);
    }

    private static SubmissaoResposta resposta(Registro r) {
        return new SubmissaoResposta(r.id, r.saga, r.projeto, r.requisito, r.versao, r.estado, r.etapa,
                r.tentativas, r.prazo, r.hash, r.retrato, r.lote, r.atualizado);
    }

    private static Problema problema(HttpStatus status, String codigo, String detalhe) {
        return new Problema(status, codigo, detalhe);
    }

    private static class Registro {
        UUID id, saga, projeto, requisito, versao, comando, comandoLote, comandoCancelarLote,
                comandoCancelarRetrato, retrato, lote, autor;
        String hash, estado, etapa, motivo;
        int tentativas;
        Instant proxima, prazo, atualizado;
        Object relacionados;
    }
}
