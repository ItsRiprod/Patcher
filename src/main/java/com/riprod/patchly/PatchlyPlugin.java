package com.riprod.patchly;

import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.AssetPackRegisterEvent;
import com.hypixel.hytale.server.core.asset.AssetPackUnregisterEvent;
import com.hypixel.hytale.server.core.asset.LoadAssetEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import java.util.concurrent.CompletableFuture;

public final class PatchlyPlugin extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private final PatchManager patchManager;

    public PatchlyPlugin(JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Patcher %s initializing...", this.getManifest().getVersion().toString());
        patchManager = new PatchManager(this.getManifest());
    }

    @Override
    public CompletableFuture<Void> preLoad() {
        patchManager.preLoad();
        return super.preLoad();
    }

    @Override
    protected void setup() {
        this.getEventRegistry().register(EventPriority.LAST, LoadAssetEvent.class,
                e -> patchManager.rebuildAndApply("boot:LoadAssetEvent"));
        this.getEventRegistry().register(AssetPackRegisterEvent.class, e -> {
            String name = e.getAssetPack().getName();
            if (PatchManager.isSyntheticOverridePack(name)) return;
            patchManager.rebuildAndApply("packRegister:" + name);
        });
        this.getEventRegistry().register(AssetPackUnregisterEvent.class, e -> {
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
