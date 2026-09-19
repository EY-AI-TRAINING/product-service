# Feature Specification: Product Catalog Search

**Feature Branch**: `001-add-search-endpoint`

**Created**: 2026-09-19

**Status**: Draft

**Input**: User description: "add search endpoint"

## Clarifications

### Session 2026-09-19

- Q: Should matching products appear in the same order as they would in a full catalog listing? → A: Same order as a full catalog listing (relative order of matches is unchanged)

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Find products by search text (Priority: P1)

A catalog consumer has a partial product name or description in mind and wants
matching products without reviewing the entire catalog. They submit a search
term and receive only the products whose name or description contains that
term.

**Why this priority**: This is the feature. Without text search, consumers must
scan the full catalog by hand.

**Independent Test**: Create at least two products with distinct names, search
using a term that matches only one, and confirm the result contains that
product and not the other.

**Acceptance Scenarios**:

1. **Given** the catalog contains "Blue Widget" and "Red Gadget", **When** a
   consumer searches for "widget", **Then** the result includes "Blue Widget"
   and does not include "Red Gadget".
2. **Given** a product whose description contains "outdoor", **When** a
   consumer searches for "outdoor", **Then** that product appears in the
   results even if its name does not contain "outdoor".
3. **Given** matching products exist, **When** search succeeds, **Then** each
   result includes the same product identity, name, description, and price
   that a full catalog listing would show for those products, and the matches
   appear in the same relative order as in that listing.
4. **Given** a consumer is not signed in, **When** they search with a valid
   term, **Then** search proceeds the same way as other public catalog reads.
5. **Given** search is available, **When** a consumer consults the published
   catalog documentation, **Then** it describes search, the required term,
   success with matches, success with no matches, and invalid-input failure.

---

### User Story 2 - Empty results are a successful search (Priority: P2)

A catalog consumer searches for a term that matches nothing. They should see
an empty result set, not a "missing resource" failure, so they can try another
term.

**Why this priority**: Distinguishes "no matches" from "the catalog or the
search capability is unavailable." Useful immediately after P1.

**Independent Test**: Search for a term that does not appear in any product
name or description and confirm a successful empty list.

**Acceptance Scenarios**:

1. **Given** a catalog with products whose names and descriptions do not
   contain "xyzzy", **When** a consumer searches for "xyzzy", **Then** they
   receive a successful empty result with no product entries.
2. **Given** an empty catalog, **When** a consumer searches for any valid
   term, **Then** they receive a successful empty result.

---

### User Story 3 - Reject unusable search text (Priority: P3)

A catalog consumer submits search without a usable term (missing or only
whitespace). The catalog must refuse the request with a clear validation
message instead of treating it as "list everything" or "match nothing."

**Why this priority**: Prevents accidental full-catalog dumps and ambiguous
empty lists. Search still works if this story ships after P1 and P2.

**Independent Test**: Submit search with no term and with only spaces; both
must be rejected as invalid input.

**Acceptance Scenarios**:

1. **Given** a consumer omits the search term, **When** they request search,
   **Then** the request is rejected as invalid input with a clear message.
2. **Given** a consumer supplies only whitespace as the search term, **When**
   they request search, **Then** the request is rejected as invalid input with
   a clear message.

---

### Edge Cases

- Matching MUST ignore letter case ("Widget" matches "widget").
- A term that appears anywhere inside the name or description still matches
  (searching "get" matches "Gadget").
- Extra spaces around a otherwise valid term are trimmed before matching.
- A very long term that matches nothing returns a successful empty result, not
  a failure, unless it exceeds the same input-size limits used for product
  name and description.
- Special characters are treated as ordinary text, not as a query language.
- Search is read-only: it MUST NOT create, change, or delete products.
- Results reflect the catalog at the moment of the search (newly added
  products can match; deleted products MUST NOT appear).
- When several products match, their relative order MUST be the same as in a
  full catalog listing (search MUST NOT reorder matches).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Consumers MUST be able to search the product catalog by a single
  text term without retrieving the full catalog.
- **FR-002**: A product MUST match when the term appears anywhere in its name
  or its description, ignoring letter case.
- **FR-003**: Search MUST return only matching products, each with identity,
  name, description, and price consistent with a normal catalog listing.
  Matching products MUST appear in the same relative order as they would in a
  full catalog listing.
- **FR-004**: Search MUST succeed with an empty result when no product
  matches; "no matches" is not a missing-resource error.
- **FR-005**: Search MUST require a non-blank term after trimming whitespace;
  missing or whitespace-only terms MUST be rejected as invalid input with the
  same style of validation message used when adding a product with missing
  required fields.
- **FR-006**: Search MUST be available without signing in, consistent with the
  rest of the unauthenticated catalog.
- **FR-007**: Search MUST NOT add paging, ranking, price or category filters,
  or saved searches.
- **FR-008**: Existing catalog operations (list all, view one, add, replace,
  remove) MUST keep their current behavior.
- **FR-009**: Published catalog documentation MUST describe the search
  capability, its required term, success with matches, success with no
  matches, and invalid-input failure.

### Key Entities

- **Product**: An item in the catalog with identity, name, optional
  description, and price. Search reads products; it does not define a new
  product type.
- **Search term**: A single text value supplied by the consumer. After trim it
  MUST be non-empty. It is matched against name and description.
- **Search result**: An ordered collection of matching products. Empty is a
  valid result. Order MUST match the relative order of those products in a
  full catalog listing.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A consumer who knows part of a product name can obtain that
  product in the search result on the first try, without reviewing the full
  catalog, in under 2 seconds for catalogs of up to a few hundred products.
- **SC-002**: 100% of valid searches return a successful result: either the
  matching products or an empty list when nothing matches.
- **SC-003**: 100% of missing or whitespace-only search terms are rejected as
  invalid input with a human-readable validation message.
- **SC-004**: After search is available, list, view, add, replace, and remove
  still complete with the same outcomes as before this feature.
- **SC-005**: At least 90% of demonstration searches that use a distinctive
  fragment of a known product name return that product and omit unrelated
  products.

## Assumptions

- Consumers of this feature are catalog clients (people or systems) that
  already list and manage products; no new actor or sign-in model is required.
- Search matches **name and description** only. Price, identity, and other
  fields are not searchable in this version.
- Match semantics default to **case-insensitive “appears anywhere in the
  text”**, which is the usual catalog “type a bit of the name” behavior.
- A dedicated search request is in scope; listing every product remains a
  separate action and is unchanged.
- Returning **all matches** in one response is acceptable. Paging is out of
  scope unless requested later.
- Result order is required to match the existing full-catalog listing order
  (not name sort, identity sort, or unspecified).
- Search reads the same product catalog used today; it does not keep a
  separate copy of products.
- Documentation of search is part of the existing catalog docs, not a
  separate product.
- Input-size limits for an overly long search term follow the same maximums
  used for product name and description when those limits exist; otherwise a
  non-matching long term still returns an empty success.
