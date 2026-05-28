---
title: "For Mod Developers"
order: 2
published: true
draft: false
---
# For Mod Developers

This is for **Java plugins** that want their own patching built in, so your mod ships `.patch` files and applies them itself with no separate `Patchly.jar` for the user to install. You bundle Patchly's classes into your plugin jar with the Gradle Shadow plugin and drive `PatchManager` from your plugin lifecycle.

> Asset-only pack and just want to ship `.patch` files? You do not need any of this. See **[For Pack Developers](Pack-Developers)**.

## Quickstart

1. Put `Patchly-X.Y.Z.jar` in a `deps/` folder and shade it via a dedicated `shaded` configuration (so only Patchly is folded in).
2. Construct a `PatchManager(getManifest())` in your plugin.
3. Call `preLoad()`, register the three asset events in `setup()`, and call `shutdown()`.
4. Ship your `.patch` files the same way a pack developer does.

The two code blocks below are everything you need to copy. The explanations and caveats come after, skippable until you hit them.

## 1. Add the Shadow plugin and the lib jar

Drop `Patchly-X.Y.Z.jar` into a `deps/` folder in your project, then wire up Shadow:

```kotlin
plugins {
    id("hytale-mod") version "0.+"
    id("com.gradleup.shadow") version "8.3.5"
}

// a dedicated configuration so ONLY Patchly is shaded, not your compile deps
val shaded by configurations.creating

dependencies {
    // compile against the API, and mark it for shading into the final jar
    shaded(files("deps/Patchly-2.1.0.jar"))
    implementation(files("deps/Patchly-2.1.0.jar"))
}

tasks.shadowJar {
    archiveClassifier.set("")        // shadow jar IS the published artifact
    mergeServiceFiles()
    configurations = listOf(shaded)  // shade only what's in `shaded`
}

tasks.jar { enabled = false }        // disable the thin jar
tasks.build { dependsOn(tasks.shadowJar) }
```

## 2. Drive `PatchManager` from your plugin

`PatchManager` needs four touch points in your `JavaPlugin`: construct it, wipe-and-prep in `preLoad`, register three asset events in `setup`, and clean up in `shutdown`:

```java
import com.riprod.patchly.PatchManager;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.server.core.asset.LoadAssetEvent;
import com.hypixel.hytale.server.core.asset.AssetPackRegisterEvent;
import com.hypixel.hytale.server.core.asset.AssetPackUnregisterEvent;

public final class MyPlugin extends JavaPlugin {
    private final PatchManager patchManager;

    public MyPlugin(JavaPluginInit init) {
        super(init);
        patchManager = new PatchManager(this.getManifest());
    }

    @Override
    public CompletableFuture<Void> preLoad() {
        patchManager.preLoad();              // wipe + recreate the override dir
        return super.preLoad();
    }

    @Override
    protected void setup() {
        // initial merge once all base assets are loaded
        getEventRegistry().register(EventPriority.LAST, LoadAssetEvent.class,
                e -> patchManager.rebuildAndApply("boot:LoadAssetEvent"));

        // re-merge when packs come and go at runtime
        getEventRegistry().register(AssetPackRegisterEvent.class, e -> {
            String name = e.getAssetPack().getName();
            if (PatchManager.isSyntheticOverridePack(name)) return;   // ignore our own output
            patchManager.rebuildAndApply("packRegister:" + name);
        });
        getEventRegistry().register(AssetPackUnregisterEvent.class, e -> {
            String name = e.getAssetPack().getName();
            if (PatchManager.isSyntheticOverridePack(name)) return;
            patchManager.rebuildAndApply("packUnregister:" + name);
        });
    }

    @Override
    protected void shutdown() {
        patchManager.shutdown();
    }
}
```

## 3. Ship your `.patch` files

Put `.patch` files in your pack tree exactly as a pack developer would: mirror the target asset's path with a `.patch` extension. See **[For Pack Developers](Pack-Developers)** and the [syntax reference](../) for the merge rules (`+` append, `null` removal, `$Requires`, `$Priority`).

That is the working setup. The sections below explain why each piece is shaped this way.

---

## Why bundle instead of depend?

Patchly is a single self-contained class set with zero runtime dependencies. Shading it into your jar means:

- Your users install **one** jar, not two.
- Your patches apply even if no standalone `Patchly.jar` is present.
- If a standalone jar or another bundling mod is also installed, Patchly's JVM-wide ownership election (via the `patcher.owner` system property) ensures exactly **one** instance does the work. The rest defer and noop. No duplicate merges, no conflicts.

## Why the dedicated `shaded` configuration?

The `shaded` configuration is the important detail. Without `configurations = listOf(shaded)`, Shadow would try to fold in every dependency, including `compileOnly` server libs. Scoping it to a one-jar configuration keeps the output to your code plus Patchly.

## What each `PatchManager` call does

| Call | When | Purpose |
|---|---|---|
| `new PatchManager(getManifest())` | construction | Claims JVM ownership (or defers if another instance already owns it). The manifest names the override pack. |
| `preLoad()` | before assets load | Wipes the synthetic override directory for a clean slate. |
| `rebuildAndApply(reason)` on `LoadAssetEvent` (priority `LAST`) | after all base assets load | The initial merge pass. `LAST` ensures every base asset is resolved first. |
| `rebuildAndApply(reason)` on register/unregister | runtime pack changes | Re-merges so patches still apply to packs added or removed after boot. The `isSyntheticOverridePack` guard prevents an infinite loop when our own pack registers. |
| `shutdown()` | plugin teardown | Clears the override directory. |

> The `isSyntheticOverridePack` guard on both runtime events is **not optional**. Registering the synthetic override pack fires `AssetPackRegisterEvent`, which would re-enter `rebuildAndApply` and loop. The guard short-circuits on our own pack's name.

## Caveat: testing with `./gradlew runServer`

**Do not** also place a standalone `Patchly.jar` in the dev server's `mods/` folder while testing a bundling mod. The dev server scans your `deps/` jar, finds its `manifest.json`, and registers it as a pack. If a second copy is in `mods/` it registers twice and the server dies. You do not need the standalone jar anyway: your shaded mod already contains Patchly. Just remove it from `mods/`.

## Coexistence

In production, your bundling mod, a standalone `Patchly.jar`, and other bundling mods can all be installed at once. The first to construct a `PatchManager` writes the `patcher.owner` system property and becomes the single active owner; every other instance logs that it is deferring and does nothing. Your mod works standalone, alongside the standalone jar, and alongside other Patchly-bundling mods. No coordination required.
