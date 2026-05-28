---
title: "For Pack Developers"
order: 1
published: true
draft: false
---
# For Pack Developers

This is for **asset-only packs**: no Java, just JSON. You want to change assets that ship in another mod (or in the base game) without copying and maintaining the whole file. Patchly does the merging; your pack just ships `.patch` files and declares Patchly as a dependency.

## The four steps

| Step | Do this |
|---|---|
| 1 | Put `Patchly-X.Y.Z.jar` in the server's `mods/` folder. |
| 2 | Add a `.patch` at the asset's path (`.json` swapped for `.patch`). |
| 3 | Add `"Riprod:Patchly": "*"` to `Dependencies` in your `manifest.json`. |
| 4 | (Optional) Gate individual patches on other mods with `$Requires`. |

Steps 1 through 3 are everything you need. Stop reading once your patch works; step 4 and the reference below are there when you hit something.

## 1. Install Patchly

Drop `Patchly-X.Y.Z.jar` into the server's `mods/` folder. There is nothing to configure. On boot it scans every pack, merges any `.patch` files it finds, and registers the result. One Patchly jar serves every pack on the server.

## 2. Add a `.patch` file to your pack

Mirror the path of the asset you are patching, swapping `.json` for `.patch`. To patch the iron helmet:

```
MyPack/
  manifest.json
  Server/
    Item/
      Items/
        Armor/
          Iron/
            Armor_Iron_Head.patch
```

The `.patch` contains only the fields you want to change:

```json
{
  "Armor": {
    "StatModifiers": {
      "Mana": [{ "Amount": 126, "CalculationType": "Additive" }]
    }
  }
}
```

The path is the only link to the source. Patchly resolves the target from whichever pack actually owns it; you never reference the source pack by name in the file. For the full merge rules (`+` append, `null` removal, `$Requires`, `$Priority`), see the [syntax reference](../).

## 3. Require Patchly as a dependency

If your pack ships `.patch` files but Patchly is not installed, nothing merges and your changes silently do nothing. Declaring the dependency makes the server refuse to load your pack without Patchly, and guarantees Patchly loads first.

Dependencies are a map of `"Group:Name": "<version>"`. Patchly's id is **`Riprod:Patchly`**. Use `"*"` to accept any version:

```json
{
  "Group": "Author",
  "Name": "MyPack",
  "Version": "1.0.0",
  "Description": "Buffs iron armor with mana",
  "Authors": [{ "Name": "Author" }],
  "ServerVersion": "^0.5.0",
  "Dependencies": {
    "Riprod:Patchly": "*"
  },
  "OptionalDependencies": {},
  "IncludesAssetPack": true,
  "Main": null
}
```

| Manifest key | Meaning |
|---|---|
| `Dependencies` | Hard requirement. The pack will not load if `Riprod:Patchly` is absent. Use this when your pack is useless without patching. |
| `OptionalDependencies` | Soft. Patchly loads first if present, but your pack still loads without it. Use this if your patches are a nice-to-have on assets that work on their own. |
| `IncludesAssetPack: true` | Marks this as an asset-only pack. |
| `Main: null` | No Java entrypoint. |

That is the complete path. The two sections below are for specific situations.

## Cross-mod patches with `$Requires`

A manifest dependency is all-or-nothing for the whole pack. To apply **one specific patch** only when some other mod is present, use `$Requires` inside that `.patch` file instead:

```json
{
  "$Requires": "Riprod:Hexcode",
  "Armor": {
    "StatModifiers": {
      "Mana": [{ "Amount": 126, "CalculationType": "Additive" }]
    }
  }
}
```

This patch is skipped (with a log line) unless `Riprod:Hexcode` is loaded. A single pack can carry optional compatibility patches for several mods, each activating only when its target is present. The pack itself still only hard-depends on `Riprod:Patchly`.

## Resolving conflicts with another pack

If two packs patch the same field, both apply in load order and the last one wins. To guarantee yours wins regardless of load order, bump `$Priority`:

```json
{ "$Priority": 100, "Armor": { "StatModifiers": { "Mana": [{ "Amount": 9999, "CalculationType": "Additive" }] } } }
```

Higher `$Priority` applies last and wins on conflicting fields. Lower-priority `+` appends from other packs still stack onto fields you did not touch.

## Good to know

| Topic | Detail |
|---|---|
| Hot-reload | Ship your pack as a folder (not zipped) and editing a `.patch` re-merges live, no restart. Zip/jar packs apply once at load. |
| No source edits | You never modify the target mod's files. Updating that mod will not clobber your changes; Patchly re-merges against the new version on next boot. |
| Output location | Merged assets go to `mods/<group>_<name>_PatcherOverrides/`, wiped on every cold start. Ignore this folder; it is regenerated. |
| Benign boot line | `[AssetModule] Skipping pack at ..._PatcherOverrides: missing or invalid manifest.json` is expected. The synthetic pack is registered programmatically, not scanned from disk. |
