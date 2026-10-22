# ATHENA — Livro de Arquitetura

Este livro descreve a arquitetura projetada do ATHENA, suas responsabilidades, regras de consistência, integrações e limites operacionais. As definições representam decisões de projeto e não comprovam implementação ou desempenho. Metas quantitativas, compatibilidade de clientes, recuperação e aceitação acadêmica dependem das evidências de validação registradas na seção 10.1.

# 1\. Visão geral e objetivos

## 1.1 Finalidade

O ATHENA é uma plataforma de gestão, rastreabilidade e revisão de requisitos de software. Organiza documentos, requisitos identificáveis, versões, relações e decisões de revisão, preservando a origem e o histórico das alterações. Complementa as ferramentas de documentação e gestão utilizadas pela equipe.

A arquitetura deve permitir identificar os itens relacionados a uma alteração, preparar sua revisão e disponibilizar versões publicadas em um Livro de Requisitos. A interface de Gestão atende à elaboração e à revisão; a interface Livro atende à leitura do conteúdo publicado.

## 1.2 Limites e princípios

* As operações essenciais podem ser realizadas manualmente. A assistência por inteligência artificial é opcional e ocorre por um agente externo conectado pelo Model Context Protocol (MCP).  
* Interface e agente obedecem às mesmas regras de negócio, permissões, validações, auditoria e controles de concorrência. O agente atua em nome do usuário e dentro da delegação concedida.  
* Relações confirmadas, sugestões automatizadas e projeções de consulta são informações distintas. Uma sugestão exige avaliação e não se torna uma relação oficial automaticamente.  
* Cada serviço é proprietário de seus dados. Outros serviços utilizam APIs ou mensagens, sem acesso direto ao banco do domínio responsável.  
* A submissão para revisão é uma SAGA independente da análise por IA e termina quando a revisão está aberta. Aprovação, rejeição e publicação pertencem ao fluxo posterior.

No MVP, o Livro é restrito aos membros autorizados do projeto. Compartilhar seu endereço não concede acesso. Publicação externa, sincronização bidirecional com mesclagem automática e conectores adicionais são evoluções.

# 2\. Requisitos arquiteturalmente relevantes

## 2.1 Requisitos funcionais

* Organizar workspaces, projetos, membros e papéis de acesso.  
* Manter documentos e requisitos estruturados, organizados por épicos, com identificador, conteúdo, origem, responsável, versões e histórico.  
* Registrar relações entre requisitos do mesmo projeto e apresentar um mapa de rastreabilidade.  
* Controlar uma candidata aberta por requisito, sua submissão, pendências de revisão, decisão e publicação explícita.  
* Manter a versão publicada disponível para consulta enquanto outra candidata é elaborada ou revisada.  
* Importar e sincronizar conteúdo de forma explícita, preservando a fonte e submetendo alterações ao fluxo de elaboração.  
* Permitir assistência por agente pessoal, com recuperação de contexto autorizado e sugestões acompanhadas de evidências.

**Requisitos é proprietário do editor, dos comentários e das baselines de conteúdo; Rastreabilidade mantém relações, retratos e indicadores de cobertura; Revisões mantém lotes e pendências; Projetos e acesso mantém a caixa de notificações, alimentada por eventos dos domínios. Comentários e menções referenciam requisito, versão e, quando houver, âncora textual; âncoras não localizadas após edição são sinalizadas, sem reassociação silenciosa. Baselines referenciam conjuntos imutáveis de versões e retratos. O MVP utiliza editor com salvamento e concorrência otimista, comentários e notificações internas; edição simultânea em tempo real fica para evolução. Alertas textuais assistidos pertencem a Análise de Impacto/MCP e continuam sujeitos à decisão humana.**

## 2.2 Requisitos de qualidade e restrições

Integridade do histórico, isolamento de acesso por projeto, controle de alterações concorrentes e recuperação de operações distribuídas orientam o desenho. A aplicação não deve confirmar um efeito distribuído antes da confirmação dos participantes responsáveis.

| Informação ou operação | Critério de confirmação ou meta |
| :---- | :---- |
| Conteúdo e versão do requisito | Confirmação após persistência no serviço Requisitos. |
| Relação oficial | Confirmação após persistência no modelo de escrita de Rastreabilidade. |
| Projeção do mapa | Atualização em até 5 segundos em operação normal. |
| Índice de contexto MCP | Atualização em até 20 segundos em operação normal. |
| Submissão para revisão | Aceite assíncrono; sucesso somente após concluir a SAGA. |

O cenário inicial de referência contempla 20 usuários simultâneos, 5.000 requisitos e 20.000 relações por projeto. As metas propostas são p95 de até 1 segundo nas consultas comuns, atualização do mapa em até 5 segundos e do índice em até 20 segundos para pelo menos 95% dos eventos nesse cenário. Consultas derivadas informam a última atualização e a situação de defasagem. Os testes devem registrar mistura de operações, taxa de requisições, duração, hardware, tamanho dos documentos, erros e percentis. Esses valores são critérios iniciais de aceitação ainda não medidos; não constituem SLA de produção. Disponibilidade será observada e reportada nos ensaios, sem promessa contratual nesta etapa.

## 2.3 Restrições acadêmicas

O enunciado exige pelo menos quatro serviços de domínio, bancos em instâncias separadas, dois clientes distintos e dois BFFs. O desenho adota seis serviços, dois clientes Angular e dois BFFs. Também deve demonstrar contratos REST, HATEOAS, Gateway, outbox, CQRS e uma SAGA com pelo menos três serviços e compensação efetiva.

A demonstração de IA deve incluir uma funcionalidade real do domínio, pipeline RAG completo, LangChain com chains e ao menos uma tool que consulte dados reais. Deve comparar respostas de pelo menos cinco perguntas com e sem recuperação, implementar ao menos dois mecanismos de resiliência do LLM e apresentar latência medida e estimativa de custo por operação.

**O repositório incluirá um cliente de referência Python para a demonstração integrada: LangChain executa uma chain fixa, consulta dados reais por uma ferramenta MCP, recupera contexto e invoca um LLM externo configurável. A entrega deve mostrar pelo menos cinco perguntas com e sem recuperação, falhas induzidas, mecanismos de resiliência, latência e estimativa de custo. A aceitação desse cliente como parte da demonstração acadêmica permanece condicionada à validação expressa do professor; uma conexão MCP isolada não comprova os demais requisitos.**

# 3\. Decisões arquiteturais e justificativas

## 3.1 Decomposição e propriedade dos dados

A solução adota seis serviços de domínio. O corte separa identidade e organização, conteúdo versionado, relações oficiais, trabalho de revisão, conectores externos e contexto para análise. Cada fronteira reúne regras e dados com ciclo próprio de alteração.

Gateway, BFFs, RabbitMQ, publicadores de outbox e consumidores de projeção são componentes de apoio e não ampliam a contagem de serviços de domínio.

## 3.2 Persistência por serviço

Cada domínio utiliza uma instância PostgreSQL independente, com banco, credenciais e volume próprios. A escolha mantém integridade referencial e transações locais dentro de cada fronteira. Análise de Impacto/MCP utiliza pgvector para armazenar vetores e recuperar contexto.

Rastreabilidade mantém o grafo no modelo relacional. A representação de requisitos como nós e relações como arestas não exige um banco de grafos específico para o MVP. Redis e Neo4j não integram o desenho inicial.

As seis instâncias podem executar na mesma máquina; independência de instância não implica seis servidores físicos.

## 3.3 Gateway e dois BFFs

Spring Cloud Gateway centraliza a entrada HTTP, a validação de tokens e de sessão ativa, o roteamento, CORS, rate limiting e registros técnicos. Os BFFs compõem respostas específicas para Gestão e Livro. As regras de negócio permanecem nos serviços.

