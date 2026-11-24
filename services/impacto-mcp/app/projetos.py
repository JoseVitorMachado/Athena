"""Barreiras de exclusão de projetos."""


def bloquear_acesso(conexao, projeto_id):
    """Serializa alterações e a limpeza dos dados de um projeto."""
    conexao.execute("SELECT pg_advisory_xact_lock(hashtextextended(%s, 2))", (str(projeto_id),))


def estado(conexao, projeto_id):
    return conexao.execute("""SELECT EXISTS(SELECT 1 FROM projetos_bloqueados WHERE projeto_id = %s),
        EXISTS(SELECT 1 FROM projetos_excluidos WHERE projeto_id = %s)""", (projeto_id, projeto_id)).fetchone()


def ignorar_evento(tipo, bloqueado, excluido):
    """Impede a recriação de dados após a exclusão ou o fechamento."""
    return excluido or (bloqueado and tipo == "VersaoPublicada")
