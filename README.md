# AE2 Translations

A community resource pack that overrides the in-game translations of
[Applied Energistics 2](https://github.com/AppliedEnergistics/Applied-Energistics-2)
for **Minecraft 1.21.1**.

It exists because AE2 manages its translations only through Crowdin and does not
accept language Pull Requests. Here, they're welcome — for both the UI text and
the in-game guide.

## Installation

1. Download the `.zip` from [Releases](../../releases) (do **not** unzip it).
2. Drop it into your `.minecraft/resourcepacks/` folder.
3. In-game: Options → Resource Packs → enable it.
4. Set your in-game language (Options → Language) to the one you want to use.

It's client-side: every player installs it on their own.

## What's included

- **Interface translations** — item, block, tooltip, and GUI text, under
  `assets/ae2/lang/`.
- **The in-game guide** — the AE2 Guide (powered by GuideMe), under
  `assets/ae2/ae2guide/`.

Both are per-language and fall back to English for anything not yet translated,
so partial translations are perfectly fine.

---

## Contributing: interface text

The language files live in `assets/ae2/lang/<locale>.json` (e.g. `de_de.json`,
`fr_fr.json`, `pt_br.json`).

1. Fork the repo and edit or add your locale's file.
2. `assets/ae2/lang/en_us.json` is the **source of truth** for keys and order.
3. **Preserve** the `%s`, `%d`, `%1$d`, `%%` placeholders exactly — you may only
   reorder them to match your language's grammar.
4. Run the ordering script (see below) so your file matches the layout of the
   others.
5. Open a Pull Request.

### The ordering script

`scripts/Reorganize.java` keeps every locale in the **same order** as
`en_us.json`, grouped by concept and separated by blank lines. This makes files
comparable line by line and easy to review.

Run it (no compilation needed, Java 11+):

```
java scripts/Reorganize.java assets/ae2/lang
```

What it does:

- Treats `en_us.json` as the master and **never modifies it**. Its order and its
  blank-line grouping are the single source of truth.
- Rewrites every other locale to match that exact order and grouping.
- **Fills missing keys**: if your locale lacks a key, the English line is copied
  verbatim into its correct spot as a placeholder for you to translate.
- Keeps stale keys (present in a locale but no longer in `en_us`) grouped at the
  end of the file so they're easy to spot and remove.

To find what still needs translating in any locale, look for lines whose value is
still identical to the English one — those are the untranslated placeholders.

> **Note:** blank lines are valid JSON, so the grouping doesn't affect the game.
> Re-order things by editing `en_us.json` only; the script mirrors it everywhere.

---

## Contributing: the in-game guide

The AE2 Guide is written in Markdown and lives at `assets/ae2/ae2guide/`. In the
mod's own source it sits in a top-level `guidebook/` folder, but in the built jar
(and here) it is mounted at `assets/ae2/ae2guide/`.

### Where translated pages go

Each language goes in a `_<locale>` subfolder that **mirrors the English page
tree**:

```
assets/ae2/ae2guide/getting-started.md              <- English (base)
assets/ae2/ae2guide/ae2-mechanics/channels.md
assets/ae2/ae2guide/_es_es/getting-started.md       <- Spanish (Spain)
assets/ae2/ae2guide/_es_es/ae2-mechanics/channels.md
assets/ae2/ae2guide/_de_de/getting-started.md       <- German
...
```

GuideMe picks the folder matching the player's language and falls back to the
English page when a translated one is missing, so you can translate pages
gradually.

### What to translate inside a page

Guide pages are Markdown with YAML frontmatter and special components. Translate
only the human-readable text:

- **Do translate:** body prose, the frontmatter `title`, and the *visible text*
  of links `[visible text](path.md)`.
- **Do NOT touch:** component tags (`<ItemLink>`, `<GameScene>`,
  `<ImportStructure>`, etc.) and their attributes (`id=`, `src=`, coordinates,
  colors), file paths and anchors inside links, and the frontmatter `parent` and
  `icon` values.

### Do NOT copy the shared assets

Do **not** copy the `assets/` subfolder (the `.snbt` scene structures and `.png`
images) into your `_<locale>` folder. Those files have no text, are shared by all
languages, and are resolved from the base guide. Only the `.md` files are
translated.

---

## Licensing

This project is released under **CC0 1.0 (public domain)**, matching how AE2
itself licenses its text and translations. The imported base translations come
from AE2's community contributors and are likewise CC0.

Do not add copyrighted assets (e.g., AE2's own textures, models, or logo — those
are CC BY-NC-SA); anything you contribute should be your own work or CC0.

## Version

- Minecraft 1.21.1
- `pack_format`: 34