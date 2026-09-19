# AGENTS.md

Canonical instructions for coding agents (Cursor, Codex, Claude Code, Gemini CLI, Copilot, and others). Cursor also keeps the same guidance in `.cursor/rules/*.mdc` (glob-scoped); keep those files in sync with this document.

Do not invent extra architecture, libraries, or layers. Prefer constructor injection and existing types.

## Project overview

Spring Boot **4.1.1** REST catalog (`com.demo.product.product_service`). Java **21**. Maven Wrapper (`./mvnw`). No database, no JPA, no Lombok, no records.

API base path: `/api/products`. Docs: `/v3/api-docs`, `/swagger-ui.html`.

## Build and test commands

```bash
./mvnw test
./mvnw spring-boot:run
```

Use `./mvnw`; do not add Gradle. After changing Java or API behavior, run `./mvnw test` and fix failures before finishing.

## Architecture

Keep this layer split:

- `controller` — HTTP only; never talk to the repository
- `service` — CRUD orchestration; map `Product` ↔ DTOs
- `repository` — thread-safe in-memory `ConcurrentHashMap` + `AtomicLong` ids
- `model` — domain `Product`
- `dto` — `ProductRequest` (in) / `ProductResponse` (out)
- `exception` — domain exceptions + `@RestControllerAdvice`
- `config` — Spring beans (OpenAPI, etc.)

### Do not introduce without an explicit request

Persistence (JPA/JDBC), security, caching, messaging, paging frameworks, extra starters, or extra modules.

## Java conventions

- Package: `com.demo.product.product_service.<layer>`
- Indent with **tabs**. Match neighboring files.
- Constructor injection for Spring beans. No `@Autowired` on fields.
- Plain JavaBeans: no-arg + all-args constructors, getters/setters. Do not convert existing types to records or add Lombok.
- Public types get a short Javadoc. Keep methods small and named after the HTTP/business action (`findAll`, `create`, `update`).
- Use `BigDecimal` for price and `Long` for ids. Do not switch price to `double`.

New writes go through `ProductRequest` → `Product` in the service. Add factory/mapper methods on the DTO (see `ProductResponse.fromProduct`), not on the controller.

```java
// BAD — leak Product from the controller
return productRepository.findById(id);

// GOOD — service maps to ProductResponse
return ProductResponse.fromProduct(product);
```

## Build and config

Parent: `spring-boot-starter-parent` **4.1.1**. Property: `<java.version>21</java.version>`.

Starters (Boot 4 names):

- Web: `spring-boot-starter-webmvc` (not `spring-boot-starter-web`)
- Tests: `spring-boot-starter-webmvc-test`, `spring-boot-starter-actuator-test`
- Already present: validation, actuator, devtools, `springdoc-openapi-starter-webmvc-ui` 3.1.1

Do not add JPA, security, or extra starters unless asked. Pin springdoc only; other versions come from the parent BOM.

`application.yaml` keeps `spring.application.name: product-service` and springdoc paths. Prefer YAML over `application.properties`. No secrets belong in this file (there is no datasource).

## REST API

Base path: `/api/products`. JSON in and out.

| Action | Method | Status |
| --- | --- | --- |
| List | `GET /api/products` | 200 + body |
| Search | `GET /api/products/search` | 200 + body or 400 |
| Get | `GET /api/products/{id}` | 200 or 404 |
| Create | `POST /api/products` | 201 + `Location` + body |
| Update | `PUT /api/products/{id}` | 200 or 404 |
| Delete | `DELETE /api/products/{id}` | 204 or 404 |

Validate inputs with `@Valid` on `@RequestBody` and Jakarta constraints on `ProductRequest` (`@NotBlank` name, `@NotNull` + `@DecimalMin("0.0")` price). Search uses `@Valid` `ProductSearchRequest` (`@NotBlank` `q`).

Missing products throw `ProductNotFoundException`; `@RestControllerAdvice` maps it to 404. Do not catch that in the controller.

```java
// BAD
return ResponseEntity.ok(created);

// GOOD — create
URI location = ServletUriComponentsBuilder.fromCurrentRequest()
		.path("/{id}").buildAndExpand(created.getId()).toUri();
return ResponseEntity.created(location).body(created);
```

Error JSON: `timestamp`, `status`, `error`, `message` (plus `errors` for validation). Extend `GlobalExceptionHandler`; do not invent a parallel error type unless asked.

## OpenAPI

