# Implementation Plan: Product Catalog Search

**Branch**: `001-add-search-endpoint` | **Date**: 2026-09-19 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-add-search-endpoint/spec.md`

**Note**: `/speckit-plan` argument `create 10 tasks` is recorded as a decomposition constraint. This command does not write `tasks.md`; `/speckit-tasks` MUST emit exactly **10** tasks matching the seed below.

## Summary

Add a dedicated, unauthenticated catalog search so consumers can find products by a
single text term in name or description (case-insensitive, appears-anywhere) without
listing the full catalog. `GET /api/products/search?q=` returns `200` + `ProductResponse`
array (empty list when nothing matches). Missing or whitespace-only `q` returns `400`
with the existing validation error JSON. Implementation stays inside the current
controller → service → repository split: snapshot `findAll()`, filter, single
`map` to DTOs. No paging, index, JPA, or security module.

## Technical Context

**Language/Version**: Java 21

**Primary Dependencies**: Spring Boot 4.1.1 (`spring-boot-starter-webmvc`, validation),
springdoc-openapi 3.1.1

**Storage**: In-memory `ConcurrentHashMap` + `AtomicLong` (existing `ProductRepository`).
No new store.

**Testing**: JUnit 5, `@SpringBootTest` + `@AutoConfigureMockMvc`, Jackson 3
`tools.jackson.databind.ObjectMapper`, real in-memory store (`ProductControllerTest`)

**Target Platform**: Linux JVM service (Spring Boot)

**Project Type**: REST web service (single Maven module)

**Performance Goals**: Search completes in under 2 seconds for catalogs of a few
hundred products (SC-001)

**Constraints**: Constitution v1.0.0 — no JPA, security, caching, paging, extra
modules; controller MUST NOT call repository; no `values()` scan to look up one id;
no extra search index; `BigDecimal` price, `Long` ids; tabs; constructor injection

**Scale/Scope**: Process-local unbounded catalog; return all matches in one response;
one new read path plus tests and OpenAPI

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate | Status | Notes |
|------|--------|-------|
| I. Layered catalog | Pass | Search orchestration in `ProductService`; HTTP only in `ProductController` |
| II. REST JSON contract | Pass | Base `/api/products`; 200 + body or 400 validation; empty match is 200 not 404 |
| III. Test-first MockMvc | Pass | New cases in `ProductControllerTest`; real store; ids from create |
| IV. OpenAPI in sync | Pass | Annotate search 200/400; extend `/v3/api-docs` assertion |
| V. Simplicity | Pass | No JPA, Security, cache, paging, extra starters |
| Query param validation | Pass | Bind `q` to a JavaBean + `@Valid` so `MethodArgumentNotValidException` reuses `GlobalExceptionHandler` (extend handler only if that binding cannot fire) |
| Performance | Pass | Filter the `findAll()` snapshot; do not add an index or wrap the map in `synchronized` |

**Post-design re-check**: Still Pass. Contract is GET search on a literal `/search` path
(not `/{id}`) so `"search"` is never parsed as a product id. Complexity Tracking unused.

## Project Structure

### Documentation (this feature)

```text
specs/001-add-search-endpoint/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
src/main/java/com/demo/product/product_service/
├── ProductServiceApplication.java
├── config/OpenApiConfig.java
├── controller/ProductController.java      # add GET /search
├── dto/ProductRequest.java
├── dto/ProductResponse.java
├── dto/ProductSearchRequest.java          # new query JavaBean
├── exception/GlobalExceptionHandler.java
├── exception/ProductNotFoundException.java
├── model/Product.java
├── repository/ProductRepository.java      # unchanged snapshot findAll
└── service/ProductService.java            # add search(String)

src/test/java/com/demo/product/product_service/
├── ProductServiceApplicationTests.java
└── controller/ProductControllerTest.java  # search scenarios

src/main/resources/application.yaml
pom.xml
AGENTS.md
.cursor/rules/rest-api.mdc
```

**Structure Decision**: Existing single Maven Spring Boot module. Do not add packages,
modules, or a separate search store.

## Task seed (exactly 10)

`/speckit-tasks` MUST produce **10** tasks (no more, no fewer), in this order:

1. Add `ProductSearchRequest` JavaBean (`q` required, `@NotBlank`, `@Schema`).
2. Add `ProductService.search` over `findAll()` snapshot (trim, case-insensitive
   contains on name or description, preserve listing order, `ProductResponse.fromProduct`).
3. Add `GET /api/products/search` on `ProductController` with `@Valid` query bean,
   `@Tag`/`@Operation`/`@ApiResponses` for 200 and 400.
4. MockMvc: name match returns only the matching product (P1).
5. MockMvc: description match and case-insensitive match (P1).
6. MockMvc: several matches keep the same relative order as `GET /api/products`.
7. MockMvc: no matches and empty catalog return 200 with `[]` (P2).
8. MockMvc: missing `q` and whitespace-only `q` return 400 with `errors` (P3).
9. MockMvc: `/v3/api-docs` includes `/api/products/search` and documents 200/400.
10. Update `AGENTS.md` and `.cursor/rules/rest-api.mdc` with the search row (keep
    constitution/docs in sync). Do not add JPA, paging, or Spring Security.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

None.
