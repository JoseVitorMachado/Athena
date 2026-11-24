<div align="center">

# ATHENA

### Inteligência e estratégia para prever o impacto das mudanças em requisitos.

<br>

<img width="100%" alt="ATHENA" src="https://github.com/user-attachments/assets/fc2e4c6b-97cb-435f-abfe-b1563c8bf251" />

<br>

**Plataforma web para gestão, manutenção e rastreabilidade de requisitos de software.**

</div>

---


## 👥 Integrantes

<table>
<tr>
<td>

- **Ana Clara Rocha Gomes**
- **Bárbara Oliveira Fonseca**
- **Fernando Chaves Scarabeli**
- **Jhennifer Hellen Campos Silva**
- **José Vítor Machado de Oliveira**

</td>
</tr>
</table>


---

## Documentação

| O que | Onde |
|---|---|
| Índice da documentação e mapa da Parte 2 | [docs/README.md](docs/README.md) |
| Livro de Arquitetura v1.0 | [docs/arquitetura/livro-de-arquitetura-v1.0.md](docs/arquitetura/livro-de-arquitetura-v1.0.md) |
| Diagramas (componentes, comunicação, SAGA) | [docs/arquitetura/diagramas/](docs/arquitetura/diagramas/) |
| Contratos de API (OpenAPI 3.1) | [docs/api/](docs/api/README.md) |
| Contratos para leitura no navegador | [docs/api/apresentacao.html](docs/api/apresentacao.html) (baixe e abra com dois cliques) |
| Matriz de autorização e catálogo MCP | [matriz-autorizacao.md](docs/api/matriz-autorizacao.md) · [mcp-ferramentas.md](docs/api/mcp-ferramentas.md) |
| Entregas de Empreendedorismo | [docs/empreendedorismo/](docs/empreendedorismo/) |

## Como executar

O sistema roda de duas formas: **Docker Compose** (um comando, ideal para desenvolvimento) e **Kubernetes** local com minikube. As duas sobem os mesmos containers: 6 serviços de domínio, 6 PostgreSQL, RabbitMQ, Gateway, 2 BFFs e 2 clientes Angular; no compose há ainda o Mailpit, caixa de e-mails de desenvolvimento, Prometheus e Grafana (21 no total). Os bancos já sobem com um projeto de demonstração.

### Pré-requisitos

