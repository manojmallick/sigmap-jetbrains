# Changelog

All notable changes to the SigMap JetBrains plugin are documented here.

Format: [Semantic Versioning](https://semver.org/)

---

## [Unreleased]

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
