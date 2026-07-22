# AE2 Translations

A community resource pack that overrides the in-game translations of
[Applied Energistics 2](https://github.com/AppliedEnergistics/Applied-Energistics-2)
for **Minecraft 1.21.1**.

It exists because AE2 manages its translations only through Crowdin and does not
accept language Pull Requests. Here, they're welcome.

## Installation

1. Download the `.zip` from [Releases](../../releases) (do **not** unzip it).
2. Drop it into your `.minecraft/resourcepacks/` folder.
3. In-game: Options → Resource Packs → enable it.
4. Set your in-game language (Options → Language) to the one you want to use.

It's client-side: every player installs it on their own.

## Contributing a translation

1. Fork the repo and edit or add the file at `assets/ae2/lang/<locale>.json`
   (e.g., `de_de.json`, `fr_fr.json`, `pt_br.json`).
2. Use `assets/ae2/lang/en_us.json` (or the `en_us` from the mod version you run)
   as the source of truth for the keys.
3. **Preserve** the `%s`, `%d`, `%1$d`, `%%` placeholders exactly — you may only
   reorder them to match your language's grammar.
4. Open a Pull Request.

## Version

- Minecraft 1.21.1
- `pack_format`: 34