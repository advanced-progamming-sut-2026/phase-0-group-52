# Contributing

Quick pointers for working in this repo. Full detail lives in the docs linked below.

Build tool is Gradle (`build.gradle`/`settings.gradle`, wrapped via `gradlew`). LibGDX is on the
classpath for the upcoming graphical phase, but nothing currently uses it — the game is still the
plain text/console app entered through `Main`.

## Workflow

Branch/commit/PR/tag conventions: [GIT_WORKFLOW.md](GIT_WORKFLOW.md).

Short version: never commit directly to `main` or `develop`. Branch off `develop` as
`feature/<name>`, `fix/<name>`, `test/<name>`, `docs/<name>`, `chore/<name>`, or
`refactor/<name>`, open a PR into `develop`, get one review, then merge.

## Code style

- `./gradlew checkstyleMain` — naming, line length (120 cols), method length (50 lines).
- `./gradlew pmdMain` — unused locals/fields/methods, oversized methods/classes.
- Both are opt-in, same as the old Maven setup: `./gradlew build`/`test` succeed regardless of
  lint violations, only the two commands above enforce them.
- Rules are defined in [checkstyle.xml](../../checkstyle.xml) / [pmd-ruleset.xml](../../pmd-ruleset.xml)
  at the repo root, matching the course spec (`Phase 0-1.pdf`, Checkstyle/PMD section, also
  transcribed in [Phase 0-1.md](Phase%200-1.md)).
- **No comments in source files at all** — no line comments, no block comments, no Javadoc.
  Names carry the meaning; if something needs explaining, put it in `docs/`.

## Reference docs

- [../COMMANDS.md](../COMMANDS.md) — full CLI command reference for the game.
- [../CONSTANTS.md](../CONSTANTS.md) — every `static final` constant, grouped by system.
- [../RUBRIC_STATUS.md](../RUBRIC_STATUS.md) — implementation status against the phase rubric.
- [Phase 0-1.md](Phase%200-1.md) — plain-text/markdown transcript of the Phase 0-1 spec PDF
  (Persian), for grepping/quoting without opening the PDF.

## Build / run / test

```
./gradlew build
./gradlew run
./gradlew test
```
