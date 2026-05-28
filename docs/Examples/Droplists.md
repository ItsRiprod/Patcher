---
title: "Add to a Droplist or Wire a New One"
order: 8
published: true
draft: false
---
# Add to a Droplist or Wire a New One

Droplists live in `Server/Drops/`. A block points at one by id, and breaking the block rolls that list. You want a block to drop something extra, or to drop from a list you authored. There are two distinct tasks here, and only one of them is a patch each time.

## Wanted

- Case A: gravel keeps its stock drop but also has a chance to roll a `PortalKey_Maze_Var1`, but only when `icarus:legacy` is present.
- Case B: a block you do not own should drop from a brand-new droplist you wrote.

## Before

Without Patchly, changing one entry means your override file has to restate the whole asset, because Hytale does not deep-merge nested blocks. Here are the entire real files you would copy and maintain the old way, with the parts you actually wanted marked.

For Case A, the stock gravel droplist at `Server/Drops/Soil_Gravel.json`:

```json
{
  "Container": {
    "Type": "Multiple",
    "Containers": [ // you want to ADD one more drop here, not replace what is here
      {
        "Type": "Single",
        "Item": {
          "ItemId": "Rubble_Stone",
          "QuantityMax": 2,
          "QuantityMin": 1
        }
      }
    ]
  }
}
```

For Case B, a real block that references a droplist by id, the entire `Server/Item/Items/Soil/Gravel/Soil_Gravel_Sand.json`:

```json
{
  "Parent": "Soil_Gravel",
  "TranslationProperties": {
    "Name": "server.items.Soil_Gravel_Sand.name"
  },
  "Icon": "Icons/ItemsGenerated/Soil_Gravel_Sand.png",
  "Categories": [
    "Blocks.Rocks"
  ],
  "Set": "Rock_Sandstone",
  "BlockType": {
    "Textures": [
      {
        "All": "BlockTextures/Soil_Gravel_Sand.png",
        "Weight": 1
      }
    ],
    "Gathering": {
      "Breaking": {
        "GatherType": "Soils",
        "DropList": "Rubble_Sandstone" // you want to point this at your own list
      }
    },
    "ParticleColor": "#c69f4f",
    "TransitionTexture": "BlockTextures/Transition_Gravel_Sand.png"
  }
}
```

A block references a droplist under its `BlockType` either by id (as `Soil_Gravel_Sand` does above with `"DropList": "Rubble_Sandstone"`) or with an inline `"DropList": { "Container": {...} }`. The id string maps to the matching file in `Server/Drops/`.

## The patch

### Case A - add a drop to an existing list

Patch at `Server/Drops/Soil_Gravel.patch`:

```json
{
  "$Requires": "icarus:legacy",
  "Container": {
    "Containers+": [
      {
        "Type": "Single",
        "Item": { "ItemId": "PortalKey_Maze_Var1", "QuantityMax": 1, "QuantityMin": 0 }
      }
    ]
  }
}
```

`PortalKey_Maze_Var1` is a real Icarus item, so `$Requires: "icarus:legacy"` gates the whole patch: the bonus drop only appears when the Icarus pack is installed. The `+` appends; plain `"Containers"` would wipe the stock `Rubble_Stone` drop. See [Replace vs append](../#replace-vs-append-arrays).

### Case B - point a block at a brand-new droplist

First create the new list. This is a normal asset, not a patch (there is nothing to merge with). Write `Server/Drops/Custom_Gravel_Drops.json` with its own `Container`. Then patch the block to use it. The block file is `Server/Item/Items/Soil/Gravel/Soil_Gravel_Sand.json`, so the patch is `Server/Item/Items/Soil/Gravel/Soil_Gravel_Sand.patch`:

```json
{ "BlockType": { "Gathering": { "Breaking": { "DropList": "Custom_Gravel_Drops" } } } }
```

## After

- Case A: gravel still drops `Rubble_Stone` (1-2) AND now also rolls `PortalKey_Maze_Var1`, but only when `icarus:legacy` is present. The original entry survives because of the `+`.
- Case B: breaking `Soil_Gravel_Sand` now rolls `Custom_Gravel_Drops` instead of its old `Rubble_Sandstone` list.

## Notes

- Appending only works when the container is `"Type": "Multiple"` (it has a `Containers` array). `Soil_Gravel` is. If a droplist is `"Type": "Single"` (one `Item`, no array), you cannot append. Replace the whole `Container` with a `Multiple` that lists the original `Item` plus yours.
- Creating a new droplist file is not a patch. Use Patchly to append to an existing list, or to repoint an existing block's `DropList`.
- If two packs append to the same list, both appends stack. Order follows `$Priority` then load order. See [`$Priority`](../#priority-decide-the-winner-on-conflicts).
- Reach for [`$Requires`](../#requires-only-apply-if-certain-packs-are-installed) on the block repoint in Case B too so it only fires when the pack holding `Custom_Gravel_Drops` is present.
