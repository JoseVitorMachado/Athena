import fs from 'node:fs';
import path from 'node:path';
import YAML from 'yaml';
const dir=path.resolve('docs/api');const cache=new Map();
function load(file){if(!cache.has(file))cache.set(file,YAML.parse(fs.readFileSync(file,'utf8')));return cache.get(file);}
function resolve(value,file){
 if(Array.isArray(value))return value.map(x=>resolve(x,file));
 if(!value||typeof value!=='object')return value;
 if(value.$ref){
  const [rel,frag]=value.$ref.split('#');const f=rel?path.resolve(path.dirname(file),rel):file;
  let node=load(f);for(const key of frag.slice(1).split('/'))node=node[key.replace(/~1/g,'/').replace(/~0/g,'~')];
  const result=resolve(node,f);for(const [k,v] of Object.entries(value))if(k!=='$ref')result[k]=resolve(v,file);return result;
 }
 return Object.fromEntries(Object.entries(value).map(([k,v])=>[k,resolve(v,file)]));
}
const tools=[];
for(const name of ['requisitos','rastreabilidade','revisoes','projetos-acesso','integracoes','impacto-mcp']){
 const file=path.join(dir,name+'.yaml');const d=load(file);
 for(const [route,ops] of Object.entries(d.paths))for(const [method,original] of Object.entries(ops)){
  if(!route.includes('{projetoId}')||route.includes('/internal/')||original['x-interno']||route.includes('/confirmacoes-'))continue;
  const o=resolve(original,file);const properties={},required=[];
  for(const p of o.parameters||[]){
   const key=p.name==='If-Match'?'ifMatch':p.name==='Idempotency-Key'?'chaveIdempotencia':p.name;
   properties[key]={...p.schema,description:(p.description||'')+' Encaminhar em '+p.in+' como '+p.name+'.'};
   if(p.required)required.push(key);
  }
  if(o.requestBody){
   properties.corpo=o.requestBody.content['application/json'].schema;required.push('corpo');
   if(method==='post'&&route.endsWith('/decisoes')||method==='post'&&route.endsWith('/publicacoes')){
    properties.corpo.required=[...new Set([...(properties.corpo.required||[]),'comprovanteHumano'])];
   }
  }
  const inputSchema={$schema:'https://json-schema.org/draft/2020-12/schema',type:'object',properties,required,additionalProperties:false};
  tools.push({name:o.operationId,description:o.description,inputSchema,'x-dominio':name,'x-operacao-dominio':o.operationId,'x-metodo':method.toUpperCase(),'x-rota':route,'x-projeto':'projetoId','x-papel-minimo':o['x-papel-minimo'],'x-escopo':o['x-escopo-mcp'],...(o['x-decidir']?{'x-decidir':o['x-decidir']}:{})});
 }
}
const intro=`# Ferramentas MCP — ATHENA 1.0.0

Catálogo de contrato proposto para /mcp (Streamable HTTP), separado de OpenAPI (§§6.1, 6.11 e 7.5). Nomes correspondem aos operationIds de domínio. As ferramentas não são uma implementação MCP nem comprovam compatibilidade dos clientes.

Cada chamada revalida usuário, autorização independente da interface, projeto delegado, escopo e papel atual. Troca o token para aud do serviço proprietário, preservando sujeito/projeto/delegação e reduzindo escopos. Não repassa token MCP bruto. Revogação bloqueia chamadas e entregas posteriores inclusive em stream já aberto. Não existe tool de autoemitir confirmação humana. Cada emissão só ocorre na tela ATHENA autenticada e no serviço responsável.

Escopos são independentes: read consulta; write sugestões/conteúdo/relações/submissões; approve decide; publish publica; admin administra; delete-project exclui fisicamente e exige Owner. Um escopo não implica outro. Decidir e publicar exigem corpo.comprovanteHumano emitido em tela, uso único, cinco minutos e hash da ação, consumido na transação do efeito. Revalidar versão, papel e acesso. Não aceitar declaração do agente como prova.

O cliente externo gera texto. criarAnalise prepara contexto e registra operação; criarSugestaoAnalise propõe o canal de retorno de saída não confiável e depende da decisão indicada. Similaridade não é probabilidade. Sugestão não cria relação oficial sem ação autorizada. Políticas x-decidir permanecem pendentes e não concedem permissão. Em especial editarPendencia é bloqueada até definir responsável.

Parâmetros de caminho e consulta aparecem no nível superior; payload em corpo; If-Match em ifMatch; Idempotency-Key em chaveIdempotencia. Campos padrão OAuth não são tools. O esquema expandido é autocontido, draft 2020-12. O projeto das tools é sempre projetoId explícito; conta/workspace/autenticação e endpoints exclusivos internos ficam fora da delegação de projeto. Roteamento MCP é feito pelo servidor; x-rota é destino interno, nunca URL para repassar token MCP.

Erros: devolver falha MCP estruturada com codigo, traceId e status de domínio, sem marcar efeito confirmado após timeout. Idempotência recupera a operação já existente. Comunicação SAGA/eventos pertence a /docs/mensageria, fora desta entrega REST. Campos propostos e pendências estão em PROPOSTAS.md.

## Índice

| Tool | Domínio | Papel mínimo | Escopo | Operação de domínio |
|---|---|---|---|---|
`;
let text=intro+tools.map(t=>`| ${t.name} | ${t['x-dominio']} | ${t['x-papel-minimo']} | ${t['x-escopo']} | ${t['x-metodo']} ${t['x-rota']} |`).join('\n')+'\n';
for(const t of tools)text+='\n## '+t.name+'\n\nPapel mínimo: **'+t['x-papel-minimo']+'**; escopo **'+t['x-escopo']+'**; projeto **projetoId**.\n\n'+t.description+'\n\n'+(t['x-decidir']?'Decisão pendente: '+t['x-decidir']+'\n\n':'')+'```json\n'+JSON.stringify(t.inputSchema,null,2)+'\n```\n';
fs.writeFileSync(path.join(dir,'mcp-ferramentas.md'),text);
fs.writeFileSync('docs/seguranca/tools-mcp.json',JSON.stringify({versao:'1.0.0',tools},null,2)+'\n');
console.log(`${tools.length} tools com entrada JSON Schema expandida e mapeamento de autorização.`);
