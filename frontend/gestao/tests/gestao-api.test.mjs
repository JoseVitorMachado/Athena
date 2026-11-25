import '@angular/compiler';
import assert from 'node:assert/strict';
import { after, test } from 'node:test';
import { mkdtempSync, readFileSync, rmSync, writeFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import ts from 'typescript';
import { Injector } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

// Executa a classe real com o runner do Node, sem acrescentar dependências.
const raiz = dirname(dirname(fileURLToPath(import.meta.url)));
const temporario = mkdtempSync(join(raiz, '.teste-sessao-'));
after(() => rmSync(temporario, { recursive: true, force: true }));
const compilado = ts.transpileModule(readFileSync(join(raiz, 'src/app/gestao-api.ts'), 'utf8'), {
  compilerOptions: { target: ts.ScriptTarget.ES2022, module: ts.ModuleKind.ES2022, experimentalDecorators: true },
});
const modulo = join(temporario, 'gestao-api.mjs');
writeFileSync(modulo, compilado.outputText);
const { GestaoApi } = await import(pathToFileURL(modulo).href);
const renovar = '/api/v1/autenticacao/renovacao';
const requisitos = '/api/v1/gestao/projetos/p/requisitos';
const relacoes = '/api/v1/gestao/projetos/p/relacoes';
const sessao = (token = 'original', validade = 900_000) => ({
  accessToken: token, tokenType: 'Bearer', csrfToken: `csrf-${token}`,
  expiraEm: new Date(Date.now() + validade).toISOString(),
});
const falha = (status) => new HttpErrorResponse({ status });

function ambiente(inicial = sessao()) {
  const pendentes = [];
  const http = {
    get(url, opcoes) { return this.pedido('GET', url, undefined, opcoes); },
    post(url, corpo, opcoes) { return this.pedido('POST', url, corpo, opcoes); },
    pedido(metodo, url, corpo, opcoes) {
      return new Observable((observador) => {
        pendentes.push({ metodo, url, corpo, opcoes, observador });
      });
    },
  };
  const injector = Injector.create({ providers: [GestaoApi, { provide: HttpClient, useValue: http }] });
  const api = injector.get(GestaoApi);
  api.definirSessao(inicial);
  const proximo = (url) => {
    const indice = pendentes.findIndex((r) => r.url === url);
    assert.notEqual(indice, -1, `Pedido esperado: ${url}`);
    return pendentes.splice(indice, 1)[0];
  };
  const responder = (pedido, corpo = {}) => { pedido.observador.next(corpo); pedido.observador.complete(); };
  const observar = (observable) => {
    const resultado = {};
    observable.subscribe({ next: (valor) => { resultado.valor = valor; }, error: (erro) => { resultado.erro = erro; } });
    return resultado;
  };
  return { api, pendentes, proximo, responder, observar };
}

test('sessão válida envia Bearer sem renovação', () => {
  const a = ambiente();
  const r = a.observar(a.api.listarRequisitos('p'));
  const pedido = a.proximo(requisitos);
  assert.equal(pedido.opcoes.headers.Authorization, 'Bearer original');
  a.responder(pedido, { items: [] });
  assert.deepEqual(r.valor, { items: [] });
  assert.equal(a.pendentes.length, 0);
});

test('token expirado renova antes da consulta, com cookie e CSRF', () => {
  const a = ambiente(sessao('vencido', -1));
  a.observar(a.api.listarRequisitos('p'));
  const pedido = a.proximo(renovar);
  assert.equal(pedido.opcoes.withCredentials, true);
  assert.equal(pedido.opcoes.headers['X-CSRF-Token'], 'csrf-vencido');
  assert.equal(a.pendentes.length, 0);
  a.responder(pedido, sessao('novo'));
  assert.equal(a.proximo(requisitos).opcoes.headers.Authorization, 'Bearer novo');
});

test('requisições simultâneas compartilham uma única renovação', () => {
  const a = ambiente();
  a.observar(a.api.listarRequisitos('p'));
  a.observar(a.api.listarRelacoes('p'));
  a.proximo(requisitos).observador.error(falha(401));
  a.proximo(relacoes).observador.error(falha(401));
  assert.equal(a.pendentes.length, 1);
  a.responder(a.proximo(renovar), sessao('novo'));
  assert.equal(a.proximo(requisitos).opcoes.headers.Authorization, 'Bearer novo');
  assert.equal(a.proximo(relacoes).opcoes.headers.Authorization, 'Bearer novo');
  assert.equal(a.pendentes.length, 0);
});

test('401 atrasado reutiliza a sessão já renovada', () => {
  const a = ambiente();
  a.observar(a.api.listarRequisitos('p'));
  a.observar(a.api.listarRelacoes('p'));
  const atrasada = a.proximo(relacoes);
  a.proximo(requisitos).observador.error(falha(401));
  a.responder(a.proximo(renovar), sessao('novo'));
  a.responder(a.proximo(requisitos));
  atrasada.observador.error(falha(401));
  assert.equal(a.proximo(relacoes).opcoes.headers.Authorization, 'Bearer novo');
  assert.equal(a.pendentes.length, 0);
});

test('repetição de POST após 401 preserva o corpo e não entra em loop', () => {
  const a = ambiente();
  const corpo = { titulo: 'Teste', conteudo: 'Texto' };
  const r = a.observar(a.api.criarRequisito('p', corpo));
  a.proximo(requisitos).observador.error(falha(401));
  a.responder(a.proximo(renovar), sessao('novo'));
  const repeticao = a.proximo(requisitos);
  assert.equal(repeticao.metodo, 'POST');
  assert.deepEqual(repeticao.corpo, corpo);
  repeticao.observador.error(falha(401));
  assert.equal(r.erro.status, 401);
  assert.equal(a.pendentes.length, 0);
  assert.equal(a.observar(a.api.listarRequisitos('p')).erro.status, 401);
});

test('403 de domínio não renova nem repete escrita', () => {
  const a = ambiente();
  const r = a.observar(a.api.criarRelacao('p', {}));
  a.proximo(relacoes).observador.error(falha(403));
  assert.equal(r.erro.status, 403);
  assert.equal(a.pendentes.length, 0);
});

test('refresh rejeitado encerra a sessão e pede novo login', () => {
  for (const status of [401, 403]) {
    const a = ambiente(sessao('vencido', -1));
    const r = a.observar(a.api.listarRequisitos('p'));
    a.proximo(renovar).observador.error(falha(status));
    assert.equal(r.erro.status, 401);
    assert.match(r.erro.error.detail, /entre novamente/);
    assert.equal(a.observar(a.api.listarRequisitos('p')).erro.status, 401);
    assert.equal(a.pendentes.length, 0);
  }
});

test('falha temporária do refresh permite nova tentativa na próxima operação', () => {
  const a = ambiente(sessao('vencido', -1));
  const r = a.observar(a.api.listarRequisitos('p'));
  a.proximo(renovar).observador.error(falha(503));
  assert.equal(r.erro.status, 503);
  a.observar(a.api.listarRequisitos('p'));
  a.responder(a.proximo(renovar), sessao('novo'));
  assert.equal(a.proximo(requisitos).opcoes.headers.Authorization, 'Bearer novo');
});

test('logout durante refresh não restaura a sessão nem envia a operação', () => {
  const a = ambiente(sessao('vencido', -1));
  const r = a.observar(a.api.listarRequisitos('p'));
  const pedido = a.proximo(renovar);
  a.api.definirSessao(null);
  a.responder(pedido, sessao('novo'));
  assert.equal(r.erro.status, 401);
  assert.equal(a.pendentes.length, 0);
  assert.equal(a.observar(a.api.listarRequisitos('p')).erro.status, 401);
});

test('refresh antigo não sobrescreve sessão de outro login', () => {
  const a = ambiente(sessao('vencido', -1));
  a.observar(a.api.listarRequisitos('p'));
  const pedido = a.proximo(renovar);
  a.api.definirSessao(sessao('outra-conta'));
  a.responder(pedido, sessao('antiga-renovada'));
  a.observar(a.api.listarRelacoes('p'));
  assert.equal(a.proximo(relacoes).opcoes.headers.Authorization, 'Bearer outra-conta');
  assert.equal(a.pendentes.length, 0);
});

test('renovações seguintes usam o CSRF rotacionado', () => {
  const a = ambiente();
  a.observar(a.api.listarRequisitos('p'));
  a.proximo(requisitos).observador.error(falha(401));
  a.responder(a.proximo(renovar), sessao('novo'));
  a.responder(a.proximo(requisitos));
  a.observar(a.api.listarRequisitos('p'));
  a.proximo(requisitos).observador.error(falha(401));
  assert.equal(a.proximo(renovar).opcoes.headers['X-CSRF-Token'], 'csrf-novo');
});
