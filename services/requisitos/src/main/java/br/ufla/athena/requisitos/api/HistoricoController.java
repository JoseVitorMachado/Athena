package br.ufla.athena.requisitos.api;

import br.ufla.athena.requisitos.dominio.RequisitoRepository;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/projetos/{projetoId}/requisitos/{requisitoId}/historico")
public class HistoricoController {
    private final JdbcTemplate banco;
    private final RequisitoRepository requisitos;
    public HistoricoController(JdbcTemplate banco, RequisitoRepository requisitos) {
        this.banco = banco; this.requisitos = requisitos;
    }
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Evento(UUID eventoId, UUID requisitoId, UUID versaoId, String acao, UUID autor, Instant data, String detalhe) { }

    @GetMapping
    @Transactional(readOnly = true)
    public Pagina<Evento> listar(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        requisitos.findByIdAndProjetoId(requisitoId, projetoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requisito não encontrado"));
        var pagina = PageRequest.of(page, size);
        var itens = banco.query("SELECT * FROM historico_requisitos WHERE projeto_id=? AND requisito_id=? ORDER BY data DESC, id DESC LIMIT ? OFFSET ?",
                (rs, n) -> new Evento(rs.getObject("id", UUID.class), rs.getObject("requisito_id", UUID.class),
                        rs.getObject("versao_id", UUID.class), rs.getString("acao"), rs.getObject("autor_id", UUID.class),
                        rs.getTimestamp("data").toInstant(), rs.getString("detalhe")), projetoId, requisitoId, size, pagina.getOffset());
        Long total = banco.queryForObject("SELECT count(*) FROM historico_requisitos WHERE projeto_id=? AND requisito_id=?", Long.class, projetoId, requisitoId);
        return Pagina.de(new PageImpl<>(itens, pagina, total == null ? 0 : total),
                "/api/v1/projetos/" + projetoId + "/requisitos/" + requisitoId + "/historico?page=" + page + "&size=" + size);
    }
}
