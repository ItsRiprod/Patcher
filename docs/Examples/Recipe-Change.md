---
title: "Change a Crafting Recipe"
order: 9
published: true
draft: false
---

# Change a Crafting Recipe

Items carry a top-level `"Recipe"` object that sets craft time, knowledge gate,
inputs, and bench. You can tune one field, fully redesign the ingredient list, or
bolt on a single extra ingredient. The trick is knowing when arrays replace and
when they append.

## Wanted

Make the iron sword faster and cheaper to craft. First version: drop the time and
strip the recipe down to a few iron bars. Second version: keep the stock recipe
but add one more ingredient.

## Before

Without Patchly, changing one field means your override file has to restate the
whole asset, because Hytale does not deep-merge nested blocks. Here is the entire
`Server/Item/Items/Weapon/Sword/Weapon_Sword_Iron.json` you would copy and
maintain, with the part you actually wanted marked:

```json
{
  "Parent": "Template_Weapon_Sword",
  "TranslationProperties": {
    "Name": "server.items.Weapon_Sword_Iron.name"
  },
  "Model": "Items/Weapons/Sword/Iron.blockymodel",
  "Texture": "Items/Weapons/Sword/Iron_Texture.png",
  "Quality": "Uncommon",
  "Icon": "Icons/ItemsGenerated/Weapon_Sword_Iron.png",
  "ItemLevel": 20,
  "Recipe": { // you only want to tweak fields inside this block
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

`Recipe` is an object. `Recipe.Input` and `Recipe.BenchRequirement` are arrays.

## The patch

`Server/Item/Items/Weapon/Sword/Weapon_Sword_Iron.patch`:

```json
{
  "Recipe": {
    "TimeSeconds": 2.0,
    "Input": [
      { "ItemId": "Ingredient_Bar_Iron", "Quantity": 3 }
    ]
  }
}
```

## After

- `Recipe.TimeSeconds` becomes `2.0`. The `Recipe` object merges, so the scalar
  is overwritten in place.
- `Recipe.Input` is REPLACED with the single new entry. The recipe now costs 3
  iron bars and nothing else. The leather and fabric are gone, because restating
  an array drops everything you did not write.
- `Recipe.KnowledgeRequired` and `Recipe.BenchRequirement` survive untouched. You
  did not name them, so the merge leaves them alone.

```json
{
  "Recipe": {
    "TimeSeconds": 2.0,
    "KnowledgeRequired": false,
    "Input": [
      { "ItemId": "Ingredient_Bar_Iron", "Quantity": 3 }
    ],
    "BenchRequirement": [
      { "Type": "Crafting", "Categories": [ "Weapon_Sword" ], "Id": "Weapon_Bench" }
    ]
  }
}
```

## Add one ingredient, keep the originals

If you only want to bolt on an extra input without rewriting the list, suffix the
key with `+` to append:

```json
{
  "Recipe": {
    "Input+": [
      { "ItemId": "Ingredient_Bar_Iron", "Quantity": 2 }
    ]
  }
}
```

After this, all three stock inputs survive and the extra 2 iron bars are appended
to the end. The recipe now has four input entries.

## Notes

- The core rule is the array rule. Restating `Input` REPLACES the whole
  ingredient list. Use that to fully redesign a recipe. `Input+` APPENDS to it.
  Use that to add one ingredient on top of the originals.
- The same applies to `BenchRequirement`. Restating it swaps the bench; `+`
  adds a second bench requirement.
- Object fields like `TimeSeconds` and `KnowledgeRequired` merge in place, so you
  can change one and leave the rest. Only the keys you name are touched.
- To make the item uncraftable instead, delete the whole `Recipe` with `null`.
  See [Removing-Values](Removing-Values).
- If more than one mod edits the same recipe, `$Priority` decides last-wins. See
  the [syntax reference](../) and the [Pack Developers guide](../Guides/Pack-Developers).
