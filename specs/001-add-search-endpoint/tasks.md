# Tasks: Product Catalog Search

**Input**: Design documents from `/specs/001-add-search-endpoint/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Included. Spec independent tests, constitution Test-First, and plan seed require MockMvc coverage in `ProductControllerTest`.

**Organization**: Tasks are grouped by user story. Count is **exactly 10** (plan.md task seed). Existing Maven Spring Boot module — no new project or starters.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- Single Maven module: `src/main/java/`, `src/test/java/` at repository root

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project already initialized (Spring Boot 4.1.1, Java 21, `./mvnw`). No setup tasks.

**Checkpoint**: Use existing layout; do not add JPA, paging, security, or extra modules.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Shared search request, service filter, and HTTP mapping. MUST complete before story tests.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T001 Add `ProductSearchRequest` JavaBean in `src/main/java/com/demo/product/product_service/dto/ProductSearchRequest.java` with field `q` (String, required, `@NotBlank` so null/empty/whitespace-only are invalid), no-arg + all-args constructors, getters/setters, short Javadoc, and `@Schema`
- [X] T002 Add `ProductService.search` in `src/main/java/com/demo/product/product_service/service/ProductService.java` using `productRepository.findAll()` snapshot, `trim()` before match, case-insensitive contains on name **or** description, missing/blank description treated as no text, one `ProductResponse` per product, preserve `findAll()` relative order, map with `ProductResponse.fromProduct`
- [X] T003 Add `GET /api/products/search` (literal `/search`, not `/{id}`) on `src/main/java/com/demo/product/product_service/controller/ProductController.java` binding `@Valid ProductSearchRequest` as query (not `@RequestBody Product`), returning `List<ProductResponse>`, with `@Operation`/`@ApiResponses` for 200 (array) and 400 (validation JSON)

**Checkpoint**: Foundation ready — `GET /api/products/search?q=` can be exercised by story tests

---

## Phase 3: User Story 1 - Find products by search text (Priority: P1) 🎯 MVP

**Goal**: Consumers search by a term and get only products whose name or description contains it, in listing order.

**Independent Test**: Create at least two products with distinct names, search a term that matches only one, and confirm the other is omitted. Also match on description and ignore case.

### Tests for User Story 1

> Write these in `ProductControllerTest` after T003. Use Jackson 3 `ObjectMapper`, ids from create (not `1L`), and `jsonPath`.

- [X] T004 [US1] Add MockMvc test `searchReturnsMatchingProductsByName` in `src/test/java/com/demo/product/product_service/controller/ProductControllerTest.java` — create two products, `GET /api/products/search?q=` with a name fragment, expect 200 and only the matching product
- [X] T005 [US1] Add MockMvc tests in `src/test/java/com/demo/product/product_service/controller/ProductControllerTest.java` for description match (`q` in description only) and case-insensitive match (`Widget` vs `widget`)
- [X] T006 [US1] Add MockMvc test `searchPreservesListingOrder` in `src/test/java/com/demo/product/product_service/controller/ProductControllerTest.java` — several matches MUST appear in the same relative order as `GET /api/products`

**Checkpoint**: User Story 1 is independently testable (name, description, case, order)

---

## Phase 4: User Story 2 - Empty results are a successful search (Priority: P2)

**Goal**: No matches return a successful empty list, not a missing-resource error.

**Independent Test**: Search a term that appears in no name or description and expect 200 with `[]`.

- [X] T007 [US2] Add MockMvc tests in `src/test/java/com/demo/product/product_service/controller/ProductControllerTest.java` for no matches and (where practical) empty-catalog search — both `200` with `[]`, never 404

**Checkpoint**: User Stories 1 and 2 both work independently

---

## Phase 5: User Story 3 - Reject unusable search text (Priority: P3)

**Goal**: Missing or whitespace-only `q` is rejected as invalid input with the catalog validation JSON.

**Independent Test**: Omit `q` and send only spaces; both return 400 with `errors.q`.

- [X] T008 [US3] Add MockMvc tests in `src/test/java/com/demo/product/product_service/controller/ProductControllerTest.java` for missing `q` and whitespace-only `q` — expect 400, `$.status` 400, `$.message`, and `$.errors.q`

**Checkpoint**: All user stories independently functional

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Contract docs and agent rules stay in sync across stories

- [X] T009 [P] Extend OpenAPI MockMvc check in `src/test/java/com/demo/product/product_service/controller/ProductControllerTest.java` so `/v3/api-docs` includes `/api/products/search` and documents 200 and 400
- [X] T010 [P] Add search row to `AGENTS.md` and `.cursor/rules/rest-api.mdc` (`GET /api/products/search` 200 + body or 400). Do not add JPA, paging, or Spring Security

**Checkpoint**: Quickstart in `specs/001-add-search-endpoint/quickstart.md` can be run (`./mvnw test`)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Nothing to do — existing project
- **Foundational (Phase 2)**: T001 → T002 → T003 (DTO then service then controller). BLOCKS all user stories
- **User Stories (Phase 3+)**: Depend on T003. Same test class: T004 → T005 → T006 → T007 → T008 sequentially
- **Polish (Phase 6)**: After story tests; T009 touches the test class (after T008). T010 is docs-only

### User Story Dependencies

- **User Story 1 (P1)**: After Phase 2 — MVP search matches
- **User Story 2 (P2)**: After Phase 2 — empty `[]` (same endpoint, additional tests)
- **User Story 3 (P3)**: After Phase 2 — 400 validation (same endpoint, additional tests)

### Within Each User Story

- Shared model/service/endpoint in Phase 2 (one search path serves all stories)
- Story phases add MockMvc coverage; ids from create, not `1L`
- Story complete before the next priority when working sequentially

### Parallel Opportunities

- T001 is a new file; T002/T003 must wait on T001/T002
- T004–T009 all edit `ProductControllerTest.java` — not parallel
- T009 and T010 are different files; T010 can run beside T009 after T008

---

## Parallel Example: User Story 1

```bash
# Not parallel: T004, T005, T006 all edit ProductControllerTest.java
Task: "T004 name-match MockMvc in ProductControllerTest.java"
Task: "T005 description and case MockMvc in ProductControllerTest.java"
Task: "T006 listing-order MockMvc in ProductControllerTest.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Phase 2: T001–T003
2. Phase 3: T004–T006
3. **STOP and VALIDATE**: `./mvnw test` for name/description/case/order search
4. Demo `GET /api/products/search?q=widget`

### Incremental Delivery

1. Foundational → search endpoint exists
2. US1 tests → matching MVP
3. US2 tests → empty success
4. US3 tests → invalid `q`
5. Polish → OpenAPI + AGENTS/rules

### Parallel Team Strategy

One shared test class and controller — prefer a single implementer. T010 (docs) can overlap with T009.

---

## Notes

- [P] tasks = different files, no incomplete dependencies
- [Story] label maps to spec.md US1/US2/US3
- Do not mock `ProductRepository`
- Do not add a search index, paging, or Spring Security
- Commit after each task or phase if requested

## Phase 7: Convergence

- [X] T011 Expand search `@Operation`/`@ApiResponses` in `src/main/java/com/demo/product/product_service/controller/ProductController.java` (and OpenAPI assertions in `src/test/java/com/demo/product/product_service/controller/ProductControllerTest.java` if needed) so published docs describe the required term, 200 with matches, 200 empty list when nothing matches, and 400 invalid input per FR-009 (partial)
