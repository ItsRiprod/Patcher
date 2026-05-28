---
title: "Examples"
order: 1
published: true
draft: false
---
# Examples

If you have not read the [syntax reference](../) yet, skim it first: deep-merge, `+` append, `null` delete, `$Requires`, and `$Priority` are used throughout.

## The examples

| # | You want to... | Key technique |
|---|---|---|
| 1 | [Make one weapon work locally AND inside another mod](Dual-Template-Weapon) | reparent with `$Requires`, `null` the local interactions |
| 2 | [Add a category to a crafting bench](Crafting-Table-Category) | `Categories+` array append |
| 3 | [Give the player a new or bigger stat (like Mana)](Player-Stat) | patch an `Entity/Stats` definition |
| 4 | [Point an item's interaction at another mod's internal one](Replace-Interaction) | replace an `Interactions` slot value |
| 5 | [Change the quality of a block or armor piece](Quality-Override) | single-field replace |
| 6 | [Reference particles defined in a later-loading pack](Particles-And-Load-Order) | json reference + re-merge on register |
| 7 | [Remove a field, resistance, or recipe](Removing-Values) | `null` delete |
| 8 | [Add to a droplist or wire up a new one](Droplists) | `Containers+` append, repoint `DropList` |
| 9 | [Change a crafting recipe's ingredients or bench](Recipe-Change) | nested object merge |
| 10 | [Put an item into a creative or crafting category](Item-Into-Category) | `Categories+` on the item |

## What Patchly can and cannot patch (for now)

Patchly only patches `.json` assets. The `.patch` path mirrors the target with `.json` swapped for `.patch`.

| Patchable (`.json`) | Not patchable |
|---|---|
| Items, blocks, recipes, droplists, stat definitions, interactions, entity configs | `.particlesystem`, `.particlespawner`, `.blockymodel`, `.blockyanim`, `.png`, `.lang` |

For a non-json asset, ship a normal full file in your pack instead of a `.patch`. You can still patch the `.json` that *references* it (see [Particles And Load Order](Particles-And-Load-Order)).
