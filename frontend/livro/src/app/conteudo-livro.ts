export function htmlPublicado(conteudo: string): string {
  if (/^\s*<(?:p|div|h[1-6]|ul|ol|strong|b|i|em|u|br|pre|table|span|blockquote|a)\b/i.test(conteudo)) {
    return conteudo;
  }
  const escapar = (texto: string) => texto.replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;');
  const formatar = (texto: string) => escapar(texto).replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
  return conteudo.split(/\r?\n\r?\n/).map(bloco => {
    const linhas = bloco.split(/\r?\n/);
    const trechos: string[] = [];
    let lista: string[] = [];
    const terminarLista = () => {
      if (lista.length) trechos.push(`<ul>${lista.join('')}</ul>`);
      lista = [];
    };
    for (const linha of linhas) {
      const marcador = linha.match(/^[-*] (.+)$/);
      if (marcador) { lista.push(`<li>${formatar(marcador[1])}</li>`); continue; }
      terminarLista();
      const titulo = linha.match(/^#{1,6} (.+)$/);
      trechos.push(titulo ? `<h2>${formatar(titulo[1])}</h2>` : `<p>${formatar(linha)}</p>`);
    }
    terminarLista();
    return trechos.join('');
  }).join('');
}

export function textoPublicado(conteudo: string): string {
  const texto = conteudo.replace(/<\/(?:p|div|li|h[1-6]|td)>|<br\s*\/?\s*>/gi, ' ');
  const fragmento = document.createElement('template');
  fragmento.innerHTML = texto;
  fragmento.content.querySelectorAll('script, style').forEach(elemento => elemento.remove());
  return fragmento.content.textContent?.replace(/\s+/g, ' ').trim() ?? '';
}

export function normalizarBusca(texto: string): string {
  return texto.normalize('NFD').replace(/\p{Diacritic}/gu, '').toLocaleLowerCase('pt-BR').trim();
}
