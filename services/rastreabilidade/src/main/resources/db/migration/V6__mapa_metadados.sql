-- Metadados do requisito nos nós do mapa (US14): tipo, épico e responsável.
ALTER TABLE mapa_nos ADD COLUMN tipo varchar(40);
ALTER TABLE mapa_nos ADD COLUMN epico_id uuid;
ALTER TABLE mapa_nos ADD COLUMN responsavel_id uuid;

-- Os nós existentes não têm esses metadados: rebaixa a revisão para que a
-- reconstrução os regrave a partir de Requisitos, e marca a projeção como
-- indisponível para que o InicializadorMapa a reconstrua na subida.
UPDATE mapa_nos SET revisao_origem = -1;
UPDATE mapa_estado SET disponivel = false;