| Para | Precisa de |
|---|---|
| Docker Compose | Docker 24+ com Compose v2. Cerca de 6 GB de RAM livres. |
| Kubernetes | O de cima, mais [minikube](https://minikube.sigs.k8s.io/docs/start/) 1.33+, `kubectl` e `openssl`. Cerca de 12 GB de RAM livres. |

Não é preciso Java, Maven, Node nem Python na máquina: tudo é compilado dentro dos containers.

### 1. Docker Compose

Na raiz do repositório:

```sh
docker compose up -d --build --wait
```

A primeira vez leva alguns minutos (download das imagens base, compilação e o modelo de embeddings do RAG, que vai dentro da imagem do impacto-mcp). O `--wait` só devolve o terminal quando os 21 containers estiverem em execução ou saudáveis. Depois, acesse:

| O quê | Endereço |
|---|---|
| Cliente Gestão | http://localhost:4200 |
| Cliente Livro (conteúdo publicado) | http://localhost:4300 |
| Swagger da autenticação (cadastro, login, recuperação de senha) | http://localhost:8080/api/v1/autenticacao/docs |
| Swagger da Gestão | http://localhost:8080/api/v1/gestao/docs |
| Swagger do Livro | http://localhost:8080/api/v1/livro/docs |
| MCP pelo Gateway | `POST http://localhost:8080/mcp` |
| Caixa de e-mails de desenvolvimento (códigos de recuperação de senha) | http://localhost:8025 |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 (usuário `admin`, senha `athena`) |

**Usuários de demonstração.** Todos com a senha `Athena@2026`, membros do projeto `22222222-2222-4222-8222-222222222222` com o papel indicado no nome:

| E-mail | Papel |
|---|---|
| `owner@athena.dev` | Owner (também dono do workspace "Equipe ATHENA") |
| `mantenedor@athena.dev` | Mantenedor |
| `aprovador@athena.dev` | Aprovador |
| `editor@athena.dev` | Editor |
| `reporter@athena.dev` | Repórter |
| `leitor@athena.dev` | Leitor |

Eles só existem com `ATHENA_USUARIOS_DEMO=true`, ligado no compose e no Kubernetes de desenvolvimento.

**Usar o Swagger.** Na página da autenticação, execute `POST /login` com um dos usuários acima e copie o `accessToken` da resposta. Nas páginas da Gestão e do Livro, clique em **Authorize** e cole o token. Ele vale 15 minutos.

**Teste rápido pelo terminal.** Toda rota da API exige o JWT, exceto cadastro, login, renovação, logout e recuperação de senha:

```sh
API=http://localhost:8080/api/v1
P=22222222-2222-4222-8222-222222222222

# Login: devolve o JWT de acesso (15 min) e põe o refresh em cookie HttpOnly
TOKEN=$(curl -s -X POST $API/autenticacao/login -H 'Content-Type: application/json' \
  -d '{"email":"editor@athena.dev","senha":"Athena@2026"}' | python3 -c 'import sys,json;print(json.load(sys.stdin)["accessToken"])')
AUTH="Authorization: Bearer $TOKEN"

curl -s -H "$AUTH" $API/gestao/workspaces                               # meus workspaces
curl -s -H "$AUTH" "$API/gestao/projetos"                               # meus projetos
curl -s -H "$AUTH" "$API/gestao/projetos/$P/requisitos?q=export"        # lista com filtros
curl -s -H "$AUTH" $API/gestao/projetos/$P/requisitos/11111111-1111-4111-8111-111111111112   # detalhe com _links e _actions
curl -s -H "$AUTH" $API/gestao/projetos/$P/mapa                         # mapa (projeção CQRS)
curl -s -H "$AUTH" $API/livro/projetos/$P/livro/requisitos              # Livro: só o publicado
curl -s -H "$AUTH" $API/gestao/notificacoes                             # caixa de notificações

# MCP: busca de contexto no índice RAG (só conteúdo publicado)
curl -s -H "$AUTH" -H 'Content-Type: application/json' -H 'Accept: application/json, text/event-stream' \
  -d '{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":"buscar_contexto","arguments":{"projetoId":"'$P'","q":"exportar documento"}}}' \
  http://localhost:8080/mcp

curl -i $API/gestao/projetos                                            # sem token: 401
curl -i -H "$AUTH" $API/projetos/$P/requisitos                          # serviço de domínio não é exposto: 404
```

Para parar: `docker compose down` (mantém os dados). Para apagar também os volumes dos bancos: `docker compose down -v`. Senhas e portas podem ser trocadas copiando `.env.example` para `.env`.

**Redes do compose (isolamento entre camadas):**

| Rede | Quem está nela | Acesso externo |
|---|---|---|
| `edge` | clientes Gestão e Livro, Gateway, Mailpit, Prometheus e Grafana | portas 4200, 4300 e 8080; 8025, 9090 e 3000 só em `127.0.0.1` |
| `app` | Gateway, BFFs, serviços de domínio, RabbitMQ, Mailpit (SMTP), Prometheus e Grafana | nenhum (`internal`) |
| `dados-<serviço>` | um serviço e o seu banco | nenhum (`internal`) |

Cada banco só é alcançável pelo próprio serviço; o Gateway recebe o tráfego da aplicação e o monitoramento fica disponível só no localhost.

### Demonstrações

O painel **ATHENA — visão geral** no Grafana permite acompanhar as transições da SAGA e as exclusões em andamento.

Os comandos abaixo usam `API`, `P` e `AUTH` do teste rápido. Para acompanhar o que acontece, deixe os logs abertos noutro terminal: `docker compose logs -f requisitos revisoes rastreabilidade`.

**SAGA de submissão com falha e compensação.** A submissão é orquestrada por Requisitos: Rastreabilidade cria o retrato e Revisões abre o lote. Para forçar a falha, Revisões passa a criar o lote e "perder" a resposta:

```sh
docker compose exec revisoes wget -qO- --header 'content-type: application/json' \
  --post-data '{"abrirLote":"SILENCIAR"}' http://127.0.0.1:8080/api/v1/internal/falhas
```

Submeta um requisito pela Gestão (ação `submeter` do detalhe). Depois de 3 tentativas de 10 s, os logs de Requisitos mostram `EM_ANDAMENTO -> COMPENSANDO`, o cancelamento do lote e do retrato, e por fim `COMPENSADA`. A candidata volta a `RASCUNHO`. O modo `RECUSAR` faz Revisões recusar na hora. Para voltar ao normal, use `{"abrirLote":"NENHUMA"}`.

**Consistência eventual do mapa (CQRS).** A escrita de relações é síncrona, e a projeção do mapa é atualizada só por eventos. Para atrasar a projeção:

```sh
docker compose exec rastreabilidade wget -qO- --header 'content-type: application/json' \
  --post-data '{"atrasoSegundos":6}' http://127.0.0.1:8080/api/v1/internal/projecao
```

Crie uma relação (`POST $API/gestao/projetos/$P/relacoes`) e consulte o mapa logo em seguida: ele aparece como `ATUALIZACAO_PENDENTE` e, após alguns segundos, `ATUALIZADO` com a aresta nova. Volte com `{"atrasoSegundos":0}`.

**Ciclo de vida completo de um requisito.** Na Gestão: editar a candidata, submeter, aprovar (como `aprovador@athena.dev`) e publicar. O requisito passa a aparecer no Livro e no `buscar_contexto` do MCP. Ao cancelá-lo (`POST .../cancelamentos` com `{"confirmacao":true}`), ele sai do Livro e do índice RAG. Ao reativá-lo, ele só volta depois de uma nova aprovação e publicação.

**Notificações.** Mencionar alguém num comentário, concluir uma submissão (Aprovadores são avisados) e decidir a revisão (quem submeteu é avisado) geram entradas em `GET $API/gestao/notificacoes`. Convites, transferências de propriedade e mudanças de papel também geram.

**Exclusão definitiva de projeto (processo distribuído).** O Owner arquiva o projeto e pede a exclusão confirmando o nome (`POST $API/gestao/projetos/{p}/exclusoes`, com `If-Match` e `Idempotency-Key`). Projetos e acesso coordena `FECHANDO -> EXCLUINDO -> CONCLUIDA`. Cada serviço de domínio bloqueia o projeto e confirma quando não há operação em curso, depois apaga os dados e guarda só uma marca técnica que impede mensagens atrasadas de recriá-los. O andamento fica em `GET .../exclusoes/{id}`. Para ver a retentativa, rode `docker compose stop integracoes` antes de pedir a exclusão: ela fica em `FECHANDO` aguardando esse serviço. Depois de `docker compose start integracoes`, o comando reenviado a cada 15 s é entregue e a exclusão conclui. Os logs de `projetos-acesso` mostram cada transição.

**Confirmação humana para o agente (MCP).** O agente pode decidir revisões e publicar pelas ferramentas `decidir_revisao` e `publicar_versao`, mas só com um comprovante humano. O fluxo: `preparar_confirmacao` calcula o hash da ação; a pessoa confirma na interface do ATHENA, redigitando a senha (`POST $API/gestao/projetos/$P/revisoes/{lote}/confirmacoes-decisao` ou `.../requisitos/{r}/confirmacoes-publicacao`); o comprovante recebido vale 5 minutos, uma única vez, e só para aquela ação, versão e usuário. Sem ele, ou com o comprovante de outra pessoa ou de outra decisão, o domínio responde 403. Todas as ferramentas do MCP conferem a cada chamada se o usuário é membro do projeto. O Gateway descarta cabeçalhos internos enviados pelo cliente.

**Recuperação de senha.** `POST $API/autenticacao/recuperacao` com `{"email":"..."}` responde sempre 202. O código de 6 dígitos chega em http://localhost:8025 e vale 10 minutos. A conclusão (`POST $API/autenticacao/recuperacao/conclusao` com `email`, `codigo` e `novaSenha`) encerra todas as sessões da conta.

**Testar do zero sem perder os dados locais:**

```sh
docker compose stop
docker compose -p athena-limpo up -d --build --wait
# ... testes ...
docker compose -p athena-limpo down -v
docker compose up -d --wait
```

### 2. Kubernetes (minikube)

Os comandos abaixo valem para Linux e macOS. No Windows (PowerShell), troque o `eval` do passo 2 por `& minikube -p athena docker-env --shell powershell | Invoke-Expression`.

**1. Criar o cluster** com Calico (para as NetworkPolicies valerem) e runtime Docker (para construir as imagens direto no cluster):

```sh
minikube start -p athena --driver=docker --container-runtime=docker --cpus=6 --memory=12g --cni=calico
minikube -p athena addons enable ingress
minikube -p athena addons enable metrics-server   # opcional, usado pelo HPA
```

**2. Construir as imagens dentro do minikube.** No mesmo terminal:

```sh
eval $(minikube -p athena docker-env)
docker compose build
```

**3. Criar o namespace e o Secret da chave de assinatura do JWT.** A chave é gerada na hora e nunca fica no repositório:

```sh
kubectl apply -f k8s/00-namespace.yaml
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out /tmp/athena-jwt.pem
kubectl -n athena create secret generic athena-jwt --from-file=chave-privada.pem=/tmp/athena-jwt.pem
rm /tmp/athena-jwt.pem
```

**4. Aplicar os manifests** e esperar tudo ficar pronto (de 3 a 5 minutos):

```sh
kubectl apply -f k8s/
kubectl -n athena wait --for=condition=Available deployment --all --timeout=600s
kubectl -n athena get pods
```

**5. Acessar pelo Ingress:**

```sh
minikube -p athena ip     # por exemplo 192.168.49.2
```

| O quê | Endereço |
|---|---|
| Cliente Gestão | `http://<ip>/gestao/` |
| Cliente Livro | `http://<ip>/livro/` |
| API | `http://<ip>/api/v1/gestao/...` e `http://<ip>/api/v1/livro/...` |
| MCP | `POST http://<ip>/mcp` |

No macOS e no Windows o IP do minikube não é acessível pelo host: rode `minikube -p athena tunnel` num terminal separado e use `http://127.0.0.1` no lugar de `http://<ip>`.

**6. Demonstrar a escalabilidade.** O BFF Gestão não tem estado, então pode ter várias réplicas. Cada resposta traz no cabeçalho `X-Athena-Instancia` o nome do pod que a atendeu:

```sh
kubectl -n athena scale deployment/bff-gestao --replicas=3
kubectl -n athena rollout status deployment/bff-gestao
kubectl -n athena get pods -l app.kubernetes.io/name=bff-gestao

for i in $(seq 1 12); do
  curl -s -D - -o /dev/null "http://$(minikube -p athena ip)/api/v1/gestao/projetos/22222222-2222-4222-8222-222222222222/requisitos" | grep -i x-athena-instancia
done | sort | uniq -c
```

As requisições se distribuem entre as três réplicas. Para voltar: `--replicas=1`. Opcionalmente, o escalonamento automático por CPU fica em `k8s/extras/hpa-bff-gestao.yaml` (`kubectl apply -f k8s/extras/hpa-bff-gestao.yaml`); ele não é aplicado por padrão porque desfaz um `kubectl scale` manual.

**7. Desligar:**

```sh
kubectl delete namespace athena   # apaga os recursos e os volumes do ATHENA
minikube -p athena stop           # ou "minikube -p athena delete" para remover o cluster
```

**O que há em `k8s/`:**

| Arquivo | Conteúdo |
|---|---|
| `00-namespace.yaml` | Namespace `athena` |
| `01-configmap.yaml` | ConfigMap `athena-config`: endereços internos, nomes dos bancos, migrações |
| `02-secrets.yaml` | Secrets das credenciais dos bancos e do RabbitMQ (valores de desenvolvimento, iguais aos do `.env.example`) |
| `10-bancos.yaml` | 6 StatefulSets PostgreSQL com Service headless e PersistentVolumeClaim de 1 Gi |
| `11-rabbitmq.yaml` | StatefulSet do RabbitMQ com Service headless e volume |
| `20-servicos-dominio.yaml` | Deployments e Services ClusterIP dos 6 serviços de domínio |
| `30-bffs.yaml` | Deployments e Services dos BFFs Gestão e Livro |
| `40-gateway-e-clientes.yaml` | Gateway e os dois clientes Angular |
| `45-ingress.yaml` | Ingress: `/gestao`, `/livro` para os clientes; `/api` e `/mcp` para o Gateway |
| `50-network-policies.yaml` | Isolamento: banco só do seu serviço, broker só do domínio, domínio só de BFF e Gateway |
| `extras/hpa-bff-gestao.yaml` | HPA opcional do BFF Gestão |
| `extras/mailpit.yaml` | Caixa de e-mails de desenvolvimento (SMTP para a recuperação de senha) |
| `extras/monitoramento.yaml` | Prometheus e Grafana opcionais; acesso ao Grafana com `kubectl -n athena port-forward svc/grafana 3000:3000` |

Todos os containers têm requests e limits, usuário sem privilégios nas aplicações (PostgreSQL e RabbitMQ seguem o modelo de usuários das imagens oficiais) e probes de startup, readiness e liveness. A liveness dos serviços não depende do banco, para uma falha de dependência não reiniciar tudo em cascata.

### Dockerfiles e o ganho do multi-stage

Cada aplicação tem o seu Dockerfile. Os bancos e o RabbitMQ também têm imagem própria, em `infra/`: o PostgreSQL acrescenta script de inicialização, parâmetros e healthcheck, e o RabbitMQ já sobe com as exchanges `athena.commands` e `athena.events`.

As aplicações usam build em dois estágios: o primeiro tem as ferramentas de compilação, e a imagem final leva só o necessário para executar.

| Aplicação | Estágio de build | Imagem final | O que fica de fora |
|---|---|---|---|
| Serviços Java, BFFs e Gateway | Maven + JDK 17: 868 MB | JRE 17 + jar: 370 MB | Maven, compilador, código-fonte e dependências de build |
| Clientes Angular | Node 24 + `node_modules`: 599 MB | nginx + arquivos estáticos: 90 MB | Node, `node_modules` e código TypeScript |
| impacto-mcp | Python + pip: 244 MB | Python + venv: 237 MB | pip, cache de download e ferramentas de instalação |

Além do tamanho, a imagem final tem menos pacotes para manter atualizados e não oferece compilador nem gerenciador de pacotes a quem invadir o container.

### O que está implementado

- **Autenticação e autorização.** Cadastro, login, renovação com rotação do refresh token, logout e recuperação de senha. JWT RS256 de 15 minutos, validado no Gateway (sessão ativa a cada chamada) e nos BFFs (papel atual no projeto a cada chamada).
- **Mensageria confiável.** Outbox com confirmação do broker e retentativas (2, 10, 30 e 60 s); inbox idempotente; filas de retentativa e DLQ por serviço.
- **SAGA orquestrada de submissão** com prazos, compensação e marca durável de cancelamento.
- **CQRS do mapa de rastreabilidade**, com projeção atualizada por eventos e estados `ATUALIZADO`, `ATUALIZACAO_PENDENTE` e `RECONSTRUINDO`.
- **Índice RAG** com `multilingual-e5-small` (ONNX, dentro da imagem), alimentado só por conteúdo publicado e exposto pela tool MCP `buscar_contexto`.
- **Exclusão definitiva de projeto** coordenada entre os seis serviços, com barreira, confirmação de quiescência, limpeza idempotente, retentativa e marca técnica.
- **Gestão:** workspaces, projetos (arquivar e restaurar), membros, convites e transferência de propriedade; épicos; requisitos com filtros, metadados, cancelamento e reativação; comentários com menções e âncoras; revisão e publicação; notificações; HATEOAS (`_links` e `_actions` conforme estado e papel).

Ainda não implementado: integrações com GitHub, OpenProject e Google Drive (o serviço `integracoes` é só esqueleto) e a autorização OAuth própria do agente MCP (hoje o agente usa o JWT do usuário).

### Contratos de API

**Ler os contratos sem instalar nada:** abra `docs/api/apresentacao.html` no navegador. O arquivo funciona offline.

**Validar os contratos e regenerar os artefatos** (Node.js 24 e npm, na raiz do repositório):

```sh
npm ci
npm run validar        # lint, bundles em docs/api/dist, matriz, catálogo MCP e testes das regras
npm run apresentacao   # recria docs/api/apresentacao.html a partir dos bundles
```

**Abrir no Swagger UI** (com Docker), também na raiz:

```sh
docker run --rm -p 127.0.0.1:8090:8080 \
  -e SWAGGER_JSON=/contratos/bff-gestao.bundled.yaml \
  -v "$PWD/docs/api/dist:/contratos:ro" \
  docker.swagger.io/swaggerapi/swagger-ui
```

Acesse http://localhost:8090 (a porta 8080 fica com o Gateway). Para ver outro contrato, troque `bff-gestao.bundled.yaml` por outro arquivo de `docs/api/dist/`. O host `athena.example.invalid` dos contratos é ilustrativo.

---

## Visão de produto


### Problema

Durante o desenvolvimento de software, requisitos, histórias de usuário, regras de negócio, critérios de aceitação e outros artefatos estão relacionados entre si. Quando um requisito é alterado, essa mudança pode impactar outros requisitos, funcionalidades, cenários de teste e artefatos utilizados pela equipe. Entretanto, essas relações frequentemente permanecem implícitas ou distribuídas entre diferentes documentos, ferramentas e conversas. Como consequência, a identificação dos impactos de uma mudança pode depender da memória e do conhecimento de pessoas específicas da equipe. Isso pode fazer com que alterações sejam incorporadas apenas parcialmente, deixando requisitos ou artefatos desatualizados e gerando retrabalho, perda de tempo e consumo adicional de recursos.

### Público-alvo

O ATHENA é voltado principalmente para profissionais e equipes envolvidos na especificação, desenvolvimento e manutenção de software, incluindo:

- Analistas de requisitos;
- Product Owners;
- Desenvolvedores;
- Profissionais de qualidade e testes;
- Gestores de projetos;
- Equipes de desenvolvimento de software.

### Proposta de valor

O ATHENA busca ajudar equipes de software a identificar o que pode precisar ser revisado quando um requisito é alterado.

A plataforma funciona como uma camada complementar às ferramentas já utilizadas pela equipe, organizando documentos e requisitos, explicitando as relações entre eles e apoiando a identificação e análise dos possíveis impactos de mudanças.

Seu objetivo não é substituir ferramentas como Google Drive, GitHub, Jira ou OpenProject, mas conectar informações e artefatos já existentes, tornando dependências e impactos mais visíveis.

Com isso, o ATHENA pretende reduzir:

- A dependência da memória individual;
- A consulta manual a múltiplas ferramentas;
- O risco de alterações serem propagadas apenas parcialmente;
- Inconsistências entre requisitos e outros artefatos;
- O esforço necessário para analisar mudanças.

### Descrição do MVP

O MVP do ATHENA deverá permitir o fluxo principal de manutenção e análise de requisitos:

1. Cadastrar ou importar documentos e requisitos;
2. Registrar relações entre requisitos;
3. Visualizar relações por meio de rastreabilidade;
4. Alterar e versionar um requisito;
5. Analisar possíveis impactos provocados pela alteração;
6. Apresentar sugestões de itens potencialmente afetados;
7. Permitir que o usuário aceite, rejeite ou corrija as sugestões;
8. Registrar a decisão tomada durante a revisão.

#### Funcionalidades previstas para o MVP

- Cadastro e importação de requisitos;
- Requisitos estruturados;
- Registro manual de relações;
- Mapa de rastreabilidade;
- Histórico e versionamento;
- Comparação entre versões;
- Análise assistida de impacto;
- Fluxo básico de revisão;
- Integração inicial com uma fonte de documentação;
- Integração inicial com uma ferramenta de gestão do trabalho.
- Sincronização com ferramentas populares, como o google drive, open project e o github.


### Tecnologias utilizadas

> As tecnologias ainda estão em processo de definição e poderão ser refinadas durante o desenvolvimento.

#### Front-end

- Angular;
- TypeScript.

#### Back-end

- Java 17 e Spring Boot (cinco serviços de domínio, Gateway e os dois BFFs);
- Python (serviço de Análise de Impacto/MCP);
- RabbitMQ (comandos e eventos entre serviços).

#### Banco de dados

- PostgreSQL, uma instância por serviço;
- pgvector no serviço de Análise de Impacto/MCP.

#### APIs

- APIs HTTP;
- OpenAPI / Swagger.

#### Inteligência Artificial

A inteligência artificial será utilizada para apoiar:

- Análise de alterações em requisitos;
- Identificação de possíveis relações;
- Detecção de problemas de qualidade;
- Sugestão de possíveis impactos de mudanças.

As sugestões produzidas pela IA não serão aplicadas automaticamente. A decisão final permanecerá sob responsabilidade dos usuários.

#### Infraestrutura e DevOps

- Docker;
- Kubernetes;
- Git;
- GitHub;
- Possível utilização de CI/CD;
- SonarQube;
- Prometheus;
- Grafana.

---

link do Video: https://youtu.be/Dv1k_tkIz-U?si=jLkPJ-GIG6OohDBa

## Concepção do sistema

A **ATHENA** é uma proposta de plataforma voltada à **gestão inteligente de requisitos de software**, com foco na automação da rastreabilidade e na análise de impacto de mudanças em requisitos.

Seu objetivo é apoiar equipes de software na compreensão das relações existentes entre **requisitos, regras de negócio, critérios de aceitação, testes, documentos e demais artefatos** associados ao desenvolvimento de um sistema.


### Problema e motivação

<img width="559" height="454" alt="image" src="https://github.com/user-attachments/assets/9714ecc6-c3ba-469d-9fda-3d5f65574e91" />

Requisitos de software não permanecem estáticos durante todo o desenvolvimento de um sistema. Eles evoluem conforme surgem novas necessidades, alterações de negócio, decisões de projeto e mudanças no próprio produto.

Essa evolução cria uma rede de dependências entre diferentes artefatos do projeto, como histórias de usuário, regras de negócio, critérios de aceitação, testes, documentos, tarefas, código e requisitos relacionados.

O problema surge quando essas relações não estão explícitas ou não são mantidas de forma organizada. Uma alteração aparentemente simples em um requisito pode exigir a revisão de diversos outros elementos do projeto.

Por exemplo, uma mudança em um requisito relacionado à autenticação pode exigir a revisão de critérios de aceitação, regras de negócio, testes e outros requisitos relacionados.

Quando a rastreabilidade entre esses elementos não está clara, os efeitos de uma mudança podem não ser percebidos imediatamente. Como consequência, podem surgir inconsistências, retrabalho e atrasos durante o desenvolvimento.

Outro ponto importante é que as informações de um projeto normalmente ficam distribuídas em diferentes ferramentas e documentos. Requisitos podem estar em uma ferramenta de gestão, código em um repositório, documentos em serviços de armazenamento e decisões importantes podem permanecer apenas no conhecimento de determinados integrantes da equipe.

Nesse cenário, parte do contexto do projeto pode se perder, principalmente quando:

1. as informações estão espalhadas em fontes diferentes;
2. parte do conhecimento depende de pessoas específicas;
3. mudanças são revisadas de forma incompleta;
4. inconsistências são percebidas apenas posteriormente, gerando retrabalho.


A ATHENA surge a partir desse problema: **tornar visíveis as relações entre os elementos de um projeto para que a equipe consiga compreender melhor o impacto provocado por uma mudança em um requisito**.

---

### O contexto de um projeto se perde quando as relações não estão visíveis

Um dos principais problemas identificados é a dificuldade de preservar o contexto completo de um projeto quando requisitos, documentos, regras, testes e decisões permanecem separados.

A ausência de relações explícitas entre esses elementos gera quatro situações principais:

#### Informações ficam em fontes diferentes

Os dados necessários para compreender um requisito podem estar distribuídos em diferentes ferramentas, documentos e ambientes.

#### Parte do conhecimento depende de pessoas específicas

Quando determinadas relações ou decisões não estão documentadas, o entendimento do sistema passa a depender da memória dos integrantes que participaram dessas decisões.

#### Mudanças podem ser revisadas de forma incompleta

Sem uma visão clara das dependências, uma equipe pode modificar um requisito sem perceber que outros artefatos também precisam ser revisados.

#### Inconsistências aparecem posteriormente

Quando um impacto não é identificado no momento da alteração, o problema pode surgir apenas durante a implementação, os testes ou etapas posteriores, aumentando a possibilidade de retrabalho.

A ATHENA busca reduzir esse problema tornando as relações entre os elementos do projeto mais visíveis e acessíveis.

---

### Qual é a ideia?

A proposta da ATHENA é desenvolver uma ferramenta para **automação de rastreabilidade e análise de impacto em requisitos de software**.

A plataforma deverá reunir informações sobre os requisitos e suas relações, permitindo que a equipe visualize de maneira mais clara quais elementos podem ser afetados quando uma alteração acontece.

Em vez de depender apenas da memória individual dos membros da equipe, a solução busca registrar essas relações de maneira estruturada, tornando o conhecimento sobre os requisitos mais acessível e compartilhado.

A ideia central pode ser resumida da seguinte forma:

> Quando um requisito muda, a ATHENA ajuda a equipe a descobrir o que precisa ser revisado.

A ferramenta não pretende substituir a análise realizada pelos profissionais responsáveis pelo projeto. Seu papel é apoiar esse processo, apresentando relações, possíveis impactos e informações relevantes para a tomada de decisão.

---

### Esboço da solução
<img width="1492" height="573" alt="image" src="https://github.com/user-attachments/assets/13d9f614-9128-4446-a34e-32a9000f0afb" />



A ATHENA deverá apoiar o fluxo de análise de requisitos por meio de cinco ações principais.

#### 1. Registrar ou importar requisitos e documentos

A plataforma permitirá centralizar requisitos e documentos relevantes para o projeto, criando uma base de informações que poderá ser utilizada nas análises posteriores.

#### 2. Relacionar requisitos, critérios e regras

Os diferentes elementos do projeto poderão ser relacionados entre si, permitindo representar dependências e conexões existentes entre requisitos, critérios de aceitação, regras de negócio e outros artefatos.

#### 3. Detectar uma mudança em um requisito

Quando um requisito for alterado, o sistema deverá identificar essa mudança e utilizar as relações existentes para iniciar a análise de impacto.

#### 4. Sugerir possíveis impactos com justificativa

A ATHENA deverá apresentar os elementos que podem ser afetados pela alteração e fornecer uma justificativa para as sugestões apresentadas.

O objetivo não é determinar automaticamente o que deve ser feito, mas oferecer informações que ajudem a equipe a compreender quais pontos precisam ser revisados.

#### 5. Registrar revisão humana e decisão

As sugestões apresentadas pelo sistema deverão passar pela análise dos profissionais responsáveis pelo projeto.

A decisão tomada pela equipe poderá ser registrada, mantendo um histórico das revisões e das decisões realizadas.

Dessa forma, a ATHENA atua como uma ferramenta de apoio: **o sistema auxilia na identificação e análise dos impactos, enquanto a decisão final permanece com a equipe responsável pelo projeto**.

---

### Fundamentação do problema e da solução

A proposta apresentada para a ATHENA é sustentada por quatro trabalhos relacionados à análise de impacto, rastreabilidade, identificação de anomalias em requisitos e uso de modelos de linguagem no desenvolvimento de software.

#### Arora et al. (2015)

O trabalho **“Change impact analysis for natural language requirements: an NLP approach”** aborda a análise de impacto de mudanças em requisitos escritos em linguagem natural.

A relação com a ATHENA está na necessidade de identificar quais elementos podem ser afetados quando um requisito é alterado. Essa ideia sustenta uma das principais funções propostas para a plataforma: ajudar a equipe a compreender as consequências de uma mudança antes que seus efeitos apareçam em etapas posteriores do desenvolvimento.

#### Rubasinghe, Meedeniya e Perera (2018)

O trabalho **“Traceability management with impact analysis in DevOps based software development”** relaciona a gestão de rastreabilidade com a análise de impacto em ambientes de desenvolvimento de software baseados em DevOps.

Essa referência está diretamente ligada ao problema tratado pela ATHENA: requisitos e demais artefatos de um projeto possuem relações importantes que precisam ser identificadas e mantidas para que uma alteração possa ser analisada de forma adequada.

#### Pereira, Costa e Parreira Júnior (2024)

O estudo **“A comparative study of tools for anomaly detection in software requirements”** trata da detecção de anomalias em requisitos de software.

Esse trabalho contribui para a proposta de apoiar equipes na identificação de problemas presentes na documentação de requisitos, ampliando a capacidade de revisão e reduzindo a possibilidade de inconsistências permanecerem sem identificação durante a evolução do projeto.

#### Tabarsi et al. (2026)

O trabalho **“LLMs’ reshaping of people, processes, products, and society in software development”** discute o uso de modelos de linguagem de grande escala no contexto do desenvolvimento de software.

Na proposta da ATHENA, recursos baseados em inteligência artificial podem ser utilizados como apoio à análise dos requisitos e dos possíveis impactos de mudanças. Entretanto, a revisão humana permanece como parte essencial do processo.


Por isso, a plataforma adota como princípio que **o sistema apoia a análise, mas a decisão final continua com as pessoas responsáveis pelo projeto**.

| Referência | Relação com a ATHENA |
|---|---|
| Arora et al. (2015) | Análise de impacto em requisitos escritos em linguagem natural |
| Rubasinghe, Meedeniya e Perera (2018) | Rastreabilidade e análise de impacto em ambientes de desenvolvimento |
| Pereira, Costa e Parreira Júnior (2024) | Detecção de anomalias em documentação de requisitos |
| Tabarsi et al. (2026) | Uso de LLMs no desenvolvimento de software com participação e revisão humana |

Em conjunto, esses trabalhos fundamentam a proposta de uma plataforma capaz de relacionar requisitos, apoiar a análise de mudanças, identificar possíveis problemas e fornecer informações para auxiliar a equipe durante a tomada de decisão.


---

### Impacto social esperado

O impacto esperado da ATHENA está relacionado à ampliação do acesso ao conhecimento sobre os requisitos de um projeto.

Ao tornar relações e dependências mais visíveis, a plataforma busca diminuir a dependência do conhecimento individual e facilitar a compreensão do projeto por diferentes membros de uma equipe.

#### Quem é beneficiado?

A proposta pode beneficiar principalmente:

- analistas de requisitos;
- Product Owners;
- desenvolvedores;
- profissionais de QA;
- gestores;
- equipes acadêmicas envolvidas no desenvolvimento de software.

#### De que forma?

Os principais benefícios esperados são:

- diminuição de pontos cegos na documentação;
- redução de retrabalho causado por alterações incompletas;
- apoio a uma tomada de decisão mais fundamentada e transparente;
- menor dependência da memória individual de membros específicos da equipe;
- maior visibilidade das relações existentes entre requisitos e outros artefatos;
- maior facilidade para identificar quais elementos precisam ser revisados após uma mudança.

#### Como medir o impacto?

O impacto da solução poderá ser acompanhado por indicadores como:

- **Tempo para identificar impactos:** tempo necessário para localizar os elementos que precisam ser revisados após uma alteração.
- **Cobertura de rastreabilidade:** proporção de requisitos e artefatos que possuem relações registradas no sistema.
- **Sugestões consideradas úteis pela equipe:** quantidade ou percentual de sugestões que efetivamente contribuíram para o processo de revisão.
- **Taxa de assertividade da IA:** percentual de sugestões de impacto e de anomalias aceitas pela equipe durante a revisão humana.

Essas métricas permitem avaliar se a ferramenta está contribuindo para tornar o processo de manutenção de requisitos mais claro, rastreável e útil para os profissionais envolvidos.

---

### Referências

1. ARORA, C. et al. **Change impact analysis for natural language requirements: an NLP approach.** IEEE RE, 2015.

2. RUBASINGHE, I.; MEEDENIYA, D.; PERERA, I. **Traceability management with impact analysis in DevOps based software development.** ICACCI, 2018.

3. PEREIRA, G.; COSTA, H.; PARREIRA JÚNIOR, P. **A comparative study of tools for anomaly detection in software requirements.** SBQS, 2024.

4. TABARSI, A. et al. **LLMs’ reshaping of people, processes, products, and society in software development.** *Empirical Software Engineering*, 2026.
