const fs = require('node:fs');
const path = require('node:path');
const Ajv = require('ajv/dist/2020');
const addFormats = require('ajv-formats');
const inventory = JSON.parse(fs.readFileSync(path.resolve('scripts/inventario.json'), 'utf8'));
const methods = ['get','post','patch','delete','put','options','head','trace'];
const publicPaths = new Set([
 'post /api/v1/autenticacao/cadastro', 'post /api/v1/autenticacao/login',
 'post /api/v1/autenticacao/recuperacao','post /api/v1/autenticacao/recuperacao/conclusao',
 'get /oauth2/jwks', 'get /.well-known/oauth-authorization-server','get /.well-known/oauth-protected-resource'
]);
module.exports = function athena(doc, options, ctx) {
 const errors=[];
 const fail=(message,p=[])=>errors.push({message,path:p});
 const source=String(ctx.document.source || '');
 const filename=path.basename(source).replace('.bundled.yaml','.yaml');
 const entries=inventory.filter(x=>x.arquivo===filename);
 const expected=new Map(entries.map(x=>[x.metodo+' '+x.caminho,x]));
 const seen=new Set(),ids=new Set();
 for(const [p,item] of Object.entries(doc.paths||{})) for(const m of methods){
  const o=item[m]; if(!o)continue;
  const loc=['paths',p,m], key=m+' '+p;seen.add(key);
  if(!/^[a-z][A-Za-z0-9]*$/.test(o.operationId||'') || ids.has(o.operationId))fail('operationId obrigatório, camelCase e único.',loc);
  ids.add(o.operationId);
  const sec=o.security??doc.security;
  const isPublic=Array.isArray(sec)&&sec.length===0;
  if(isPublic&&!publicPaths.has(key))fail('security: [] fora das exceções enumeradas.',loc);
  if(publicPaths.has(key)&&!isPublic)fail('Exceção pública deve declarar security: [].',loc);
  if(!isPublic&&!['Leitor','Repórter','Editor','Aprovador','Mantenedor','Owner'].includes(o['x-papel-minimo']))fail('x-papel-minimo ausente/inválido.',loc);
  if(!isPublic&&!o['x-escopo-mcp'])fail('Mapeamento de escopo MCP obrigatório (§7.5).',loc);
  if(!isPublic&&(!sec||sec.some(x=>!Object.keys(x).length)))fail('Segurança efetiva não pode ser ausente ou anônima.',loc);
  if(m==='get'&&o.requestBody)fail('GET não pode ter requestBody.',loc);
  if(!o.responses?.['429']?.headers?.['Retry-After'])fail('429 com Retry-After obrigatório.',loc);
  if(!isPublic)for(const status of ['401','403'])if(!o.responses?.[status])fail(status+' obrigatório em operação protegida.',loc);
  for(const [code,r] of Object.entries(o.responses||{})){
   if(/^[45]/.test(code)&&!r.content?.['application/problem+json'])fail('Erro deve oferecer application/problem+json.',[...loc,'responses',code]);
   if(['201','202'].includes(code)&&!r.headers?.Location)fail('Location obrigatório.',[...loc,'responses',code]);
   if(['204'].includes(code)&&r.content)fail('204 deve ser sem corpo.',[...loc,'responses',code]);
  }
  if(m==='get'&&!o.parameters?.some(x=>x.name==='page')&&o.responses?.['200']&&!o.responses['200'].headers?.ETag)fail('GET único exige ETag.',loc);
  if(m==='post'&&/\/decisoes$/.test(p)&&o['x-escopo-mcp']!=='athena:approve')fail('Decisão exige escopo independente athena:approve.',loc);
  if(m==='post'&&/\/publicacoes$/.test(p)&&o['x-escopo-mcp']!=='athena:publish')fail('Publicação exige escopo independente athena:publish.',loc);
  if(m==='post'&&/\/projetos\/\{projetoId\}\/exclusoes$/.test(p)&&(o['x-escopo-mcp']!=='athena:delete-project'||o['x-papel-minimo']!=='Owner'))fail('Exclusão física exige Owner e athena:delete-project.',loc);
  const params=[...(item.parameters||[]),...(o.parameters||[])];
  const match=params.some(x=>x.name==='If-Match'&&x.required===true);
  const idem=params.some(x=>x.name==='Idempotency-Key'&&x.required===true);
  const e=expected.get(key);
  if(entries.length&&!e)fail('Operação fora do inventário congelado.',loc);
  if(e){
   const has=k=>e.cabecalhos.some(x=>x===k||x===({'If-Match':'IfMatch','Idempotency-Key':'IdempotencyKey'}[k]));
   if(match!==has('If-Match'))fail('If-Match diverge do inventário.',loc);
   if(idem!==has('Idempotency-Key'))fail('Idempotency-Key diverge do inventário.',loc);
   for(const status of e.sucesso)if(!o.responses?.[status])fail('Status de sucesso '+status+' ausente.',loc);
  }
  if(match)for(const status of ['412','428'])if(!o.responses?.[status])fail(status+' obrigatório para If-Match.',loc);
 }
 for(const key of expected.keys())if(!seen.has(key))fail('Operação ausente: '+key,['paths']);
 if(filename==='bff-livro.yaml')for(const x of seen)if(!x.startsWith('get '))fail('Livro só admite GET.');
 const ajv=new Ajv({strict:false,allErrors:true,validateFormats:true});addFormats(ajv);
 function validate(schema,value,where){
  try{
   const test=ajv.compile(schema);
   if(!test(value))fail('Exemplo inválido: '+ajv.errorsText(test.errors),where);
  }catch(e){fail('Schema/exemplo não validável: '+e.message,where);}
 }
 function schemas(s,p){
  if(!s||typeof s!=='object'||Array.isArray(s))return;
  if(Object.hasOwn(s,'example'))validate(s,s.example,[...p,'example']);
  for(const [k,v] of Object.entries(s.properties||{}))schemas(v,[...p,'properties',k]);
  if(s.items)schemas(s.items,[...p,'items']);
  for(const kind of ['allOf','anyOf','oneOf'])for(const [i,x] of (s[kind]||[]).entries())schemas(x,[...p,kind,i]);
 }
 for(const [name,s] of Object.entries(doc.components?.schemas||{})){
  if(!Object.hasOwn(s,'example'))fail('Schema nomeado sem example.',['components','schemas',name]);
  schemas(s,['components','schemas',name]);
 }
 function walk(v,p){
  if(!v||typeof v!=='object')return;
  if(v.schema){
   schemas(v.schema,[...p,'schema']);
   if(Object.hasOwn(v,'example'))validate(v.schema,v.example,[...p,'example']);
   for(const [name,e] of Object.entries(v.examples||{}))if(Object.hasOwn(e,'value'))validate(v.schema,e.value,[...p,'examples',name]);
  }
  for(const [k,x] of Object.entries(v))if(!['example','examples','schema','schemas'].includes(k))walk(x,[...p,k]);
 }
 walk(doc,[]);
 return errors;
};
