# Serviço Integrações (Java 17 + Spring Boot).
#
# Build em dois estágios:
#   1. "build" usa Maven + JDK para compilar e empacotar o jar;
#   2. a imagem final leva só o JRE e o jar.
# Ganho: Maven, JDK, código-fonte e o repositório local de dependências ficam
# fora da imagem que roda. Ela sai várias vezes menor e com menos superfície de
# ataque, porque não carrega compilador nem ferramentas de build.

# ---- estágio 1: build ---------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build

# O cache do Maven é montado só durante o build (não entra em nenhuma camada) e
# é compartilhado entre os serviços; "locked" evita escrita concorrente.
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -B -q -DskipTests package

# ---- estágio 2: execução ------------------------------------------------------
FROM eclipse-temurin:17.0.21_9-jre-alpine

# Usuário sem privilégios, com UID fixo para o Kubernetes validar runAsNonRoot.
RUN addgroup -S -g 10001 athena && adduser -S -u 10001 -G athena athena
WORKDIR /app
COPY --from=build /build/target/app.jar app.jar

USER 10001
EXPOSE 8080

# Heap proporcional ao limite de memória do container.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+ExitOnOutOfMemoryError"

HEALTHCHECK --interval=10s --timeout=5s --start-period=60s --retries=6 \
  CMD wget -q -O /dev/null http://127.0.0.1:8080/actuator/health/readiness || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
