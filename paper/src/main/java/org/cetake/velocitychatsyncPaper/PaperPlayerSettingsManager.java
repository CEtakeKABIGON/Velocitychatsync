package org.cetake.velocitychatsyncPaper;

import java.util.*;

public class PaperPlayerSettingsManager {

    private static Map<UUID, PlayerSyncSettings> settings = new HashMap<>();

    public static class PlayerSyncSettings {
        public boolean chat;
        public boolean advancements;
        public boolean deathLog;

        public PlayerSyncSettings() {
            this(true, true, true); // デフォルト値
        }

        public PlayerSyncSettings(boolean chat, boolean advancements, boolean deathLog) {
            this.chat = chat;
            this.advancements = advancements;
            this.deathLog = deathLog;
        }

    }


    public static void setPlayerSyncSettings(String settingData) {
        String[] parts = settingData.split("\\|", 5);
        if (parts.length != 5) {return;}
        UUID playerId = parts[1].getBytes().length > 0 ? UUID.fromString(parts[1]) : null;

        PlayerSyncSettings customSettings = new PlayerSyncSettings();
        customSettings.chat = parts[2].equals("true");
        customSettings.advancements = parts[3].equals("true");
        customSettings.deathLog = parts[4].equals("true");

        setSettings(playerId, customSettings);

    }

    public static void setSettings(UUID playerUUID, PlayerSyncSettings settingsData) {
        settings.put(playerUUID, settingsData);
    }


    public static boolean getChatEnable(UUID uuid) {
        return settings.computeIfAbsent(uuid, PlayerUuid -> new PlayerSyncSettings()).chat;
    }

    public static boolean getAdvancementsEnable(UUID uuid) {
        return settings.computeIfAbsent(uuid, PlayerUuid -> new PlayerSyncSettings()).advancements;
    }

    public static boolean getDeathLogEnable(UUID uuid) {
        return settings.computeIfAbsent(uuid, PlayerUuid -> new PlayerSyncSettings()).deathLog;
    }
}
