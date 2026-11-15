const fs=require('node:fs');const path=require('node:path');const YAML=require('yaml');const check=require('../.spectral/functions/athena.js');
const cache=new Map();function load(f){if(!cache.has(f))cache.set(f,YAML.parse(fs.readFileSync(f,'utf8')));return cache.get(f);}
function resolve(x,file){
 if(Array.isArray(x))return x.map(v=>resolve(v,file));
 if(!x||typeof x!=='object')return x;
 if(x.$ref){const [rel,frag]=x.$ref.split('#');const f=rel?path.resolve(path.dirname(file),rel):file;let v=load(f);for(const k of frag.slice(1).split('/'))v=v[k.replace(/~1/g,'/').replace(/~0/g,'~')];return {...resolve(v,f),...Object.fromEntries(Object.entries(x).filter(([k])=>k!=='$ref').map(([k,v])=>[k,resolve(v,file)]))};}
 return Object.fromEntries(Object.entries(x).map(([k,v])=>[k,resolve(v,file)]));
}
const file=path.resolve('docs/api/revisoes.yaml');const original=resolve(load(file),file);const ctx={document:{source:file}};
const base='/api/v1/projetos/{projetoId}/lotes';const target=base+'/{loteId}/decisoes';
const mutations=[
 ['escopo de decisão indevido',d=>d.paths[target].post['x-escopo-mcp']='athena:write','athena:approve'],
 ['papel ausente',d=>delete d.paths[target].post['x-papel-minimo'],'x-papel-minimo'],
 ['acesso anônimo indevido',d=>d.paths[target].post.security=[],'exceções'],
 ['429 ausente',d=>delete d.paths[target].post.responses['429'],'429'],
 ['Problem Details ausente',d=>delete d.paths[target].post.responses['409'].content['application/problem+json'],'application/problem+json'],
 ['Location ausente',d=>delete d.paths[target].post.responses['201'].headers.Location,'Location'],
 ['If-Match ausente',d=>d.paths[target].post.parameters=d.paths[target].post.parameters.filter(p=>p.name!=='If-Match'),'If-Match'],
 ['Idempotency-Key ausente',d=>d.paths[target].post.parameters=d.paths[target].post.parameters.filter(p=>p.name!=='Idempotency-Key'),'Idempotency-Key'],
 ['GET com corpo',d=>d.paths[base].get.requestBody={required:true,content:{}},'GET'],
 ['rota perdida',d=>delete d.paths[target],'Operação ausente'],
 ['operationId repetido',d=>d.paths[target].get.operationId=d.paths[target].post.operationId,'único'],
 ['exemplo contra schema',d=>d.components.schemas.DecisaoEntrada.example.decisao='INVALIDA','Exemplo inválido'],
 ['rejeição sem justificativa',d=>{const e=d.components.schemas.DecisaoEntrada.example;e.decisao='REJEITAR';delete e.justificativa;},'Exemplo inválido']
];
const baseline=check(original,{},ctx);if(baseline.length)throw Error('Baseline inválido: '+JSON.stringify(baseline));
for(const [name,mutate,expected] of mutations){const d=structuredClone(original);mutate(d);if(!check(d,{},ctx).some(e=>e.message.includes(expected)))throw Error('Regra não detectou '+name);}
console.log(`${mutations.length} mutações indevidas detectadas pelas regras; baseline sem erro.`);
const gestao=load(path.resolve('docs/api/bff-gestao.yaml'));
const livro=load(path.resolve('docs/api/bff-livro.yaml'));
const g=gestao.paths['/api/v1/gestao/projetos/{projetoId}/requisitos/{requisitoId}'].get.responses['200'].content['application/json'].examples;
const l=livro.paths['/api/v1/livro/projetos/{projetoId}/livro/requisitos/{requisitoId}'].get.responses['200'].content['application/json'].examples['leitor-publicado'].value;
const e=g['editor-aguardando-revisao'].value,a=g['aprovador-aprovada'].value;
if(e.requisitoId!==l.requisitoId||e.codigo!==l.codigo||e.versaoEmEdicao!==4||l.versaoPublicada!==3)throw Error('Identidade/comparação HATEOAS divergente.');
if(e.estadoVersao!=='AGUARDANDO_REVISAO'||e.pendenciasAbertas!==2||Object.keys(e._actions).join()!=='consultar-revisao')throw Error('Exemplo Editor incorreto.');
if(a.estadoVersao!=='APROVADA'||a._actions.publicar.method!=='POST'||!a._actions.publicar.precondicoes.includes('If-Match')||!a._actions.publicar.precondicoes.includes('Idempotency-Key'))throw Error('Ação publicar incompleta.');
if('_actions' in l||l.referencias.some(r=>r.disponivel===false&&Object.keys(r).length!==1))throw Error('Livro expõe ação ou referência restrita.');
console.log('3 exemplos HATEOAS coerentes; referência restrita sem identificadores e Livro sem ações de escrita.');
