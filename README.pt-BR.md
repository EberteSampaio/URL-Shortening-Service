# URL Shortening Service

> 🇺🇸 **Read this in English:** [README.md](README.md)

API REST de encurtamento de URLs construída com **Spring Boot 4** e **Java 25**.
Os códigos curtos são gerados de forma determinística a partir do ID do registro
usando [Sqids](https://sqids.org/), com **PostgreSQL** como persistência,
**Flyway** para versionamento de schema e **Redis** como cache de leitura.

---

## Sumário

- [Funcionalidades](#funcionalidades)
- [Stack](#stack)
- [Arquitetura](#arquitetura)
- [Como funciona o código curto](#como-funciona-o-código-curto)
- [Começando](#começando)
  - [Pré-requisitos](#pré-requisitos)
  - [Subindo com Docker Compose](#subindo-com-docker-compose)
  - [Rodando localmente](#rodando-localmente)
- [Configuração](#configuração)
- [API](#api)
  - [Criar URL curta](#criar-url-curta)
  - [Buscar URL original](#buscar-url-original)
  - [Atualizar URL](#atualizar-url)
  - [Remover URL](#remover-url)
  - [Estatísticas da URL](#estatísticas-da-url)
  - [Status do serviço](#status-do-serviço)
  - [Formato de erro](#formato-de-erro)
- [Documentação interativa](#documentação-interativa)
- [Cache](#cache)
- [Banco de dados](#banco-de-dados)
- [Testes](#testes)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Licença](#licença)

---

## Funcionalidades

- Criação de URLs curtas com validação de formato (`http`/`https`)
- Criação **idempotente**: enviar a mesma URL duas vezes retorna o mesmo registro
- Consulta, atualização e remoção pelo código curto
- Contagem de acessos por URL, exposta em um endpoint de estatísticas
- Endpoint de status do serviço, para checagem de liveness
- Cache distribuído em Redis com invalidação automática em update/delete
- Cache resiliente: se o Redis estiver fora, a aplicação continua respondendo (fallback para o banco)
- Tratamento de erros centralizado, com payload de erro consistente
- Documentação OpenAPI / Swagger UI
- Migrations versionadas com Flyway (Hibernate apenas valida o schema)
- Imagem Docker multi-stage com layers do Spring Boot e usuário não-root

---

## Stack

| Camada         | Tecnologia                                  |
|----------------|---------------------------------------------|
| Linguagem      | Java 25                                     |
| Framework      | Spring Boot 4.1.0 (Web MVC, Data JPA, Validation, Cache) |
| Banco          | PostgreSQL 16                               |
| Migrations     | Flyway                                      |
| Cache          | Redis 7 (Spring Data Redis)                 |
| Geração de IDs | Sqids 0.1.0                                 |
| Documentação   | springdoc-openapi 3.1.0                     |
| Build          | Maven (wrapper incluso)                     |
| Container      | Docker / Docker Compose                     |

---

## Arquitetura

```
HTTP  ──►  UrlResource        (@RestController — validação de entrada, DTOs, status HTTP)
             │
             ▼
           UrlService         (@Service — regra de negócio, transações, @Cacheable/@CacheEvict)
             │        │
             │        └──►  Redis        (cache "urls", chave = shortCode)
             ▼
           UrlRepository      (Spring Data JPA)
             │
             ▼
           PostgreSQL         (tabela short_urls, schema gerido pelo Flyway)
```

Exceções de domínio (`DomainException`, `UrlNotFoundException`) sobem até o
`GlobalHandlerException` (`@RestControllerAdvice`), que as traduz para o
formato `ApiError`. Falhas inesperadas recebem um `errorId` (UUID) que é
logado no servidor e devolvido ao cliente, sem vazar detalhes internos.

---

## Como funciona o código curto

1. A URL original é persistida e recebe um `id` sequencial do PostgreSQL.
2. Esse `id` é codificado com Sqids, gerando um código curto ofuscado
   (ex.: `1` → `5CAHFl9`) — sem sequência previsível exposta.
3. O código é gravado na coluna `short_code`, que possui índice único.

Parâmetros do Sqids são configuráveis:

- `SQIDS_MIN_LENGTH` (padrão `7`) — tamanho mínimo do código
- `SQIDS_ALPHABET` (padrão: alfanumérico `[0-9A-Za-z]`)

> Alterar o alfabeto ou o tamanho mínimo muda os códigos gerados para
> **novos** registros. Códigos já emitidos continuam válidos, pois ficam
> persistidos na tabela.

---

## Começando

### Pré-requisitos

- **Docker + Docker Compose** — caminho recomendado, não exige nada instalado além disso
- Para rodar fora de container: **JDK 25**, **PostgreSQL 16** e **Redis 7**

### Subindo com Docker Compose

```bash
git clone git@github.com:EberteSampaio/URL-Shortening-Service.git
cd URL-Shortening-Service

cp .env.example .env      # ajuste DB_PASSWORD e demais valores
docker compose up --build
```

Isso sobe três serviços:

| Serviço      | Container     | Porta (host)                    |
|--------------|---------------|---------------------------------|
| Aplicação    | `us-app`      | `${SERVER_HOST_PORT:-8080}`     |
| PostgreSQL   | `us-postgres` | `${DB_HOST_PORT:-5432}`         |
| Redis        | `us-redis`    | `6379`                          |

A aplicação só inicia após os healthchecks de Postgres e Redis passarem.
As migrations do Flyway rodam automaticamente no startup.

Verifique:

```bash
curl -s http://localhost:8080/api-docs | head
```

Para derrubar tudo (mantendo os volumes de dados):

```bash
docker compose down
```

Para remover também os dados persistidos:

```bash
docker compose down -v
```

### Rodando localmente

Com PostgreSQL e Redis já disponíveis na máquina:

```bash
cp .env.example .env      # aponte DB_HOST/REDIS_HOST para localhost
./mvnw spring-boot:run
```

A aplicação lê o `.env` da raiz via `spring.config.import=optional:file:.env[.properties]`.
O import é opcional: em produção basta exportar as variáveis de ambiente reais.

Para gerar o JAR executável:

```bash
./mvnw clean package
java -jar target/url-shortening-0.0.1-SNAPSHOT.jar
```

---

## Configuração

Todas as chaves têm default em `application.properties`, exceto as marcadas
como obrigatórias. Copie `.env.example` para `.env` e ajuste.

Há dois arquivos de exemplo, com as mesmas chaves e os mesmos valores —
apenas os comentários mudam de idioma:

| Arquivo               | Comentários |
|-----------------------|-------------|
| `.env.example`        | Inglês      |
| `.env.example.pt-BR`  | Português   |

Use o que preferir como base:

```bash
cp .env.example.pt-BR .env
```

> O `.env` está no `.gitignore` e **nunca** deve ser commitado.

### Aplicação

| Variável      | Padrão           | Descrição              |
|---------------|------------------|------------------------|
| `APP_NAME`    | `url-shortening` | Nome da aplicação      |
| `SERVER_PORT` | `8080`           | Porta HTTP do servidor |

### Banco de dados (obrigatório)

| Variável      | Padrão           | Descrição                     |
|---------------|------------------|-------------------------------|
| `DB_HOST`     | `localhost`      | Host do PostgreSQL            |
| `DB_PORT`     | `5432`           | Porta do PostgreSQL           |
| `DB_NAME`     | `url_shortening` | Nome do banco                 |
| `DB_USERNAME` | —                | Usuário (**obrigatório**)     |
| `DB_PASSWORD` | —                | Senha (**obrigatório**)       |

### Cache

| Variável     | Padrão      | Descrição                          |
|--------------|-------------|------------------------------------|
| `REDIS_HOST` | `localhost` | Host do Redis                      |
| `REDIS_PORT` | `6379`      | Porta do Redis                     |
| `CACHE_TTL`  | `3600000`   | TTL padrão do cache, em ms (1 hora) |

### Pool de conexões (HikariCP)

| Variável                     | Padrão    | Descrição                          |
|------------------------------|-----------|------------------------------------|
| `DB_POOL_MAX_SIZE`           | `10`      | Máximo de conexões                 |
| `DB_POOL_MIN_IDLE`           | `5`       | Mínimo de conexões ociosas         |
| `DB_POOL_CONNECTION_TIMEOUT` | `30000`   | Timeout de obtenção de conexão (ms) |
| `DB_POOL_IDLE_TIMEOUT`       | `600000`  | Tempo até fechar conexão ociosa (ms) |
| `DB_POOL_MAX_LIFETIME`       | `1800000` | Vida máxima de uma conexão (ms)    |

### JPA / Flyway / Sqids

| Variável                    | Padrão     | Descrição                                        |
|-----------------------------|------------|--------------------------------------------------|
| `JPA_DDL_AUTO`              | `validate` | Mantenha `validate`: o schema é do Flyway        |
| `JPA_SHOW_SQL`              | `true`     | Loga o SQL gerado (desligado no compose)         |
| `JPA_FORMAT_SQL`            | `true`     | Formata o SQL nos logs                           |
| `FLYWAY_ENABLED`            | `true`     | Habilita as migrations no startup                |
| `FLYWAY_BASELINE_ON_MIGRATE`| `true`     | Cria baseline em banco pré-existente             |
| `SQIDS_MIN_LENGTH`          | `7`        | Tamanho mínimo do código curto                   |
| `SQIDS_ALPHABET`            | `0-9A-Za-z`| Alfabeto usado na codificação                    |

---

## API

Base URL: `http://localhost:8080/api/shorten`
Todas as requisições e respostas usam `application/json`.

### Criar URL curta

```http
POST /api/shorten
Content-Type: application/json

{
  "link": "https://www.example.com/uma/url/bem/longa"
}
```

**`201 Created`** — com header `Location: /api/shorten/{id}`

```json
{
  "id": 1,
  "url": "https://www.example.com/uma/url/bem/longa",
  "shortcode": "5CAHFl9",
  "createdAt": "2026-08-25T10:30:00",
  "updatedAt": "2026-08-25T10:30:00"
}
```

A operação é idempotente por URL: enviar a mesma `link` novamente devolve o
registro já existente, com o mesmo `shortcode`. A coluna `url` tem constraint
de unicidade que garante isso no nível do banco.

O campo `link` é validado como URL absoluta e precisa casar com `^https?://.+`.

```bash
curl -i -X POST http://localhost:8080/api/shorten \
  -H 'Content-Type: application/json' \
  -d '{"link":"https://www.example.com"}'
```

### Buscar URL original

```http
GET /api/shorten/{shortCode}
```

**`200 OK`** — mesmo payload do create.
**`404 Not Found`** — código curto inexistente.

```bash
curl http://localhost:8080/api/shorten/5CAHFl9
```

> Esta é uma API JSON: o endpoint retorna a URL original no corpo da resposta,
> **não** faz redirect HTTP 301/302.

Toda chamada bem-sucedida a este endpoint conta como um acesso e incrementa o
contador exibido em [Estatísticas da URL](#estatísticas-da-url). Códigos
inexistentes falham com `404` antes de qualquer contagem.

### Atualizar URL

```http
PUT /api/shorten/{shortCode}
Content-Type: application/json

{
  "link": "https://www.example.com/nova-url"
}
```

**`200 OK`** — o `shortcode` é preservado e a entrada correspondente é
removida do cache.
**`404 Not Found`** — código curto inexistente.

### Remover URL

```http
DELETE /api/shorten/{shortCode}
```

**`204 No Content`** — removido, com invalidação de cache.
**`404 Not Found`** — código curto inexistente.

### Estatísticas da URL

```http
GET /api/shorten/{shortCode}/stats
```

**`200 OK`**

```json
{
  "id": 1,
  "url": "https://www.example.com/uma/url/bem/longa",
  "shortcode": "5CAHFl9",
  "createdAt": "2026-08-25T10:30:00",
  "updatedAt": "2026-08-25T10:30:00",
  "accessCount": 42
}
```

**`404 Not Found`** — código curto inexistente.

```bash
curl http://localhost:8080/api/shorten/5CAHFl9/stats
```

O `accessCount` é quantas vezes o [endpoint de consulta](#buscar-url-original)
resolveu esse código curto. Ler as estatísticas **não** conta como acesso.

Este endpoint ignora o cache Redis de propósito e lê direto do banco: o `Link`
cacheado carrega o contador congelado no momento em que entrou no cache, então
servir estatísticas a partir dele reportaria um número com até 30 minutos de
atraso.

### Status do serviço

```http
GET /api/status
```

**`200 OK`**

```json
{
  "status": "UP",
  "timestamp": "2026-08-25T10:30:00"
}
```

```bash
curl http://localhost:8080/api/status
```

Checagem de liveness enxuta. Ela prova apenas que a camada HTTP está
respondendo — não verifica PostgreSQL nem Redis.

### Formato de erro

Todos os erros seguem o mesmo contrato (`ApiError`):

```json
{
  "timestamp": "2026-08-25T10:31:22.481",
  "status": 400,
  "error": "Erro de validação",
  "message": "Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente.",
  "path": "/api/shorten",
  "fields": [
    { "field": "link", "message": "O link deve ser uma URL válida" }
  ]
}
```

O array `fields` só aparece em erros de validação de campo. Em falhas
inesperadas (`500`), `message` traz um código de correlação (`errorId`) que
também é gravado nos logs do servidor.

| Status | Quando ocorre                                             |
|--------|-----------------------------------------------------------|
| `400`  | Payload inválido ou regra de negócio violada              |
| `404`  | Código curto não encontrado                               |
| `500`  | Falha inesperada — resposta traz `errorId` para o suporte |

---

## Documentação interativa

Com a aplicação no ar:

- **Swagger UI**: <http://localhost:8080/documentation.html>
- **OpenAPI JSON**: <http://localhost:8080/api-docs>

---

## Cache

O cache é declarativo, via anotações do Spring Cache sobre o `UrlService`:

- `getLinkByShortCode` é `@Cacheable(value = "urls", key = "#shortCode")`
- `update` e `delete` são `@CacheEvict` na mesma chave

Configuração em `CacheConfig`:

- Cache `urls`: TTL de **30 minutos**, valores serializados em JSON
- Demais caches: TTL padrão de **1 hora**
- Valores `null` não são cacheados
- Um `CacheErrorHandler` customizado captura falhas de GET/PUT/EVICT/CLEAR,
  registra um `WARN` e deixa a requisição seguir para o banco — uma
  indisponibilidade do Redis degrada a performance, não a disponibilidade

---

## Banco de dados

Schema versionado pelo Flyway em `src/main/resources/db/migrations`:

| Migration | Descrição                                        |
|-----------|--------------------------------------------------|
| `V1`      | Cria a tabela `short_urls`                       |
| `V2`      | Adiciona constraint de unicidade na coluna `url` |
| `V3`      | Adiciona a coluna `access_count`                 |

Schema resultante:

```sql
CREATE TABLE short_urls
(
    id           BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    url          TEXT   NOT NULL UNIQUE,
    short_code   VARCHAR(255) UNIQUE,
    access_count BIGINT NOT NULL DEFAULT 0,
    created_at   TIMESTAMP WITHOUT TIME ZONE,
    updated_at   TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_short_urls PRIMARY KEY (id)
);
```

O `access_count` é incrementado com um `UPDATE ... SET access_count =
access_count + 1` atômico, e não com um ciclo read-modify-write, de forma que
acessos concorrentes ao mesmo código curto não perdem incrementos. Como efeito
colateral, o comando não dispara o `@UpdateTimestamp` do Hibernate, o que
mantém o `updated_at` significando "a URL foi editada" em vez de "a URL foi
acessada".

Os timestamps são gerados pelo banco (`@CreationTimestamp`/`@UpdateTimestamp`
com `SourceType.DB`). O Hibernate roda em `ddl-auto=validate`, ou seja, nunca
altera o schema — toda mudança estrutural deve entrar como uma nova migration.

---

## Testes

```bash
./mvnw test
```

`UrlResourceIntegrationTest` é um slice `@WebMvcTest` que exercita a camada
web com `MockMvc` e o `UrlService` mockado via `@MockitoBean`, cobrindo os
quatro endpoints de CRUD, os status HTTP e o header `Location`.

Os endpoints `/stats` e `/api/status` ainda não têm testes, e o contador de
acessos também não — esse precisa de um teste na camada de persistência, já que
o incremento acontece em uma query JPQL `@Modifying`, não em código Java.

---

## Estrutura do projeto

```
src/main/java/br/com/deveberte/urlshortening/
├── UrlShorteningApplication.java
├── api/
│   ├── dto/
│   │   ├── PingResponse.java            # payload do status do serviço
│   │   ├── UrlRequest.java              # payload de entrada + validações
│   │   ├── UrlResponse.java             # payload de saída
│   │   └── UrlStatisticResponse.java    # payload de saída + accessCount
│   └── resource/
│       ├── APIStatusResource.java       # GET /api/status
│       └── UrlResource.java             # controller REST + anotações OpenAPI
├── config/
│   ├── CacheConfig.java                 # Redis, TTLs e tolerância a falhas
│   ├── SqidsConfig.java                 # bean do gerador de códigos
│   ├── SqidsRecord.java                 # @ConfigurationProperties app.sqids
│   └── exceptionhandler/
│       ├── ApiError.java                # contrato de erro
│       ├── FieldWithError.java
│       └── GlobalHandlerException.java  # @RestControllerAdvice
├── domain/entity/
│   └── Link.java                        # entidade JPA (short_urls)
├── exception/
│   ├── DomainException.java
│   └── UrlNotFoundException.java
├── repository/
│   └── UrlRepository.java
└── service/
    └── UrlService.java                  # regra de negócio + cache

src/main/resources/
├── application.properties
└── db/migrations/                       # migrations Flyway
```

---

## Licença

Projeto de estudo, sem licença definida no momento.
