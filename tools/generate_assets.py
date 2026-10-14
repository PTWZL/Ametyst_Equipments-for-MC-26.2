"""Generates every JSON file and texture for the Amethyst Equipment mod.

Run with Python 3 and Pillow installed:
    python tools/generate_assets.py

It is idempotent: re-running overwrites the generated files. The hand-written Java
sources and fabric.mod.json are not touched.

The colour source for the item and armour art is a handful of unmodified vanilla textures
in ``tools/reference/`` (see the README there); they are build-time inputs only and are
never shipped inside the mod jar.
"""
from __future__ import annotations

import json
from pathlib import Path

from PIL import Image

MOD_ID = "amethyst_equipment"
ROOT = Path(__file__).resolve().parent.parent
RES = ROOT / "src" / "main" / "resources"
ASSETS = RES / "assets" / MOD_ID
DATA = RES / "data" / MOD_ID
REF = Path(__file__).resolve().parent / "reference"

ITEMS = [
    "amethyst_ingot",
    "amethyst_sword",
    "amethyst_spear",
    "amethyst_pickaxe",
    "amethyst_axe",
    "amethyst_shovel",
    "amethyst_hoe",
    "amethyst_helmet",
    "amethyst_chestplate",
    "amethyst_leggings",
    "amethyst_boots",
    "refraction_book",
]

HANDHELD = {
    "amethyst_sword",
    "amethyst_pickaxe",
    "amethyst_axe",
    "amethyst_shovel",
    "amethyst_hoe",
}

ENCHANTMENT = "refraction"


