---
name: code-coverage-report
description: >
  Generates and summarizes JaCoCo code coverage for this Maven Spring Boot
  product-service. Use when the user asks for a coverage report, JaCoCo,
  test coverage, uncovered lines/classes, or to measure how well tests cover
  src/main/java.
---

# Code coverage report

Run JaCoCo with `./mvnw` (never `mvn` or Gradle). Do not add JPA, security, caching, or extra modules. Adding `jacoco-maven-plugin` is allowed when it is missing.

## Workflow

Copy and track:

```
Coverage Progress:
- [ ] Confirm or add JaCoCo plugin
- [ ] Run tests + report
- [ ] Summarize jacoco.csv
- [ ] Present markdown report
```

### 1. Confirm or add JaCoCo

If `pom.xml` has no `jacoco-maven-plugin`, add this under `<build><plugins>` (version comes from Spring Boot parent BOM):

```xml
<plugin>
	<groupId>org.jacoco</groupId>
	<artifactId>jacoco-maven-plugin</artifactId>
	<executions>
		<execution>
			<goals>
				<goal>prepare-agent</goal>
			</goals>
		</execution>
		<execution>
			<id>report</id>
			<phase>test</phase>
			<goals>
				<goal>report</goal>
			</goals>
		</execution>
	</executions>
</plugin>
```

Do not change Java version, parent, or existing starters.

### 2. Generate the report

From the repo root:

```bash
./mvnw -q test jacoco:report
```

Required outputs (fail if missing):

- `target/site/jacoco/index.html`
- `target/site/jacoco/jacoco.csv`
- `target/site/jacoco/jacoco.xml`

If tests fail, stop. Report the failure; do not invent coverage numbers.

### 3. Summarize

Run the bundled parser (execute it; do not rewrite it):

```bash
python3 .cursor/skills/code-coverage-report/scripts/summarize-jacoco.py target/site/jacoco/jacoco.csv
```

Optional: open uncovered source in `target/site/jacoco/**/*.html` when a class needs line-level detail.

### 4. Present the report

Use this template. Fill from the script output, not estimates.

```markdown
# Code coverage report

**Instruction coverage:** X% (covered / total)
**Branch coverage:** X%
**Line coverage:** X%

HTML: `target/site/jacoco/index.html`

## By package
| Package | Line % | Missed lines | Instruction % |
|---|---:|---:|---:|
| ... | | | |

## Lowest-coverage classes
| Class | Line % | Missed lines | Missed branches |
|---|---:|---:|---:|
| ... | | | |

## Gaps
- Layer or class with weak tests, and what is untested (CRUD path, validation, 404, etc.)

## Next tests
- Concrete test names / scenarios that would raise coverage
```

Highlight any **production** class under **80% line coverage**. `ProductServiceApplication` may stay low (bootstrap only); say so instead of padding tests for it.

## Project facts

- Tests live under `src/test/java`; production under `src/main/java`.
- Controller tests are MockMvc against the real in-memory store (`ProductControllerTest`). Do not recommend mocking the repository unless the user asks for unit tests.
- Typical uncovered layers if only controller tests exist: `service`, `repository`, `exception`, `dto`, `model`, `config`.
- Ignore generated reports under `target/`. Do not commit them.

## Do not

- Fabricate percentages if JaCoCo did not run
- Switch to Gradle, Cobertura, or IntelliJ-only reports
- Add coverage gates (`<rules>` / check goals) unless the user asks
- Change test style (Jackson 3 `ObjectMapper`, no hardcoded product ids)
