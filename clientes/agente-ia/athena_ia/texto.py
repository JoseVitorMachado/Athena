"""Conversão do conteúdo do editor (HTML) para texto plano antes da indexação."""

import re
from html.parser import HTMLParser

# Mesma regra do frontend para reconhecer conteúdo salvo pelo editor.
_HTML = re.compile(r"^\s*<(?:p|div|h[1-6]|ul|ol|strong|b|i|em|u|br|pre|table|span|blockquote|a)\b", re.I)
_BLOCOS = {"p", "div", "h1", "h2", "h3", "h4", "h5", "h6", "li", "ul", "ol", "pre", "blockquote", "tr", "table"}


class _Extrator(HTMLParser):
    def __init__(self):
        super().__init__(convert_charrefs=True)
        self.partes = []
        self.ignorando = 0

    def handle_starttag(self, tag, attrs):
        if tag in ("script", "style"):
            self.ignorando += 1
        elif tag == "br" or tag in _BLOCOS:
            self.partes.append("\n")
            if tag == "li":
                self.partes.append("- ")

    def handle_endtag(self, tag):
        if tag in ("script", "style"):
            self.ignorando = max(0, self.ignorando - 1)
        elif tag in _BLOCOS:
            self.partes.append("\n")

    def handle_data(self, data):
        if not self.ignorando:
            self.partes.append(data)


def texto_plano(conteudo):
    if not conteudo or not _HTML.match(conteudo):
        return conteudo
    extrator = _Extrator()
    extrator.feed(conteudo)
    extrator.close()
    linhas = (re.sub(r"[ \t ]+", " ", linha).strip() for linha in "".join(extrator.partes).split("\n"))
    return "\n".join(linha for linha in linhas if linha and linha != "-")
