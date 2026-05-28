---
title: "Change Block or Armor Quality"
order: 5
published: true
draft: false
---

# Change Block or Armor Quality

Every item asset carries a top-level `"Quality"` string that sets its rarity tier
and name color. Bumping it is the simplest kind of patch: a single scalar replace.

## Wanted

Promote the iron sword and the iron helmet from `Uncommon` to `Epic` so they show
the Epic rarity color, without touching their model, recipe, stats, or interactions.

## Before

Without Patchly, changing one field means your override file has to restate the
whole asset, because Hytale does not deep-merge nested blocks. To flip a single
`"Quality"` string from `Uncommon` to `Epic`, you would copy and maintain both of
these entire files verbatim, just so the one marked line lands. Every other field
below is dead weight you now own forever, and a copy that drifts the day the base
game retunes the sword's damage or the helmet's recipe.

**`Server/Item/Items/Weapon/Sword/Weapon_Sword_Iron.json`**

```json
{
  "Parent": "Template_Weapon_Sword",
  "TranslationProperties": {
    "Name": "server.items.Weapon_Sword_Iron.name"
  },
  "Model": "Items/Weapons/Sword/Iron.blockymodel",
  "Texture": "Items/Weapons/Sword/Iron_Texture.png",
  "Quality": "Uncommon", // the ONLY thing you wanted to change (-> "Epic")
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

**`Server/Item/Items/Armor/Iron/Armor_Iron_Head.json`**

```json
{
  "TranslationProperties": {
    "Name": "server.items.Armor_Iron_Head.name"
  },
  "Quality": "Uncommon", // the ONLY thing you wanted to change (-> "Epic")
  "ItemLevel": 20,
  "PlayerAnimationsId": "Block",
  "ItemSoundSetId": "ISS_Armor_Heavy",
  "Categories": [
    "Items.Armors"
  ],
  "Icon": "Icons/ItemsGenerated/Armor_Iron_Head.png",
  "IconProperties": {
    "Rotation": [
      22.5,
      45,
      22.5
    ],
    "Scale": 0.5,
    "Translation": [
      0,
      -15
    ]
  },
  "Model": "Items/Armors/Iron/Head.blockymodel",
  "Texture": "Items/Armors/Iron/Head_Texture.png",
  "Recipe": {
    "Input": [
      {
        "ItemId": "Ingredient_Bar_Iron",
        "Quantity": 9
      },
      {
        "ItemId": "Ingredient_Leather_Light",
        "Quantity": 4
      },
      {
        "ItemId": "Ingredient_Fabric_Scrap_Linen",
        "Quantity": 3
      }
    ],
    "BenchRequirement": [
      {
        "Id": "Armor_Bench",
        "Type": "Crafting",
        "Categories": [
          "Armor_Head"
        ]
      }
    ],
    "KnowledgeRequired": false,
    "TimeSeconds": 3
  },
  "Interactions": {
    "Primary": {
      "Interactions": [
        {
          "Type": "EquipItem"
        }
      ]
    },
    "Secondary": {
      "Interactions": [
        {
          "Type": "EquipItem"
        }
      ]
    }
  },
  "Armor": {
    "ArmorSlot": "Head",
    "BaseDamageResistance": 0,
    "CosmeticsToHide": [
      "EarAccessory",
      "Ear",
      "Haircut",
      "HeadAccessory"
    ],
    "DamageResistance": {
      "Physical": [
        {
          "Amount": 0.05,
          "CalculationType": "Multiplicative"
        }
      ],
      "Projectile": [
        {
          "Amount": 0.05,
          "CalculationType": "Multiplicative"
        }
      ]
    },
    "StatModifiers": {
      "Health": [
        {
          "Amount": 9,
          "CalculationType": "Additive"
        }
      ]
    }
  },
  "MaxDurability": 100,
  "DurabilityLossOnHit": 0.5,
  "Tags": {
    "Type": [
      "Armor"
    ],
    "Family": [
      "Iron"
    ]
  }
}
```

## The patch

Two patches, one per target. Each path mirrors the target with `.json` swapped for
`.patch`.

`Server/Item/Items/Weapon/Sword/Weapon_Sword_Iron.patch`:

```json
{
  "Quality": "Epic"
}
```

`Server/Item/Items/Armor/Iron/Armor_Iron_Head.patch`:

```json
{
  "Quality": "Epic"
}
```

## After

- `Quality` is now `"Epic"` on both items.
- Everything else on each item is untouched: model, recipe, stats, and
  interactions all survive, because the merge only sees the one key you wrote.

## Notes

- Quality is a plain string. Use one of the values that stock assets use:
  `Common`, `Uncommon`, `Rare`, `Epic`, `Legendary`, `Junk`, `Tool`,
  `Template`, `Technical`, `Developer`, `Debug`.
- Placeable blocks are item assets too and carry `"Quality"` at the same top
  level, so the exact same pattern works on a block item.
- Because this is a scalar replace, there is no `+` (array append) or `null`
  (delete) involved. See [Removing-Values](Removing-Values) for those.
- If two packs both set `Quality` on the same item, the one with the higher
  `$Priority` wins. See the [syntax reference](../) and the
  [Pack Developers guide](../Guides/Pack-Developers).
