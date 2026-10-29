<a id="readme-top"></a>

<!-- PROJECT SHIELDS -->
[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![MIT License][license-shield]][license-url]



<!-- PROJECT LOGO -->
<br />
<div align="center">
  <h3 align="center">Library-SB</h3>

  <p align="center">
    API de biblioteca con un scraper configurable de novelas web
    <br />
    <a href="#documentación-de-la-api"><strong>Ver la documentación »</strong></a>
    <br />
    <br />
    <a href="https://github.com/jcastaneda30/Library-SB/issues/new?labels=bug">Reportar un error</a>
    &middot;
    <a href="https://github.com/jcastaneda30/Library-SB/issues/new?labels=enhancement">Proponer una mejora</a>
  </p>
</div>



<!-- TABLE OF CONTENTS -->
<details>
  <summary>Contenido</summary>
  <ol>
    <li>
      <a href="#acerca-del-proyecto">Acerca del proyecto</a>
      <ul>
        <li><a href="#construido-con">Construido con</a></li>
        <li><a href="#arquitectura">Arquitectura</a></li>
      </ul>
    </li>
    <li>
      <a href="#empezando">Empezando</a>
      <ul>
        <li><a href="#prerrequisitos">Prerrequisitos</a></li>
        <li><a href="#instalación">Instalación</a></li>
        <li><a href="#configuración">Configuración</a></li>
      </ul>
    </li>
    <li>
      <a href="#uso">Uso</a>
      <ul>
        <li><a href="#documentación-de-la-api">Documentación de la API</a></li>
        <li><a href="#scraper">Scraper</a></li>
      </ul>
    </li>
    <li><a href="#desarrollo">Desarrollo</a></li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#contribuir">Contribuir</a></li>
    <li><a href="#licencia">Licencia</a></li>
    <li><a href="#contacto">Contacto</a></li>
    <li><a href="#agradecimientos">Agradecimientos</a></li>
  </ol>
</details>



<!-- ABOUT THE PROJECT -->
## Acerca del proyecto

Library-SB es una API REST para gestionar una biblioteca de libros y novelas web. Además de autores,
géneros y libros, incluye un **scraper configurable** que extrae novelas de sitios web y las guarda en la
biblioteca:

* Cada sitio se configura una vez con su URL base y reglas de extracción (selectores CSS o JSON Pointer),
  sin escribir código.
* De cada novela se guarda la ficha (título, portada, autores, géneros, sinopsis, estado y número de
  capítulos) y sus capítulos divididos en **párrafos individuales**, conservando el formato del texto
  (negrita, cursiva, subrayado, color...) y las imágenes.
* Funciona también con sitios que cargan el contenido con JavaScript, abriéndolos en Chromium sin interfaz.
* Las imágenes se guardan en disco o en un bucket de Amazon S3.

<p align="right">(<a href="#readme-top">volver arriba</a>)</p>



### Construido con

* [![Java][Java-badge]][Java-url]
* [![Spring Boot][SpringBoot-badge]][SpringBoot-url]
* [![PostgreSQL][PostgreSQL-badge]][PostgreSQL-url]
* [![Flyway][Flyway-badge]][Flyway-url]
* [![jsoup][jsoup-badge]][jsoup-url]
* [![Playwright][Playwright-badge]][Playwright-url]
* [![Amazon S3][S3-badge]][S3-url]
* [![Swagger][Swagger-badge]][Swagger-url]
* [![Docker][Docker-badge]][Docker-url]
* [![Maven][Maven-badge]][Maven-url]

<p align="right">(<a href="#readme-top">volver arriba</a>)</p>



### Arquitectura

El código está organizado **por funcionalidad**: cada carpeta agrupa su controlador, DTOs, modelo,
repositorio y servicio.

```
src/main/java/com/library/
├── LibraryApplication.java
├── common/          Auditoría, configuración de Swagger y manejo global de errores
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

La dependencia va en un solo sentido: **`scraper` usa los servicios de `library`**, pero `library` no sabe
que el scraper existe. Todo el procesamiento (descargar, interpretar, sanear) ocurre en `scraper`;
`library` solo guarda y consulta.

<p align="right">(<a href="#readme-top">volver arriba</a>)</p>



<!-- GETTING STARTED -->
## Empezando

### Prerrequisitos

* JDK 17 o superior
* Docker (para PostgreSQL), o un PostgreSQL 17 propio

### Instalación

1. Clona el repositorio
   ```sh
   git clone https://github.com/jcastaneda30/Library-SB.git
   cd Library-SB
   ```
2. Crea el archivo de variables de entorno y cambia al menos `POSTGRES_PASSWORD`. Lo leen tanto Docker
   Compose como Spring y no se sube al repositorio.
   ```sh
   cp .env.example .env
   ```
3. Levanta PostgreSQL. Al arrancar la aplicación, Flyway crea las tablas.
   ```sh
   docker compose up -d
   ```
4. Instala Chromium (una vez por máquina; solo hace falta para sitios que cargan con JavaScript)
   ```sh
   ./mvnw exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"
   ```
5. Arranca la aplicación
   ```sh
   ./mvnw spring-boot:run
   ```

La API queda en `http://localhost:8080`.

