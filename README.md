<div align="center">

# SigMap — JetBrains Plugin

### AI context engine for IntelliJ IDEA, WebStorm, PyCharm, GoLand and all JetBrains IDEs

[![JetBrains Marketplace](https://img.shields.io/jetbrains/plugin/v/26371-sigmap--ai-context-engine?label=JetBrains%20Marketplace&color=7c6af7&logo=jetbrains)](https://plugins.jetbrains.com/plugin/26371-sigmap--ai-context-engine)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/26371-sigmap--ai-context-engine?color=blue&logo=jetbrains)](https://plugins.jetbrains.com/plugin/26371-sigmap--ai-context-engine)
[![Rating](https://img.shields.io/jetbrains/plugin/r/rating/26371-sigmap--ai-context-engine?color=brightgreen)](https://plugins.jetbrains.com/plugin/26371-sigmap--ai-context-engine)
[![Release](https://img.shields.io/github/v/release/manojmallick/sigmap-jetbrains?color=7c6af7&label=release)](https://github.com/manojmallick/sigmap-jetbrains/releases)
[![License: MIT](https://img.shields.io/badge/license-MIT-green)](LICENSE)
[![Node ≥18](https://img.shields.io/badge/node-%E2%89%A518-brightgreen?logo=node.js)](https://nodejs.org)

**80.0% retrieval hit@5 · 96.9% token reduction · 29 languages · Zero npm deps**

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

## What's new in v4.0

- Standalone release — independent version cycle from the SigMap CLI core
- Compatible with SigMap CLI v6.0 (graph-boosted retrieval, incremental cache)
- Updated IDE compatibility: IntelliJ 2024.1 → 2026.1

---

## Features

| Feature | Description |
|---|---|
| **Health Status Bar** | Live grade A–F + age (`SigMap: B 3h`) in the bottom status bar |
| **Regenerate Context** | Tools → SigMap → Regenerate Context or `Ctrl+Alt+G` |
| **Open Context File** | One click to open `.github/copilot-instructions.md` |
| **View Roadmap** | Opens the SigMap docs in your browser |
| **Auto-refresh** | Status re-checks every 60 seconds |

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
[plugins.jetbrains.com/plugin/26371-sigmap--ai-context-engine](https://plugins.jetbrains.com/plugin/26371-sigmap--ai-context-engine)

### Manual (.zip)

1. Download `sigmap-X.Y.Z.zip` from [Releases](https://github.com/manojmallick/sigmap-jetbrains/releases)
2. **Settings** → **Plugins** → ⚙️ → **Install Plugin from Disk…**
3. Select the ZIP → restart IDE

---

## Requirements

| Requirement | Details |
|---|---|
| **JetBrains IDE** | 2024.1 – 2026.1 (IDEA, WebStorm, PyCharm, GoLand, RubyMine, …) |
| **Node.js** | 18 or higher |
| **SigMap CLI** | `npm install -g sigmap` or `npx sigmap` |

---

## Usage

### Regenerate context

**Status bar** — click the `SigMap: X Xh` widget  
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
| Retrieval hit@5 | **80.0%** vs 13.6% baseline |
| Graph-boosted hit@5 | **83.3%** |
| Overall token reduction | **96.9%** |
| Prompt reduction | **40.8%** (2.84 → 1.68) |
| Languages supported | **29** |

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