Gestão precisa de candidatas, pendências e ações de edição; Livro precisa do conteúdo publicado e de suas referências. A separação permite contratos adequados a cada cliente sem transferir ao Gateway decisões sobre edição ou publicação.

## 3.4 SAGA orquestrada

Requisitos coordena a submissão porque é proprietário da candidata e do início e término desse processo. A orquestração concentra o estado, a etapa esperada, os prazos e as compensações, facilitando o acompanhamento e a recuperação da sequência entre Requisitos, Rastreabilidade e Revisões.

A escolha evita distribuir as decisões de avanço e compensação entre reações independentes de uma coreografia. Não existe transação de banco abrangendo os três participantes.

## 3.5 CQRS e consistência eventual

Rastreabilidade separa o modelo de escrita das relações oficiais e a projeção de leitura do mapa. Ambos permanecem no mesmo PostgreSQL, em estruturas distintas. O objetivo é preparar a representação consultada com frequência sem enfraquecer as validações de escrita.

Eventos atualizam o mapa e o índice de contexto. Consultas derivadas admitem defasagem sinalizada; submissões usam o modelo oficial de relações, e não a projeção do mapa.

## 3.6 IA externa e supervisão humana

O serviço Análise de Impacto/MCP fornece ingestão, embeddings locais, recuperação e operações autorizadas. A geração textual é executada no cliente externo do usuário. O cliente acadêmico Python utiliza LangChain e um LLM remoto configurável.

Essa separação mantém o fluxo manual independente do LLM e distingue o armazenamento de contexto das decisões humanas e das relações oficiais. O backend não utiliza um modelo gerador próprio no fluxo definido; executa localmente o modelo de embeddings.

# 4\. Componentes, camadas e comunicação

## 4.1 Serviços de domínio

| Serviço | Responsabilidade e dados próprios |
| :---- | :---- |
| Projetos e acesso | Contas, senhas, sessões, autorizações do agente, workspaces, projetos, membros, papéis e caixa de notificações internas. Inclui servidor de autorização e coordenação do fechamento/exclusão de projetos. |
| Requisitos | Documentos, requisitos, conteúdo, versões, histórico, candidatas, comentários, baselines de conteúdo e registros imutáveis de publicação. Coordena a SAGA de submissão. |
| Rastreabilidade | Relações oficiais, tipos e direção, validação de precedências, retratos imutáveis, projeção do mapa e indicadores de cobertura. |
| Revisões | Lotes, pendências, responsáveis e decisões de revisão. |
| Integrações | Configurações, credenciais de conexão, vínculos externos, operações e histórico de sincronização. |
| Análise de Impacto/MCP | Documentos indexados, metadados, vetores, ingestão, recuperação e operações de análise; interfaces MCP e REST. |

Os cinco primeiros serviços e os dois BFFs utilizam Java 17 e Spring Boot. Análise de Impacto/MCP utiliza Python. Gestão e Livro são dois clientes Angular com TypeScript.

## 4.2 Caminhos de comunicação

Gestão e Livro acessam o Gateway, que encaminha cada requisição ao BFF correspondente. Os BFFs consultam as APIs dos domínios e compõem as respostas. Os serviços internos não são expostos diretamente aos clientes.

O agente acessa /mcp no Gateway por Streamable HTTP. O Gateway preserva os cabeçalhos do protocolo e permite streaming. O serviço MCP invoca as operações dos domínios mediante autorização destinada ao serviço chamado.

RabbitMQ transporta comandos e eventos da SAGA, decisões de revisão e atualizações dos modelos derivados. Publicadores e consumidores pertencem aos próprios serviços.

## 4.3 Composição do Livro de Requisitos

O BFF Livro não possui banco nem cópia própria do conteúdo. Consulta Requisitos para obter a versão publicada corrente e a situação atual do requisito. As relações exibidas pertencem ao retrato associado àquela publicação; o mapa da Gestão apresenta as relações atuais do projeto.

Um requisito cancelado não aparece no Livro corrente. A reativação exige nova aprovação e publicação antes de voltar ao Livro. A indisponibilidade do conteúdo ou da confirmação de acesso e situação impede a consulta. Se apenas as relações estiverem indisponíveis, o conteúdo pode ser apresentado com aviso explícito.

**Cada publicação mantém um registro imutável com publicacaoId, requisitoId, versaoId, submissaoId, retratoId, autor e data. O retrato é o aprovado naquela tentativa; rejeição, reenvio ou alteração da candidata não modifica publicações anteriores. O BFF Livro compõe conteúdo e relações a partir desse conjunto e revalida acesso e situação atuais. Referências a requisitos cancelados podem aparecer sinalizadas a leitores autorizados, sem restaurar o requisito ao Livro corrente. Referências sem autorização não revelam identificador legível, título, descrição ou conteúdo; a interface indica apenas referência indisponível quando isso não expuser informação restrita.**

# 5\. Modelo de dados e ciclo de vida

## 5.1 Modelo conceitual e isolamento

Projetos e acesso mantém usuários, sessões, autorizações, workspaces, projetos e vínculos de membros. Requisitos mantém documentos, requisitos e versões, com origem e histórico. Revisões referencia a versão submetida por meio de lotes e pendências. Rastreabilidade mantém relações e retratos identificados para cada submissão. Integrações registra a correspondência entre recursos internos e artefatos externos. Análise de Impacto/MCP mantém cópias indexadas, metadados de origem e versão e vetores.

Referências entre domínios são resolvidas por identificadores e contratos; nenhum serviço consulta tabelas de outro banco. O modelo de cada domínio deve ser mantido em /docs/dados e refletido em migrações versionadas, com campos, cardinalidades, índices e restrições. Entidades vinculadas ao projeto incluem projetoId; versões, submissões, publicações, relações e decisões possuem IDs estáveis e revisão técnica quando mutáveis. Chaves estrangeiras existem apenas dentro do banco proprietário. Aplicam-se unicidade de código por projeto, uma candidata aberta por requisito, uma submissão ativa por requisito e unicidade das relações normalizadas. Histórico de negócio é preservado durante a vida do projeto; exclusão física, backups e registros técnicos obedecem às políticas das seções 5.6, 6.8 e 8.4. O detalhamento físico e sua implementação serão comprovados pelas migrações, não por esta descrição conceitual.

A API utiliza UUID estável. O código legível do requisito, como REQ-42, é separado e único dentro do projeto. A revisão técnica de concorrência é distinta da versão de conteúdo.

## 5.2 Estados da versão

| Estado | Semântica |
| :---- | :---- |
| RASCUNHO | Candidata disponível para elaboração e edição. |
| EM\_SUBMISSAO | SAGA em execução para preparar o envio. |
| AGUARDANDO\_REVISAO | Lote aberto para avaliação humana. |
| APROVADA | Candidata aprovada, elegível para publicação explícita. |
| PUBLICADA | Conteúdo disponibilizado no Livro; versão imutável. |

O fluxo regular é RASCUNHO → EM\_SUBMISSAO → AGUARDANDO\_REVISAO → APROVADA → PUBLICADA. A rejeição devolve a candidata a RASCUNHO e preserva a decisão e o conteúdo submetido. Uma falha de submissão também pode devolvê-la a RASCUNHO, após compensações confirmadas.

Cada requisito possui apenas uma candidata aberta. Seu conteúdo fica bloqueado em EM\_SUBMISSAO e AGUARDANDO\_REVISAO. Alterar uma candidata APROVADA devolve-a a RASCUNHO e exige nova revisão. Após publicação, uma alteração inicia outra candidata; a versão publicada anterior permanece no histórico.

