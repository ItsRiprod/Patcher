---
title: "Add a Crafting Bench Category"
order: 2
published: true
draft: false
---
# Add a Crafting Bench Category

The weapon bench ships with five crafting tabs. You want hexcode's Arcane tab to appear on that bench without touching the five that already exist.

## Wanted

- A new "Arcane" tab on the stock weapon bench.
- The five built-in tabs (Sword, Mace, Battleaxe, Daggers, Bow) untouched.
- Your staff items show up under the new tab.

## Before

Without Patchly, changing one field means your override file has to restate the whole asset, because Hytale does not deep-merge nested blocks. Here is the entire `Server/Item/Items/Bench/Bench_Weapon.json` you would copy and maintain, with the one line you actually wanted to touch marked:

```json
{
  "TranslationProperties": {
    "Name": "server.items.Bench_Weapon.name",
    "Description": "server.items.Bench_Weapon.description"
  },
  "Icon": "Icons/ItemsGenerated/Bench_Weapon.png",
  "Categories": [
    "Furniture.Benches"
  ],
  "Recipe": {
    "TimeSeconds": 3,
    "Input": [
      {
        "ItemId": "Ingredient_Bar_Copper",
        "Quantity": 2
      },
      {
        "ResourceTypeId": "Wood_Trunk",
        "Quantity": 10
      },
      {
        "ResourceTypeId": "Rock",
        "Quantity": 5
      }
    ],
    "BenchRequirement": [
      {
        "Type": "Crafting",
        "Categories": [
          "Workbench_Crafting"
        ],
        "Id": "Workbench"
      }
    ]
  },
  "BlockType": {
    "Material": "Solid",
    "DrawType": "Model",
    "Opacity": "Transparent",
    "CustomModel": "Blocks/Benches/Weapon.blockymodel",
    "CustomModelTexture": [
      {
        "Texture": "Blocks/Benches/Weapon_Texture.png",
        "Weight": 1
      }
    ],
    "VariantRotation": "NESW",
    "HitboxType": "Bench_Weapon",
    "BlockEntity": {
      "Components": {
        "BenchBlock": {}
      }
    },
    "State": {
      "Definitions": {
        "CraftCompleted": {
          "CustomModelAnimation": "Blocks/Benches/Weapon_Crafting.blockyanim",
          "Looping": true
        },
        "CraftCompletedInstant": {
          "CustomModelAnimation": "Blocks/Benches/Weapon_Crafting.blockyanim"
        }
      }
    },
    "Gathering": {
      "Breaking": {
        "GatherType": "Benches"
      }
    },
    "Bench": {
      "Type": "Crafting",
      "LocalOpenSoundEventId": "SFX_Weapon_Bench_Open",
      "LocalCloseSoundEventId": "SFX_Weapon_Bench_Close",
      "CompletedSoundEventId": "SFX_Weapon_Bench_Craft",
      "BenchUpgradeSoundEventId": "SFX_Workbench_Upgrade_Start_Default",
      "BenchUpgradeCompletedSoundEventId": "SFX_Workbench_Upgrade_Complete_Default",
      "Categories": [ // you only want to ADD one entry to this list (the other 5 must survive)
        {
          "Id": "Weapon_Sword",
          "Icon": "Icons/CraftingCategories/Armory/Sword.png",
          "Name": "server.benchCategories.sword"
        },
        {
          "Id": "Weapon_Mace",
          "Icon": "Icons/CraftingCategories/Armory/Mace.png",
          "Name": "server.benchCategories.mace"
        },
        {
          "Id": "Weapon_Battleaxe",
          "Icon": "Icons/CraftingCategories/Armory/Battleaxe.png",
          "Name": "server.benchCategories.battleaxe"
        },
        {
          "Id": "Weapon_Daggers",
          "Icon": "Icons/CraftingCategories/Armory/Daggers.png",
          "Name": "server.benchCategories.daggers"
        },
        {
          "Id": "Weapon_Bow",
          "Icon": "Icons/CraftingCategories/Armory/Bow.png",
          "Name": "server.benchCategories.bow"
        }
      ],
      "Id": "Weapon_Bench",
      "TierLevels": [
        {
          "CraftingTimeReductionModifier": 0.0,
          "UpgradeRequirement": {
            "Material": [
              {
                "ItemId": "Ingredient_Bar_Iron",
                "Quantity": 20
              },
              {
                "ItemId": "Ingredient_Leather_Light",
                "Quantity": 30
              },
              {
                "ItemId": "Ingredient_Fabric_Scrap_Linen",
                "Quantity": 30
              },
              {
                "ItemId": "Ingredient_Sac_Venom",
                "Quantity": 15
              }
            ],
            "TimeSeconds": 3
          }
        },
        {
          "UpgradeRequirement": {
            "Material": [
              {
                "ItemId": "Ingredient_Bar_Thorium",
                "Quantity": 25
              },
              {
                "ItemId": "Ingredient_Bar_Cobalt",
                "Quantity": 25
              },
              {
                "ItemId": "Ingredient_Fire_Essence",
                "Quantity": 20
              },
              {
                "ItemId": "Ingredient_Ice_Essence",
                "Quantity": 40
              },
              {
                "ItemId": "Ingredient_Void_Essence",
                "Quantity": 100
              }
            ],
            "TimeSeconds": 3
          },
          "CraftingTimeReductionModifier": 0.15
        },
        {
          "CraftingTimeReductionModifier": 0.3
        }
      ]
    },
    "BlockParticleSetId": "Stone",
    "ParticleColor": "#5C583E",
    "Support": {
      "Down": [
        {
          "FaceType": "Full"
        }
      ]
    },
    "BlockSoundSetId": "Stone"
  },
  "PlayerAnimationsId": "Block",
  "IconProperties": {
    "Scale": 0.37,
    "Translation": [
      18.7,
      -15
    ],
    "Rotation": [
      22.5,
      45,
      22.5
    ]
  },
  "Tags": {
    "Type": [
      "Bench"
    ]
  },
  "MaxStack": 1,
  "ItemLevel": 2,
  "ItemSoundSetId": "ISS_Blocks_Wood"
}
```