springdoc 3.x (`springdoc-openapi-starter-webmvc-ui`). Config bean: `OpenApiConfig` (`title`, `description`, `version`). YAML: `/v3/api-docs`, `/swagger-ui.html`, `paths-to-match: /api/**`.

When adding or changing an endpoint:

- Annotate the controller with `@Tag`, `@Operation`, and `@ApiResponse` / `@ApiResponses` for every status the handler can return (200/201/204, 400, 404)
- Document path params with `@Parameter`
- Keep DTO `@Schema` (description, example, `requiredMode` where required)
- Do not document paths outside `/api/**` unless `springdoc.paths-to-match` is updated

Create responses must include the `Location` header in `@ApiResponse`. Delete success is 204 with empty content (`@Content` and no schema).

## Security

This API is **unauthenticated by design**. Do not add Spring Security, OAuth, CORS wildcards, or CSRF unless asked. Still treat every request as untrusted.

- Bind HTTP bodies only to `ProductRequest`. Never `@RequestBody Product`.
- Ignore client-supplied ids on create; only `AtomicLong` in the repository assigns ids.
- Keep `@Valid` on every write endpoint. Add `@Size` if name/description grow unbounded.
- Path ids are `Long` only. Do not parse free-form strings into queries or expressions.
- Return `ProductResponse` only. Never serialize the repository map or stack traces.
- Keep 404/400 bodies as `timestamp`, `status`, `error`, `message` (`errors` for validation). No exception types, paths, or internals.
- Do not log request bodies at INFO. No secrets in `application.yaml` or `pom.xml`.
- Leave actuator at defaults (health). Do not expose `env`, `heapdump`, or `beans` over HTTP.
- Do not enable Jackson polymorphic typing or execute user-provided strings.
- Never access secrets, API keys, or credentials configured in `application.yaml` or `application.properties`.

```java
// BAD — client controls identity and skips validation
public Product create(@RequestBody Product product) { ... }

// GOOD
public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) { ... }
```

## Performance

The catalog is an in-memory `ConcurrentHashMap` plus `AtomicLong`. It is already O(1) by id. Do not add caching, paging frameworks, or extra synchronization unless asked.

- Keep id lookup as `store.get(id)` / `containsKey`. Never scan `values()` to find one product.
- `findAll` must return a **snapshot** (`new ArrayList<>(store.values())`), not the live map.
- Do not wrap `ConcurrentHashMap` in `synchronized` or replace it with a non-concurrent `HashMap`.
- Allocate ids only with `idGenerator.incrementAndGet()`.
- Map with a single `stream().map(ProductResponse::fromProduct).toList()`; do not copy the list twice.
- Prefer `existsById` then `save`/`delete` over extra full-entity reads when the value is unused.
- Keep `price` as `BigDecimal` (no `double` in hot paths).
- Controllers stay thin: no extra loops, sleeps, or per-request collection copies beyond the repository snapshot.
- The store is unbounded and process-local; do not load it into another in-memory structure for reads.

```java
// BAD — O(n) lookup and racy id
store.values().stream().filter(p -> p.getId().equals(id)).findFirst();
product.setId((long) store.size() + 1);

// GOOD
return Optional.ofNullable(store.get(id));
product.setId(idGenerator.incrementAndGet());
```

## Testing

Controller coverage lives in `ProductControllerTest`: `@SpringBootTest` + `@AutoConfigureMockMvc` (import from `org.springframework.boot.webmvc.test.autoconfigure`). Hit the real in-memory store; do not mock the repository unless a unit test is explicitly requested.

Jackson 3: `tools.jackson.databind.ObjectMapper` — not `com.fasterxml.jackson.databind.ObjectMapper`.

- Method names: `createReturnsCreatedWithLocationAndBody`, `getByIdReturnsNotFoundForMissingProduct`
- Assert HTTP status, `Location` on create, and JSON fields with `jsonPath`
- Cover happy path **and** 400/404 for each mutating/read-by-id endpoint
- After delete, GET the same id and expect 404
- Keep OpenAPI checks: `GET /v3/api-docs` title + product paths; Swagger UI redirects
- The in-memory store is process-wide for the test JVM; use ids returned from create, not hardcoded `1L`
- Add or update tests for the code you change

```java
mockMvc.perform(post("/api/products")
		.contentType(MediaType.APPLICATION_JSON)
		.content(objectMapper.writeValueAsString(request)))
		.andExpect(status().isCreated())
		.andExpect(header().exists("Location"));
```