> Las variables `POSTGRES_*` solo se aplican la primera vez que se crea el volumen del contenedor. Para
> empezar de cero: `docker compose down -v` (borra los datos).

### Configuración

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

Los valores por defecto de la conexión son solo para desarrollo local. Si usas `compose.yaml`, define las
cinco variables `POSTGRES_*` en `.env` para que la aplicación y el contenedor coincidan.

**Imágenes en S3.** Con `STORAGE_TYPE=s3`, las credenciales **no** van en la configuración: se usan las
de la cadena estándar de AWS (`AWS_ACCESS_KEY_ID`/`AWS_SECRET_ACCESS_KEY`, `~/.aws/credentials` o el rol
IAM). El bucket puede ser privado: las imágenes se entregan con URLs prefirmadas de 1 hora. Permisos
necesarios: `s3:PutObject` y `s3:GetObject` sobre `arn:aws:s3:::<bucket>/images/*`.

**Detrás de un proxy.** Las URLs absolutas que devuelve la API (por ejemplo, las de imágenes) usan el
host público de las cabeceras `X-Forwarded-Proto`/`X-Forwarded-Host`. Si la API se expone sin proxy
delante, conviene desactivar `server.forward-headers-strategy` o configurar el proxy para que
sobrescriba esas cabeceras.

<p align="right">(<a href="#readme-top">volver arriba</a>)</p>



<!-- USAGE EXAMPLES -->
## Uso

### Documentación de la API

Todos los endpoints, sus parámetros y los campos de cada petición están documentados en Swagger UI:

* Swagger UI: [`http://localhost:8080/swagger-ui.html`](http://localhost:8080/swagger-ui.html)
* Especificación OpenAPI: `http://localhost:8080/v3/api-docs`

### Scraper

1. **Registra un sitio** con su URL base (`https://novelbin.xyz/`) y sus reglas de extracción. Se hace
   una sola vez por sitio.
2. **Pruébalo** con *preview* hasta que extraiga bien la ficha y el primer capítulo. No guarda nada.
3. **Pide novelas** por URL (`https://novelbin.xyz/novel/skyfire-avenue`). La petición se asigna al
   sitio cuya URL base la contiene y se procesa en segundo plano.
4. La ficha se guarda como **libro** y cada capítulo **dividido en párrafos**, con sus imágenes.

Hay una petición por URL de novela: pedirla de nuevo la reutiliza y solo trae los capítulos que falten.
Así sirve para actualizar novelas en curso o continuar una que falló.

Configuración probada para `https://novelbin.xyz/`. La ficha viene en el HTML; los capítulos se cargan
con JavaScript y sus URLs se generan a partir del número de capítulos:

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

Qué significa cada campo, cada tipo de regla y cada variable de las plantillas de URL está en Swagger, en
el esquema de la petición de registrar un sitio.

**Sitios que cargan con JavaScript.** `javascriptRendering` indica qué páginas se abren en Chromium sin
interfaz: `NONE` (ninguna, por defecto), `CHAPTERS` (solo capítulos) o `ALL` (todas). Con
`waitForSelector` se espera a que aparezca el contenido en vez de a que terminen de cargar los anuncios.

**Buenas prácticas.**
* `requestDelayMs` (1000 ms por defecto) espacia las peticiones a un mismo host. Con renderizado, cada
  capítulo tarda unos 2–3 s.
* Revisa los términos de uso y el `robots.txt` de cada sitio antes de importar novelas completas.
* El scraper no intenta saltarse protecciones anti-bot: si un sitio bloquea las peticiones, la novela
  queda en `FAILED` con el motivo.

<p align="right">(<a href="#readme-top">volver arriba</a>)</p>



<!-- DEVELOPMENT -->
## Desarrollo

**Tests.** Los unitarios no necesitan base de datos ni red:
```sh
./mvnw test -Dtest='!LibraryApplicationTests' -Dsurefire.failIfNoSpecifiedTests=false
```
`LibraryApplicationTests` levanta el contexto completo y necesita PostgreSQL.

**Migraciones.** Están en `src/main/resources/db/migration` y Flyway las aplica al arrancar; Hibernate
solo valida el esquema (`ddl-auto=validate`). No modifiques una migración ya aplicada: crea una nueva. Si
al renombrar o borrar una aparece `Found more than one migration with version N`, ejecuta `./mvnw clean`.

**Dependencias sin uso.** El `pom.xml` incluye starters de RabbitMQ, Redis, Elasticsearch y WebFlux que la
aplicación todavía no usa; no hace falta tener esos servicios en marcha.

<p align="right">(<a href="#readme-top">volver arriba</a>)</p>



<!-- ROADMAP -->
## Roadmap

- [x] Autores, géneros y libros
- [x] Scraper configurable por sitio
- [x] Capítulos divididos en párrafos con formato e imágenes
- [x] Renderizado de sitios que cargan con JavaScript
- [x] Almacenamiento de imágenes en S3
- [x] Documentación con Swagger
- [ ] Autenticación y permisos
- [ ] Tests de integración con base de datos
- [ ] Quitar los starters que no se usan
- [ ] Incluir la colección de Bruno en el repositorio

Consulta los [issues abiertos](https://github.com/jcastaneda30/Library-SB/issues) para ver la lista
completa de mejoras propuestas y problemas conocidos.

<p align="right">(<a href="#readme-top">volver arriba</a>)</p>



<!-- CONTRIBUTING -->
## Contribuir

Las contribuciones son bienvenidas. Si tienes una sugerencia, haz un fork del repositorio y abre un pull
request, o abre un issue con la etiqueta "enhancement".

1. Haz un fork del proyecto
2. Crea una rama para tu cambio (`git checkout -b feature/MiCambio`)
3. Haz commit de tus cambios (`git commit -m 'Añade MiCambio'`)
4. Sube la rama (`git push origin feature/MiCambio`)
5. Abre un pull request

### Principales contribuidores

<a href="https://github.com/jcastaneda30/Library-SB/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=jcastaneda30/Library-SB" alt="contrib.rocks image" />
</a>

<p align="right">(<a href="#readme-top">volver arriba</a>)</p>



<!-- LICENSE -->
## Licencia

Distribuido bajo la licencia MIT. Consulta [`LICENSE`](LICENSE) para más información.

La licencia cubre el código de este proyecto, no el contenido que se importa con el scraper: las novelas
siguen perteneciendo a sus autores y traductores.

<p align="right">(<a href="#readme-top">volver arriba</a>)</p>



<!-- CONTACT -->
## Contacto

jcastaneda30 - [@jcastaneda30](https://github.com/jcastaneda30)

Enlace del proyecto: [https://github.com/jcastaneda30/Library-SB](https://github.com/jcastaneda30/Library-SB)

<p align="right">(<a href="#readme-top">volver arriba</a>)</p>



<!-- ACKNOWLEDGMENTS -->
## Agradecimientos

* [Best-README-Template](https://github.com/othneildrew/Best-README-Template)
* [Choose an Open Source License](https://choosealicense.com)
* [jsoup](https://jsoup.org)
* [Playwright for Java](https://playwright.dev/java/)
* [springdoc-openapi](https://springdoc.org)
* [Img Shields](https://shields.io)

<p align="right">(<a href="#readme-top">volver arriba</a>)</p>



<!-- MARKDOWN LINKS & IMAGES -->
[contributors-shield]: https://img.shields.io/github/contributors/jcastaneda30/Library-SB.svg?style=for-the-badge
[contributors-url]: https://github.com/jcastaneda30/Library-SB/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/jcastaneda30/Library-SB.svg?style=for-the-badge
[forks-url]: https://github.com/jcastaneda30/Library-SB/network/members
[stars-shield]: https://img.shields.io/github/stars/jcastaneda30/Library-SB.svg?style=for-the-badge
[stars-url]: https://github.com/jcastaneda30/Library-SB/stargazers
[issues-shield]: https://img.shields.io/github/issues/jcastaneda30/Library-SB.svg?style=for-the-badge
[issues-url]: https://github.com/jcastaneda30/Library-SB/issues
[license-shield]: https://img.shields.io/github/license/jcastaneda30/Library-SB.svg?style=for-the-badge
[license-url]: https://github.com/jcastaneda30/Library-SB/blob/main/LICENSE
[Java-badge]: https://img.shields.io/badge/Java_17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white
[Java-url]: https://openjdk.org/
[SpringBoot-badge]: https://img.shields.io/badge/Spring_Boot_4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white
[SpringBoot-url]: https://spring.io/projects/spring-boot
[PostgreSQL-badge]: https://img.shields.io/badge/PostgreSQL_17-4169E1?style=for-the-badge&logo=postgresql&logoColor=white
[PostgreSQL-url]: https://www.postgresql.org/
[Flyway-badge]: https://img.shields.io/badge/Flyway-CC0200?style=for-the-badge&logo=flyway&logoColor=white
[Flyway-url]: https://flywaydb.org/
[jsoup-badge]: https://img.shields.io/badge/jsoup-263238?style=for-the-badge
[jsoup-url]: https://jsoup.org/
[Playwright-badge]: https://img.shields.io/badge/Playwright-2EAD33?style=for-the-badge&logo=playwright&logoColor=white
[Playwright-url]: https://playwright.dev/java/
[S3-badge]: https://img.shields.io/badge/Amazon_S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white
[S3-url]: https://aws.amazon.com/s3/
[Swagger-badge]: https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black
[Swagger-url]: https://springdoc.org/
[Docker-badge]: https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white
[Docker-url]: https://www.docker.com/
[Maven-badge]: https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white
[Maven-url]: https://maven.apache.org/
