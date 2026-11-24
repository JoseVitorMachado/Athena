"""Embeddings locais do multilingual-e5-small."""

import os
import threading

DIMENSAO = 384
MODELO = "intfloat/multilingual-e5-small@614241f"
_lock = threading.Lock()
_tokenizer = None
_sessao = None


def _carregar():
    global _tokenizer, _sessao
    with _lock:
        if _sessao is None:
            import onnxruntime as ort
            from tokenizers import Tokenizer

            # Nenhum dado de uso sai do serviço: telemetria do runtime desligada.
            ort.disable_telemetry_events()

            diretorio = os.environ.get("MODELO_DIR", "/opt/modelo")
            tokenizer = Tokenizer.from_file(os.path.join(diretorio, "tokenizer.json"))
            tokenizer.enable_truncation(max_length=512)
            tokenizer.enable_padding(pad_id=tokenizer.token_to_id("<pad>"), pad_token="<pad>")
            opcoes = ort.SessionOptions()
            opcoes.intra_op_num_threads = 2
            sessao = ort.InferenceSession(os.path.join(diretorio, "model.onnx"),
                                         sess_options=opcoes, providers=["CPUExecutionProvider"])
            _tokenizer, _sessao = tokenizer, sessao
        return _tokenizer, _sessao


def contar_tokens(texto: str) -> int:
    tokenizer, _ = _carregar()
    # Sem truncamento ou padding para medir o texto completo.
    with _lock:
        tokenizer.no_truncation()
        tokenizer.no_padding()
        try:
            return len(tokenizer.encode(texto).ids)
        finally:
            tokenizer.enable_truncation(max_length=512)
            tokenizer.enable_padding(pad_id=tokenizer.token_to_id("<pad>"), pad_token="<pad>")


def _gerar(textos: list[str]) -> list[list[float]]:
    if not textos:
        return []
    import numpy as np

    tokenizer, sessao = _carregar()
    with _lock:
        codificados = tokenizer.encode_batch(textos)
    ids = np.asarray([e.ids for e in codificados], dtype=np.int64)
    mascara = np.asarray([e.attention_mask for e in codificados], dtype=np.int64)
    disponiveis = {"input_ids": ids, "attention_mask": mascara,
                   "token_type_ids": np.zeros_like(ids)}
    entradas = {i.name: disponiveis[i.name] for i in sessao.get_inputs() if i.name in disponiveis}
    saida = sessao.run(None, entradas)[0]
    pesos = mascara[:, :, None]
    media = (saida * pesos).sum(axis=1) / np.maximum(pesos.sum(axis=1), 1)
    norma = np.maximum(np.linalg.norm(media, axis=1, keepdims=True), 1e-12)
    return (media / norma).tolist()


def gerar_consulta(texto: str) -> list[float]:
    return _gerar(["query: " + texto])[0]


def gerar_trechos(textos: list[str]) -> list[list[float]]:
    return _gerar(["passage: " + texto for texto in textos])


def literal(vetor: list[float]) -> str:
    return "[" + ",".join(f"{v:.8f}" for v in vetor) + "]"
