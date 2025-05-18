package org.cetake.velocitychatsyncPaper;

import java.util.*;

public class PaperPlayerSettingsManager {
    public static class PlayerSyncSettings {
        public boolean chat = true;
        public boolean advancements = true;
        public boolean deathLog = true;
    }

    private final Map<UUID, PlayerSyncSettings> playerSettings = new HashMap<>();
    private final Map<UUID, Set<String>> playerChannelSettings = new HashMap<>();

    public void updateFromPluginMessage(String message) {
        // 例: toggleSetting|<uuid>|chat=true|advancements=false|deathLog=true
        String[] parts = message.split("\\|");
        if (!"toggleSetting".equals(parts[0]) || parts.length < 5) return;

        try {
            UUID uuid = UUID.fromString(parts[1]);
            PlayerSyncSettings settings = new PlayerSyncSettings();
            for (int i = 2; i < parts.length; i++) {
                String[] kv = parts[i].split("=", 2);
                if (kv.length != 2) continue;
                switch (kv[0]) {
                    case "chat" -> settings.chat = Boolean.parseBoolean(kv[1]);
                    case "advancements" -> settings.advancements = Boolean.parseBoolean(kv[1]);
                    case "deathLog" -> settings.deathLog = Boolean.parseBoolean(kv[1]);
                }
            }
            playerSettings.put(uuid, settings);
        } catch (IllegalArgumentException ignored) {}
    }

    public PlayerSyncSettings getSettings(UUID uuid) {
        return playerSettings.getOrDefault(uuid, new PlayerSyncSettings());
    }

    public boolean isChatEnabled(UUID uuid) {
        return getSettings(uuid).chat;
    }

    public boolean isAdvancementEnabled(UUID uuid) {
        return getSettings(uuid).advancements;
    }

    public boolean isDeathLogEnabled(UUID uuid) {
        return getSettings(uuid).deathLog;
    }

    public boolean isChannelEnabled(UUID playerId, String channel) {
        Set<String> enabled = playerChannelSettings.getOrDefault(playerId, Set.of("global"));
        return enabled.contains(channel);
    }

    public void enableChannel(UUID playerId, String channel) {
        playerChannelSettings.computeIfAbsent(playerId, k -> new HashSet<>()).add(channel);
    }

    public void disableChannel(UUID playerId, String channel) {
        playerChannelSettings.computeIfAbsent(playerId, k -> new HashSet<>()).remove(channel);
    }

    public boolean handleIncomingMessage(String message, UUID playerUUID) {
        String[] parts = message.split("\\|", 2);
        if (parts.length < 2) return false;

        String action = parts[0];
        String data = parts[1];

        switch (action) {
            case "toggleSetting" -> {
                updateFromPluginMessage(message);
                return false;
            }
            case "JoinMessage", "DisconnectMessage", "otherChannelMessage", "AdvancementsMessage", "DeathLog" -> {
                PlayerSyncSettings settings = getSettings(playerUUID);
                return switch (action) {
                    case "otherChannelMessage" -> settings.chat;
                    case "AdvancementsMessage" -> settings.advancements;
                    case "DeathLog" -> settings.deathLog;
                    default -> true;
                };
            }
            default -> {
                return true;
            }
        }
    }
}
