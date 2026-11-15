// Gera docs/api/apresentacao.html: um único arquivo, sem servidor nem internet,
// com os 8 contratos embutidos. Abre com dois cliques em qualquer navegador.
// Uso: npm run apresentacao   (depois de npm run validar, que atualiza dist/)
import { readFileSync, writeFileSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";
import YAML from "yaml";

const raiz = join(dirname(fileURLToPath(import.meta.url)), "..");
const dist = join(raiz, "docs/api/dist");

const contratos = [
  ["bff-gestao", "BFF Gestão", "Cliente de elaboração, edição e revisão"],
  ["bff-livro", "BFF Livro", "Cliente de leitura do conteúdo publicado"],
  ["requisitos", "Requisitos", "Documentos, candidatas, versões, publicações; orquestra a SAGA"],
  ["rastreabilidade", "Rastreabilidade", "Relações oficiais, mapa (CQRS), cobertura e retratos"],
  ["revisoes", "Revisões", "Lotes, pendências e decisões"],
  ["projetos-acesso", "Projetos e acesso", "Identidade, OAuth, sessões, workspaces, projetos e membros"],
  ["integracoes", "Integrações", "Google Drive/Docs, OpenProject, vínculos e sincronizações"],
  ["impacto-mcp", "Impacto / MCP", "Análises, sugestões, busca de contexto (RAG) e índice"],
].map(([id, titulo, papel]) => ({
  id,
  titulo,
  papel,
  doc: YAML.parse(readFileSync(join(dist, `${id}.bundled.yaml`), "utf8"), { maxAliasCount: -1 }),
}));

const dados = {
  resumo:
    "Contratos OpenAPI 3.1 elaborados contract-first a partir do Livro de Arquitetura v1.0. " +
    "Os clientes Angular falam só com os dois BFFs pelo Gateway; os seis serviços de domínio ficam em rede interna.",
  geradoEm: new Date().toLocaleDateString("pt-BR"),
  inicioDominios: 2,
  contratos,
};

// "</" escapado para o JSON não fechar a tag <script> antes da hora.
const json = JSON.stringify(dados)
  .replace(/<\//g, "<\\/")
  .split(String.fromCharCode(0x2028)).join("\\u2028")
  .split(String.fromCharCode(0x2029)).join("\\u2029");
const modelo = readFileSync(join(raiz, "scripts/apresentacao.template.html"), "utf8");
const saida = join(raiz, "docs/api/apresentacao.html");
writeFileSync(saida, modelo.replace("__DADOS__", () => json));

const total = contratos.reduce((n, c) => n + Object.values(c.doc.paths ?? {}).reduce((m, p) => m + Object.keys(p).filter((k) => ["get", "post", "put", "patch", "delete"].includes(k)).length, 0), 0);
console.log(`apresentacao.html gerado: ${contratos.length} contratos, ${total} operações, ${(Buffer.byteLength(modelo) + Buffer.byteLength(json)) / 1024 | 0} KB.`);
