package br.ufla.athena.integracoes.dominio;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConexaoRepository extends JpaRepository<Conexao, UUID> {

    Page<Conexao> findByProjetoId(UUID projetoId, Pageable pageable);

    Optional<Conexao> findByIdAndProjetoId(UUID id, UUID projetoId);
}
