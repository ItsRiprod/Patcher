---
title: "Put an Item Into a Category"
order: 10
published: true
draft: false
---

# Put an Item Into a Category

Items carry a top-level `Categories` array of string ids that drives creative-library and inventory grouping. You want to add an existing item to a custom category without losing its stock memberships.

## Wanted

- Make the vanilla iron sword ALSO appear under hexcode's staff category `Hexcode.Staves`, but only when the Hexcode mod is installed.
- Keep its stock `Items.Weapons` membership intact.

## Before

Without Patchly, adding one category means your override file has to restate the whole asset, because Hytale does not deep-merge nested blocks and arrays replace rather than append. Worse here: this item has no `Categories` key of its own. It inherits `"Categories": [ "Items.Weapons" ]` from its parent, `Template_Weapon_Sword`. So the old way you would copy this entire `Server/Item/Items/Weapon/Sword/Weapon_Sword_Iron.json` AND, because arrays replace, restate the full inherited `Categories` list yourself just to add one entry:

```json
{
  "Parent": "Template_Weapon_Sword", // Categories is inherited from here as ["Items.Weapons"]; the old way you must copy this whole file and restate it
  "TranslationProperties": {
    "Name": "server.items.Weapon_Sword_Iron.name"
  },
  "Model": "Items/Weapons/Sword/Iron.blockymodel",
  "Texture": "Items/Weapons/Sword/Iron_Texture.png",
  "Quality": "Uncommon",
  "Icon": "Icons/ItemsGenerated/Weapon_Sword_Iron.png",
  "ItemLevel": 20,
  "Recipe": {
    "TimeSeconds": 3.5,
    "KnowledgeRequired": false,
    "Input": [
      {
        "ItemId": "Ingredient_Bar_Iron",
        "Quantity": 6
      },
      {
        "ItemId": "Ingredient_Leather_Light",
        "Quantity": 3
      },
      {
        "ItemId": "Ingredient_Fabric_Scrap_Linen",
        "Quantity": 3
      }
    ],
    "BenchRequirement": [
      {
        "Type": "Crafting",
        "Categories": [
          "Weapon_Sword"
        ],
        "Id": "Weapon_Bench"
      }
    ]
  },
  "InteractionVars": {
    "Swing_Left_Damage": {
      "Interactions": [
        {
          "Parent": "Weapon_Sword_Primary_Swing_Left_Damage",
          "DamageCalculator": {
            "BaseDamage": {
              "Physical": 10
            }
          },
          "DamageEffects": {
            "WorldSoundEventId": "SFX_Sword_T2_Impact",
            "LocalSoundEventId": "SFX_Sword_T2_Impact"
          }
        }
      ]
    },
    "Swing_Right_Damage": {
      "Interactions": [
        {
          "Parent": "Weapon_Sword_Primary_Swing_Right_Damage",
          "DamageCalculator": {
            "BaseDamage": {
              "Physical": 10
            }
          },
          "DamageEffects": {
            "WorldSoundEventId": "SFX_Sword_T2_Impact",
            "LocalSoundEventId": "SFX_Sword_T2_Impact"
          }
        }
      ]
    },
    "Swing_Down_Damage": {
      "Interactions": [
        {
          "Parent": "Weapon_Sword_Primary_Swing_Down_Damage",
          "DamageCalculator": {
            "BaseDamage": {
              "Physical": 18
            }
          },
          "DamageEffects": {
            "WorldSoundEventId": "SFX_Sword_T2_Impact",
            "LocalSoundEventId": "SFX_Sword_T2_Impact"
          }
        }
      ]
    },
    "Thrust_Damage": {
      "Interactions": [
        {
          "Parent": "Weapon_Sword_Primary_Thrust_Damage",
          "DamageCalculator": {
            "BaseDamage": {
              "Physical": 26
            }
          },
          "EntityStatsOnHit": [
            {
              "EntityStatId": "SignatureEnergy",
              "Amount": 3
            }
          ],
          "DamageEffects": {
            "WorldSoundEventId": "SFX_Sword_T2_Impact",
            "LocalSoundEventId": "SFX_Sword_T2_Impact"
          }
        }
      ]
    },
    "Vortexstrike_Spin_Damage": {
      "Interactions": [
        {
          "Parent": "Weapon_Sword_Signature_Vortexstrike_Spin_Damage",
          "DamageCalculator": {
            "BaseDamage": {
              "Physical": 19
            }
          },
          "EntityStatsOnHit": [],
          "DamageEffects": {
            "WorldSoundEventId": "SFX_Sword_T2_Impact",
            "LocalSoundEventId": "SFX_Sword_T2_Impact"
          }
        }
      ]
    },
    "Vortexstrike_Stab_Damage": {
      "Interactions": [
        {
          "Parent": "Weapon_Sword_Signature_Vortexstrike_Stab_Damage",
          "DamageCalculator": {
            "BaseDamage": {
              "Physical": 56
            }
          },
          "EntityStatsOnHit": [],
          "DamageEffects": {
            "WorldSoundEventId": "SFX_Sword_T2_Impact",
            "LocalSoundEventId": "SFX_Sword_T2_Impact"
          }
        }
      ]
    },
    "Guard_Wield": {
      "Interactions": [
        {
          "Parent": "Weapon_Sword_Secondary_Guard_Wield",
          "StaminaCost": {
            "Value": 10,
            "CostType": "Damage"
          }
        }
      ]
    }
  },
  "MaxDurability": 120,
  "DurabilityLossOnHit": 0.21
}
```

For context, the inherited value lives in the parent `Template_Weapon_Sword`:

```json
{
  "Categories": [ "Items.Weapons" ]
}
```

So the fully resolved `Weapon_Sword_Iron` has `"Categories": [ "Items.Weapons" ]`, even though that line lives in the parent, not the child.

## The patch

Path: `Server/Item/Items/Weapon/Sword/Weapon_Sword_Iron.patch`

```json
{
  "$Requires": "Riprod:Hexcode",
  "Categories+": [ "Hexcode.Staves" ]
}
```

## After

The merged item's `Categories` is:

```json
{
  "Categories": [ "Items.Weapons", "Hexcode.Staves" ]
}
```

- `Hexcode.Staves` was appended.
- `Items.Weapons` survived because `+` appends instead of replacing.
- The whole patch only applies when the `Riprod:Hexcode` mod is installed; without it, the patch is skipped and the sword keeps its stock `Categories`.

This works because Patchly merges your patch onto the FULLY RESOLVED base asset, with inheritance already applied. The inherited `Items.Weapons` entry is present at merge time even though it came from the parent file, so `Categories+` appends to it.

## Notes

- The `+` matters. A plain `Categories` key would REPLACE the array and drop `Items.Weapons`.
- `$Requires` gates the entire patch on another mod being present. `Riprod:Hexcode` is the hexcode spell-crafting mod, and `Hexcode.Staves` is the creative category its staff items use. Listing a vanilla sword there only makes sense when hexcode is installed, so gating avoids dangling references on servers that lack it.
- The category id must be registered somewhere (a creative tab / ItemCategory, or a bench category) for it to actually show up. This example is the ITEM side: it puts an item INTO a category. To DEFINE a new bench category, see [Crafting-Table-Category](Crafting-Table-Category).
- This is also a clean demonstration that Patchly patches the resolved asset, not the raw child file. See the syntax reference at [../](../) for how `+` and array merging work.
