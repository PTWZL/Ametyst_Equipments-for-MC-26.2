# Reference textures

These are **unmodified vanilla Minecraft textures**, extracted from the 26.2 client jar.
They are build-time inputs for `../generate_assets.py` only:

| File | Used for |
| --- | --- |
| `humanoid_netherite.png` | the worn armour layer (64x32) — recoloured to amethyst, and the head/body/arm fronts are cut out for the helmet and chestplate item icons |
| `leggings_netherite.png` | the worn leggings layer (64x32) — recoloured to amethyst |
| `item_diamond_*.png` | the tool, sword and spear pixel shapes — recoloured to amethyst |
| `item_enchanted_book.png` | the Refraction Book icon — recoloured to amethyst |

Nothing in this directory is packaged into the mod jar; only the recoloured output under
`src/main/resources/assets/amethyst_equipment/textures/` is.

If you would rather not keep Mojang assets in your repository, delete this directory and
re-extract the same nine files from a Minecraft 26.2 client jar into it before running the
generator. The generated textures already committed under `src/main/resources` do not
require the generator to run again.

Minecraft assets are property of Mojang Studios; this mod is not affiliated with or
endorsed by Mojang or Microsoft.
