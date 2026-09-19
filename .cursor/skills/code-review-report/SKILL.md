---
name: code-review-report
description: >
  Reviews product-service source code against .cursor/rules and reports
  findings. Use when the user asks for a code review, code-review-report,
  rule compliance check, or to review source as per Cursor rules.
---

# Code review report

Review the source as per the Cursor rules and report the findings. Do not change code unless the user asks to fix items. Do not invent rules that are not in `.cursor/rules/`.

## Workflow

```
Review Progress:
- [ ] Load rules
- [ ] Collect source
- [ ] Check each rule file
- [ ] Write the report
```

### 1. Load rules

Read **every** `.cursor/rules/*.mdc` file. Those files are the only review criteria. If a rule file is added or changed, use the current text — do not rely on memory of this skill.

Current rule set (re-scan the directory; include any new `.mdc`):

| File | Focus |
|---|---|
| `project.mdc` | Boot 4.1.1, Java 21, `./mvnw`, layer split, no JPA/Lombok/records |
| `java-conventions.mdc` | packages, tabs, constructor injection, JavaBeans, DTO mapping, `BigDecimal`/`Long` |
| `maven-spring-boot.mdc` | parent, starters (`webmvc` not `web`), YAML, pin springdoc only |
| `rest-api.mdc` | `/api/products` verbs/status, `@Valid`, 201+Location, error JSON |
| `openapi.mdc` | `@Tag`/`@Operation`/`@ApiResponse`, DTO `@Schema`, springdoc paths |
| `security.mdc` | unauthenticated by design, `ProductRequest` only, no secrets, actuator defaults |
| `performance.mdc` | `ConcurrentHashMap`+`AtomicLong`, O(1) get, snapshot `findAll` |
| `testing.mdc` | MockMvc, Jackson 3 `ObjectMapper`, real store, 400/404, OpenAPI tests |

### 2. Collect source

Review, unless the user names a smaller path:

- `src/main/java/**/*.java`
- `src/test/java/**/*.java`
- `pom.xml`
- `src/main/resources/application.yaml`

Skip `target/`, `.cursor/skills/`, and generated reports.

### 3. Check each rule

For every rule file, walk matching sources and record only evidenced violations (file + line). Map checks:

**project.mdc** — layers stay split (controller never uses repository); no database/JPA/Lombok/records; constructor injection; no extra modules.

**java-conventions.mdc** — `com.demo.product.product_service.<layer>`; tabs; no field `@Autowired`; JavaBeans not records; mapping on DTOs/service (`ProductResponse.fromProduct`); `BigDecimal` price, `Long` ids; short Javadoc on public types.

**maven-spring-boot.mdc** — parent 4.1.1, Java 21, `./mvnw`; `spring-boot-starter-webmvc` (not `web`); no extra starters; YAML not properties; no secrets.

**rest-api.mdc** — CRUD table (200/201+Location/204/404); `@Valid` + Jakarta constraints; `ProductNotFoundException` not caught in controller; error fields `timestamp`, `status`, `error`, `message` (`errors` on validation).

**openapi.mdc** — `OpenApiConfig`; controller `@Tag`/`@Operation`/`@ApiResponses` for every status; create documents `Location`; delete 204 empty; DTO `@Schema`.

**security.mdc** — no Spring Security/OAuth/CORS wildcards unless asked; never `@RequestBody Product`; ids only from `AtomicLong`; path ids `Long`; no stack traces or domain types in JSON; actuator not exposing `env`/`heapdump`/`beans`; never read secrets from config.

**performance.mdc** — `store.get`/`containsKey` not `values()` scans; `findAll` snapshot; no `synchronized` around the map; `idGenerator.incrementAndGet()`; single `stream().map(...).toList()`; no caching/paging.

**testing.mdc** — `@SpringBootTest` + `@AutoConfigureMockMvc` from `org.springframework.boot.webmvc.test.autoconfigure`; `tools.jackson.databind.ObjectMapper`; no repository mocks unless requested; behavior-style test names; happy path and 400/404; post-delete GET 404; OpenAPI/Swagger checks; ids from create, not `1L`.

Do **not** flag missing JPA, Spring Security, Lombok, records, caching, or extra modules — rules forbid adding them without an explicit request.

### 4. Write the report

Use this template. Every finding needs a rule file, path, and line. If a section has no issues, write `Pass`.

```markdown
# Code review report

**Scope:** [paths reviewed]
**Rules:** `.cursor/rules/*.mdc` (list files actually read)

## Summary
- Critical: N | Should fix: N | Suggestion: N
- Verdict: Pass / Needs work

## Findings

### 1. [Critical|Should fix|Suggestion] Short title
- **Rule:** `filename.mdc`
- **Where:** `path:line`
- **Issue:** what the code does
- **Expected:** what the rule requires

## Checklist
| Rule | Result |
|---|---|
| project.mdc | Pass / N findings |
| java-conventions.mdc | |
| maven-spring-boot.mdc | |
| rest-api.mdc | |
| openapi.mdc | |
| security.mdc | |
| performance.mdc | |
| testing.mdc | |

## Notes
- Rule-compliant items worth calling out (optional, brief)
- Out of scope: anything the rules say not to add
```

Severity:

- **Critical** — breaks architecture, trust boundary, or a hard rule (wrong layer, `@RequestBody Product`, secrets, JPA/Lombok/records introduced)
- **Should fix** — violates a stated convention (status codes, mapping, test ObjectMapper, O(n) lookup)
- **Suggestion** — small consistency/docs gap that still matches behavior

## Do not

- Modify source as part of this skill (report only)
- Duplicate or paraphrase rule files into the report; cite them
- Run tests or JaCoCo unless the user also asks for coverage
- Recommend persistence, security, caching, messaging, Lombok, or records
