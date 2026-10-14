# Amethyst Equipment — full specification

[中文说明](README.zh.md) · [Landing page](README.md)

A Fabric mod for Minecraft **26.2** + Fabric Loader **0.19.5** + Fabric API **0.159.0+26.2**.

It adds a complete amethyst gear set: an amethyst ingot (smelted from amethyst shards, and
also found in abandoned mineshaft chests), a sword, a spear, a pickaxe, an axe, a shovel and
a hoe, plus a helmet / chestplate / leggings / boots set.

**The armour, sword and spear scale with the light level where the player is standing.**

---

## 1. Stats

Let `x` be the light level at the player's feet (0–15; the greater of block light and sky
light, so it drops at night and in thunderstorms, and only block light counts in the Nether).

### Armour (each piece is calculated this way)

| Stat | Formula |
| --- | --- |
| Armour | golden armour value `+ x × 0.8` |
| Knockback resistance | `+ x × 0.2` |

The golden base values (identical to vanilla):

| Piece | Base armour | At full light (x=15) | Durability |
| --- | --- | --- | --- |
| Helmet | 1 | 13 | 407 |
| Chestplate | 3 | 15 | 592 |
| Leggings | 5 | 17 | 555 |
| Boots | 2 | 14 | 481 |

> Every piece gains `x × 0.8` independently (the formula is per piece). So wearing all four
> at full light gives `11 + 4 × 15 × 0.8 = 59` total armour and `+12` knockback resistance.

### Weapons (sword and spear)

| Stat | Formula |
| --- | --- |
| Attack damage | `6 + x × 0.6` |

That is 6 at light 0 and **15** at light 15.

### Refraction (levels 1–10)

With level `L`, **every per-light coefficient of that item** is multiplied by `1 + 0.5 × L`:

| Level | Coefficient multiplier | Sword / spear per light | Armour per light | Knockback resistance per light |
| --- | --- | --- | --- | --- |
| 0 (none) | ×1 | +0.6 | +0.8 | +0.2 |
| 1 | ×1.5 | +0.9 | +1.2 | +0.3 |
| 5 | ×3.5 | +2.1 | +2.8 | +0.7 |
| 10 | ×6 | +3.6 | +4.8 | +1.2 |

Example: a sword with Refraction 5 at light 15 does `6 + 15 × 0.6 × 3.5 = 37.5` attack damage.

**Obtaining and using it**

1. **Craft the level 1 book**: crafting table — amethyst block at (1,1), diamond block at
   (2,2), amethyst block at (3,3) → Refraction Book (level 1).
2. **Infuse your gear**: put the Refraction Book plus any one piece of amethyst gear into a
   crafting table as a shapeless recipe. The level is written onto the gear (the higher of
   "the book's level" and "the gear's existing level") and the book is returned as a plain book.
3. **Go higher**: use `/amethystequipment refraction <1-10>` to get a book of that level,
   then repeat step 2. Infusing the same piece repeatedly raises it level by level.

> **Important note about it being an "enchantment" (a 26.2 platform limitation)**
>
> I first implemented it as a genuine data-driven enchantment, but that is **impossible** in
> 26.2, which I confirmed experimentally:
>
> 1. The enchantment registry is populated **only from vanilla's own data**. Putting
>    `data/<namespace>/enchantment/refraction.json` into the mod is never read — I deliberately
>    wrote that file as invalid JSON and not a single parse error appeared in the log; dumping
>    the registry at startup showed **exactly the 44 vanilla enchantments** and none of ours.
> 2. It cannot be registered from code either: 26.2's `BuiltInRegistries` has **no
>    `ENCHANTMENT` field**.
> 3. Fabric API has no hook for it — `EnchantmentEvents` can only observe/modify the enchanting
>    process, not add entries (I unpacked every Fabric API module to check).
>
> Refraction is therefore implemented as a **custom data component**. Functionally it is what
> was asked for: levels 1–10, a name and tooltip, craftable, and the effect
> "light coefficients ×(1 + 0.5 × level)". The only visible difference is that it appears as
> its own tooltip line rather than inside the vanilla enchantment list. If Mojang ever opens
> up modded enchantment registration, this can be switched back to a real enchantment.

### Durability

Every tool, weapon and armour piece uses **9/10 of the netherite value**:

* Tools / sword / spear: `2031 × 9/10 = 1827`
* Armour: netherite durability factor 37, rounded per piece after `× 9/10` (see the table above)

---

## 2. Items and recipes

Recipe shapes are **identical to vanilla**, with the material swapped for amethyst ingots.

