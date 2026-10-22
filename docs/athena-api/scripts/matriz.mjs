import fs from 'node:fs';
import path from 'node:path';
import YAML from 'yaml';
const dir=path.resolve('docs/api');
const rows=[],ids=new Set();
for(const file of fs.readdirSync(dir).filter(x=>x.endsWith('.yaml')).sort()){
 const d=YAML.parse(fs.readFileSync(path.join(dir,file),'utf8'));
 for(const [route,item] of Object.entries(d.paths||{}))for(const method of ['get','post','patch','delete','put']){
  const o=item[method];if(!o)continue;
  if(ids.has(o.operationId))throw Error('operationId duplicado global: '+o.operationId);ids.add(o.operationId);
  rows.push([file,method.toUpperCase(),route,o.operationId,o['x-papel-minimo']??'Pública',o['x-escopo-mcp']??'—',o['x-interno']?'Sim':'Não',o['x-decidir']?'Pendente':'Definida']);
 }
}
const header='# Matriz de autorização\n\nGerada por `npm run matriz` a partir de x-papel-minimo, x-escopo-mcp e x-interno. Fonte §§7.4–7.5. Hierarquia cumulativa; acesso atual, escopo, estado e propriedade continuam obrigatórios. Leitor em ação de conta/workspace é piso autenticado, não associação ou concessão de administração de projeto. Política pendente deve negar até definição.\n\n';
const table='| Contrato | Método | Rota | operationId | Papel mínimo | Escopo MCP | Exclusivo interno | Política |\n|---|---|---|---|---|---|---|---|\n'+rows.map(r=>'| '+r.join(' | ')+' |').join('\n')+'\n';
fs.writeFileSync(path.join(dir,'matriz-autorizacao.md'),header+table);
fs.mkdirSync('docs/seguranca',{recursive:true});
fs.writeFileSync('docs/seguranca/endpoints.json',JSON.stringify({versao:'1.0.0',operacoes:rows.map(r=>Object.fromEntries(['contrato','metodo','rota','operacaoDominio','papelMinimo','escopo','interno','politica'].map((k,i)=>[k,r[i]])))},null,2)+'\n');
const expected=JSON.parse(fs.readFileSync('scripts/inventario.json','utf8'));
if(rows.length!==expected.length)throw Error('Cobertura incompleta da matriz.');
console.log(`${rows.length} operações na matriz; operationIds únicos globalmente.`);
