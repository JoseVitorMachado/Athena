package br.ufla.athena.rastreabilidade.dominio;

import java.util.Optional;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RelacaoRepository extends JpaRepository<Relacao, UUID>, JpaSpecificationExecutor<Relacao> {

    List<Relacao> findAllByProjetoId(UUID projetoId);

    Optional<Relacao> findByIdAndProjetoId(UUID id, UUID projetoId);

    boolean existsByProjetoIdAndOrigemIdAndDestinoIdAndTipo(UUID projetoId, UUID origemId, UUID destinoId,
                                                           TipoRelacao tipo);
}