## The patch

Put this at `Server/Item/Items/Bench/Bench_Weapon.patch` in YOUR pack:

```json
{
  "BlockType": {
    "Bench": {
      "Categories+": [
        { "Id": "Arcane_Hexcode", "Icon": "Icons/CraftingCategories/Arcane/Arcane_Hexbook.png", "Name": "hexcode.workbench.benchCategories.hex" }
      ]
    }
  }
}
```

The key is `Categories+`, not `Categories`. The `+` appends to the array. A plain `Categories` would REPLACE the whole array and wipe out Sword, Mace, Battleaxe, Daggers, and Bow.

## After

The merged `Categories` array holds all six entries, in order: the five stock tabs first, then `Arcane_Hexcode` appended last.

- What changed: a sixth category, `Arcane_Hexcode`, is now on the bench.
- What survived: all five original categories, unchanged.

Any item whose `Recipe.BenchRequirement` lists `Categories` of `["Arcane_Hexcode"]` with an `Id` of `"Weapon_Bench"` now appears under the new tab. The item side is a separate patch; see [Add an Item to a Category](Item-Into-Category).

## Notes

- The `+` suffix is load-bearing. Without it the array is replaced, not extended. See the array rules in the [syntax reference](../).
- hexcode ships this exact pattern as a real Patchly patch in the wild at `Server/Item/Items/Bench/Bench_Arcane.patch` (it appends the same `Arcane_Hexcode` category onto its own Arcane bench).
- The `Name` value (`hexcode.workbench.benchCategories.hex`) is a lang key, not display text. The `.lang` file that defines it is shipped as a normal asset in your pack. Lang files are NOT patchable, so you cannot deep-merge translations onto another pack's `.lang`.
- If your staff items live in a pack that may load after the weapon bench, you do not need to do anything special. Patchly re-merges on every pack registration, so the append resolves correctly regardless of load order.
- If another pack also appends a tab and ordering matters, set `$Priority` on the patch that should apply last.
