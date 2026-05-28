---
title: "Give the Player a Stat (Mana)"
order: 3
published: true
draft: false
---

# Give the Player a Stat (Mana)

Hytale ships a Mana stat, but it is zeroed out. Vanilla mana only exists when an item grants it through `StatModifiers` (armor with `"Mana": [...]`). You want every player to have a base mana pool of 50 that regenerates on its own, with no mana-granting armor required.

## Wanted

- Mana starts at 50 and caps at 50 for everyone.
- The existing regeneration (a tick every 0.2s) keeps working.
- Item `StatModifiers` that add Mana still stack on top of the new base.

## Before

Without Patchly, changing one field means your override file has to restate the whole asset, because Hytale does not deep-merge nested blocks. Stat definitions live as individual files in `Server/Entity/Stats/`, and the stock `Server/Entity/Stats/Mana.json` ships zeroed. Here is the entire file you would copy and maintain, with the parts you actually wanted marked:

```json
{
  "InitialValue": 0, // you want this to be 50
  "Min": 0,
  "Max": 0, // ...and this to be 50
  "Shared": false,
  "ResetType": "MaxValue",
  "Regenerating": [
    {
      "Interval": 0.2,
      "Amount": 1,
      "RegenType": "Additive",
      "Conditions": [
        {
          "Id": "Alive"
        },
        {
          "Id": "NoDamageTaken",
          "Delay": 6
        },
        {
          "Id": "Charging",
          "Inverse": true
        }
      ]
    }
  ]
}
```

## The patch

Put the patch at `Server/Entity/Stats/Mana.patch`:

```json
{
  "InitialValue": 50,
  "Max": 50
}
```

## After

Only the two named fields change; everything else survives the deep-merge:

- `InitialValue`: `0` -> `50`. Players spawn with a full pool.
- `Max`: `0` -> `50`. The pool now caps at 50.
- `Regenerating` is untouched. The 0.2s additive tick still runs, so the pool refills.
- `Min`, `Shared`, `ResetType` are untouched.

Item `StatModifiers` that add Mana now stack on top of the 50 base instead of being the only source of mana.

The merged result:

```json
{
  "InitialValue": 50,
  "Min": 0,
  "Max": 50,
  "Shared": false,
  "ResetType": "MaxValue",
  "Regenerating": [
    {
      "Interval": 0.2,
      "Amount": 1,
      "RegenType": "Additive",
      "Conditions": [
        { "Id": "Alive" },
        { "Id": "NoDamageTaken", "Delay": 6 },
        { "Id": "Charging", "Inverse": true }
      ]
    }
  ]
}
```

## Notes

- A mod that adds its own brand-new stat just ships a new file like `Server/Entity/Stats/Magic_Power.json` (as hexcode does for `Magic_Power`, `Volatility`, and `MagicCharges`). There is nothing to merge, so that is a normal asset, not a patch. Patchly is for MODIFYING a stat that another pack (or the base game) already defines, like retuning Mana's `Max` or regen here.
- If several mods retune the same stat, use `$Priority` to decide whose values win. The highest priority applies last on a conflict. See [Removing-Values](Removing-Values) for deletes and the [syntax reference](../) for the full ruleset.