A versão 3 pode continuar publicada enquanto a versão 4 está em revisão. Publicar a versão 4 torna-a corrente sem modificar o conteúdo da versão 3\.

**Cada tentativa de revisão possui submissaoId próprio, snapshot imutável do conteúdo, hash do conteúdo, versão, retrato e lote associados. Rejeitar ou solicitar correções encerra o lote com justificativa e permite retornar a candidata a RASCUNHO, sem apagar o conteúdo avaliado. Um reenvio cria outra tentativa e outro retrato. Durante uma SAGA ativa, correção, edição ou nova submissão são recusadas com conflito; a candidata só é liberada após conclusão ou compensação confirmada.**

## 5.3 Situação do requisito

A situação do requisito é ATIVO ou CANCELADO e não se confunde com o estado da versão. ATIVO indica participação no escopo atual, mesmo sem versão publicada.

Editor ou superior pode cancelar um requisito mediante confirmação explícita. O cancelamento remove-o das listagens ativas e do Livro corrente, preservando conteúdo, versões, decisões e relações. Os vínculos existentes sinalizam o cancelamento. Novas edições, submissões e publicações ficam bloqueadas.

O cancelamento é recusado com 409 Conflict enquanto houver SAGA ou revisão em andamento. Editor ou superior pode reativar o requisito: a situação retorna a ATIVO e uma nova candidata RASCUNHO é baseada no último conteúdo. O retorno ao Livro exige nova aprovação e publicação.

## 5.4 Relações e retratos

O catálogo do MVP contém DEPENDE\_DE e BLOQUEIA, direcionais, e RELACIONADO\_A, sem direção. Leituras como Pré-requisito/Dependente e Bloqueia/Bloqueada por descrevem sentidos de uma mesma aresta e não geram registros duplicados.

São proibidas autorrelações, relações entre projetos e duplicações equivalentes, inclusive extremos invertidos em RELACIONADO\_A. Ciclos são bloqueados em cada tipo direcional e no grafo combinado de precedências definido abaixo; conexões circulares em RELACIONADO\_A são permitidas.

O modelo de escrita mantém as relações oficiais. Um retrato preserva o contexto de relações capturado para uma submissão. A compensação cancela esse retrato sem apagar as relações oficiais.

O mapa retorna no máximo 500 nós e 1.000 arestas, com filtros e indicação explícita de truncamento. Esses limites afetam a visualização e não removem relações.

**Toda criação, alteração e remoção de relação adquire a mesma trava transacional por projeto no banco de Rastreabilidade, inclusive por importação ou MCP. Sob essa trava, o serviço verifica extremos, duplicações e ciclos antes do commit; índices únicos reforçam a normalização. RELACIONADO\_A armazena extremos em ordem canônica. Para a validação combinada, A DEPENDE\_DE B estabelece precedência B → A; A BLOQUEIA B estabelece precedência A → B. Ciclos são proibidos no grafo combinado dessas precedências e em cada tipo isolado. RELACIONADO\_A não participa dessa restrição. DEPENDE\_DE descreve necessidade funcional; BLOQUEIA descreve impedimento de avanço e não é criado automaticamente como inverso. Eventos e projeções só são produzidos após a validação do modelo oficial.**

## 5.5 Projeção de leitura do mapa

Eventos de relações atualizam nós, arestas e contagens; eventos de Requisitos atualizam títulos e estados. O consumidor respeita a revisão da informação para impedir que eventos antigos sobrescrevam dados novos.

A consulta informa a última atualização e as situações atualizado, atualização pendente ou reconstruindo. O funcionamento do consumidor, por si só, não comprova atualização.

A reconstrução utiliza relações oficiais e dados obtidos pelas APIs de Requisitos. Mudanças ocorridas durante o processo devem ser incorporadas. A projeção anterior permanece disponível como defasada até a nova estar pronta; sem projeção anterior, a consulta informa indisponibilidade temporária.

**A projeção utiliza gerações identificadas, com nós, arestas, contagens, revisões por entidade e posições de eventos processados. Os eventos incluem RelationCreated, RelationChanged, RelationRemoved, RequirementChanged e RequirementSituationChanged, com IDs e versões de origem. Na reconstrução, registra-se um marco por fluxo; o consumidor conserva as mudanças enquanto uma geração nova é carregada pelas fontes oficiais. Eventos posteriores ao marco são reaplicados com controle de revisão, inclusive remoções, e a troca do identificador da geração ativa ocorre atomicamente quando a nova alcança o marco de corte. A geração anterior permanece sinalizada como defasada até a troca. O atraso é medido por eventos pendentes, idade do evento mais antigo e posições processadas; horário do último consumo, sozinho, não comprova atualização.**

## 5.6 Arquivamento e exclusão

Mantenedor e Owner podem arquivar e restaurar projetos. Um projeto arquivado permite consulta, bloqueia alterações e fica fora das listagens de projetos ativos.

Somente o Owner pode excluir definitivamente um projeto arquivado, com confirmação pelo nome e sem SAGA, revisão ou sincronização em andamento. Projetos e acesso registra EXCLUINDO, bloqueia o acesso e coordena a limpeza persistida nos demais serviços. Falhas são repetidas; a conclusão exige todas as confirmações. Uma marca técnica impede recriação por mensagens atrasadas. Arquivos dos provedores externos não são apagados.

A exclusão de workspace exige confirmação de seu Owner pelo nome e aplica exclusão lógica aos projetos vinculados; não executa sua exclusão física.

**A exclusão é uma operação persistida de Projetos e acesso. Após arquivamento e confirmação do Owner, o projeto entra em FECHANDO: novas admissões de escrita são bloqueadas, cada domínio confirma seu bloqueio local e operações admitidas são encerradas ou compensadas. Confirmada a quiescência e a ausência de revisão ou sincronização ativa, o estado passa a EXCLUINDO e cada serviço executa limpeza idempotente, respondendo pela própria outbox. O coordenador repete falhas e só conclui após todas as confirmações. Marcas técnicas mínimas, sem conteúdo do projeto nem dados pessoais, permanecem enquanto mensagens ou restaurações puderem reintroduzir dados; sua remoção exige encerrar essa possibilidade. Backups expiram após sete dias e a restauração reaplica o registro de exclusões antes de reabrir acesso. A propriedade do workspace é separada da propriedade dos projetos: seu Owner gerencia membros e exclusão lógica do workspace, sem receber implicitamente acesso ou exclusão física de todos os projetos.**

# 6\. Integrações, APIs e processos distribuídos

## 6.1 Roteamento externo

| Entrada | Destino | Finalidade |
| :---- | :---- | :---- |
| /api/v1/gestao/\*\* | BFF Gestão | Elaboração, edição, projetos e revisões. |
| /api/v1/livro/\*\* | BFF Livro | Consulta do conteúdo publicado. |
| /mcp | Análise de Impacto/MCP | Ferramentas por Streamable HTTP. |

O Gateway não coordena a SAGA e não repete automaticamente escritas. As novas tentativas pertencem ao fluxo responsável pela operação. Análise de Impacto/MCP também expõe REST documentado para recursos como operações e análises.

**Cada serviço e BFF terá contrato OpenAPI versionado em /docs/api, com recursos, métodos, payloads, respostas, permissões e exemplos de erro. As exceções públicas são explicitamente enumeradas: autenticação, início e conclusão da recuperação, chaves públicas e metadados de descoberta OAuth/MCP; nenhuma exceção concede leitura de projeto. Endpoints técnicos e métricas ficam restritos à rede de operação. O recurso MCP publica seus metadados de autorização e utiliza o servidor de autorização anunciado. Limites configuráveis por usuário e IP protegem autenticação, recuperação, consultas e escritas, com 429 e Retry-After. A topologia e CORS seguem a seção 7.3. Os serviços internos exigem identidade apropriada e não ficam expostos diretamente à Internet.**

