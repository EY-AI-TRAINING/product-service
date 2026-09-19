# Product catalog search contract

Base path: `/api/products`. Existing CRUD paths are unchanged.

## `GET /api/products/search`

Search the catalog by a single text term.

### Query parameters

| Name | Required | Description |
|------|----------|-------------|
| q | yes | Non-blank search term. Matched case-insensitively if it appears anywhere in product name or description. |

### Responses

| Status | Body | When |
|--------|------|------|
| 200 | JSON array of product objects (`id`, `name`, `description`, `price`) | Term is valid. Empty array if nothing matches. Relative order matches `GET /api/products`. |
| 400 | Catalog validation JSON: `timestamp`, `status`, `error`, `message`, `errors` | `q` missing or blank. `errors.q` carries the field message. |

Does **not** return 404 when there are no matches.

### Example — matches

```http
GET /api/products/search?q=widget
```

```json
[
  {
    "id": 1,
    "name": "Blue Widget",
    "description": "A simple widget",
    "price": 9.99
  }
]
```

### Example — no matches

```http
GET /api/products/search?q=xyzzy
```

```json
[]
```

### Example — invalid term

```http
GET /api/products/search
```

```json
{
  "timestamp": "2026-09-19T08:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": {
    "q": "must not be blank"
  }
}
```

Exact `errors.q` text may follow the Jakarta `@NotBlank` default or a custom
message; tests MUST assert status, `$.status`, `$.message`, and `$.errors.q`.
