package com.kuronami.tradediary.data;

import com.kuronami.tradediary.TradeDiary;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.GsonBuilder;
import com.google.gson.Gson;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player diary persistence (server-side) for Forge 1.20.1.
 *
 * <p>Each player gets a JSON file under {@code <world>/data/tradediary/<uuid>.json}.
 * Entries are appended on trade completion and flushed to disk immediately —
 * tiny payload, no need for batching. The in-memory cache MUST be invalidated
 * on server stop so a different world doesn't see the previous world's entries.
 */
public final class TradeStorage {

    private static final String FOLDER_NAME = "tradediary";

    private static final Codec<List<TradeEntry>> LIST_CODEC = TradeEntry.CODEC.listOf();

    private static final Map<UUID, List<TradeEntry>> CACHE = new ConcurrentHashMap<>();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private TradeStorage() {}

    public static void invalidate() {
        CACHE.clear();
        TradeDiary.LOGGER.debug("Trade Diary cache cleared.");
    }

    private static Path folder(MinecraftServer server) {
        Path worldData = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve(FOLDER_NAME);
        try {
            Files.createDirectories(worldData);
        } catch (IOException e) {
            TradeDiary.LOGGER.error("Failed to create diary folder: {}", worldData, e);
            return null;
        }
        return worldData;
    }

    private static Path file(MinecraftServer server, UUID uuid) {
        Path f = folder(server);
        return f == null ? null : f.resolve(uuid + ".json");
    }

    public static List<TradeEntry> get(MinecraftServer server, UUID uuid) {
        return CACHE.computeIfAbsent(uuid, key -> load(server, key));
    }

    public static List<TradeEntry> get(ServerPlayer player) {
        return get(player.server, player.getUUID());
    }

    public static void append(ServerPlayer player, TradeEntry entry) {
        UUID uuid = player.getUUID();
        List<TradeEntry> list = get(player.server, uuid);
        synchronized (list) {
            list.add(entry);
        }
        save(player.server, uuid);
    }

    /**
     * Append, OR — if the previous entry was the same trade with the same
     * villager within {@code mergeWindowMs} — combine counts in-place instead.
     * Collapses shift-click bursts into one entry.
     */
    public static void appendOrMerge(ServerPlayer player, TradeEntry entry, long mergeWindowMs) {
        UUID uuid = player.getUUID();
        List<TradeEntry> list = get(player.server, uuid);
        synchronized (list) {
            if (!list.isEmpty()) {
                TradeEntry last = list.get(list.size() - 1);
                if (entry.epochMillis() - last.epochMillis() <= mergeWindowMs
                        && safeEq(last.villagerUuid(), entry.villagerUuid())
                        && safeEq(last.resultItem(), entry.resultItem())
                        && safeEq(last.costAItem(), entry.costAItem())
                        && safeEq(last.costBItem(), entry.costBItem())
                        && last.professionLevel() == entry.professionLevel()) {
                    TradeEntry merged = new TradeEntry(
                            entry.epochMillis(),
                            entry.worldDay(),
                            entry.dimensionId(),
                            entry.biomeId(),
                            entry.x(), entry.y(), entry.z(),
                            entry.villagerUuid(),
                            entry.profession(),
                            entry.professionLevel(),
                            entry.costAItem(), last.costACount() + entry.costACount(),
                            entry.costBItem(), last.costBCount() + entry.costBCount(),
                            entry.resultItem(), last.resultCount() + entry.resultCount()
                    );
                    list.set(list.size() - 1, merged);
                    save(player.server, uuid);
                    return;
                }
            }
            list.add(entry);
        }
        save(player.server, uuid);
    }

    private static boolean safeEq(String a, String b) {
        if (a == null) return b == null;
        return a.equals(b);
    }

    private static List<TradeEntry> load(MinecraftServer server, UUID uuid) {
        Path path = file(server, uuid);
        if (path == null || !Files.exists(path)) {
            return Collections.synchronizedList(new ArrayList<>());
        }
        try {
            String json = Files.readString(path, StandardCharsets.UTF_8);
            JsonElement root = JsonParser.parseString(json);
            List<TradeEntry> list = LIST_CODEC.parse(JsonOps.INSTANCE, root)
                    .resultOrPartial(err -> TradeDiary.LOGGER.warn("Diary parse error for {}: {}", uuid, err))
                    .orElseGet(ArrayList::new);
            return Collections.synchronizedList(new ArrayList<>(list));
        } catch (IOException e) {
            TradeDiary.LOGGER.warn("Failed to read diary {}: {}", path, e.toString());
            return Collections.synchronizedList(new ArrayList<>());
        }
    }

    private static void save(MinecraftServer server, UUID uuid) {
        Path path = file(server, uuid);
        if (path == null) return;
        List<TradeEntry> list = CACHE.getOrDefault(uuid, List.of());
        List<TradeEntry> snapshot;
        synchronized (list) {
            snapshot = new ArrayList<>(list);
        }
        try {
            JsonElement json = LIST_CODEC.encodeStart(JsonOps.INSTANCE, snapshot)
                    .getOrThrow(false, msg -> { throw new RuntimeException("encode diary: " + msg); });
            Files.writeString(path, GSON.toJson(json), StandardCharsets.UTF_8);
        } catch (Exception e) {
            TradeDiary.LOGGER.warn("Failed to write diary {}: {}", path, e.toString());
        }
    }

    public static void clearCache() {
        CACHE.clear();
    }
}