## 6.2 Convenções REST

* Versionamento por /api/v1 para mudanças compatíveis. Quebras de contrato exigem /api/v2. A versão da API independe da versão do requisito.  
* Recursos no plural. POST cria recursos; PATCH altera parcialmente campos permitidos. Submissões, decisões e publicações são recursos de negócio próprios.  
* Paginação com page=0 e size=20 por padrão, limitada a 100 itens. Envelope: items, page, size, totalElements, totalPages e \_links.  
* Ordenação padrão sort=atualizadoEm,desc, com identificador como desempate. Campos permitidos são definidos por recurso; parâmetros inválidos retornam 400\.  
* Filtros de requisitos: projetoId, q, situacao, estadoVersao, tipo, epicoId e responsavelId. Cancelados ficam fora por padrão e a busca respeita o acesso do usuário.  
* Respostas de sucesso: 200 para consulta e edição; 201 com Location para criação síncrona; 202 com acompanhamento para processamento assíncrono; 204 para remoção sem corpo.

Erros REST seguem Problem Details, RFC 9457, com Content-Type application/problem+json e campos type, title, status, detail e instance. Código de negócio e identificador de rastreamento são acrescentados quando necessários.

Referências normativas registradas: RFC 9457 — https\://www\.rfc-editor.org/rfc/rfc9457.html; RFC 6585 — https\://www\.rfc-editor.org/rfc/rfc6585.html.

## 6.3 Concorrência e idempotência HTTP

Consultas retornam ETag para a revisão técnica. Edição da candidata, submissão, decisão, publicação, cancelamento e reativação exigem If-Match sobre o recurso correspondente. Ausência retorna 428 Precondition Required; revisão desatualizada retorna 412 Precondition Failed. A comparação e a gravação são atômicas no serviço proprietário.

Criações de submissão, decisão, publicação, cancelamento e reativação exigem Idempotency-Key, vinculada a usuário, projeto e operação. O resultado concluído permanece por 24 horas. Repetir a mesma chave e o mesmo pedido recupera a operação; conteúdo diferente com a mesma chave retorna 409 Conflict.

A submissão aceita retorna 202 Accepted e Location para acompanhamento. Outra submissão para o mesmo requisito enquanto houver uma ativa retorna 409 Conflict. Requisitos garante essa restrição; o BFF e o MCP encaminham a operação.

**A ordem de avaliação é: autenticar, verificar acesso atual, consultar a Idempotency-Key no escopo de usuário/projeto/operação e comparar a impressão digital do pedido. Se a operação já existe e o pedido é o mesmo, retorna-se seu resultado ou acompanhamento, sem reavaliar o If-Match que a própria execução pode ter tornado antigo. A mesma chave com outro pedido retorna 409\. Apenas uma operação nova valida a presença e o valor de If-Match antes de executar. Reserva da chave, efeito local e resultado são protegidos transacionalmente contra chamadas concorrentes. A recuperação nunca reexecuta a ação nem revela resultado a quem perdeu acesso.**

## 6.4 Contratos específicos dos BFFs e HATEOAS

O recurso Requisito oferece navegação e ações conforme o estado e a permissão. Leitor não recebe ações de edição ou submissão. Publicação só é apresentada para candidata APROVADA e usuário autorizado. Cada chamada é revalidada pelo backend, inclusive quando utiliza um link obtido anteriormente.

| Campo ou conteúdo | Gestão — exemplo | Livro — exemplo |
| :---- | :---- | :---- |
| Identidade | Mesmo UUID; código REQ-42. | Mesmo UUID; código REQ-42. |
| Título | Exportar documento de requisitos. | Exportar documento de requisitos. |
| Versão | versaoEmEdicao: 4\. | versaoPublicada: 3\. |
| Estado e revisão | AGUARDANDO\_REVISAO; loteId e 2 pendências abertas. | Conteúdo da versão publicada. |
| Resposta específica | acoesPermitidas: consultar-revisao. | Critérios de aceite e referências publicadas. |

A representação HATEOAS utiliza \_links para self, coleção, versões, submissão e revisão, e \_actions para operações disponíveis. Cada ação informa href, method, contentType, campos aceitos e precondições, incluindo If-Match, Idempotency-Key e confirmação humana quando aplicáveis. Exemplo de ação: publicar aponta para POST /api/v1/gestao/projetos/{projetoId}/requisitos/{requisitoId}/publicacoes e exige candidata APROVADA, papel Aprovador ou superior e versão correspondente. A ação não aparece para quem não pode executá-la; chamadas por URL conhecida continuam sujeitas à mesma validação. Os exemplos completos e os erros ficam no OpenAPI de cada BFF; nomes de ações sem URL e método não substituem esses contratos.

## 6.5 SAGA de submissão para revisão

A transação de negócio envolve Requisitos, Rastreabilidade e Revisões. A preparação manual ou assistida pode ocorrer antes da submissão; a SAGA não espera geração por IA.

1. Requisitos registra a submissão, o estado da SAGA e a candidata EM\_SUBMISSAO; grava RegistrarRetratoRastreabilidade na outbox, na mesma transação local.  
2. Rastreabilidade registra o retrato das relações confirmadas relevantes, o resultado do comando e RetratoRastreabilidadeRegistrado na outbox.  
3. Após a confirmação do retrato, o coordenador envia AbrirLoteRevisao. Revisões cria o lote e as pendências, registra o resultado e LoteRevisaoAberto na outbox.  
4. Após a confirmação do lote, Requisitos marca a SAGA CONCLUIDA e a candidata AGUARDANDO\_REVISAO.

O coordenador aceita respostas apenas para o estado e a etapa esperados. A SAGA possui os estados EM\_ANDAMENTO, CONCLUIDA, COMPENSANDO, COMPENSACAO\_PENDENTE e COMPENSADA. Esses estados são distintos dos estados da versão.

## 6.6 Compensações e mensagens atrasadas

No cenário em que o retrato foi criado e a abertura do lote falhou ou não foi confirmada, Requisitos inicia COMPENSANDO. Primeiro envia CancelarLoteRevisao e aguarda CancelamentoLoteConfirmado; depois envia CancelarRetratoRastreabilidade e aguarda CancelamentoRetratoConfirmado.

Revisões cancela o lote e as pendências que existirem. Rastreabilidade marca o retrato da submissão como cancelado, preservando as relações oficiais. Ambos registram uma marca durável de cancelamento mesmo quando a criação ainda não chegou. Uma criação tardia da mesma operação é recusada.

Somente após todas as confirmações Requisitos devolve a candidata a RASCUNHO e marca COMPENSADA. Conteúdo e auditoria são preservados. Sem confirmação de alguma compensação, mantém COMPENSACAO\_PENDENTE e não libera a candidata.

O princípio também se aplica a efeitos que possam ter ocorrido sem resposta: timeout indica ausência de confirmação, não ausência de execução. Uma confirmação tardia não reativa o sucesso de uma SAGA em compensação.

| Comando | Participante | Confirmação |
| :---- | :---- | :---- |
| RegistrarRetratoRastreabilidade | Rastreabilidade | RetratoRastreabilidadeRegistrado |
| AbrirLoteRevisao | Revisões | LoteRevisaoAberto |
| CancelarLoteRevisao | Revisões | CancelamentoLoteConfirmado |
| CancelarRetratoRastreabilidade | Rastreabilidade | CancelamentoRetratoConfirmado |

sagaId identifica a submissão; commandId identifica uma ação lógica; eventId identifica uma resposta. A repetição de uma ação mantém commandId e reutiliza o resultado persistido. O cancelamento utiliza seu próprio commandId.