| Item | Recipe | Notes |
| --- | --- | --- |
| Amethyst Ingot | smelt / blast 1 amethyst shard | 0.7 experience, same as vanilla ore smelting |
| Amethyst Sword | `X` / `X` / `#` | `X` = amethyst ingot, `#` = stick |
| Amethyst Spear | `··X` / `·#·` / `#··` | same as vanilla spears |
| Amethyst Pickaxe | `XXX` / `·#·` / `·#·` | |
| Amethyst Axe | `XX` / `X#` / `·#` | |
| Amethyst Shovel | `X` / `#` / `#` | |
| Amethyst Hoe | `XX` / `·#` / `·#` | |
| Amethyst Helmet | `XXX` / `X·X` | |
| Amethyst Chestplate | `X·X` / `XXX` / `XXX` | |
| Amethyst Leggings | `XXX` / `X·X` / `X·X` | |
| Amethyst Boots | `X·X` / `X·X` | |
| Refraction Book (level 1) | `1··` / `·2·` / `··3` | `1`/`3` = amethyst block, `2` = diamond block |
| Refraction infusion | shapeless: Refraction Book + any amethyst gear | writes the level onto the gear, returns a plain book |

* **Abandoned mineshafts**: an extra, independent loot pool is **appended** to the vanilla
  table (the vanilla drops are not replaced) with a 25% chance of 1–2 amethyst ingots.
* The amethyst ingot and Refraction Book are in the "Ingredients" creative tab; the gear is in
  the "Combat" tab.
* Amethyst tools/weapons and armour are repaired with amethyst ingots.

---

## 3. Installing

1. Install Fabric Loader for Minecraft 26.2.
2. Put [Fabric API](https://modrinth.com/mod/fabric-api) (0.159.0+26.2 or newer) in your
   `mods` folder.
3. Put `amethyst-equipment-1.0.0.jar` (from `build/libs/`, or the release download) in the same
   `mods` folder.
4. Launch the game.

Note for launchers that use **version isolation** (HMCL, and MultiMC/Prism in "isolated"
mode): the folder is then `versions\26.2\mods` rather than `.minecraft\mods`.

### Building

```bash
./gradlew build
```

Requires JDK 25. The jar lands in `build/libs/`. To regenerate the textures and data files:

```bash
python tools/generate_assets.py    # needs Python 3 + Pillow
```

The generated assets are committed, so the generator is optional. The colour source it reads
lives in `tools/reference/` (see the README there).

---

## 4. In-game self-check commands

These require permission level 2 (`/op`). All subcommands live under `/amethystequipment`:

| Command | Effect |
| --- | --- |
| `/amethystequipment items` | reports the registration state of all 12 items |
| `/amethystequipment stats` | prints durability, armour and the per-light coefficients |
| `/amethystequipment light` | reports the current light level and the live stat values |
| `/amethystequipment light <0-15>` | previews the values at any light level |
| `/amethystequipment refraction <1-10>` | hands out a Refraction Book of that level |
| `/amethystequipment grant` | gives one of every amethyst item |

Item tooltips also show the current light level, the current bonus, and the bonus at full light.

---

## 5. Project layout

```
amethyst-equipment/
├─ build.gradle / gradle.properties / settings.gradle
├─ README.md                          Chinese documentation
├─ README.en.md                       this file
├─ tools/generate_assets.py           generates every JSON and PNG
└─ src/main/
   ├─ java/cn/amethyst/amethystequipment/
   │  ├─ AmethystEquipment.java       entrypoint, constants and resource keys
   │  ├─ AmethystItems.java           12 items + the two materials
   │  ├─ ModTags.java                 repair-ingredient item tags
   │  ├─ ModRecipes.java              recipe serializer/type registration
   │  ├─ Refraction.java              the Refraction data component
   │  ├─ RefractionApplyRecipe.java   "book + gear -> infused gear"
   │  ├─ LightScaling.java            light -> attributes, the core logic (server tick)
   │  ├─ LootTables.java              abandoned mineshaft loot pool
   │  ├─ CreativeTabs.java            creative tab entries
   │  ├─ DebugCommands.java           the /amethystequipment command
   │  └─ client/
   │     ├─ AmethystEquipmentClient.java
   │     └─ LightTooltip.java         live values in the tooltip
   └─ resources/
      ├─ fabric.mod.json / pack.mcmeta
      ├─ assets/amethyst_equipment/   item models, equipment textures, lang files
      └─ data/amethyst_equipment/     recipes and tags
```

---

## 6. Implementation notes (how 26.2 differs from older versions)

In 26.2 there are no `ArmorItem` / `TieredItem` / `SwordItem` classes any more; everything is
configured up front through `Item.Properties`:

```java
new Item.Properties().durability(1827).sword(AMETHYST_TOOL_MATERIAL, 5.0F, -2.4F)
new Item.Properties().durability(407).humanoidArmor(AMETHYST_ARMOR_MATERIAL, ArmorType.HELMET)
new Item.Properties().durability(1827).spear(AMETHYST_TOOL_MATERIAL, 0.85F, 0.82F, 0.65F, ...)
```

The important details:

* **Attack damage**: vanilla adds the player's own base 1.0 on top of what the item supplies.
  To display a base attack damage of 6, the custom `ToolMaterial` has an attack damage bonus of
  `0.0` and `sword()` / `spear()` are passed `5.0`, giving `5.0 + 0.0 + 1.0 = 6.0`.
* **The spear's nine parameters** (the order matters — I had them shifted at first, which broke
  the right-click animation): `attackDuration, damageMultiplier, delay, dismountTime,
  dismountThreshold, knockbackTime, knockbackThreshold, damageTime, damageThreshold`.
  The right-click charge pose timeline is computed from
  `delay / dismountTime / knockbackTime / damageTime` and must satisfy `dismountTime ≥ 1`,
  `knockbackTime ≥ dismountTime + 2` and `damageTime ≥ knockbackTime + 0.25`;
  `knockbackThreshold` (5.1) and `damageThreshold` (4.6) are constants shared by every vanilla
  spear. The values are now **copied verbatim from the vanilla copper spear** (copper's
  `attackDamageBonus` is also `0.0`, so it takes exactly the same code paths). At startup I
  dumped the data components of the amethyst spear next to the vanilla copper and diamond
  spears and confirmed `kinetic_weapon`, `swing_animation`, `piercing_weapon`, `attack_range`
  and `use_effects` are **identical**.
