package org.cetake.velocitychatsyncPaper;

import java.util.*;

public class PaperPlayerSettingsManager {

    private Map<UUID, PlayerSyncSettings> settings = new HashMap<>();

    public static class PlayerSyncSettings {
        public boolean chat = true;
        public boolean advancements = true;
        public boolean deathLog = true;
    }


    public static void setPlayerSyncSettings(String settingData) {
        String[] parts = settingData.split("\\|", 5);
        if (parts.length != 5) {return;}
        UUID playerId = parts[1].;

        PlayerSyncSettings customSettings = new PlayerSyncSettings();
        customSettings.chat = false;
        customSettings.advancements = true;
        customSettings.deathLog = false;

        syncManager.setSettings(playerId, customSettings);

    }

    public boolean getChatEnable(UUID uuid) {
        return settings.computeIfAbsent(uuid, PlayerUuid -> new PlayerSyncSettings()).chat;
    }

    public boolean getAdvancementsEnable(UUID uuid) {
        return settings.computeIfAbsent(uuid, PlayerUuid -> new PlayerSyncSettings()).advancements;
    }

    public boolean getDeathLogEnable(UUID uuid) {
        return settings.computeIfAbsent(uuid, PlayerUuid -> new PlayerSyncSettings()).deathLog;
    }
}