Comandos identificam projeto, requisito e versão. Criações levam o prazo da submissão e não produzem novos efeitos após expirar. Eventos possuem eventId e repetem sagaId e commandId. Falhas informam código, descrição e classificação temporária ou definitiva.

**Comandos e eventos possuem envelope versionado: schemaVersion, messageId, messageType, occurredAt, correlationId, causationId, projetoId, sagaId, commandId e payload. Comandos de criação incluem requisitoId, versaoId, submissaoId, hash do conteúdo e deadline; respostas incluem o recurso criado e a revisão ou uma falha com code, detail e retryable. commandId identifica a ação lógica e permanece nas tentativas; messageId/eventId identifica a mensagem e attempt registra o envio. O coordenador persiste etapa, comando esperado, tentativas, prazo e resultado: AGUARDANDO\_RETRATO, AGUARDANDO\_LOTE, CANCELANDO\_LOTE ou CANCELANDO\_RETRATO, subordinados aos estados gerais da SAGA. Recusas definitivas de negócio iniciam compensação; falhas temporárias seguem a política de retentativas. JSON Schemas e exemplos compatíveis ficam em /docs/mensageria.**

## 6.7 Prazos e recuperação

| Fluxo | Política inicial |
| :---- | :---- |
| Etapa da SAGA | 3 tentativas no total; até 10 segundos de espera por tentativa; intervalos de 2 e 4 segundos. |
| Submissão | Prazo total de 90 segundos. Esgotamento inicia compensação. |
| Erro definitivo de negócio | Compensar sem repetir a criação. |
| Compensação sem confirmação | Após as tentativas, manter pendente e repetir a cada 60 segundos; não expira com a submissão. |
| Mensagens fora da SAGA | 5 tentativas no total; intervalos de 2, 10, 30 e 60 segundos. Após esgotamento, DLQ e sinalização de falha. |

Os valores são configurações iniciais, sujeitos a medição. A política geral de DLQ não encerra nem substitui a recuperação das compensações.

## 6.8 Outbox, inbox e publicação confiável

Salvar no banco e publicar separadamente cria risco de dual write. Nos participantes da SAGA, efeito de negócio, resultado do comando/inbox e intenção de resposta/outbox são persistidos na mesma transação local. Requisitos inclui o estado da candidata e do coordenador; Rastreabilidade inclui o retrato ou cancelamento; Revisões inclui o lote ou cancelamento.

A outbox contém identificador, tipo, correlação de SAGA quando aplicável, payload JSON, criação, tentativas, próxima tentativa e publicação. A inbox registra identificador único da mensagem, processamento e resultado, com identificação do comando lógico.

O consumidor verifica duplicação, aplica o efeito e registra resultado e resposta na mesma transação. Somente após o commit confirma o processamento ao RabbitMQ. O publicador marca o envio após a confirmação do broker. Uma queda entre publicar e registrar pode gerar repetição, tratada pela idempotência.

O comando já concluído reutiliza seu resultado e permite reenviar a confirmação sem duplicar o efeito. Falhas temporárias não são registradas como conclusão definitiva que impeça sua repetição.

Filas duráveis, mensagens persistentes e volumes preservam mensagens. Com broker indisponível, intenções permanecem na outbox; com consumidor indisponível, mensagens permanecem na fila. A confirmação do broker não equivale à confirmação de processamento do domínio.

Mensagens inválidas ou incompatíveis seguem para a DLQ com motivo registrado. Recusas de negócio esperadas produzem respostas próprias. Após correção, o reprocessamento controlado mantém o identificador original e registra a tentativa. Versão e estado são validados, sem pressupor ordem global de entrega.

**Utilizam-se exchanges duráveis athena.commands e athena.events do tipo topic, com filas por participante ou consumidor, filas de retentativa e DLQ por fluxo. Filas de trabalho não descartam mensagens apenas por idade; criação expirada produz resposta de recusa. Publicadores usam publisher confirms e consumidores confirmam somente após commit. Outbox/inbox e resultados de comandos só são limpos após conclusão e encerramento da janela autorizada de replay; como referência inicial, a janela de reprocessamento é de sete dias, preservando separadamente comandos, cancelamentos e exclusões que ainda possam reaparecer. DLQ não é descartada automaticamente: requer diagnóstico, correção e replay auditado com o identificador original. Operações não concluídas nunca expiram por essa limpeza. No índice MCP, o recebimento persiste inbox e trabalho de indexação na mesma transação; o worker gera embeddings fora da transação longa, revalida versão e exclusão e ativa os novos chunks em transação, conservando a versão anterior até a troca. Trabalhos interrompidos são retomados sem duplicar chunks.**

## 6.9 Revisão, decisão e publicação

Revisões registra a decisão, seu autor e a versão avaliada. Uma aprovação de Aprovador, Mantenedor ou Owner é suficiente, inclusive para alteração própria, desde que todas as pendências bloqueantes estejam resolvidas. A primeira decisão final válida encerra o lote; decisões concorrentes posteriores recebem conflito.

Decisão e evento são gravados na mesma transação por outbox. RabbitMQ entrega o evento a Requisitos, que aplica APROVADA ou devolve a candidata a RASCUNHO na rejeição. A aplicação é idempotente.

Se a comunicação falhar, a decisão permanece em Revisões e aparece como pendente de aplicação. Isso inclui falhas encaminhadas à DLQ. Publicar exige que Requisitos já tenha confirmado APROVADA; a decisão registrada apenas em Revisões ainda não libera a ação.

A publicação é explícita e pertence a Aprovador, Mantenedor ou Owner. A propagação da decisão ocorre após a SAGA e não amplia a transação de submissão.

**O lote é criado como PREPARADO, sem aceitar decisões. Depois de persistir a SAGA CONCLUIDA e AGUARDANDO\_REVISAO, Requisitos grava AtivarLoteRevisao na outbox; Revisões ativa o lote idempotentemente. Até a ativação, a interface informa preparação pendente. A decisão identifica lote, submissaoId, versaoId e hash do conteúdo. Eventos só são aplicados à tentativa corrente e ao estado esperado; uma decisão antiga não aprova uma candidata posterior. Edição de candidata aprovada invalida sua elegibilidade anterior. A publicação compara estado, versão aprovada e revisão técnica e grava a publicação atomicamente em Requisitos. Conflitos entre decisões finais são resolvidos por controle transacional, prevalecendo a primeira decisão válida.**

## 6.10 Integrações externas

Os primeiros conectores são Google Drive/Google Docs para documentação e OpenProject para gestão do trabalho. GitHub começa com vínculos para artefatos. Sincronização completa de GitHub, Jira e outros conectores são evoluções.

Integrações preserva fonte, identificador externo, versão conhecida e histórico. Mantenedor e Owner configuram conexões; Editor ou superior solicita sincronização de conteúdo autorizado. Credenciais permanecem no backend.

A sincronização parte de ação explícita, executa em segundo plano e possui operação persistida. Conteúdo recebido entra como rascunho ou proposta de alteração e não substitui automaticamente uma versão publicada.

Antes de aplicar, Integrações compara a versão externa conhecida e a revisão local. Alterações concorrentes interrompem a aplicação e exigem resolução humana. Após timeout de escrita externa, verifica o resultado antes de tentar criar novamente.

Operação e intenção de execução são gravadas na mesma transação local; o resultado externo é registrado separadamente. Banco local e provedor não formam uma única transação.

