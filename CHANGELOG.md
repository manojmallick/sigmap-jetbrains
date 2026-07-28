# Changelog

All notable changes to the SigMap JetBrains plugin are documented here.

Format: [Semantic Versioning](https://semver.org/)

---

## [Unreleased]

---

## [4.3.0] — 2026-07-28

Minor release — the retrieval half of SigMap arrives in the IDE.

### Added
- **SigMap Ask tool window (#10):** right-anchored panel — type a natural-language question (Enter to run), get ranked files with score and first-signature preview via `--query <text> --json`, double-click to open. Respects the Tools → SigMap CLI path override; the query runs on a pooled thread with a 15 s bound; missing CLI and empty results surface as status-line messages, never exceptions. New `SigMapQuery` module (args building + JSON parsing) is unit-tested.

---

## [4.2.0] — 2026-07-28

Minor release — settings page, richer status-bar menu, Open Context File feedback, refreshed listing, and the Gradle Plugin 2.x build migration.

### Added
- **Settings page (#8):** Tools → SigMap — explicit CLI path override (a `.js` path runs through node) and health-probe cadence in minutes. Project-level, persisted to `.idea/sigmap.xml`, takes effect without restart.
- **Status-bar action menu (#8):** the widget now uses `MultipleTextValuesPresentation` — clicking opens a popup with Regenerate / Open Context File / View Roadmap instead of hardwiring Regenerate.
- **Open Context File feedback (#8):** when no context file exists (including the CLAUDE.md / .cursorrules / .windsurfrules fallbacks), a notification with a "Generate now" action appears instead of silently doing nothing.

### Changed
- **Build migrated to the IntelliJ Platform Gradle Plugin 2.18.1 (#6):** replaces end-of-life `org.jetbrains.intellij` 1.17.4. Gradle wrapper 8.5 → 9.6.1, Kotlin 2.2.20, verifier now runs via `verifyPlugin` (release workflow updated). The plugin zip no longer bundles kotlin-stdlib (platform provides it) — 1.7 MB → 37 KB.
- **Marketplace listing refreshed (#8):** 21 → 33 languages, "97%" → 96.8% average token reduction, settings + menu documented.

---

## [4.1.0] — 2026-07-28

Minor release — IDE compatibility through 2026.2, Windows health-probe fix, bounded external processes, and instant status-bar refresh after regeneration.

### Added
- **Instant status refresh (#4):** `RegenerateAction` publishes a `SigMapContextListener.TOPIC` message-bus event on success; the status-bar widget subscribes and re-probes immediately instead of waiting up to 60 s for the next tick.
- **`GenContextLocator` (#4):** the full cross-platform command resolution (local `gen-context.js`, `node_modules/.bin`, Volta/nvm/Homebrew/npm-global paths, PATH, login-shell fallback, Windows `where`/`.cmd`/`.exe` candidates) extracted from `RegenerateAction` into one shared object with a per-project cache — successful resolutions are reused, dropped when their files disappear, and failures are never cached.

### Changed
- **IDE compatibility extended to 2026.2** — `until-build` raised `261.*` → `262.*` (the build line the Marketplace verifier already checks against).
- **Actions declare `ActionUpdateThread.BGT`** — removes the deprecated implicit `OLD_EDT` update-thread default.
- **Health probe throttled (#4):** the status bar spawned `which node` + `gen-context --health --json` every 60 s per project; it now runs the CLI only when the context file's mtime changes or the last probe is ≥ 10 min old, recomputing just the age display locally in between.
- **External processes bounded (#4):** the health probe times out after 30 s (`destroyForcibly`), and regeneration is now a cancellable background task with a 5-minute cap.

### Fixed
- **Windows health probe (#4):** the status bar's private resolver shelled out to `which node` (nonexistent on Windows), silently degrading every Windows install to the mtime-only grade — it now uses the shared `GenContextLocator`.
- **Widget disposal no longer blocks (#4):** `dispose()` used `awaitTermination(5 s)`, which could stall project close; now `shutdownNow()`.

---

## [4.0.1] — 2026-07-27

Patch release — fixes both warnings from the JetBrains Marketplace Plugin Verifier (reported against IntelliJ IDEA 2026.2.1 EAP).

### Fixed
- **Plugin Verifier warnings (scheduled-for-removal API + override-only violation):** the stale-context notification's "Regenerate" button invoked `AnAction.actionPerformed(...)` directly (an `@ApiStatus.OverrideOnly` method) with an event built via `AnActionEvent.createFromDataContext(...)` (scheduled for removal). It now fires the action through `ActionManager.tryToExecute(...)` — the same platform-sanctioned path the status-bar click handler already used. Verifier reports **Compatible with zero warnings** against IC-241.19416.15 and IC-252.28539.33.

---

## [4.0.0] — 2026-04-20

First standalone release — the plugin was extracted from the SigMap monorepo into its own repository with an independent version cycle.

### Added
- **Health Status Bar** — live context-health grade A–F with age, token count, and reduction % (`SigMap: B · 3h`), refreshed every 60 seconds; reads `gen-context --health --json` when available, falls back to file mtime.
- **Regenerate Context** action — Tools → SigMap → Regenerate Context or `Ctrl+Alt+G`.
- **Open Context File** action — one click to open `.github/copilot-instructions.md`.
- **View Roadmap** action — opens the SigMap docs in the browser.
- **Stale-context notification** — one popup per session when the context file is ≥ 24 h old, with Regenerate / Dismiss actions.

### Changed
- Compatible with SigMap CLI v6.0 (graph-boosted retrieval, incremental cache).
- IDE compatibility: IntelliJ Platform 2024.1 (build 241) and newer.
