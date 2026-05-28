---
title: "Particles from a Later-Loading Pack"
order: 6
published: true
draft: false
---
# Particles from a Later-Loading Pack

You want your weapon to show the `I_Fire_Blue` particle, but the particle system that defines it ships in a separate pack that loads after yours. Done naively, your item asset resolves before the particle exists.

## Wanted

- Your item displays the `I_Fire_Blue` particle.
- The particle system itself lives in a different pack (`icarus:legacy`) that loads after your item asset, defined at `Server/Particles/.../I_Fire_Blue.particlesystem`.
- The stock particles already on the item stay intact.

The key fact: particle systems are `.particlesystem` files and are NOT patchable. Patchly only patches `.json`. What you patch is the `.json` item asset that REFERENCES a particle by its `SystemId` string. The string is resolved against the registered particle systems at merge time.

## Before

Without Patchly, adding one particle reference means your override file has to restate the whole asset, because Hytale does not deep-merge nested blocks. Here is the entire `Server/Item/Items/Weapon/Sword/Template_Weapon_Sword.json` you would copy and maintain, with the line you actually wanted to extend marked:

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
    "Secondary": "Root_Weapon_Sword_Secondary_Guard",
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
        "Particles": [ // you want to ADD one particle entry to this array
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

`SystemId` is a string id. A stock system it names lives at `Server/Particles/.../Sword_Signature_Ready.particlesystem`; the one you want to add, `I_Fire_Blue`, lives at `Server/Particles/.../I_Fire_Blue.particlesystem` inside the `icarus:legacy` pack.

## The patch

Goes at the item's path with `.json` swapped for `.patch`. Use `Particles+` to APPEND your reference into the existing condition's array instead of replacing the stock particles. `$Requires` keeps the reference out until `icarus:legacy` is actually present.

```json
{
  "$Requires": "icarus:legacy",
  "ItemAppearanceConditions": {
    "SignatureEnergy": [
      {
        "Particles+": [
          {
            "SystemId": "I_Fire_Blue",
            "TargetNodeName": "Handle",
            "TargetEntityPart": "PrimaryItem"
          }
        ]
      }
    ]
  }
}
```

Adjust the condition key (`SignatureEnergy` here) and the entry shape to match the real asset you are patching. The point is the `+` suffix: it appends a reference without wiping what is already there.

## After

- The `SignatureEnergy` condition's `Particles` array now holds both the stock `Sword_Signature_Ready` entry AND your `I_Fire_Blue` entry.
- The stock particle survives because `Particles+` appends rather than replaces.
- The merged item is written after `icarus:legacy` registers, so by the time the asset resolves, the `I_Fire_Blue` system already exists and the `SystemId` string resolves.

Why the load order works out: Patchly re-runs its merge on every `AssetPackRegisterEvent`. When `icarus:legacy` registers, the merge re-runs and the merged item is (re)written with `I_Fire_Blue` present. `$Requires: "icarus:legacy"` means the reference is skipped on any earlier pass when `icarus:legacy` is not yet loaded, so you never write a dangling `SystemId`.

## Notes

- You CANNOT patch the `.particlesystem` file itself (yet). It is not JSON. If the particle system does not exist in any pack, ship its `.particlesystem` as a NORMAL full asset file in your pack. Patchly only wires the JSON reference to it.
- The same rule applies to models (`.blockymodel`), textures (`.png`), and lang (`.lang`): patch the JSON that REFERENCES the binary asset, never the binary asset. Ship those as normal full files.
- Use `Particles+`, not `Particles`, unless you genuinely intend to drop the stock particles. A bare `Particles` array REPLACES the whole array. See the [syntax reference](../) for the append rules and [Removing Values](Removing-Values) for deletion.
- Use `$Requires` whenever your patch names an id that only exists once another pack is loaded. It guards against writing references the engine cannot resolve yet. Full meta-key reference is in the [syntax reference](../).
