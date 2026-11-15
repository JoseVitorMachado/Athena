# Cliente Livro (Angular + TypeScript), servido por nginx.
#
# Build em dois estágios:
#   1. "build" usa Node para instalar dependências e compilar o Angular;
#   2. a imagem final é um nginx com apenas os arquivos estáticos gerados.
# Ganho: Node, node_modules (centenas de MB) e o código-fonte TypeScript não vão
# para a imagem que roda. Sobra um servidor web pequeno, sem runtime de JavaScript
# no servidor e com muito menos pacotes para atualizar.

# ---- estágio 1: build ---------------------------------------------------------
FROM node:24.22.3-alpine AS build
WORKDIR /build

# Dependências primeiro: esta camada só é refeita quando o package-lock muda.
COPY package.json package-lock.json ./
RUN --mount=type=cache,target=/root/.npm npm ci --no-audit --no-fund

COPY . .
RUN npx ng build --configuration production --base-href /livro/

# ---- estágio 2: execução ------------------------------------------------------
# Variante sem privilégios do nginx: roda como usuário 101 e escuta na 8080.
FROM nginxinc/nginx-unprivileged:1.30.6-alpine

COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /build/dist/livro/browser /usr/share/nginx/html/livro

# Endereço do Gateway dentro da rede; trocado por variável de ambiente.
ENV GATEWAY_URL=http://gateway:8080

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --start-period=10s --retries=6 \
  CMD wget -q -O /dev/null http://127.0.0.1:8080/saude || exit 1
