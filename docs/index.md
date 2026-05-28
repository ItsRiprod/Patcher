---
title: "Introduction"
order: 1
published: true
draft: false
---
# Patchly - The Perfect Pure Patching Plugin

**Patch JSON assets instead of rewriting them. Zero dependencies.**

Patchly lets one pack reach into another pack's asset and change only the fields it cares about, leaving everything else untouched. You drop a `.patch` file next to the asset you want to change, and Patchly deep-merges it onto the resolved original.

## Quickstart (under a minute)

1. Put `Patchly-X.Y.Z.jar` in your server's `mods/` folder. No config.
2. In your pack, create a file at the same path as the asset you want to change, swapping `.json` for `.patch`. To change `Server/Item/Items/Armor/Iron/Armor_Iron_Head.json`, make `Server/Item/Items/Armor/Iron/Armor_Iron_Head.patch`.
3. Put only the fields you want to change inside it:

```json
{
  "Armor": {
    "StatModifiers": {
      "Mana": [{ "Amount": 126, "CalculationType": "Additive" }]
    }
  }
}
```

4. Boot the server. Patchly merges your patch onto the real iron helmet, adds `Mana` next to the existing `Health`, and leaves everything else alone.

That is the whole 80% case.

## For setting up and getting started...

| You are... | You ship... | Go to |
|---|---|---|
| An asset-only pack (JSON, no Java) | `.patch` files + a manifest dependency on Patchly | [For Pack Developers](Guides/Pack-Developers) |
| A Java plugin that wants patching built in | Patchly shaded into your jar + a few `PatchManager` calls | [For Mod Developers](Guides/Mod-Developers) |

Everything below is reference. Skip ahead to your guide and come back when you hit something.

---

# Patch syntax

A `.patch` is plain JSON. Every key you write is merged onto the matching key in the resolved base asset. Here is every rule at a glance, then each one explained.

| Feature | Write this | Result |
|---|---|---|
| Deep merge (objects) | `{ "A": { "B": 1 } }` | Only leaf `B` changes; sibling keys survive. |
| Replace array (default) | `"Categories": [...]` | Discards the parent's array, uses yours. |
| Append to array | `"Categories+": [...]` | Keeps the parent's entries, adds yours at the end. |
| Delete a key | `"DamageResistance": null` | Removes that key from the merged asset. |
| Gate on packs | `"$Requires": "Group:Name"` or `[...]` | Patch applies only if all named packs are installed; otherwise skipped with a log line. |
| Win on conflicts | `"$Priority": 100` | Integer, default 0. Higher applies last and wins on conflicting fields. |
| Free notes | `"$Comment": "..."` | Any top-level `$`-key is metadata, stripped before merge. |

## Deep merge (objects)

Objects merge recursively. Only the leaf values you specify change:

```json
{ "Armor": { "StatModifiers": { "Mana": [{ "Amount": 126, "CalculationType": "Additive" }] } } }
```

`Mana` is added; sibling keys (`Health`, `DamageResistance`, `ArmorSlot`) survive.

## Replace vs append (arrays)

Arrays **replace** by default. To **append** to the existing array, suffix the key with `+`:

```json
{
  "BlockType": {
    "Bench": {
      "Categories+": [
        { "Id": "Arcane_Hexcode", "Icon": "...", "Name": "..." }
      ]
    }
  }
}
```

The parent's existing `Categories` entries stay; this one is added to the end. (`"Categories": [...]` with no `+` would discard the parent's entries.)

## Removing a key

A `null` value **deletes** that key from the merged asset:

```json
{ "Armor": { "DamageResistance": null } }
```

The merged `Armor_Iron_Head` will have no `DamageResistance` block at all.

## `$Requires` - only apply if certain packs are installed

A string, or an array of strings (all must be present). Each is matched against a pack's `Group:Name`:

```json
{ "$Requires": "Riprod:Hexcode", "Armor": { } }
```

```json
{ "$Requires": ["Riprod:Hexcode", "Author:SomeOtherPack"], "Armor": { } }
```

