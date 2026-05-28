---
title: "Repoint an Interaction at Another Mod"
order: 4
published: true
draft: false
---
# Repoint an Interaction at Another Mod

You ship a normal sword. You want its right-click to open Hexcode's spell casting instead of the vanilla guard, but only when Hexcode is installed, and you want everything else about the sword left alone.

## Wanted

- The sword stays a sword: same identity, same `Parent`, same `Primary` swing, same `Ability1` signature.
- Right-click (`Secondary`) opens Hexcode's casting interaction instead of the guard.
- Only happens when `Riprod:Hexcode` is installed.
- One slot changes. Nothing else.

## Before

Without Patchly, repointing one interaction slot means your override file has to restate the whole sword, because Hytale does not deep-merge nested blocks. Here is the entire `Server/Item/Items/Weapon/Sword/Template_Weapon_Sword.json` you would copy and maintain, with the one line you actually wanted to change marked:

```json
{
  "TranslationProperties": {
    "Name": "server.items.Template_Weapon_Sword.name"
  },
  "DroppedItemAnimation": "Items/Animations/Dropped/Dropped_Diagonal_Left.blockyanim",
  "Model": "Items/Weapons/Sword/Iron.blockymodel",
  "Texture": "Items/Weapons/Sword/Iron_Texture.png",
  "Icon": "Icons/ItemsGenerated/Weapon_Sword_Iron.png",
  "PlayerAnimationsId": "Sword",
  "Reticle": "DefaultMelee",
  "Quality": "Template",
  "ItemLevel": 15,
  "Categories": [
    "Items.Weapons"
  ],
  "IconProperties": {
    "Scale": 0.35,
    "Translation": [
      -22,
      -22
    ],
    "Rotation": [
      45,
      90,
      0
    ]
  },
  "Utility": {
    "Compatible": true
  },
  "Interactions": {
    "Primary": "Root_Weapon_Sword_Primary",
    "Secondary": "Root_Weapon_Sword_Secondary_Guard", // you only want to repoint this ONE slot
    "Ability1": "Root_Weapon_Sword_Signature_Vortexstrike"
  },
  "Tags": {
    "Type": [
      "Weapon"
    ],
    "Family": [
      "Sword"
    ]
  },
  "Weapon": {
    "EntityStatsToClear": [
      "SignatureEnergy"
    ],
    "StatModifiers": {
      "SignatureEnergy": [
        {
          "Amount": 20,
          "CalculationType": "Additive"
        }
      ]
    }
  },
  "ItemAppearanceConditions": {
    "SignatureEnergy": [
      {
        "Condition": [
          100,
          100
        ],
        "ConditionValueType": "Percent",
        "Particles": [
          {
            "SystemId": "Sword_Signature_Ready",
            "TargetNodeName": "Handle",
            "PositionOffset": {
              "X": 0.8
            },
            "TargetEntityPart": "PrimaryItem"
          },
          {
            "SystemId": "Sword_Signature_Status_Spawn",
            "TargetNodeName": "Handle",
            "PositionOffset": {
              "X": 0.5
            },
            "TargetEntityPart": "PrimaryItem"
          },
          {
            "SystemId": "Sword_Signature_Status",
            "TargetNodeName": "Handle",
            "PositionOffset": {
              "X": 0.55
            },
            "TargetEntityPart": "PrimaryItem",
            "Scale": 1.3
          }
        ],
        "FirstPersonParticles": [
          {
            "SystemId": "Sword_Signature_Ready",
            "TargetNodeName": "Handle",
            "PositionOffset": {
              "X": 0.8
            },
            "TargetEntityPart": "PrimaryItem"
          },
          {
            "SystemId": "Sword_Signature_Status_Spawn",
            "TargetNodeName": "Handle",
            "TargetEntityPart": "PrimaryItem",
            "PositionOffset": {
              "Y": -100
            }
          },
          {
            "SystemId": "Sword_Signature_Status",
            "TargetNodeName": "Handle",
            "PositionOffset": {
              "X": 0.5
            },
            "TargetEntityPart": "PrimaryItem"
          }
        ],
        "ModelVFXId": "Sword_Signature_Status"
      }
    ]
  },
  "ItemSoundSetId": "ISS_Weapons_Blade_Large",
  "MaxDurability": 80,
  "DurabilityLossOnHit": 0.21
}
```

`Interactions` is an object keyed by slot. Each value is the id of a root interaction asset. Hexcode ships its own roots, including `Hexstaff_Secondary`, the right-click that opens hex casting. To change just that one slot the old way, you had to ship and keep every other field above in sync forever.

## The patch

Put it next to the target, `.json` swapped for `.patch`:

```
Server/Item/Items/Weapon/Sword/Template_Weapon_Sword.patch
```

```json
{
  "$Requires": "Riprod:Hexcode",
  "Interactions": {
    "Secondary": "Hexstaff_Secondary"
  }
}
```

## After

Because `Interactions` is an object, the merge recurses into it and only the named key changes:

```json
{
  "Interactions": {
    "Primary": "Root_Weapon_Sword_Primary",
    "Secondary": "Hexstaff_Secondary",
    "Ability1": "Root_Weapon_Sword_Signature_Vortexstrike"
  }
}
```

- `Secondary` now resolves to Hexcode's `Hexstaff_Secondary`. Right-click opens casting.
- `Primary` stays `Root_Weapon_Sword_Primary`. The swing is unchanged.
- `Ability1` stays `Root_Weapon_Sword_Signature_Vortexstrike`. The signature is unchanged.

When Hexcode is absent, `$Requires` is not satisfied, Patchly skips the patch, and the sword keeps its guard.

## Notes

- `$Requires: "Riprod:Hexcode"` is not optional here. If you point a slot at an id that no installed pack provides, that slot breaks. Gating on the pack that ships the id guarantees the target exists before you reference it.
- This is the surgical opposite of the whole-template reparent in [Dual-Template-Weapon](Dual-Template-Weapon). There you swap `Parent` and null the local interactions so the new template owns the whole behavior set. Here you keep the item exactly as it is and retarget a single slot.
- Object keys merge; they do not replace. That is why naming only `Secondary` leaves `Primary` and `Ability1` intact. See [Removing-Values](Removing-Values) for how `null` would instead delete a slot.
