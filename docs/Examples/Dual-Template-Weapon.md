---
title: "Weapon: Local AND Cross-Mod"
order: 1
published: true
draft: false
---
# Weapon: Local AND Cross-Mod

You ship an iron sword. If a player also runs the Hexcode mod, you want that same sword to become a castable hexstaff. With one item file, no fork.

## Wanted

- One item: `Weapon_Sword_Iron`.
- When Hexcode is NOT installed: it stays an ordinary iron sword (swing, guard, signature).
- When `Riprod:Hexcode` IS installed: it becomes a hexstaff (right-click opens casting, ability keys, etc.).
- No second item id, no duplicate file. The base asset never changes.

## Before

Without Patchly, making this item dual-purpose means your override file has to restate the whole asset, because Hytale does not deep-merge nested blocks. Here is the entire `Server/Item/Items/Weapon/Sword/Weapon_Sword_Iron.json` you would copy and maintain, with the two parts you actually wanted to change marked:

```json
{
  "Parent": "Template_Weapon_Sword", // you want this to become "Template_HexStaff" when Hexcode is present
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
  "InteractionVars": { // ...and all of these sword interactions removed, so the staff template fills them in
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

`Template_Weapon_Sword` supplies the actual interaction roots:

```json
{
  "Interactions": {
    "Primary": "Root_Weapon_Sword_Primary",
    "Secondary": "Root_Weapon_Sword_Secondary_Guard",
    "Ability1": "Root_Weapon_Sword_Signature_Vortexstrike"
  }
}
```

Hexcode ships its own template, `Template_HexStaff`:

```json
{
  "Interactions": {
    "Primary": "Hexstaff_Primary",
    "Secondary": "Hexstaff_Secondary",
    "Ability1": "Hexstaff_Ability1",
    "Ability3": "Hexstaff_Ability3"
  }
}
```

## The patch

Put it next to the item, `.json` swapped for `.patch`:

```
Server/Item/Items/Weapon/Sword/Weapon_Sword_Iron.patch
```

```json
{
  "$Requires": "Riprod:Hexcode",
  "Parent": "Template_HexStaff",
  "InteractionVars": null
}
```

## After

Two outcomes, decided entirely by whether Hexcode is loaded.

Hexcode absent:

- `$Requires` is not satisfied, so Patchly skips the whole patch.
- The item is untouched: `Parent` stays `Template_Weapon_Sword`, `InteractionVars` keeps its swing override. It is a normal sword.

Hexcode present:

- The patch applies. `Parent` becomes `Template_HexStaff`, so the item now inherits `Primary`/`Secondary`/`Ability1`/`Ability3` from the hexstaff template.
- `"InteractionVars": null` deletes the sword's combat override. Without that delete, the leftover `Swing_Left_Damage` block would shadow the staff behavior and you'd get a half-sword, half-staff item.
- `"Quality": "Uncommon"` and every other field on the base item survive, because the patch only touches the two keys it names.

Net: the same file is a sword on one server and a hexstaff on another.

## Notes

- `$Requires` is the whole trick. It is a guard, not a merge field: if the named pack is missing the patch never runs, so the base asset is your default and the patch is the cross-mod upgrade. Use `Group:Name` form, here `Riprod:Hexcode`.
- Setting a key to `null` deletes it. That is how you clear an inherited or local block so a new `Parent` can fill it in cleanly. See [Removing-Values](Removing-Values) for the delete rule on its own.
- This is the heavy hammer: a whole-template swap that changes what the item fundamentally is. If you only want to retarget one slot (say, make `Secondary` cast a spell while the rest stays a sword), patch a single interaction instead. See [Replace-Interaction](Replace-Interaction) for that surgical approach.
- Patchly re-merges on every pack register, so it does not matter whether Hexcode loads before or after your pack. The `$Requires` check is re-evaluated each time.