If any required pack is missing, the patch is **skipped** with a log line, so you can safely ship cross-mod patches that only activate when both mods are present. There is no exclude or negative form today.

## `$Priority` - decide the winner on conflicts

An integer, default `0`. Patches apply in ascending order, so **higher priority applies last and wins** on directly-conflicting fields. Tie-break is pack load order.

```json
{
  "$Priority": 100,
  "Armor": { "StatModifiers": { "Mana": [{ "Amount": 9999, "CalculationType": "Additive" }] } }
}
```

Two mods patching the same field both apply, but the higher `$Priority` writes last. Lower-priority `+` appends still stack onto fields the higher patch did not touch.

## Reserved `$` keys

Any **top-level** key starting with `$` is metadata. It is stripped before merge and never written to the asset. `$Requires` and `$Priority` are the two with meaning today; `$Comment` (or any other `$Foo`) is free for your own notes:

```json
{ "$Comment": "buff iron helm mana for the mage build", "Armor": { } }
```

---

# Why this works (for the curious)

You do not need this section to use Patchly. It explains the problem Patchly solves, for anyone who wants to know why a plain JSON override is not enough.

Take a real asset, `Armor_Iron_Head.json`. The iron helmet ships with this `Armor` block:

```json
{
  "Armor": {
    "ArmorSlot": "Head",
    "DamageResistance": {
      "Physical":   [{ "Amount": 0.05, "CalculationType": "Multiplicative" }],
      "Projectile": [{ "Amount": 0.05, "CalculationType": "Multiplicative" }]
    },
    "StatModifiers": {
      "Health": [{ "Amount": 9, "CalculationType": "Additive" }]
    }
  }
}
```

You only want to add a Mana bonus. The obvious move is `Parent: "super"` and a restated `Armor` block:

```json
{
  "Parent": "super",
  "Armor": {
    "StatModifiers": {
      "Mana": [{ "Amount": 126, "CalculationType": "Additive" }]
    }
  }
}
```

This looks right. It is not. Hytale's `Parent` inheritance merges only at the outer asset level. Nested codec fields like `Armor` use `.append(...)`, not `.appendInherited(...)`, so your `Armor` block replaces the parent's wholesale:

| Field | What happens |
|---|---|
| `Health: +9` | Gone. Your `StatModifiers` replaced the parent's. |
| `DamageResistance` | Null. You did not restate it. |
| `ArmorSlot` | Falls back to codec default `"Head"`. Fine here, but on a chestpiece this would quietly turn it into a helmet. |

No error. No warning. Just a helmet that lost its armor.

Patchly fixes this by reading the **fully resolved** `Armor_Iron_Head.json`, deep-merging your patch field by field, and writing the result into a synthetic override pack that wins. `Mana` lands inside the existing `StatModifiers` next to `Health`; `DamageResistance` and `ArmorSlot` are never touched. You write the diff and Patchly keeps everything else.

## Where things land

At load, Patchly walks every registered pack, resolves the latest version of each target asset, deep-merges all matching `.patch` files onto it, and writes the result into a synthetic override pack that takes precedence.

| Question | Answer |
|---|---|
| Where do I put the `.patch`? | Same path as the target, `.json` swapped for `.patch`. To patch `Armor_Iron_Head.json`, ship `Server/Item/Items/Armor/Iron/Armor_Iron_Head.patch`. |
| Which packs can be patched? | Every registered pack: folder, `.zip`, and `.jar`. |
| Does it hot-reload? | Folder packs re-merge live on `.patch` edit. Zip/jar packs apply once at load. |
| Where does the merged output go? | `mods/<group>_<name>_PatcherOverrides/`, wiped on every cold start. |
| What about that "Skipping pack ... missing or invalid manifest.json" boot line? | Benign. The synthetic pack is registered programmatically, not scanned from disk. |

---

# Next steps

- **[For Pack Developers](Guides/Pack-Developers)** - ship `.patch` files in an asset-only pack and require Patchly as a dependency.
- **[For Mod Developers](Guides/Mod-Developers)** - bundle Patchly directly into your Java plugin with Gradle Shadow.
