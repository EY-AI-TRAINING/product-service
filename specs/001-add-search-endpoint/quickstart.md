# Quickstart: Product Catalog Search

Validate search end-to-end against the running catalog. Contract details:
[contracts/search-endpoint.md](./contracts/search-endpoint.md). Data rules:
[data-model.md](./data-model.md).

## Prerequisites

- Java 21
- Maven Wrapper (`./mvnw`)
- Catalog on `http://localhost:8080` (`./mvnw spring-boot:run`) or use MockMvc
  via `./mvnw test`

## Automated check

```bash
./mvnw test
```

Expected: existing CRUD tests still pass; new search tests cover name match,
description match, case-insensitive match, listing order, empty `[]`, missing
and blank `q` → 400, and OpenAPI path `/api/products/search`.

## Manual check

1. Create two products:

```bash
curl -sS -D - -H 'Content-Type: application/json' \
  -d '{"name":"Blue Widget","description":"A simple widget","price":9.99}' \
  http://localhost:8080/api/products

curl -sS -H 'Content-Type: application/json' \
  -d '{"name":"Red Gadget","description":"outdoor use","price":5.00}' \
  http://localhost:8080/api/products
```

2. Search by name fragment — expect only the widget:

```bash
curl -sS 'http://localhost:8080/api/products/search?q=widget'
```

3. Search by description — expect the gadget:

```bash
curl -sS 'http://localhost:8080/api/products/search?q=outdoor'
```

4. No matches — expect `[]` and HTTP 200:

```bash
curl -sS -D - 'http://localhost:8080/api/products/search?q=xyzzy'
```

5. Missing term — expect HTTP 400 with `errors.q`:

```bash
curl -sS -D - 'http://localhost:8080/api/products/search'
```

6. Docs — search path present:

```bash
curl -sS http://localhost:8080/v3/api-docs | grep -o '/api/products/search'
```

## Out of scope for this check

Paging, ranking, price filters, authentication, and a separate search index.