* **The spear model must use `minecraft:item/generated`** (this was the actual cause of the
  wrong held/thrust pose): the vanilla spear's GUI model parents to `item/generated`, while I
  had intuitively written `item/handheld`. `item/handheld` carries a different set of display
  transforms, which throws off both the held pose and the charge thrust — the spear's in-hand
  pose is controlled by the item definition's `display_context` selection, which forwards to
  the `_in_hand` model (parent `minecraft:item/spear_in_hand`). After the change, the amethyst
  spear's model JSON matches the vanilla spear field for field.
* **Light scaling**: the dynamic armour and weapon values are applied as **transient attribute
  modifiers from the server tick** (`Attributes.ARMOR`, `Attributes.KNOCKBACK_RESISTANCE`,
  `Attributes.ATTACK_DAMAGE`). They are only rebuilt when the light level, the equipped gear or
  the Refraction level changes, and the result is cached. The server computes them and vanilla's
  normal attribute synchronisation delivers them to the client, so the numbers in the tooltip
  are exactly the numbers in force.
* **Item icons**: 26.2 renders armour items with a `minecraft:item/generated` model, so each
  piece needs its own 16×16 icon. The worn `humanoid` layer texture **must not** be reused for
  it (that is a 64×32 sheet of unwrapped UV fragments; using it directly shows a mess of
  misplaced pixels, which was the earlier bug). The four armour icons are now **taken from the
  vanilla armour texture's own pixels**: the helmet uses the 8×8 head front, the chestplate the
  8×12 body front plus the two arm fronts as shoulder pads, and both are mapped onto the
  amethyst ramp — so the shapes match vanilla armour. For the leggings and boots, vanilla's
  leggings sheet only offers a 4-pixel-high hip band and scattered leg islands, so those two
  silhouettes are drawn by hand in the same amethyst ramp to keep the set visually consistent.
* **Equipment textures**: `assets/amethyst_equipment/equipment/amethyst.json` declares that the
  `humanoid` and `humanoid_leggings` layers use `amethyst_equipment:amethyst`, which resolves to
  `assets/amethyst_equipment/textures/entity/equipment/humanoid/amethyst.png` and
  `.../humanoid_leggings/amethyst.png` (both 64×32).
* **Where the art comes from**: the tools and weapons are the vanilla diamond tool pixel shapes
  recoloured to amethyst; the worn armour layers reuse the vanilla netherite armour pixel layout
  recoloured (so they line up perfectly with the player model); the four armour icons and the
  Refraction Book icon are drawn/recoloured.

---

## 7. Localisation

The mod ships four language files and falls back to `en_us` for anything not listed:

| File | Language |
| --- | --- |
| `en_us.json` | English — also the default fallback for every other language |
| `zh_cn.json` | Simplified Chinese |
| `zh_tw.json` | Traditional Chinese (Taiwan) |
| `zh_hk.json` | Traditional Chinese (Hong Kong) |

So the game shows Chinese when the client language is any Chinese variant, and English
otherwise.

---

## 8. Known limitations

* **Refraction is not a vanilla enchantment** but a custom data component. See the end of
  section 1: 26.2's enchantment registry only loads from vanilla data and offers mods no
  registration entry point. It is functionally equivalent but displays as its own tooltip line.
* Baby mobs wear the same texture as adults (no `humanoid_baby` layer is provided).
* The light level is "the greater of block light and sky light at the player's feet", so it
  weakens outdoors at night — which is the intended "scales with the current light level".
