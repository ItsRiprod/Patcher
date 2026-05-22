package com.riprod.patchly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;

public final class PathUtil {
    public static final String PATCH_EXTENSION = ".patch";
    public static final String JSON_EXTENSION = ".json";

    private PathUtil() {}

    public static boolean isPatchFile(@Nonnull Path path) {
        Path fileName = path.getFileName();
        if (fileName == null) return false;
        return fileName.toString().endsWith(PATCH_EXTENSION);
    }

    public static boolean isJsonFile(@Nonnull Path path) {
        Path fileName = path.getFileName();
        if (fileName == null) return false;
        return fileName.toString().endsWith(JSON_EXTENSION);
    }

    @Nullable
    public static String patchToTargetRelative(@Nonnull String relativePatchPath) {
        if (!relativePatchPath.endsWith(PATCH_EXTENSION)) return null;
        return relativePatchPath.substring(0, relativePatchPath.length() - PATCH_EXTENSION.length()) + JSON_EXTENSION;
    }

    @Nonnull
    public static String normalizeRelative(@Nonnull Path packRoot, @Nonnull Path file) {
        return packRoot.relativize(file).toString().replace('\\', '/');
    }
}
