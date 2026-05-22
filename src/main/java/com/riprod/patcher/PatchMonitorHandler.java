package com.riprod.patcher;

import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.server.core.asset.monitor.AssetMonitorHandler;
import com.hypixel.hytale.server.core.asset.monitor.EventKind;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Map;

final class PatchMonitorHandler implements AssetMonitorHandler {
    private final PatchManager manager;
    private final AssetPack pack;
    private final String key;

    PatchMonitorHandler(@Nonnull PatchManager manager, @Nonnull AssetPack pack) {
        this.manager = manager;
        this.pack = pack;
        this.key = "PatchMonitor:" + pack.getName();
    }

    @Override
    public Object getKey() {
        return key;
    }

    @Override
    public boolean test(Path path, EventKind eventKind) {
        return PathUtil.isPatchFile(path) || PathUtil.isJsonFile(path);
    }

    @Override
    public void accept(Map<Path, EventKind> events) {
        for (Map.Entry<Path, EventKind> e : events.entrySet()) {
            Path path = e.getKey();
            if (PathUtil.isPatchFile(path)) {
                manager.onPatchEvent(pack, path);
            } else if (PathUtil.isJsonFile(path)) {
                manager.onBaseEvent(pack, path);
            }
        }
    }
}
