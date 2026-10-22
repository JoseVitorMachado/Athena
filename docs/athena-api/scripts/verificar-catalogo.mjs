import fs from 'node:fs';
import Ajv from 'ajv/dist/2020.js';
import addFormats from 'ajv-formats';
const ajv=new Ajv({strict:false,allErrors:true});addFormats(ajv);
const {tools}=JSON.parse(fs.readFileSync('docs/seguranca/tools-mcp.json'));
const ids=new Set();let critical=0;
for(const t of tools){
 if(ids.has(t.name))throw Error('Tool duplicada: '+t.name);ids.add(t.name);
 ajv.compile(t.inputSchema);
 if(!t.inputSchema.required.includes('projetoId'))throw Error('Projeto obrigatório ausente: '+t.name);
 if(t['x-rota'].includes('/confirmacoes-'))throw Error('Agente não pode emitir comprovante humano.');
 if(t['x-metodo']==='POST'&&/\/(decisoes|publicacoes)$/.test(t['x-rota'])){
  if(!t.inputSchema.properties.corpo.required.includes('comprovanteHumano'))throw Error('Comprovante não obrigatório: '+t.name);
  const wanted=t['x-rota'].endsWith('/decisoes')?'athena:approve':'athena:publish';
  if(t['x-escopo']!==wanted)throw Error('Escopo crítico divergente.');critical++;
 }
}
if(critical!==2)throw Error('Faltam tools críticas esperadas.');
console.log(`${tools.length} esquemas de tools válidos; projeto explícito e confirmação humana em decisão/publicação.`);
