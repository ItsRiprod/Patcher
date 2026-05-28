---
title: "Remove a Field or Recipe"
order: 7
published: true
draft: false
---

# Remove a Field or Recipe

A `null` value in a patch deletes the key it is set on from the merged asset.
This is how you strip a field you do not want, or make an item uncraftable by
removing its recipe. The two cases below are the common ones.

## Case A: remove a nested field

Drop the helmet's damage resistance entirely, while leaving everything else
intact.

### Before

Without Patchly, dropping one nested block means your override file has to
restate the whole asset, because Hytale does not deep-merge nested blocks. Here
is the entire `Server/Item/Items/Armor/Iron/Armor_Iron_Head.json` you would copy
and maintain, with the part you actually wanted gone marked:

```json
{
  "TranslationProperties": {
    "Name": "server.items.Armor_Iron_Head.name"
  },
  "Quality": "Uncommon",
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
    "DamageResistance": { // you want this whole block gone
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

### The patch

`Server/Item/Items/Armor/Iron/Armor_Iron_Head.patch`:

```json
{
  "Armor": {
    "DamageResistance": null
  }
}
```

### After

- `Armor.DamageResistance` is gone. The merged helmet has no resistance block at
  all.
- `Armor.ArmorSlot`, `Armor.StatModifiers.Health`, and every other field
  survive, because `null` only deletes the one key it sits on.

## Case B: make an item uncraftable

Items carry a top-level `"Recipe"` object. Delete it and the item can no longer
be crafted.

### The patch

`Server/Item/Items/Armor/Iron/Armor_Iron_Head.patch`:

```json
{
  "Recipe": null
}
```

### After

- The `Recipe` key is gone, so there is no crafting entry for the helmet.
- Stats, model, and the rest of the item are untouched.

## Notes

- `null` deletes the WHOLE key it is set on. Set it as deep as the thing you
  want gone. Deleting `Armor.DamageResistance` removes only resistance; writing
  `"Armor": null` would wipe the entire armor block.
- You cannot remove a single array entry by index. Arrays replace by default, so
  to drop one element you restate the array with only the survivors. For
  example, if `StatModifiers.Health` had two entries and you wanted to keep just
  the first:

  ```json
  {
    "Armor": {
      "StatModifiers": {
        "Health": [
          { "Amount": 9, "CalculationType": "Additive" }
        ]
      }
    }
  }
  ```

  The written array fully replaces the original two-entry array. The `+` append
  suffix is the opposite tool; see the [syntax reference](../).
- Hytale's own assets use `"$Comment"`. Any top-level `$`-key is stripped before
  merge and never written, so it is not a way to remove real fields. Only `null`
  deletes.
- If another pack also sets the same key, `$Priority` decides last-wins. See the
  [syntax reference](../) and the [Pack Developers guide](../Guides/Pack-Developers).