def write_json(path: Path, payload) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(payload, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


# --------------------------------------------------------------------------------------
# JSON: item model definitions (assets/<ns>/items/) and models (assets/<ns>/models/item/)
# --------------------------------------------------------------------------------------
def generate_item_definitions() -> None:
    for name in ITEMS:
        write_json(
            ASSETS / "items" / f"{name}.json",
            {"model": {"type": "minecraft:model", "model": f"{MOD_ID}:item/{name}"}},
        )

    # The spear is special-cased exactly like the vanilla ones: a flat model for the
    # inventory and a dedicated in-hand model for the held poses, which is what the spear
    # animations are authored against.
    write_json(
        ASSETS / "items" / "amethyst_spear.json",
        {
            "model": {
                "type": "minecraft:select",
                "cases": [
                    {
                        "model": {"type": "minecraft:model", "model": f"{MOD_ID}:item/amethyst_spear"},
                        "when": ["gui", "ground", "fixed", "on_shelf"],
                    }
                ],
                "fallback": {
                    "type": "minecraft:model",
                    "model": f"{MOD_ID}:item/amethyst_spear_in_hand",
                },
                "property": "minecraft:display_context",
            },
            "swap_animation_scale": 1.95,
        },
    )


def generate_item_models() -> None:
    # Armour uses minecraft:item/generated and therefore needs a dedicated 16x16 icon;
    # the worn-armour layers are declared through the equipment asset instead.
    # The spear deliberately also uses item/generated: that is what the vanilla spear models
    # use, and item/handheld's display transforms make the held spear pose wrong.
    for name in ITEMS:
        parent = ("minecraft:item/generated"
                  if name in ("amethyst_spear", "refraction_book") or name not in HANDHELD
                  else "minecraft:item/handheld")
        write_json(
            ASSETS / "models" / "item" / f"{name}.json",
            {"parent": parent, "textures": {"layer0": f"{MOD_ID}:item/{name}"}},
        )

    # Held model for the spear, using vanilla's spear_in_hand transforms.
    write_json(
        ASSETS / "models" / "item" / "amethyst_spear_in_hand.json",
        {
            "parent": "minecraft:item/spear_in_hand",
            "textures": {"layer0": f"{MOD_ID}:item/amethyst_spear"},
        },
    )


# --------------------------------------------------------------------------------------
# JSON: equipment asset (which armour textures the humanoid layers use)
# --------------------------------------------------------------------------------------
def generate_equipment_asset() -> None:
    write_json(
        ASSETS / "equipment" / "amethyst.json",
        {
            "layers": {
                "humanoid": [{"texture": f"{MOD_ID}:amethyst"}],
                "humanoid_leggings": [{"texture": f"{MOD_ID}:amethyst"}],
            }
        },
    )


# --------------------------------------------------------------------------------------
# JSON: item tags used as the repair ingredient
# --------------------------------------------------------------------------------------
def generate_tags() -> None:
    for tag in ("amethyst_tool_materials", "repairs_amethyst_armor"):
        write_json(
            DATA / "tags" / "item" / f"{tag}.json",
            {"values": [f"{MOD_ID}:amethyst_ingot"]},
        )

    # The Refraction enchantment applies to the mod's swords, spears and armour.
    write_json(
        DATA / "tags" / "item" / "amethyst_equipment.json",
        {
            "values": [
                f"{MOD_ID}:amethyst_sword",
                f"{MOD_ID}:amethyst_spear",
                f"{MOD_ID}:amethyst_helmet",
                f"{MOD_ID}:amethyst_chestplate",
                f"{MOD_ID}:amethyst_leggings",
                f"{MOD_ID}:amethyst_boots",
            ]
        },
    )
    write_json(
        DATA / "tags" / "item" / "enchantable" / "refraction.json",
        {"values": [f"#{MOD_ID}:amethyst_equipment"]},
    )

    # Some vanilla pose/AI code keys off minecraft:spears, so the amethyst spear belongs in
    # it. A datapack tag replaces the vanilla file, so all seven vanilla spears are listed
    # as well: this keeps the tag behaving exactly as it does in vanilla plus our spear.
    write_json(
        RES / "data" / "minecraft" / "tags" / "item" / "spears.json",
        {
            "values": [
                "minecraft:diamond_spear",
                "minecraft:stone_spear",
                "minecraft:golden_spear",
                "minecraft:netherite_spear",
                "minecraft:wooden_spear",
                "minecraft:iron_spear",
                "minecraft:copper_spear",
                f"{MOD_ID}:amethyst_spear",
            ]
        },
    )


# --------------------------------------------------------------------------------------
# The Refraction enchantment
# --------------------------------------------------------------------------------------
# It is NOT generated as a data file on purpose. In 26.2 the enchantment registry is filled
# from the vanilla data only, and a mod jar's data/minecraft/enchantment/*.json is ignored
# (measured: the file was never even read). The enchantment is therefore built and
# registered from code in cn.amethyst.amethystequipment.Refraction.


# --------------------------------------------------------------------------------------
# JSON: recipes. Shapes are copied verbatim from vanilla, with the material swapped.
# --------------------------------------------------------------------------------------
STICK = "minecraft:stick"
INGOT = f"{MOD_ID}:amethyst_ingot"
SHARD = "minecraft:amethyst_shard"
AMETHYST_BLOCK = "minecraft:amethyst_block"
DIAMOND_BLOCK = "minecraft:diamond_block"


def shaped(name: str, pattern, key) -> dict:
    return {
        "type": "minecraft:crafting_shaped",
        "category": "equipment",
        "key": key,
        "pattern": pattern,
        "result": {"id": f"{MOD_ID}:{name}"},
    }


def generate_recipes() -> None:
    # Tools and weapons use the vanilla stick + material shapes.
    write_json(
        DATA / "recipe" / "amethyst_sword.json",
        shaped("amethyst_sword", ["X", "X", "#"], {"#": STICK, "X": INGOT}),
    )
    write_json(
        DATA / "recipe" / "amethyst_spear.json",
        shaped("amethyst_spear", ["  X", " # ", "#  "], {"#": STICK, "X": INGOT}),
    )
    write_json(
        DATA / "recipe" / "amethyst_pickaxe.json",
        shaped("amethyst_pickaxe", ["XXX", " # ", " # "], {"#": STICK, "X": INGOT}),
    )
    write_json(
        DATA / "recipe" / "amethyst_axe.json",
        shaped("amethyst_axe", ["XX", "X#", " #"], {"#": STICK, "X": INGOT}),
    )
    write_json(
        DATA / "recipe" / "amethyst_shovel.json",
        shaped("amethyst_shovel", ["X", "#", "#"], {"#": STICK, "X": INGOT}),
    )
    write_json(
        DATA / "recipe" / "amethyst_hoe.json",
        shaped("amethyst_hoe", ["XX", " #", " #"], {"#": STICK, "X": INGOT}),
    )

    # Armour uses the vanilla armour shapes with the material swapped (like iron/diamond).
    write_json(
        DATA / "recipe" / "amethyst_helmet.json",
        shaped("amethyst_helmet", ["XXX", "X X"], {"X": INGOT}),
    )
    write_json(
        DATA / "recipe" / "amethyst_chestplate.json",
        shaped("amethyst_chestplate", ["X X", "XXX", "XXX"], {"X": INGOT}),
    )
    write_json(
        DATA / "recipe" / "amethyst_leggings.json",
        shaped("amethyst_leggings", ["XXX", "X X", "X X"], {"X": INGOT}),
    )
    write_json(
        DATA / "recipe" / "amethyst_boots.json",
        shaped("amethyst_boots", ["X X", "X X"], {"X": INGOT}),
    )

    # Amethyst ingot from smelting/blasting amethyst shards, shaped exactly like the
    # vanilla "ore to ingot" recipes.
    for kind, experience in (("smelting", 0.7), ("blasting", 0.7)):
        write_json(
            DATA / "recipe" / f"amethyst_ingot_from_{kind}_amethyst_shard.json",
            {
                "type": f"minecraft:{kind}",
                "experience": experience,
                "group": "amethyst_ingot",
                "ingredient": SHARD,
                "result": {"id": f"{MOD_ID}:amethyst_ingot"},
            },
        )

    # Level 1 Refraction book. Shaped-recipe keys must be single-character symbols, so the
    # three diagonal cells use "1", "2" and "3". The level is baked into the item itself
    # (see AmethystItems.REFRACTION_BOOK), so no component has to be written here: a recipe
    # result's "components" block cannot resolve a mod component type.
    write_json(
        DATA / "recipe" / "refraction_book.json",
        {
            "type": "minecraft:crafting_shaped",
            "category": "misc",
            "key": {
                "1": AMETHYST_BLOCK,
                "2": DIAMOND_BLOCK,
                "3": AMETHYST_BLOCK,
            },
            "pattern": ["1  ", " 2 ", "  3"],
            "result": {"id": f"{MOD_ID}:{ENCHANTMENT}_book"},
        },
    )

    # Applying a Refraction book to amethyst gear. One custom shapeless recipe covers every
    # level (it takes the highest of the book and gear levels), which a shaped recipe and a
    # component-carrying result cannot do.
    write_json(
        DATA / "recipe" / "refraction_apply.json",
        {
            "type": f"{MOD_ID}:refraction_apply",
            "category": "misc",
        },
    )


# --------------------------------------------------------------------------------------
# Textures
# --------------------------------------------------------------------------------------
def load(name: str) -> Image.Image:
    return Image.open(REF / name).convert("RGBA")


# Amethyst ramp, dark -> light, used to recolour the vanilla tool shapes. The dark end
# keeps the wooden handle reading as dark wood while the light end gives the crystal its
# shine, so the contrast structure of the original sprite survives the recolour.
AMETHYST_RAMP = [
    (38, 20, 30),    # handle outline / near black
    (58, 32, 26),    # dark wood
    (80, 46, 44),    # wood
    (98, 58, 68),    # wood highlight
    (112, 44, 150),  # dark crystal
    (140, 70, 186),  # crystal
    (170, 100, 214),  # light crystal
    (198, 138, 232),  # bright crystal
    (222, 178, 244),  # highlight
    (243, 220, 252),  # glint
]


def recolour_tool(source: Image.Image) -> Image.Image:
    """Maps a diamond tool's luminance onto the amethyst ramp."""
    img = source.convert("RGBA")
    px = img.load()
    w, h = img.size
    last = len(AMETHYST_RAMP) - 1

    for y in range(h):
        for x in range(w):
            r, g, b, a = px[x, y]

            if a == 0:
                continue

            lum = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
            idx = int(round(lum * last))
            idx = max(0, min(last, idx))
            nr, ng, nb = AMETHYST_RAMP[idx]
            px[x, y] = (nr, ng, nb, a)

    return img


# Amethyst ramp used for the armour layers. Vanilla armour uses a flat, low-contrast
# palette, so this one is tuned to stay close to netherite's tonal range.
ARMOUR_RAMP = [
    (44, 24, 52),
    (60, 34, 70),
    (78, 46, 90),
    (96, 58, 110),
    (116, 74, 134),
    (138, 92, 158),
    (162, 116, 182),
    (188, 146, 206),
    (212, 180, 226),
    (236, 214, 244),
]


def recolour_armour(source: Image.Image) -> Image.Image:
    """Recolours a vanilla armour layer onto the amethyst palette.

    The layer geometry is kept pixel-for-pixel (so it always matches the player model);
    only the colours change. The source luminance is stretched a little so the plate
    detail of the netherite texture stays visible in purple.
    """
    img = source.convert("RGBA")
    px = img.load()
    w, h = img.size
    last = len(ARMOUR_RAMP) - 1

    for y in range(h):
        for x in range(w):
            r, g, b, a = px[x, y]

            if a == 0:
                continue

            lum = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
            idx = int(round((lum / 0.45) * last))
            idx = max(0, min(last, idx))
            nr, ng, nb = ARMOUR_RAMP[idx]
            px[x, y] = (nr, ng, nb, a)

    return img


def make_ingot() -> Image.Image:
    """A 16x16 amethyst ingot, in the same isometric style as the vanilla ingots.

    Drawn as a hexagonal prism: a narrow top face, a wider front face, and a 1px bevel
    column down each sloping side.
    """
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()

    outline = (56, 24, 40, 255)
    top_light = (226, 176, 244, 255)
    top_dark = (176, 112, 210, 255)
    front_base = (122, 58, 164, 255)
    bevel_left = (168, 106, 206, 255)
    bevel_right = (94, 42, 132, 255)
    glint = (246, 226, 254, 255)

    # (y, left, right, kind): top face rows then front face rows.
    rows = [
        (2, 6, 9, "top"),
        (3, 5, 10, "top"),
        (4, 4, 11, "top"),
        (5, 4, 11, "front"),
        (6, 3, 12, "front"),
        (7, 3, 12, "front"),
        (8, 3, 12, "front"),
        (9, 3, 12, "front"),
        (10, 3, 12, "front"),
        (11, 3, 12, "front"),
        (12, 4, 11, "front"),
    ]

    for y, x0, x1, kind in rows:
        for x in range(x0, x1 + 1):
            if kind == "top":
                px[x, y] = top_light if (x + y) % 2 else top_dark
            else:
                px[x, y] = front_base

    # Shade the sloping sides so the prism reads as three dimensional.
    for y, x0, x1, kind in rows:
        if kind != "front":
            continue

        px[x0, y] = bevel_left
        px[x1, y] = bevel_right

    # Top edge highlight and a couple of crystal glints.
    for x in range(4, 12):
        px[x, 5] = top_dark

    px[6, 3] = glint
    px[9, 3] = glint
    px[5, 7] = bevel_left
    px[6, 9] = top_dark

    # 1px outline around the whole silhouette.
    solid = {(x, y) for y, x0, x1, _ in rows for x in range(x0, x1 + 1)}
    for y, x0, x1, _ in rows:
        for x in range(x0, x1 + 1):
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                nx, ny = x + dx, y + dy

                if 0 <= nx < 16 and 0 <= ny < 16 and (nx, ny) not in solid:
                    px[nx, ny] = outline

    return img


ICON_OUTLINE = (44, 20, 52, 255)
# Hand-painted silhouettes use the amethyst ramp directly; the cut-out icons take their
# shading from the vanilla texture and are mapped onto the same ramp afterwards.
ICON_DARK = ARMOUR_RAMP[2]
ICON_MID = ARMOUR_RAMP[5]
ICON_LIGHT = ARMOUR_RAMP[7]
ICON_GLINT = ARMOUR_RAMP[9]

# --- armour item icons ---------------------------------------------------------------
# 26.2 renders armour items with minecraft:item/generated, so each piece needs a real
# 16x16 icon. The worn-armour layers are 64x32 sheets of unwrapped box faces and must
# never be used directly as an icon, so each icon is assembled from the relevant faces of
# the vanilla texture and then recoloured: the geometry is vanilla's own pixel art (which
# is what makes it read as armour) and only the palette is ours.

# Box faces inside the vanilla 64x32 humanoid armour sheets. (left, top, right, bottom)
HUMANOID_HEAD_FRONT = (8, 8, 16, 16)
HUMANOID_BODY_FRONT = (20, 20, 28, 32)
HUMANOID_ARM_FRONT = (44, 20, 48, 32)
LEGGINGS_LEG_FRONT = (4, 20, 8, 32)
LEGGINGS_LEG_SIDE = (0, 20, 4, 32)


def make_icon(parts) -> Image.Image:
    """Composes a 16x16 icon from (source, box, destination) parts, then recolours it."""
    icon = Image.new("RGBA", (16, 16), (0, 0, 0, 0))

    for source, box, destination in parts:
        piece = source.crop(box)
        icon.alpha_composite(piece, destination)

    draw_icon_detail(icon)
    return recolour_icon(icon)


def draw_icon_detail(icon: Image.Image) -> None:
    """Adds the detail the raw front faces lack: a visor, glints and a 1px outline."""
    px = icon.load()

    opaque = {(x, y) for y in range(16) for x in range(16) if px[x, y][3] != 0}

    if not opaque:
        return

    xs = [p[0] for p in opaque]
    ys = [p[1] for p in opaque]
    min_x, max_x = min(xs), max(xs)
    min_y, max_y = min(ys), max(ys)

    # A bright glint near the top-left of the silhouette and a dark one at the bottom-right,
    # mirroring how vanilla armour art shades its boxes.
    glint = (min_x + 1, min_y + 1)
    shade = (max_x - 1, max_y - 1)

    if glint in opaque:
        px[glint[0], glint[1]] = ICON_GLINT

    if shade in opaque:
        px[shade[0], shade[1]] = ICON_DARK

    # Outline everything.
    for y in range(16):
        for x in range(16):
            if (x, y) in opaque:
                continue

            if any((x + dx, y + dy) in opaque for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                px[x, y] = ICON_OUTLINE


def recolour_icon(icon: Image.Image) -> Image.Image:
    """Maps the vanilla armour shading onto the amethyst ramp, keeping the outline dark."""
    px = icon.load()
    last = len(ARMOUR_RAMP) - 1

    for y in range(16):
        for x in range(16):
            r, g, b, a = px[x, y]

            if a == 0:
                continue

            if (r, g, b) == ICON_OUTLINE[:3]:
                continue

            lum = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
            idx = max(0, min(last, int(round((0.18 + lum * 1.30) * last))))
            nr, ng, nb = ARMOUR_RAMP[idx]
            px[x, y] = (nr, ng, nb, a)

    return icon


def make_helmet_icon() -> Image.Image:
    # The 8x8 head front, inset by a pixel so the outline has room.
    return make_icon([(load("humanoid_netherite.png"), HUMANOID_HEAD_FRONT, (4, 4))])


def make_chestplate_icon() -> Image.Image:
    # Body front with the two arm fronts beside it as shoulder pads.
    return make_icon([
        (load("humanoid_netherite.png"), HUMANOID_BODY_FRONT, (4, 2)),
        (load("humanoid_netherite.png"), HUMANOID_ARM_FRONT, (0, 2)),
        (load("humanoid_netherite.png"), HUMANOID_ARM_FRONT, (12, 2)),
    ])


def shade_cell(icon: Image.Image, x0: int, y0: int, x1: int, y1: int) -> None:
    """Fills a rectangle with the amethyst armour palette.

    Light comes from the top-left, matching how the helmet and chestplate icons are lit
    (they are cut from the vanilla texture), so all four icons read as one set.
    """
    px = icon.load()

    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            if y == y0:
                px[x, y] = ICON_LIGHT
            elif y == y1 or x == x1:
                px[x, y] = ICON_DARK
            elif x == x0:
                px[x, y] = ICON_MID
            else:
                px[x, y] = ICON_MID if (x + y) % 3 else ICON_LIGHT


def finish_icon(icon: Image.Image) -> Image.Image:
    """Outlines a hand-painted silhouette. It already uses the amethyst ramp, so it is not
    re-mapped (unlike the icons cut out of the desaturated vanilla texture)."""
    draw_icon_detail(icon)
    return icon


def make_leggings_icon() -> Image.Image:
    # Belt across the top, two legs below with a gap between them: the silhouette players
    # read as "leggings". The vanilla leggings sheet only carries a 4px-high band for the
    # hip, so this silhouette is painted rather than cut out.
    icon = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    shade_cell(icon, 2, 2, 13, 5)
    shade_cell(icon, 2, 6, 6, 13)
    shade_cell(icon, 9, 6, 13, 13)
    return finish_icon(icon)


def make_boots_icon() -> Image.Image:
    # A lower, wider silhouette than the leggings: short shafts with feet that step
    # outwards, so the pair reads as boots at a glance.
    icon = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    shade_cell(icon, 3, 4, 6, 9)
    shade_cell(icon, 9, 4, 12, 9)
    shade_cell(icon, 1, 10, 6, 12)
    shade_cell(icon, 9, 10, 14, 12)
    return finish_icon(icon)


ARMOR_ICONS = {
    "amethyst_helmet": make_helmet_icon,
    "amethyst_chestplate": make_chestplate_icon,
    "amethyst_leggings": make_leggings_icon,
    "amethyst_boots": make_boots_icon,
}


def recolour_book(source: Image.Image) -> Image.Image:
    """Recolours the vanilla enchanted book onto the amethyst palette.

    Keeps the exact book silhouette (parchment body, strap, clasp) and only shifts the hue,
    then re-asserts a dark outline so it stays readable at 16x16.
    """
    img = source.convert("RGBA")
    px = img.load()
    w, h = img.size
    last = len(ARMOUR_RAMP) - 1

    for y in range(h):
        for x in range(w):
            r, g, b, a = px[x, y]

            if a == 0:
                continue

            lum = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0

            if lum < 0.10:
                px[x, y] = ICON_OUTLINE
                continue

            idx = max(0, min(last, int(round((0.20 + lum * 1.15) * last))))
            nr, ng, nb = ARMOUR_RAMP[idx]
            px[x, y] = (nr, ng, nb, a)

    return img


def generate_textures() -> None:
    item_dir = ASSETS / "textures" / "item"
    item_dir.mkdir(parents=True, exist_ok=True)

    # Ingot.
    make_ingot().save(item_dir / "amethyst_ingot.png")

    # Tools and weapons: diamond shapes recoloured to amethyst.
    tool_sources = {
        "amethyst_pickaxe": "item_diamond_pickaxe.png",
        "amethyst_axe": "item_diamond_axe.png",
        "amethyst_shovel": "item_diamond_shovel.png",
        "amethyst_hoe": "item_diamond_hoe.png",
        "amethyst_sword": "item_diamond_sword.png",
        "amethyst_spear": "item_diamond_spear.png",
    }
    for name, source in tool_sources.items():
        recolour_tool(load(source)).save(item_dir / f"{name}.png")

    # Armour item icons (flat 16x16 sprites). The worn layers are a different asset and
    # are generated below; they must not be reused here.
    for name, maker in ARMOR_ICONS.items():
        maker().save(item_dir / f"{name}.png")

    # Refraction book: the vanilla enchanted book silhouette, recoloured to amethyst.
    recolour_book(load("item_enchanted_book.png")).save(item_dir / "refraction_book.png")

    # Armour: vanilla layer geometry, amethyst palette.
    for layer, source in (
        ("humanoid", "humanoid_netherite.png"),
        ("humanoid_leggings", "leggings_netherite.png"),
    ):
        out_dir = ASSETS / "textures" / "entity" / "equipment" / layer
        out_dir.mkdir(parents=True, exist_ok=True)
        recolour_armour(load(source)).save(out_dir / "amethyst.png")

    # Mod icon: a 128x128 blown-up ingot on a dark background.
    icon = Image.new("RGBA", (128, 128), (26, 12, 40, 255))
    ingot = make_ingot().resize((112, 112), Image.Resampling.NEAREST)
    icon.alpha_composite(ingot, (8, 8))
    icon.save(ASSETS / "icon.png")


def main() -> None:
    generate_item_definitions()
    generate_item_models()
    generate_equipment_asset()
    generate_tags()
    generate_recipes()
    generate_textures()
    print(f"generated assets/data for {len(ITEMS)} items under {ROOT}")


if __name__ == "__main__":
    main()