**O contrato de cada conector registra operações suportadas, versão da API, dados de origem, limites e permissões mínimas. No Google, prioriza-se drive.file com seleção explícita de arquivos, verificando cobertura das operações de Drive/Docs; escopos adicionais exigem necessidade documentada. No OpenProject, a credencial deve ter apenas o acesso necessário ao projeto. Importações são explícitas e entram como rascunho ou proposta; vínculos e criação autorizada de tarefas têm operação persistida. Respostas 429 respeitam Retry-After; falhas temporárias usam espera progressiva. Uma escrita com timeout passa por consulta e reconciliação usando identificador externo ou marcador da operação. Se o provedor não permitir comprovar o efeito, o estado é RESULTADO\_DESCONHECIDO, com resolução humana e sem nova criação automática. Exportação ou publicação de documentos externos é evolução do MVP.**

## 6.11 MCP, recuperação de contexto e RAG

Análise de Impacto/MCP ingere requisitos e documentos autorizados e os identifica por projeto, origem e versão. O modelo inicial de embeddings é multilingual-e5-small, executado localmente, com vetores no pgvector. Modelo e versão do conteúdo são registrados para permitir reindexação.

A recuperação respeita o acesso ao projeto e ao conteúdo, inclusive rascunhos. O agente recebe contexto e referências para sugerir impactos ou relações. Sugestões registram as versões utilizadas; antes da aplicação, o sistema revalida versões e permissões. Uma mudança no conteúdo exige nova avaliação ou revisão manual.

O cliente de demonstração Python utiliza uma chain LangChain em fluxo fixo: consulta dados reais por uma tool MCP, recupera contexto e chama o LLM remoto configurável. Não depende de agente autônomo.

A chamada ao LLM tem timeout de 20 segundos por tentativa, no máximo duas tentativas e intervalo de 2 segundos. Em falha, apresenta as referências recuperadas e permite continuar manualmente. Timeout com repetição e fallback são os mecanismos de resiliência selecionados.

A avaliação compara ao menos cinco perguntas com e sem recuperação, observando correção, referências, latência e custo. O modelo gerador é registrado em cada teste. As medições e a avaliação ainda precisam ser executadas.

**A segmentação inicial respeita requisitos e seções, usando blocos de 300 a 400 tokens, com sobreposição aproximada de 50 tokens quando houver divisão, incluindo título e origem dentro do limite do tokenizer. O multilingual-e5-small usa prefixos query: e passage:, vetores normalizados e revisão fixa do modelo; nenhuma entrada ultrapassa seu limite de 512 tokens. A recuperação combina busca textual e vetorial, remove duplicatas e retorna inicialmente cinco trechos, com ajuste por avaliação de relevância e referências. Similaridade não é apresentada como probabilidade de acerto. Sugestões seguem esquema com tipo, requisito analisado, alvos, versões utilizadas, evidências, justificativa e estado de validação; a saída do LLM é tratada como dado não confiável e não concede autoridade. Acesso é filtrado antes da entrega e revalidado nas fontes oficiais, inclusive durante atraso do índice. Revogação bloqueia a leitura imediatamente; exclusão agenda remoção de chunks e vetores, impedindo sua reativação por trabalhos atrasados. Compatibilidade de cada cliente MCP, relevância, prazo de indexação de 20 segundos e timeout do LLM serão comprovados por testes no cenário da seção 2.2.**

# 7\. Segurança, identidade e autorização

## 7.1 Autenticação e identidade dos serviços

Projetos e acesso concentra cadastro e login por e-mail e senha, sessões, recuperação de acesso e autorizações do agente. A recuperação utiliza código de seis dígitos válido por 10 minutos. Spring Authorization Server integra esse serviço, sem criar outro domínio.

JWTs utilizam assinatura assimétrica. A chave privada permanece no emissor e as chaves públicas são distribuídas por JWKS, com suporte à rotação. Os destinos validam assinatura, emissor, destinatário, validade e acesso atual.

Cada serviço possui credencial própria e permissões limitadas. Processos internos utilizam identidade de serviço. Chamadas em nome do usuário usam troca de token para obter autorização destinada ao serviço chamado, preservando usuário, projeto e delegação. O token recebido pelo MCP não é repassado diretamente aos serviços internos; cabeçalhos com identificadores não concedem autoridade.

**JWTs utilizarão RS256 e JWKS com kid. Chaves privadas e credenciais permanecem fora do repositório e dos logs, em armazenamento de segredos; a rotação publica a nova chave antes do uso e preserva a chave pública anterior até expirar o último token válido, salvo revogação emergencial. A implementação OAuth deverá utilizar biblioteca consolidada e comprovar Authorization Code com PKCE, metadados e troca de tokens para os públicos-alvo internos antes da fixação das versões. Não se presume suporte automático dessas bibliotecas. A troca reduz escopos e vincula sujeito, projeto, delegação e aud ao serviço chamado; o token recebido pelo MCP não é repassado. Senhas usam Argon2id com salt e parâmetros calibrados; recuperação usa token aleatório de alta entropia, hash, validade de 15 minutos e uso único. Login e recuperação terão limites configuráveis por conta e IP, espera progressiva e respostas que não revelem existência da conta.**

## 7.2 Sessões, renovação e revogação

| Item | Validade inicial |
| :---- | :---- |
| JWT de acesso da interface ou agente | 15 minutos, limitado ao prazo restante da sessão ou autorização. |
| Sessão sem lembrar dispositivo | Até 24 horas; cookie de sessão. |
| Sessão com lembrar dispositivo | Até 30 dias; cookie persistente pelo prazo restante. |
| Autorização renovável do agente | Até 30 dias; independente da sessão da interface. |

O prazo máximo é contado desde a criação. Renovar o JWT não o reinicia. O refresh token é rotacionado em cada utilização e o anterior é invalidado; a renovação respeita o limite original.

Sair revoga apenas a sessão atual da interface. Revogar agente encerra apenas sua autorização MCP. Redefinir a senha encerra todas as sessões e autorizações do usuário.

Em cada chamada protegida, o Gateway valida o JWT e consulta Projetos e acesso para confirmar sessão ou autorização ativa. Se não conseguir confirmar, não autoriza a requisição. O bloqueio vale para novas chamadas após a revogação.

A consulta de autorização por chamada mantém a dependência síncrona de Projetos e acesso. A renovação rotaciona o refresh token em transação, com família, hash, expiração absoluta e marca de consumo; reutilização indevida revoga a família. O frontend coordena a renovação entre abas e chamadas para evitar corridas. Logout, revogação do agente e redefinição de senha respeitam os alcances descritos nesta seção. O timeout inicial da confirmação interna é de 2 segundos, sujeito a medição; indisponibilidade impede a execução e retorna 503, enquanto credencial inválida retorna 401\. A autorização é revalidada em cada chamada MCP, inclusive em conexão já aberta; novas chamadas e entregas posteriores à revogação são bloqueadas e streams podem ser encerrados. A revogação não promete desfazer uma transação já confirmada antes dela.

## 7.3 Tokens no navegador

O JWT de acesso fica somente na memória do frontend e é enviado em Authorization: Bearer. O refresh token fica em cookie HttpOnly, Secure e SameSite=Lax; seu hash e o estado da sessão são mantidos no banco. Tokens não são armazenados em localStorage ou sessionStorage.

Ao recarregar a página, o frontend pode renovar o acesso se a sessão continuar válida. Sem lembrar dispositivo, o cookie é de sessão; com a opção ativada, sua persistência respeita o prazo restante de 30 dias.

Logout revoga a sessão, remove o cookie e limpa o JWT da memória. Endpoints que usam cookie, incluindo renovação e logout, exigem proteção CSRF. SameSite é uma camada adicional.

