package org.cetake.velocitychatsyncVelocity;

import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

public class VelocityPlayerSettingsManager {

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

    private final Map<UUID, PlayerSyncSettings> playerSettings = new HashMap<>();
    private final Path settingsFile;
    private final Logger logger;

    public VelocityPlayerSettingsManager(Logger logger) {
        this.logger = logger;
        this.settingsFile = Path.of("plugins", "velocityChatsync-velocity", "player-settings.yml");
        load();
    }

    public PlayerSyncSettings getSettings(UUID uuid) {
        return playerSettings.computeIfAbsent(uuid, k -> new PlayerSyncSettings());
    }

    public void toggleSetting(UUID uuid, String type) {
        PlayerSyncSettings s = getSettings(uuid);
        switch (type.toLowerCase()) {
            case "chat" -> s.chat = !s.chat;
            case "advancements" -> s.advancements = !s.advancements;
            case "death", "deathlog" -> s.deathLog = !s.deathLog;
        }
        save();
    }

    public String createPluginMessage(UUID uuid) {
        PlayerSyncSettings s = getSettings(uuid);
        return String.format("toggleSetting|%s|%b|%b|%b",
                uuid, s.chat, s.advancements, s.deathLog);
    }

    public void load() {
        if (!settingsFile.toFile().exists()) return;
        try (InputStream input = new FileInputStream(settingsFile.toFile())) {
            Yaml yaml = new Yaml();
            Map<String, Map<String, Boolean>> data = yaml.load(input);
            if (data == null) return;
            for (Map.Entry<String, Map<String, Boolean>> entry : data.entrySet()) {
                UUID uuid = UUID.fromString(entry.getKey());
                Map<String, Boolean> values = entry.getValue();
                PlayerSyncSettings settings = new PlayerSyncSettings();
                settings.chat = values.getOrDefault("chat", true);
                settings.advancements = values.getOrDefault("advancements", true);
                settings.deathLog = values.getOrDefault("deathLog", true);
                playerSettings.put(uuid, settings);
            }
            logger.info("Loaded player settings from YML.");
        } catch (Exception e) {
            logger.error("Failed to load player settings", e);
        }
    }

    public void save() {
        try {
            Map<String, Map<String, Boolean>> data = new HashMap<>();
            for (Map.Entry<UUID, PlayerSyncSettings> entry : playerSettings.entrySet()) {
                Map<String, Boolean> values = new HashMap<>();
                values.put("chat", entry.getValue().chat);
                values.put("advancements", entry.getValue().advancements);
                values.put("deathLog", entry.getValue().deathLog);
                data.put(entry.getKey().toString(), values);
            }

            File dir = settingsFile.getParent().toFile();
            if (!dir.exists()) dir.mkdirs();

            try (Writer writer = new FileWriter(settingsFile.toFile())) {
                new Yaml().dump(data, writer);
            }
            logger.info("Saved player settings to YML.");
        } catch (Exception e) {
            logger.error("Failed to save player settings", e);
        }
    }

    public boolean has(UUID uuid) {
        return playerSettings.containsKey(uuid);
    }
}
