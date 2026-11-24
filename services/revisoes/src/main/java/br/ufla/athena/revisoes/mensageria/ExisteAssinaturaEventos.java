package br.ufla.athena.revisoes.mensageria;

import java.util.List;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class ExisteAssinaturaEventos implements Condition {
    @Override
    public boolean matches(ConditionContext contexto, AnnotatedTypeMetadata metadados) {
        List<String> assinaturas = Binder.get(contexto.getEnvironment())
                .bind("athena.mensageria.eventos-assinados", Bindable.listOf(String.class))
                .orElse(List.of());
        return assinaturas.stream().anyMatch(assinatura -> !assinatura.isBlank());
    }
}