Gestão, Livro e API serão servidos sob uma origem HTTPS, em caminhos distintos. O refresh token utiliza cookie Secure, HttpOnly e SameSite=Lax, sem Domain e com Path restrito aos endpoints de autenticação que o utilizam; a remoção usa os mesmos atributos de identificação. Sua persistência respeita a opção lembrar dispositivo e o prazo absoluto. Endpoints com cookie exigem token CSRF vinculado à sessão em cabeçalho próprio e validação de Origin, inclusive renovação e logout; SameSite não substitui essa proteção. CORS não usa curinga com credenciais e, em desenvolvimento ou outra origem explicitamente necessária, aceita apenas a lista configurada. Clientes MCP administram seus tokens OAuth e não utilizam o cookie de sessão do navegador.

## 7.4 Papéis e gestão de membros

Cada pessoa possui um papel por projeto. A hierarquia é cumulativa: Owner \> Mantenedor \> Aprovador \> Editor \> Repórter \> Leitor. O papel pode variar entre projetos. A propriedade de um projeto não concede automaticamente propriedade do workspace.

| Papel | Permissões acrescidas às dos níveis inferiores |
| :---- | :---- |
| Leitor | Consulta conteúdo autorizado, Livro, mapa e contexto RAG. |
| Repórter | Sugere melhorias, aponta problemas e registra comentários e menções em conteúdo autorizado. |
| Editor | Cria e edita candidatas e relações oficiais; submete para revisão; cancela e reativa requisitos; cria baselines e solicita sincronização autorizada. |
| Aprovador | Aprova ou rejeita alterações próprias e de terceiros; publica candidatas aprovadas. |
| Mantenedor | Administra metadados e organização; configura integrações; arquiva e restaura projetos; gerencia membros até Aprovador. |
| Owner | Gerencia Mantenedores; transfere a propriedade; exclui definitivamente projeto arquivado. |

Quem cria um projeto torna-se seu único Owner. Mantenedor não altera outro Mantenedor nem o Owner e não promove alguém a Mantenedor. Owner administra os demais membros.

A transferência exige confirmação do Owner atual e aceite de membro ativo. A troca ocorre em uma transação local; o antigo Owner passa a Mantenedor. Antes da conclusão, o Owner não pode sair, ser removido ou rebaixar o próprio papel.

Convites exigem aceite pela conta destinatária e não concedem acesso antes disso. O papel é revalidado na entrada. Remoção e rebaixamento valem nas próximas chamadas, inclusive MCP. Mudanças registram autor, membro, papéis anterior e novo e data.

**Leitor consulta conteúdo, histórico, baselines, relações, Livro e contexto autorizados. Repórter acrescenta propostas, problemas, comentários, respostas e menções; pode editar seus próprios comentários e resolvê-los, com histórico. Editor e superiores criam e editam candidatas, aceitam propostas de relações, criam/alteram/removem relações oficiais, criam baselines, submetem, cancelam/reativam e solicitam sincronização; podem resolver comentários no conteúdo que administram. Aprovador e superiores decidem revisões e publicam; Mantenedor e Owner organizam projetos, configuram integrações e arquivam/restauram. Apenas Owner exclui fisicamente projeto arquivado e transfere propriedade; Mantenedor gerencia membros até Aprovador e Owner até Mantenedor. Notificações são marcadas como lidas apenas pelo destinatário. Todas as operações revalidam acesso e estado; comentários e metadados não contornam bloqueio de conteúdo, revisão ou projeto. Retratos publicados são imutáveis. A matriz será refletida nos contratos e testes de autorização.**

## 7.5 Delegação OAuth e escopos MCP

O agente utiliza OAuth com Authorization Code \+ PKCE. O usuário autentica-se no ATHENA e escolhe projetos e escopos; a seleção inicial é somente leitura. A autorização do agente possui ciclo independente da sessão da interface e pode ser revogada separadamente.

| Escopo | Limite funcional |
| :---- | :---- |
| athena:read | Consultar projetos, requisitos, versões, relações, revisões e contexto autorizado. |
| athena:write | Registrar sugestões, criar ou editar requisitos e relações e submeter alterações, conforme o papel. |
| athena:approve / athena:publish | Escopos independentes: athena:approve registra aprovação ou rejeição; athena:publish publica candidata aprovada. Ambos exigem papel autorizado e confirmação humana específica. |
| athena:admin | Gerenciar membros, configurações e integrações conforme o papel. |
| athena:delete-project | Excluir definitivamente projeto; exclusivo do Owner. |

Cada chamada verifica projeto autorizado, escopo concedido e papel atual. Escopos são independentes e não ampliam permissões. Um Owner com apenas athena:read delega somente consultas; um Editor não pode delegar aprovação. A perda de acesso ao projeto também limita o agente.

Aprovar, rejeitar ou publicar pelo MCP exige confirmação específica em tela autenticada do ATHENA. O serviço responsável registra comprovante de uso único, válido por cinco minutos e vinculado a usuário, projeto, ação e versão. O comprovante é validado e consumido na mesma transação da ação. Mudança de versão ou permissão invalida a tentativa.

Conceder escopo não aprova antecipadamente sugestões, e uma declaração do agente não comprova confirmação humana. A autoaprovação permitida aos três papéis superiores continua exigindo decisão explícita.

**O catálogo acrescenta athena:publish, independente de athena:approve. Toda ferramenta MCP e endpoint possui mapeamento explícito para operação de domínio, projeto, escopo e papel, versionado em /docs/seguranca. Consultas exigem athena:read; sugestões, conteúdo, relações e submissões exigem athena:write; decisões exigem athena:approve; publicações exigem athena:publish; administração exige athena:admin; exclusão física exige athena:delete-project e Owner. Aprovar, rejeitar e publicar abrem confirmação autenticada no ATHENA, mostrando ação e conteúdo/versão. O serviço responsável emite comprovante de uso único, com validade de cinco minutos, vinculado também ao hash da ação, e o valida e consome na mesma transação do efeito. Escopo, papel, versão e acesso são revalidados. A identidade interna segue a troca de tokens da seção 7.1.**

# 8\. Infraestrutura, implantação e operação

## 8.1 Ambiente local

Cada aplicação possui Dockerfile, com multi-stage build onde aplicável. Docker Compose deve iniciar o conjunto com um comando, utilizando redes internas e volumes persistentes. Cada serviço executa apenas suas próprias migrações.

São seis containers PostgreSQL independentes; a instância de Análise de Impacto/MCP inclui pgvector. BFFs não possuem banco próprio.

## 8.2 Kubernetes

Aplicações utilizam Deployments e Services. Bancos e RabbitMQ utilizam StatefulSets e volumes persistentes. O Ingress encaminha tráfego ao Gateway. ConfigMaps e Secrets separam configuração e credenciais. A demonstração deve incluir aumento de réplicas de uma aplicação sem estado.

**A implantação manterá matriz de versões compatíveis testadas, lockfiles e imagens fixadas por versão e digest, sem tags flutuantes. O CI compila, verifica e publica imagens identificadas pelo commit; manifests apontam para essas imagens e mantêm histórico de implantação. Aplicações sem estado usam Deployments; PostgreSQL e RabbitMQ usam volumes próprios e serviços internos. Cada container possui requests/limits, usuário sem privilégios quando suportado e startup, readiness e liveness probes adequadas. Readiness controla recebimento de tráfego; liveness não deve provocar reinícios em cascata por falha de dependência. Armazenamento, capacidade e política de retenção dos volumes ficam explícitos nos manifests. A demonstração escala uma aplicação sem estado; banco, broker e nó único continuam sem garantia de alta disponibilidade. Compatibilidade e consumo serão verificados antes de fixar a matriz final.**

## 8.3 Observabilidade

Prometheus e Grafana acompanham métricas. Logs estruturados devem permitir correlação entre HTTP e mensagens, com identificador da operação, serviço, tentativas e motivo da falha.

