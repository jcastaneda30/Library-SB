# Library-SB

API REST de biblioteca hecha con Spring Boot. Además de gestionar autores, géneros y libros, incluye un
**scraper configurable** que extrae novelas web (ficha, capítulos, formato del texto e imágenes) y las
guarda en la biblioteca.

- [Stack](#stack)
- [Arquitectura](#arquitectura)
- [Puesta en marcha](#puesta-en-marcha)
- [Configuración](#configuración)
- [Documentación de la API](#documentación-de-la-api)
- [Scraper](#scraper)
- [Base de datos](#base-de-datos)
- [Tests](#tests)

## Stack

| | |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 4.1 (Web MVC, Data JPA, Validation) |
| Base de datos | PostgreSQL 17, migraciones con Flyway |
| Scraping | jsoup (HTML), Jackson (JSON), Playwright + Chromium (páginas que cargan con JavaScript) |
| Imágenes | Disco local o Amazon S3 (AWS SDK v2) |
| Documentación | springdoc-openapi (Swagger UI) |
| Build | Maven (wrapper incluido) |

## Arquitectura

El código está organizado **por funcionalidad**: cada carpeta agrupa su controlador, DTOs, modelo,
repositorio y servicio.

```
src/main/java/com/library/
├── LibraryApplication.java
├── common/          Auditoría (createdAt/updatedAt) y manejo global de errores
├── library/         Guarda y consulta la información
│   ├── author/
│   ├── genre/
│   ├── book/
│   ├── chapter/     Capítulos divididos en párrafos (texto o imagen)
│   └── image/       Imágenes en disco o S3
└── scraper/         Extrae novelas de sitios web
    ├── site/        Configuración de cada sitio: URL base + reglas de extracción
    ├── novel/       Peticiones de novela: estado, progreso y ejecución en segundo plano
    ├── engine/      Descarga, evaluación de reglas, división en párrafos y saneado del HTML
    ├── config/      Pool de hilos del scraper
    └── common/      Errores del scraper
```

La dependencia va en un solo sentido: **`scraper` usa los servicios de `library`**
(`BookService.importBook`, `ChapterService.importChapter`, `ImageStorageService.store`), pero `library`
no sabe que el scraper existe. Todo el procesamiento (descargar, interpretar, sanear) ocurre en
`scraper`; `library` solo guarda y consulta.

## Puesta en marcha

### Requisitos

- JDK 17 o superior
- Docker (para PostgreSQL), o un PostgreSQL 17 propio

### 1. Variables de entorno

```bash
cp .env.example .env
```

Edita `.env` y cambia al menos `POSTGRES_PASSWORD`. El archivo lo leen tanto Docker Compose como Spring
y no se sube al repositorio.

### 2. Base de datos

```bash
docker compose up -d
```

Levanta `postgres:17-alpine` con el usuario, la contraseña y la base de `.env`, en el puerto
`POSTGRES_PORT`. Al arrancar la aplicación, Flyway crea las tablas.

> Las variables `POSTGRES_*` solo se aplican la primera vez que se crea el volumen. Para empezar de cero:
> `docker compose down -v` (borra los datos).

### 3. Chromium (solo si usas sitios con renderizado JavaScript)

Se instala una vez por máquina:

```bash
./mvnw exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"
```

### 4. Arrancar

```bash
./mvnw spring-boot:run
```

La API queda en `http://localhost:8080` y su documentación en
[`http://localhost:8080/swagger-ui.html`](http://localhost:8080/swagger-ui.html).

## Configuración

Todo se configura con variables de entorno (o en `.env`). Las variables reales del sistema tienen
prioridad sobre `.env`.

| Variable | Por defecto | Descripción |
|---|---|---|
| `POSTGRES_HOST` | `localhost` | Host de PostgreSQL |
| `POSTGRES_PORT` | `5432` | Puerto (el `compose.yaml` usa `5433` si no se define) |
| `POSTGRES_DB` | `mydatabase` | Base de datos |
| `POSTGRES_USER` | `postgres` | Usuario |
| `POSTGRES_PASSWORD` | `postgres` | Contraseña |
| `STORAGE_TYPE` | `local` | Dónde guardar imágenes: `local` o `s3` |
| `S3_BUCKET` | | Bucket (obligatorio con `s3`) |
| `AWS_REGION` | | Región del bucket |
| `S3_ENDPOINT` | | Solo para MinIO/LocalStack |
| `S3_PATH_STYLE` | `false` | `true` para MinIO/LocalStack |

Los valores por defecto de la conexión son solo para desarrollo local. Si usas `compose.yaml`, define
las cinco variables `POSTGRES_*` en `.env` para que la aplicación y el contenedor coincidan.

### Imágenes en S3

```bash
STORAGE_TYPE=s3
S3_BUCKET=mi-bucket
AWS_REGION=us-east-1
```

- Las credenciales **no** van en la configuración: se usan las de la cadena estándar de AWS
  (`AWS_ACCESS_KEY_ID`/`AWS_SECRET_ACCESS_KEY`, `~/.aws/credentials` o el rol IAM).
- El bucket puede ser privado. `GET /api/images/{id}` redirige (`302`) a una URL prefirmada que dura 1 hora.
- Permisos necesarios: `s3:PutObject` y `s3:GetObject` sobre `arn:aws:s3:::mi-bucket/images/*`.

### Detrás de un proxy

`server.forward-headers-strategy=framework` hace que las URLs absolutas que devuelve la API (por ejemplo,
las de imágenes) usen el host público de las cabeceras `X-Forwarded-Proto`/`X-Forwarded-Host`. Si la API
se expone sin proxy delante, conviene desactivarlo o configurar el proxy para que sobrescriba esas cabeceras.

## Documentación de la API

Todos los endpoints, sus parámetros y los campos de cada petición están documentados en Swagger UI:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Especificación OpenAPI: `http://localhost:8080/v3/api-docs`

## Scraper

### Cómo funciona

1. **Se registra un sitio** con su URL base (`https://novelbin.xyz/`) y sus reglas de extracción. Se
   hace una sola vez por sitio.
2. **Se prueba** la configuración con `preview` hasta que extraiga bien la ficha y el primer capítulo.
3. **Se piden novelas** por URL (`https://novelbin.xyz/novel/skyfire-avenue`). La petición se asigna al
   sitio cuya URL base la contiene y se procesa en segundo plano.
4. La ficha se guarda como **libro** (con autores, géneros, portada y estado) y cada capítulo se guarda
   **dividido en párrafos**, con sus imágenes.

Hay una petición por URL de novela: pedirla de nuevo la reutiliza y solo trae los capítulos que falten.
Así sirve para actualizar novelas en curso o continuar una que falló.

### Ejemplo: novelbin

Configuración probada para `https://novelbin.xyz/`. La ficha viene en el HTML; los capítulos se cargan
con JavaScript y sus URLs se generan a partir del número de capítulos.

```json
{
  "name": "NovelBin",
  "baseUrl": "https://novelbin.xyz/",
  "requestDelayMs": 1500,
  "javascriptRendering": "CHAPTERS",
  "waitForSelector": ".reader-content p",
  "chapterUrlTemplate": "{origin}/read/{slug}/chapter-{n}",
  "rules": [
    {"field": "TITLE",           "selector": "#ssr-novel-card h1"},
    {"field": "COVER",           "selector": "#ssr-novel-card img", "attribute": "abs:src"},
    {"field": "AUTHOR",          "selector": "#ssr-novel-card p:has(strong:containsOwn(Author))", "attribute": "ownText"},
    {"field": "STATUS",          "selector": "#ssr-novel-card p:has(strong:containsOwn(Status))", "attribute": "ownText"},
    {"field": "CHAPTER_COUNT",   "selector": "#ssr-novel-card p:has(strong:containsOwn(Total Chapters))", "attribute": "ownText"},
    {"field": "GENRE",           "selector": "#ssr-novel-card div:has(> strong:containsOwn(Genres)) a"},
    {"field": "SYNOPSIS",        "selector": "#ssr-novel-card section p"},
    {"field": "CHAPTER_TITLE",   "selector": "span.reader-header-chapter"},
    {"field": "CHAPTER_CONTENT", "selector": ".reader-content"},
    {"field": "CHAPTER_EXCLUDE", "selector": "h1.sr-only"}
  ]
}
```

Qué significa cada campo y cada tipo de regla está en Swagger, en el esquema de la petición de registrar
un sitio.

### Sitios que cargan con JavaScript

Algunos sitios entregan el HTML vacío y cargan el texto con JavaScript. Para ellos, `javascriptRendering`
abre las páginas en Chromium sin interfaz y aplica las reglas al HTML ya cargado:

| Valor | Páginas renderizadas |
|---|---|
| `NONE` | Ninguna (por defecto, lo más rápido) |
| `CHAPTERS` | Solo capítulos; la ficha y el índice se descargan directamente |
| `ALL` | Todas |

`waitForSelector` indica qué elemento esperar antes de leer la página, para no depender de que terminen
de cargar los anuncios. El navegador se comparte entre peticiones, usa una sesión limpia por página y no
descarga imágenes, fuentes ni vídeos.

### Buenas prácticas

- `requestDelayMs` (1000 ms por defecto) espacia las peticiones a un mismo host. Con renderizado, cada
  capítulo tarda unos 2–3 s.
- Revisa los términos de uso y el `robots.txt` de cada sitio antes de importar novelas completas.
- El scraper no intenta saltarse protecciones anti-bot: si un sitio bloquea las peticiones, la novela
  queda en `FAILED` con el motivo.

## Base de datos

Las migraciones están en `src/main/resources/db/migration` y Flyway las aplica al arrancar. Hibernate
valida el esquema (`ddl-auto=validate`) y nunca lo modifica.

| Migración | Contenido |
|---|---|
| `V1` | Autores, géneros, libros y sus relaciones |
| `V2` | Imágenes, capítulos, párrafos, datos de importación del libro y tablas iniciales del scraper |
| `V3` | URL base de los sitios y peticiones de novela |
| `V4`, `V5` | Renderizado con JavaScript por sitio |

No modifiques una migración ya aplicada: crea una nueva. Si al renombrar o borrar una aparece el error
`Found more than one migration with version N`, ejecuta `./mvnw clean`.

## Tests

```bash
./mvnw test -Dtest='!LibraryApplicationTests' -Dsurefire.failIfNoSpecifiedTests=false
```

Los tests unitarios no necesitan base de datos ni red: cubren la extracción con reglas HTML/JSON, la
división en párrafos y el saneado del formato, las URLs base, la normalización de URLs de novelas y el
almacenamiento en S3. `LibraryApplicationTests` levanta el contexto completo y necesita PostgreSQL.

Hay una colección de [Bruno](https://www.usebruno.com/) con todas las peticiones documentadas. Por ahora
vive fuera del repositorio.

## Notas

El `pom.xml` incluye starters de RabbitMQ, Redis, Elasticsearch y WebFlux que la aplicación todavía no
usa; no hace falta tener esos servicios en marcha.
