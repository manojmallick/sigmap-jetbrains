<div align="center">

# SigMap — JetBrains Plugin

### AI context engine for IntelliJ IDEA, WebStorm, PyCharm, GoLand and all JetBrains IDEs

[![JetBrains Marketplace](https://img.shields.io/jetbrains/plugin/v/31109-sigmap--ai-context-engine?label=JetBrains%20Marketplace&color=7c6af7&logo=jetbrains)](https://plugins.jetbrains.com/plugin/31109-sigmap--ai-context-engine)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/31109-sigmap--ai-context-engine?color=blue&logo=jetbrains)](https://plugins.jetbrains.com/plugin/31109-sigmap--ai-context-engine)
[![Rating](https://img.shields.io/jetbrains/plugin/r/rating/31109-sigmap--ai-context-engine?color=brightgreen)](https://plugins.jetbrains.com/plugin/31109-sigmap--ai-context-engine)
[![Release](https://img.shields.io/github/v/release/manojmallick/sigmap-jetbrains?color=7c6af7&label=release)](https://github.com/manojmallick/sigmap-jetbrains/releases)
[![License: MIT](https://img.shields.io/badge/license-MIT-green)](LICENSE)
[![Node ≥18](https://img.shields.io/badge/node-%E2%89%A518-brightgreen?logo=node.js)](https://nodejs.org)

**85.6% retrieval hit@5 · 96.8% token reduction · 33 languages · Zero npm deps**

</div>

---

## What is SigMap?

SigMap extracts a compact **signature map** of your entire codebase — function names, class hierarchies, exported types, interfaces — and writes it to `.github/copilot-instructions.md` automatically. Every AI coding assistant reads that file as its first-message context.

This plugin brings SigMap directly into JetBrains IDEs with a live health grade in the status bar, one-click regeneration, and auto-refresh.

```
Before SigMap: "I don't know your codebase — can you share some files?"
After SigMap:  "I can see your AuthService, UserRepository, 47 API routes…"
```

---

## What's new in v4.1–v4.3

- **SigMap Ask tool window** (v4.3) — type a natural-language question, get ranked files with signature previews, double-click to open
- **Settings page** (v4.2) — Tools → SigMap: explicit CLI path override, health-probe cadence
- **Status-bar action menu** (v4.2) — click the widget for Regenerate / Open Context File / View Roadmap
- **Hardened core loop** (v4.1) — cached cross-platform command resolution (Windows fixed), throttled health probes, bounded processes, instant status refresh after regeneration
- IDE compatibility extended through **2026.2**; compatible with SigMap CLI v8.x (33 languages, 20 MCP tools)

---

## Features

| Feature | Description |
|---|---|
| **Health Status Bar** | Live grade A–F, tokens, reduction + age (`SigMap: B · 945 tok · 91% ↓`); click for the action menu |
| **SigMap Ask tool window** | Ask a question → ranked files with signature previews → double-click to open |
| **Regenerate Context** | Tools → SigMap → Regenerate Context or `Ctrl+Alt+G` — cancellable, instant status refresh |
| **Open Context File** | One click to open `.github/copilot-instructions.md` (offers to generate when missing) |
| **Settings** | Tools → SigMap: explicit CLI path override, health-probe cadence |
| **View Roadmap** | Opens the SigMap docs in your browser |
| **Auto-refresh** | Age updates every 60 s; the CLI health probe runs only on context change (or per your configured cadence) |

### Health grades

| Grade | Age | Meaning |
|:---:|---|---|
| **A** | < 1 hour | Fresh — AI has full context |
| **B** | 1–6 hours | Good |
| **C** | 6–12 hours | Aging — regenerate soon |
| **D** | 12–24 hours | Stale |
| **F** | > 24 hours | Expired — regenerate now |

---

## Installation

### JetBrains Marketplace (recommended)

1. **Settings** → **Plugins** → **Marketplace**
2. Search **SigMap**
3. Click **Install** → restart IDE

Or open the marketplace page directly:
[plugins.jetbrains.com/plugin/31109-sigmap--ai-context-engine](https://plugins.jetbrains.com/plugin/31109-sigmap--ai-context-engine)

### Manual (.zip)

1. Download `sigmap-X.Y.Z.zip` from [Releases](https://github.com/manojmallick/sigmap-jetbrains/releases)
2. **Settings** → **Plugins** → ⚙️ → **Install Plugin from Disk…**
3. Select the ZIP → restart IDE

---

## Requirements

| Requirement | Details |
|---|---|
| **JetBrains IDE** | 2024.1 – 2026.2 (IDEA, WebStorm, PyCharm, GoLand, RubyMine, …) |
| **Node.js** | 18 or higher |
| **SigMap CLI** | `npm install -g sigmap` or `npx sigmap` |

---

## Usage

### Regenerate context

**Status bar** — click the `SigMap: X` widget → **Regenerate Context**  
**Keyboard** — `Ctrl+Alt+G` (Windows/Linux) / `Cmd+Alt+G` (macOS)  
**Menu** — Tools → SigMap → Regenerate Context

### CLI commands (terminal)

```bash
sigmap                   # generate once
sigmap ask "auth flow"   # query-focused context
sigmap validate          # check coverage
sigmap judge             # score answer groundedness
sigmap --watch           # auto-regenerate on save
```

---

## Configuration

Place `gen-context.config.json` in your project root:

```json
{
  "srcDirs": ["src", "lib"],
  "exclude": ["node_modules", "dist"],
  "maxTokens": 6000,
  "secretScan": true
}
```

Full reference: [manojmallick.github.io/sigmap/guide/config](https://manojmallick.github.io/sigmap/guide/config)

---

## Benchmark

| Metric | Value |
|---|---:|
| Retrieval hit@5 | **85.6%** vs 13.6% random baseline (6.3×) |
| Honest grep-agent baseline | 42.7% → **85.6%** (2.0× lift) |
| Overall token reduction | **96.8%** (avg across 18 real repos) |
| Prompt reduction | **48%** (2.84 → 1.48 prompts/task) |
| Languages supported | **33** |

Benchmark ID: `sigmap-v8.21-main` · Date: 2026-07-19

---

## Troubleshooting

**"gen-context.js not found"**  
→ `npm install -g sigmap` or `npm install sigmap` in your project root

**Status bar not appearing**  
→ Restart the IDE after installation

**Context file not detected**  
→ The plugin looks for `.github/copilot-instructions.md`, `CLAUDE.md`, `.cursorrules`, `.windsurfrules` — ensure at least one exists

---

## Links

| | |
|---|---|
| 📖 Docs | [manojmallick.github.io/sigmap](https://manojmallick.github.io/sigmap/) |
| 🔌 VS Code extension | [github.com/manojmallick/sigmap-vscode](https://github.com/manojmallick/sigmap-vscode) |
| 🖥 CLI / core | [github.com/manojmallick/sigmap](https://github.com/manojmallick/sigmap) |
| 🐛 Issues | [github.com/manojmallick/sigmap-jetbrains/issues](https://github.com/manojmallick/sigmap-jetbrains/issues) |
| 📦 npm | [npmjs.com/package/sigmap](https://www.npmjs.com/package/sigmap) |

---

<div align="center">

MIT © 2026 [Manoj Mallick](https://github.com/manojmallick) · Made in Amsterdam 🇳🇱

</div>