São coletados latência p95 HTTP, taxa de erros, idade e quantidade de outbox e filas, DLQ, duração de SAGA, compensações pendentes, atraso das projeções e indexação, além de CPU e memória. Alertas iniciais disparam para qualquer mensagem em DLQ, compensação pendente por mais de dois minutos e atraso acima da meta por um minuto. Limites são configuráveis e ajustados por medição, distinguindo reconstrução planejada de incidente. Cada alerta referencia runbook em /docs/operacao com identificação da operação, inspeção de logs, diagnóstico, correção e reprocessamento auditado. HTTP e mensagens carregam traceId e IDs de domínio, sem registrar tokens ou conteúdo confidencial desnecessário.

## 8.4 Backup e restauração

Cada banco possui backup diário, retenção inicial de sete dias, cópia fora dos volumes ativos e teste de restauração. O intervalo admite perda de até um dia de alterações.

No ambiente acadêmico, o conjunto de backups é produzido com escritas e consumidores pausados. A restauração recupera o conjunto e verifica operações pendentes antes de reabrir o sistema.

**O backup acadêmico entra em manutenção, bloqueia novas admissões, encerra ou registra operações em curso e pausa todos os escritores: APIs de escrita, consumidores, publicadores, coordenadores, indexadores, sincronizadores e tarefas agendadas. Um manifesto identifica o marco, versões e cópias dos seis bancos; outbox, inbox, operações e marcas técnicas integram a cópia. As cópias são cifradas, têm acesso restrito e ficam fora dos volumes ativos por sete dias. A restauração usa broker isolado para não misturar mensagens posteriores ao marco; preserva a origem para diagnóstico e reconstrói entregas necessárias a partir das outboxes e comandos duráveis do conjunto restaurado. Reaplica exclusões e revogações mantidas fora do ponto restaurado, reconcilia operações e só então libera tráfego. As metas propostas são RPO de 24 horas e RTO de duas horas. O teste registra duração, integridade, retomada de SAGA e ausência de duplicações; o RTO só é considerado validado após esse ensaio.**

# 9\. Riscos e limitações

## 9.1 Disponibilidade e complexidade operacional

A consulta de sessão por chamada cria dependência síncrona de Projetos e acesso. Falhas nesse serviço impedem o acesso protegido. O Livro depende de Requisitos para confirmar conteúdo, acesso e situação. Falhas de Rastreabilidade podem degradar a apresentação de relações.

O conjunto inclui seis instâncias de banco, seis serviços de domínio, dois BFFs, Gateway, broker e observabilidade; executar todos na mesma máquina mantém falhas compartilhadas. O orçamento inicial de ensaio é de 8 vCPUs e 24 GB de RAM, com heaps Java, pools de conexão, limites de containers e armazenamento ajustados. Esse orçamento é hipótese de dimensionamento, não mínimo comprovado. A capacidade será medida com embeddings, broker, observabilidade e todos os serviços ativos, usando a carga da seção 2.2; os resultados orientarão ajustes de recursos.

## 9.2 Concorrência e consistência distribuída

Mapa e índice podem ficar defasados. Evidências e sugestões não devem ser aplicadas sem revalidar versão e acesso. A submissão usa relações oficiais, mas não oferece uma transação global entre todos os dados do projeto.

Timeouts, duplicação e reordenação de mensagens exigem estado persistido, idempotência e compensações duráveis. Uma operação com compensação pendente não pode ser apresentada como desfeita.

**O retrato captura atomicamente a revisão do grafo em Rastreabilidade e registra as versões e hashes exatos de conteúdo utilizados, obtidos pelas APIs autorizadas. Não representa uma fotografia global simultânea de todos os bancos. Mudanças posteriores mantêm o retrato e podem exigir nova revisão conforme a política de publicação. Cancelamento e submissão do mesmo requisito disputam controle transacional em Requisitos. Para arquivamento e exclusão, Projetos e acesso mantém uma barreira persistida de admissão por projeto: FECHANDO impede novas operações, os domínios bloqueiam localmente novas escritas e confirmam quiescência depois de drenar as admitidas. O estado final só é confirmado após essas respostas. Uma consulta prévia de estado ou um token antigo, isoladamente, não autoriza escrita durante o fechamento; falhas deixam a operação de fechamento pendente, sem anunciar conclusão.**

## 9.3 Assistência por IA e integrações

Sugestões dependem da cobertura, atualização e qualidade do contexto recuperado e da disponibilidade do cliente e do LLM externo. O sistema preserva o caminho manual; o fallback exibe referências recuperadas, sem produzir uma decisão automática.

A arquitetura não apresenta resultados medidos de qualidade do RAG, custo ou latência. Também não define compatibilidade já comprovada com todos os agentes pessoais. As integrações estão sujeitas aos contratos e às falhas dos provedores.

## 9.4 Fronteiras do MVP

Publicação de documentos em provedores externos, publicação pública do Livro, sincronização bidirecional com mesclagem automática, edição simultânea em tempo real e conectores adicionais são evoluções. O Livro inicial é restrito aos membros autorizados. Modelos físicos, migrações, contratos executáveis e runbooks devem materializar as decisões deste livro. As políticas de reconstrução e autorização estão definidas em nível arquitetural, sem alegação de implementação, compatibilidade ou desempenho já comprovados.

# 10\. Decisões consolidadas e validações

## 10.1 Especificações de implementação e evidências

As decisões deste livro substituem as ambiguidades anteriores. A implementação deve materializar modelos e migrações, OpenAPI dos domínios e BFFs, esquemas de mensagens, matriz de autorização e runbooks em /docs. Permanecem validações de execução: aceitação do cliente Python pelo professor; compatibilidade OAuth/troca de tokens e dos clientes MCP; avaliação RAG com e sem recuperação; testes de carga e atraso; cenários concorrentes de publicação e fechamento; compensação com mensagens duplicadas ou atrasadas; e restauração integral. Nenhum desses resultados é presumido como já demonstrado.

As metas de atualização, a avaliação RAG e as medições de latência e custo devem ser demonstradas por testes e registros. A documentação técnica e os contratos devem permanecer versionados em /docs.

## 10.2 Alinhamento entre as abas

* **US11 adota ações explícitas de submissão, decisão e publicação. Não existe transição direta RASCUNHO → PUBLICADA por dropdown. O painel distingue situação do requisito, estado da candidata e versão publicada e só oferece ações permitidas.**  
* **US26 e as demais abas utilizam Leitor, Repórter, Editor, Aprovador, Mantenedor e Owner. O rótulo Gerência é removido. Convites oferecem apenas papéis concedíveis pelo ator; Owner só é atribuído por criação do projeto ou transferência confirmada, não por convite comum.**  
* **Ata, concepção e US16–US17 adotam DEPENDE\_DE, BLOQUEIA e RELACIONADO\_A no MVP. Rótulos inversos representam leitura da direção, não novas arestas. Tipos adicionais ficam como evolução; regras de precedência, duplicação e ciclos seguem a seção 5.4.**  
* **Histórias, filtros e Livro distinguem situação ATIVO/CANCELADO, estado da candidata e versão publicada. Cancelar retira do Livro corrente; reativar cria nova candidata e exige nova aprovação e publicação. O Livro é restrito aos membros autorizados e utiliza as relações do retrato da publicação.**  
* **A concepção adota assistência externa por MCP: ATHENA prepara e recupera contexto, valida e executa operações autorizadas; o agente externo chama o LLM e gera sugestões. Embeddings locais continuam sendo um modelo de IA no backend, sem geração textual. A demonstração utiliza o cliente Python integrado e permanece sujeita à aceitação acadêmica.**
