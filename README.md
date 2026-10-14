# Amethyst Equipment

[![License: CC0-1.0](https://img.shields.io/badge/License-CC0%201.0-lightgrey.svg)](LICENSE)
[![Minecraft 26.2](https://img.shields.io/badge/Minecraft-26.2-brightgreen.svg)](#requirements)
[![Fabric Loader 0.19.5](https://img.shields.io/badge/Fabric%20Loader-0.19.5-blue.svg)](#requirements)

A Fabric mod that adds a full amethyst gear set — ingot, sword, spear, pickaxe, axe, shovel,
hoe and a four-piece armour set — whose **armour, sword and spear scale with the light level
you are standing in**.

The brighter it is, the stronger you are. Fight in the dark and you are just a person holding
a purple sword.

> **Documentation**: [中文详细说明](README.zh.md) · [English full specification](README.en.md)

---

## Features

* **Amethyst ingot** — smelted or blasted from amethyst shards, and found in abandoned
  mineshaft chests (an extra loot pool is *appended* to the vanilla table, so vanilla drops
  are untouched).
* **Full gear set** — sword, spear, pickaxe, axe, shovel, hoe, helmet, chestplate, leggings,
  boots. Recipe shapes are **identical to vanilla**, just with the material swapped.
* **Light-scaled stats** — armour gains `+0.8` armour and `+0.2` knockback resistance per light
  level on top of the golden armour values; the sword and spear do `6 + 0.6 × light`.
* **Refraction** — a craftable, 10-level upgrade that multiplies *every* per-light coefficient
  of that item by `1 + 0.5 × level` (level 10 = ×6).
* **9/10 netherite durability** on every tool, weapon and armour piece.
* **Localised** — English (also the default fallback), Simplified Chinese and Traditional
  Chinese (Taiwan / Hong Kong).

## Requirements

| | Version |
| --- | --- |
| Minecraft | 26.2 |
| Fabric Loader | 0.19.5 or newer |
| Fabric API | 0.159.0+26.2 |
| Java | 25 |

## Installation

1. Install Fabric Loader for Minecraft 26.2.
2. Put [Fabric API](https://modrinth.com/mod/fabric-api) in your `mods` folder.
3. Put `amethyst-equipment-<version>.jar` in the same `mods` folder.
4. Launch the game.

## The numbers

Let `x` be the light level at your feet (0–15 — the greater of block light and sky light, so
it weakens outdoors at night).

| | Formula | At light 15 |
| --- | --- | --- |
| Armour, per piece | golden values `+ x × 0.8` armour, `+ x × 0.2` knockback resistance | helmet 1→13, chest 3→15, legs 5→17, boots 2→14 |
| Sword / spear | `6 + x × 0.6` attack damage | 15 |

Refraction multiplies those per-light coefficients:

| Refraction level | Multiplier | Sword / spear per light |
| --- | --- | --- |
| 1 | ×1.5 | +0.9 |
| 5 | ×3.5 | +2.1 |
| 10 | ×6 | +3.6 |

A Refraction 5 sword at light 15 therefore does `6 + 15 × 0.6 × 3.5 = 37.5` damage.

Craft the level 1 book from an amethyst block / diamond block / amethyst block diagonal, then
combine the book with any amethyst gear (shapeless) to infuse it. Use
`/amethystequipment refraction <1-10>` for higher-level books.

## Building from source

```bash
./gradlew build
```

The jar lands in `build/libs/`. Requires JDK 25. The textures and JSON under
`src/main/resources` are committed, so the asset generator is optional:

```bash
python tools/generate_assets.py    # needs Python 3 + Pillow
```

## In-game self-check

Requires permission level 2 (`/op`):

| Command | Effect |
| --- | --- |
| `/amethystequipment items` | reports the registration state of all 12 items |
| `/amethystequipment stats` | prints durability, armour and the per-light coefficients |
| `/amethystequipment light [0-15]` | reports or previews the live stat values |
| `/amethystequipment refraction <1-10>` | gives a Refraction Book of that level |
| `/amethystequipment grant` | gives one of every amethyst item |

## Licence

[CC0-1.0](LICENSE) — public domain, do whatever you like with it.

## Disclaimer

Not an official Minecraft product. Not approved by or associated with Mojang Studios or
Microsoft. "Minecraft" is a trademark of Mojang Synergies AB.
