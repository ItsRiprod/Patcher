package com.riprod.patcher;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public final class JsonDeepMerge {
    private static final String APPEND_SUFFIX = "+";

    private JsonDeepMerge() {}

    @Nonnull
    public static JsonObject merge(@Nonnull JsonObject base, @Nonnull JsonObject patch) {
        JsonObject result = base.deepCopy();
        applyObjectPatch(result, patch);
        return result;
    }

    private static void applyObjectPatch(@Nonnull JsonObject target, @Nonnull JsonObject patch) {
        List<String> appendKeys = new ArrayList<>();

        for (String key : patch.keySet()) {
            if (key.length() > 1 && key.endsWith(APPEND_SUFFIX)) {
                appendKeys.add(key);
                continue;
            }
            applyReplaceOrRecurse(target, key, patch.get(key));
        }

        for (String key : appendKeys) {
            applyAppend(target, key.substring(0, key.length() - APPEND_SUFFIX.length()), patch.get(key));
        }
    }

    private static void applyReplaceOrRecurse(@Nonnull JsonObject target, @Nonnull String key, JsonElement patchValue) {
        if (patchValue == null || patchValue.isJsonNull()) {
            target.remove(key);
            return;
        }
        if (patchValue.isJsonObject()) {
            JsonElement existing = target.get(key);
            JsonObject child = (existing != null && existing.isJsonObject())
                    ? existing.getAsJsonObject()
                    : new JsonObject();
            if (existing == null || !existing.isJsonObject()) {
                target.add(key, child);
            }
            applyObjectPatch(child, patchValue.getAsJsonObject());
        } else {
            target.add(key, patchValue.deepCopy());
        }
    }

    private static void applyAppend(@Nonnull JsonObject target, @Nonnull String key, JsonElement patchValue) {
        if (patchValue == null || !patchValue.isJsonArray()) return;
        JsonArray patchArray = patchValue.getAsJsonArray();
        JsonElement existing = target.get(key);
        JsonArray result = (existing != null && existing.isJsonArray())
                ? existing.getAsJsonArray()
                : new JsonArray();
        if (existing == null || !existing.isJsonArray()) {
            target.add(key, result);
        }
        for (JsonElement el : patchArray) {
            result.add(el.deepCopy());
        }
    }

    public static void stripMergeKey(@Nonnull JsonObject obj) {
        obj.remove("merge");
    }
}
