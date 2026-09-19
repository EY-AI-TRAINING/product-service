# Research: Product Catalog Search

## 1. Dedicated search path vs query on list

**Decision**: `GET /api/products/search?q={term}` as a literal mapping, separate
from `GET /api/products` and `GET /api/products/{id}`.

**Rationale**: The spec requires a dedicated search request and unchanged list
behavior (FR-008). A literal `/search` mapping avoids Spring binding `"search"`
as `{id}` (`Long`) which would 400. `paths-to-match: /api/**` already covers it.

**Alternatives considered**:
- `GET /api/products?q=` — optional `q` would keep list-without-q unchanged but
  mixes two actions on one resource; spec asked for dedicated search.
- `GET /api/products/{id}`-style path term — collides with id lookup.

## 2. Matching algorithm and store access

**Decision**: `productRepository.findAll()` (snapshot) then one stream:
filter by case-insensitive `contains` on name **or** description, then
`map(ProductResponse::fromProduct).toList()`. Null or blank description is
treated as no description text (product can still match on name).

**Rationale**: Constitution forbids scanning `values()` to find **one** product
by id and forbids a second in-memory index. Search is a filtered listing, so
the existing snapshot is the correct source. Relative order of the snapshot is
listing order (clarification session 2026-09-19).

**Alternatives considered**:
- Extra `ConcurrentHashMap` / inverted index — violates constitution and FR-007.
- Repository `search()` that iterates `store.values()` without snapshot —
  races with writes; `findAll()` already snapshots.

**Unresolved spec note**: Clarify Q2 (no description) was skipped when planning
started. Plan uses the recommended default above. Rework risk is low.

## 3. Validation of `q` without a new error type

**Decision**: New `ProductSearchRequest` JavaBean with `q` + `@NotBlank`. Bind
as `@Valid` method argument on GET (query/model attribute, not `@RequestBody`).
That should raise `MethodArgumentNotValidException` and reuse
`GlobalExceptionHandler`. Whitespace-only fails `@NotBlank`. Service still
`trim()`s before matching so `" widget"` matches `"Widget"`.

**Rationale**: FR-005 requires the same validation error shape as create
(`timestamp`, `status`, `error`, `message`, `errors`). Extending the advice
for `ConstraintViolationException` is allowed only if bean binding does not
produce `MethodArgumentNotValidException`.

**Alternatives considered**:
- `@RequestParam(required = true) String q` — missing param uses Spring’s
  default 400 body, not catalog validation JSON.
- Manual checks in the controller — duplicates advice and thickens HTTP layer.

## 4. Status codes for empty vs invalid

**Decision**: Valid term + zero matches → **200** empty JSON array. Missing or
blank `q` → **400**. Never 404 for search.

**Rationale**: Spec P2 vs P3. 404 remains only `ProductNotFoundException`.

**Alternatives considered**: 404 on no matches — rejected by FR-004.

## 5. Tests and documentation

**Decision**: All new coverage in `ProductControllerTest` (MockMvc, Jackson 3,
ids from create). OpenAPI on the handler; assert `/api/products/search` in
`/v3/api-docs`. Sync `AGENTS.md` and `rest-api.mdc`. Do not mock the repository.

**Rationale**: Constitution III and IV.

**Alternatives considered**: Repository unit tests — not requested.

## 6. Task count

**Decision**: Exactly **10** implementation tasks (see plan.md seed).

**Rationale**: User input to `/speckit-plan`: `create 10 tasks`. `tasks.md` is
still produced by `/speckit-tasks`.

**Alternatives considered**: Finer-grained or merged tasks — rejected to honor
the requested count.
