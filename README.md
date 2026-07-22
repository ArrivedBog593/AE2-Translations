# AE2 Traducciones ES (es_ES / es_MX)

Resource pack de la comunidad que sobrescribe las traducciones de
[Applied Energistics 2](https://github.com/AppliedEnergistics/Applied-Energistics-2)
al español para **Minecraft 1.21.1**.

Existe porque AE2 gestiona sus traducciones solo por Crowdin y no acepta
Pull Requests de idiomas. Aquí **sí** se aceptan.

## Instalación

1. Descarga el `.zip` desde [Releases](../../releases) (no lo descomprimas).
2. Mételo en tu carpeta `.minecraft/resourcepacks/`.
3. En el juego: Opciones → Paquetes de recursos → actívalo.
4. Pon el idioma en **Español (México)** o **Español (España)**.

Es client-side: cada jugador lo instala por su cuenta.

## ¿Cómo contribuir una traducción?

1. Haz un fork y edita/añade el archivo en `assets/ae2/lang/<locale>.json`.
2. Usa `assets/ae2/lang/en_us.json` (o el en_us del mod que corras) como fuente de llaves.
3. **Preserva** los `%s`, `%d`, `%1$d`, `%%` tal cual — solo puedes cambiar su orden.
4. Manda tu Pull Request. 🎉

## Versión

- Minecraft 1.21.1
- `pack_format`: 34
