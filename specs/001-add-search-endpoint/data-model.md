# Data Model: Product Catalog Search

Search does not add a persisted entity. It reads existing `Product` values and
returns existing `ProductResponse` values.

## Product (existing)

| Field | Type | Rules |
|-------|------|--------|
| id | Long | Assigned by repository `AtomicLong`; not searchable |
| name | String | Required on write (`@NotBlank`); searchable |
| description | String | Optional; searchable when non-blank |
| price | BigDecimal | Required on write; not searchable |

**Relationships**: Catalog membership only. Search never creates, updates, or
deletes products.

**Lifecycle**: Unchanged. Search is a read of the catalog snapshot at request time.

## Search term (request)

Represented in HTTP as query field `q` on `ProductSearchRequest`.

| Field | Type | Rules |
|-------|------|--------|
| q | String | Required. `@NotBlank` (null, empty, and whitespace-only are invalid). Leading/trailing spaces trimmed before matching. Compared case-insensitively; must appear anywhere in `name` or `description`. Special characters are literal text. |

**Matching**:
- Name match **or** description match (not both required).
- Missing/blank description → no description text; name can still match.
- A product that matches both fields appears **once**.
- Result order = relative order of those products in `findAll()` / list.

## Search result (response)

Ordered list of `ProductResponse` (`id`, `name`, `description`, `price`). Empty
list is a valid success payload. No paging envelope, score, or total count.

## Validation summary

| Input | Outcome |
|-------|---------|
| `q` omitted | 400 validation (`errors` includes `q`) |
| `q` whitespace only | 400 validation (`errors` includes `q`) |
| `q` valid, no products match | 200 `[]` |
| `q` valid, some match | 200 array of `ProductResponse` |
