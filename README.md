# Patchly

Patch JSON assets without rewriting it from the ground up. Zero dependancies.

*Why Patchly Exists*
Hytale's native `Parent: super` only inherits at the outer asset level - most nested codec fields (e.g. `Item.armor.StatModifiers`, `DamageResistance`) use `.append(...)` not `.appendInherited(...)`, so a normal JSON override replaces the whole sub-object and silently wipes everything it didn't restate.

*What patchly does*
Patchly reads the resolved base asset and deep-merges your `.patch` onto it, so you only write the diff.

Works on every registered `AssetPack` - folder, `.zip`, and `.jar` packs all. JSON-only modders can drop `Patchly.jar` into `mods/`; Java modders can bundle Patchly into their own jar via Gradle Shadow. Patchly coordinates with other instances of patchly to ensure only one patchly is running at a time. This means that your mod can work by itself, with other patchly mods, with the patchly.jar, and more. 

## How patches work

Place a `.patch` file at the same path as the asset you're patching, swapping `.json` for `.patch`. To patch `Armor_Iron_Head.json` in another pack, ship `Server/Item/Items/Armor/Iron/Armor_Iron_Head.patch` in your pack.

Patchly walks every pack, resolves the latest version of each target, merges your `.patch` onto it, and writes the result into a synthetic override pack that takes precedence.

### Minimal patch

```json
{
  "Armor": {
    "StatModifiers": {
      "Mana": [{ "Amount": 126, "CalculationType": "Additive" }]
    }
  }
}
```

Deep-merges field-by-field. `Mana` lands inside the parent's existing `StatModifiers` block; `Health` and other siblings stay intact.

### Replace vs append on arrays

Arrays REPLACE by default. Suffix the key with `+` to append instead:

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

The parent's existing `Categories` entries stay; this entry gets added to the end.

### `$Requires` - only apply if specific packs are installed

Single pack:

```json
{
  "$Requires": "Riprod:Hexcode",
  "Armor": { ... }
}
```

Multiple packs (all must be present):

```json
{
  "$Requires": ["Riprod:Hexcode", "Author:SomeOtherPack"],
  "Armor": { ... }
}
```

Pack ids are matched against `AssetPack.getName()` (i.e. `Group:Name` from the target mod's manifest). If anything's missing the patch is skipped with a log line.

There is currently no support for excluding packs. This was deemed unnecessary, open a PR if you wish to have this functionality.

### `$Priority` - pick a winner on conflicts

Integer, default `0`. Lower applies first, higher applies last → higher wins on field conflicts. Tie-break is pack load order.

```json
{
  "$Priority": 100,
  "Armor": {
    "StatModifiers": {
      "Mana": [{ "Amount": 9999, "CalculationType": "Additive" }]
    }
  }
}
```

Two mods patching the same field both apply, but the higher `$Priority` writes last. Lower-priority `+` appends still stack onto fields the higher patch didn't touch.

### Reserved keys

Anything top-level prefixed with `$` is metadata - stripped before merge, never reaches the synthesized asset. `$Requires` and `$Priority` are the two with semantics today; `$Comment` (or any other `$Foo`) is free for your own notes.

## Using it

### As a standalone (asset-only mods)

Drop `Patchly-X.Y.Z.jar` into your server's `mods/` folder alongside your asset pack. That's it. Patchly will scan, merge, register.

### As a bundled dep (Java mods)

Add the Shadow plugin and depend on the lib jar:

```kotlin
plugins {
    id("hytale-mod") version "0.+"
    id("com.gradleup.shadow") version "8.3.5"
}

dependencies {
    implementation(files("deps/Patchly-2.0.0.jar"))
}

tasks.shadowJar {
    archiveClassifier.set("")
    mergeServiceFiles()
}

tasks.jar { enabled = false }
tasks.build { dependsOn(tasks.shadowJar) }
```

Then in your `JavaPlugin`:

```java
import com.riprod.patchly.PatchManager;

public final class MyPlugin extends JavaPlugin {
    private final PatchManager patchManager;

    public MyPlugin(JavaPluginInit init) {
        super(init);
        patchManager = new PatchManager(this.getManifest());
    }

    @Override public CompletableFuture<Void> preLoad() {
        patchManager.preLoad();
        return super.preLoad();
    }

    @Override protected void setup() {
        getEventRegistry().register(EventPriority.LAST, LoadAssetEvent.class,
                e -> patchManager.rebuildAndApply("boot"));
        getEventRegistry().register(AssetPackRegisterEvent.class, e -> {
            if (PatchManager.isSyntheticOverridePack(e.getAssetPack().getName())) return;
            patchManager.rebuildAndApply("packRegister:" + e.getAssetPack().getName());
        });
        getEventRegistry().register(AssetPackUnregisterEvent.class, e -> {
            if (PatchManager.isSyntheticOverridePack(e.getAssetPack().getName())) return;
            patchManager.rebuildAndApply("packUnregister:" + e.getAssetPack().getName());
        });
    }

    @Override protected void shutdown() { patchManager.shutdown(); }
}
```
> Note: When doing `./gradlew runServer` for testing, you cannot have Patchly.jar in the ./mods folder as well as your dependency. This is because the devserver will scan your deps and find your Patchly.jar's manifest.json and register it also as it's own pack. if it also exists in ./mods/ then it will register twice and die. You do not need Patchly.jar in your mods/ folder anyways, so just delete it lol 

If both standalone `Patchly.jar` and a bundling mod are installed in the same JVM, the first to load claims ownership via the `patcher.owner` system property; the other defers and noops. No duplicate work, no conflicts.

## Notes

- Hot reload works for `.patch` files in folder-pack mods (jar/zip packs apply once on register; no live-reload there).
- Output lives in `MODS_PATH/<group>_<name>_PatcherOverrides/` and is wiped on every cold start.
- `[AssetModule] Skipping pack at ..._PatcherOverrides: missing or invalid manifest.json` at boot is benign - the synthetic pack registers programmatically, not via filesystem scan.
