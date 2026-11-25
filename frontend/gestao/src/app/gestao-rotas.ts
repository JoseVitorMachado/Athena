export type PaginaProjeto = 'visao' | 'requisitos' | 'mapa';

export type RotaGestao =
  | { tela: 'login' }
  | { tela: 'workspaces'; workspaceId?: string }
  | { tela: 'projeto'; projetoId: string; pagina: PaginaProjeto; requisitoId?: string };

export function lerRotaGestao(url: string): RotaGestao | null {
  const [caminho] = url.split(/[?#]/);
  const partes = caminho.split('/').filter(Boolean);
  const [secao, id, pagina, requisitoId] = partes;
  const idValido = (valor?: string) => !!valor && /^[a-zA-Z0-9-]+$/.test(valor);

  if (secao === 'login' && partes.length === 1) return { tela: 'login' };
  if (!secao || (secao === 'workspaces' && partes.length === 1)) return { tela: 'workspaces' };
  if (secao === 'workspaces' && partes.length === 2 && idValido(id)) {
    return { tela: 'workspaces', workspaceId: id };
  }
  if (secao !== 'projetos' || !idValido(id)) return null;
  if (partes.length === 2 || (partes.length === 3 && pagina === 'visao-geral')) {
    return { tela: 'projeto', projetoId: id, pagina: 'visao' };
  }
  if (partes.length === 3 && (pagina === 'mapa' || pagina === 'requisitos')) {
    return { tela: 'projeto', projetoId: id, pagina };
  }
  if (partes.length === 4 && pagina === 'requisitos' && idValido(requisitoId)) {
    return { tela: 'projeto', projetoId: id, pagina, requisitoId };
  }
  return null;
}

export function caminhoGestao(rota: RotaGestao): string {
  if (rota.tela === 'login') return '/login';
  if (rota.tela === 'workspaces') return rota.workspaceId ? `/workspaces/${rota.workspaceId}` : '/workspaces';
  const base = `/projetos/${rota.projetoId}`;
  if (rota.requisitoId) return `${base}/requisitos/${rota.requisitoId}`;
  return `${base}/${rota.pagina === 'visao' ? 'visao-geral' : rota.pagina}`;
}
