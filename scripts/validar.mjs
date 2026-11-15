import {spawnSync} from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';
process.env.REDOCLY_TELEMETRY='off';
process.env.REDOCLY_SUPPRESS_UPDATE_NOTICE='true';
const files=fs.readdirSync('docs/api').filter(x=>x.endsWith('.yaml')).map(x=>'docs/api/'+x).sort();
fs.mkdirSync('docs/api/dist',{recursive:true});
const run=(bin,args)=>{const r=spawnSync(bin,args,{stdio:'inherit'});if(r.status!==0)process.exit(r.status??1);};
run('node',['scripts/catalogo-mcp.mjs']);
run('node',['scripts/verificar-catalogo.mjs']);
run('node',['scripts/testar-regras.cjs']);
run('node',['scripts/matriz.mjs']);
run('node_modules/.bin/redocly',['lint',...files,'docs/api/_shared/components.yaml','--max-problems','100','--format','stylish']);
for(const file of files)run('node_modules/.bin/redocly',['bundle',file,'--output','docs/api/dist/'+path.basename(file,'.yaml')+'.bundled.yaml']);
run('node_modules/.bin/spectral',['lint','--ruleset','.spectral.yaml','--fail-severity','warn',...files,'docs/api/_shared/components.yaml']);
// Shared is a component library, not a deployed API: validate through all 8 dereferenced consumers.
console.log('Contratos, bundles, matriz e exemplos validados.');
