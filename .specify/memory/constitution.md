<!--
Sync Impact Report
- Version change: (unfilled scaffold) → 1.0.0
- Modified principles:
  - [PRINCIPLE_1_NAME] → I. Layered Catalog Architecture
  - [PRINCIPLE_2_NAME] → II. REST JSON Contract
  - [PRINCIPLE_3_NAME] → III. Test-First HTTP Behavior (NON-NEGOTIABLE)
  - [PRINCIPLE_4_NAME] → IV. Contract Documentation Tests
  - [PRINCIPLE_5_NAME] → V. Simplicity and Explicit Scope
- Added sections:
  - Technology and Runtime Constraints
  - Quality Gates and Review
  - Governance (filled from project rules)
- Removed sections: none (template HTML comments and placeholders replaced)
- Follow-up TODOs: none
-->

# Product Service Constitution

## Core Principles

### I. Layered Catalog Architecture
The service MUST keep a strict layer split. The controller handles HTTP only
and MUST NOT call the repository. The service orchestrates CRUD and maps
`Product` to DTOs. The repository is a thread-safe in-memory
`ConcurrentHashMap` plus `AtomicLong` ids. Domain types live in `model`;
request/response types live in `dto`; domain exceptions and
`@RestControllerAdvice` live in `exception`; Spring beans live in `config`.

Rationale: Mixing HTTP, mapping, and storage in one type makes the catalog
untestable at the boundary and invites leaking `Product` over the wire.

### II. REST JSON Contract
The public API base path MUST be `/api/products`. Payloads MUST be JSON.
Handlers MUST use these statuses: list 200; get 200 or 404; create 201 with
`Location` and body; update 200 or 404; delete 204 or 404. Write endpoints
MUST use `@Valid` on `@RequestBody ProductRequest`. Missing products MUST
throw `ProductNotFoundException` (mapped to 404); the controller MUST NOT
catch it. Error JSON MUST include `timestamp`, `status`, `error`, and
`message`, plus `errors` for validation.

Rationale: Clients and OpenAPI stay aligned only when verbs, statuses, and
error shape are fixed.

### III. Test-First HTTP Behavior (NON-NEGOTIABLE)
API behavior MUST be proven in `ProductControllerTest` with `@SpringBootTest`
and `@AutoConfigureMockMvc` (import from
`org.springframework.boot.webmvc.test.autoconfigure`) against the real
in-memory store. Tests MUST use Jackson 3
`tools.jackson.databind.ObjectMapper`. Tests MUST cover the happy path and
400/404 for each mutating and read-by-id endpoint, then GET after delete and
expect 404. Product ids MUST come from create responses, not hardcoded `1L`.
Do not mock the repository unless a unit test is explicitly requested.

Rationale: The store is process-local; MockMvc against the real map is the
contract. Mocked repositories hide mapping and status bugs.

### IV. Contract Documentation Tests
OpenAPI MUST stay in sync with handlers. `OpenApiConfig` MUST set title,
description, and version. YAML MUST expose `/v3/api-docs` and
`/swagger-ui.html` with `paths-to-match: /api/**`. Each handler MUST have
`@Tag`, `@Operation`, and `@ApiResponse` / `@ApiResponses` for every status
it can return. Create MUST document the `Location` header. Delete 204 MUST
use empty `@Content` with no schema. DTOs MUST keep `@Schema`. Tests MUST
assert OpenAPI title and product paths and that Swagger UI redirects.

Rationale: Undocumented statuses become silent breaking changes for API
consumers.

### V. Simplicity and Explicit Scope
Do not introduce persistence (JPA/JDBC), Spring Security, caching, messaging,
paging frameworks, extra starters, or extra modules without an explicit
request. Prefer constructor injection and existing types. Do not convert
existing types to records or add Lombok. Price MUST be `BigDecimal`; ids MUST
be `Long`.

Rationale: This catalog is an in-memory training service. Extra modules
change the threat model and the runtime without a stated need.

## Technology and Runtime Constraints

Stack MUST remain Spring Boot 4.1.1, Java 21, and Maven Wrapper (`./mvnw`).
Do not add Gradle. Web starter MUST be `spring-boot-starter-webmvc` (not
`spring-boot-starter-web`). Pin springdoc only
(`springdoc-openapi-starter-webmvc-ui` 3.1.1); other versions come from the
parent BOM. Config MUST use `application.yaml` (not `application.properties`)
with `spring.application.name: product-service`.

Java types MUST live under `com.demo.product.product_service.<layer>`.
Indent with tabs. Spring beans MUST use constructor injection; field
`@Autowired` is forbidden. Types MUST be plain JavaBeans (no-arg and
all-args constructors, getters/setters). Public types MUST have a short
Javadoc. Methods MUST stay small and named after the HTTP/business action
(`findAll`, `create`, `update`). Mapping MUST live on DTOs/service
(`ProductResponse.fromProduct`), never by returning `Product` from the
controller.

Security: the API is unauthenticated by design. Do not add OAuth, CORS
wildcards, or CSRF unless asked. Bind bodies only to `ProductRequest`; never
`@RequestBody Product`. Ignore client-supplied ids on create; only
`AtomicLong` assigns ids. Path ids MUST be `Long`. Return `ProductResponse`
only. Do not serialize the repository map, stack traces, exception types, or
internal paths. Do not log request bodies at INFO. No secrets in
`application.yaml` or `pom.xml`. Never read secrets, API keys, or
credentials from config. Leave actuator at defaults (health); do not expose
`env`, `heapdump`, or `beans`. Do not enable Jackson polymorphic typing or
execute user-provided strings. Add `@Size` when name or description would
otherwise be unbounded.

Performance: id lookup MUST use `store.get(id)` / `containsKey`, never a
`values()` scan. `findAll` MUST return a snapshot
(`new ArrayList<>(store.values())`). Do not wrap `ConcurrentHashMap` in
`synchronized` or replace it with `HashMap`. Allocate ids only with
`idGenerator.incrementAndGet()`. Map lists with a single
`stream().map(ProductResponse::fromProduct).toList()`. Prefer `existsById`
then `save`/`delete` when the entity value is unused. Controllers MUST stay
thin (no extra loops, sleeps, or per-request copies beyond the repository
snapshot). Do not add caching or extra in-memory indexes for reads.

## Quality Gates and Review

Before finishing a change that touches Java or API behavior, `./mvnw test`
MUST pass. Reviews MUST verify layer split, DTO mapping, status codes,
OpenAPI annotations, and that tests use Jackson 3 and ids from create.
Controller tests MUST assert HTTP status, `Location` on create, and JSON
fields with `jsonPath`. Runtime agent guidance lives in `AGENTS.md` and
`.cursor/rules/*.mdc`; those files MUST stay consistent with this
constitution. Complexity (new modules, synchronization, or parallel error
types) MUST be justified by an explicit user request.

## Governance

This constitution supersedes ad-hoc practices, README shortcuts, and prior
chat conventions when they conflict. Amendments MUST update this file,
bump `CONSTITUTION_VERSION` (MAJOR for removed or incompatible principles,
MINOR for new or expanded principles/sections, PATCH for clarifications),
set `LAST_AMENDED_DATE` to the amendment day (ISO `YYYY-MM-DD`), and record
a Sync Impact Report for review. Compliance review is expected on every
pull request and agent task that changes `src/`, `pom.xml`, or
`application.yaml`. Use `/speckit-constitution` to amend governance; do not
edit dependent Spec Kit templates from this command.

**Version**: 1.0.0 | **Ratified**: 2026-09-19 | **Last Amended**: 2026-09-19
