"""Segmentação de conteúdo publicado, com medição de tokens injetável."""


def segmentar(codigo, titulo, conteudo, criterios, contar_tokens):
    cabecalho = f"{codigo} {titulo}".strip()
    corpo = conteudo.strip()
    if criterios:
        corpo += "\nCritérios de aceite:\n" + "\n".join(criterios)
    palavras = corpo.split()
    if contar_tokens("passage: " + cabecalho) >= 400:
        raise ValueError("Código e título excedem o limite de segmentação")
    if not palavras:
        return [cabecalho]
    trechos = []
    inicio = 0
    while inicio < len(palavras):
        fim = inicio
        while fim < len(palavras):
            candidato = cabecalho + "\n" + " ".join(palavras[inicio:fim + 1])
            if contar_tokens("passage: " + candidato) > 400:
                break
            fim += 1
        if fim == inicio:
            raise ValueError("Uma palavra excede o limite de segmentação")
        trechos.append(cabecalho + "\n" + " ".join(palavras[inicio:fim]))
        if fim == len(palavras):
            break
        retorno = fim
        while retorno > inicio + 1 and contar_tokens(" ".join(palavras[retorno - 1:fim])) <= 50:
            retorno -= 1
        inicio = retorno
    return trechos
