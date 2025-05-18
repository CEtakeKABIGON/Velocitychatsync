package org.cetake.velocitychatsyncVelocity;

import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigManager {
    private final Logger logger;
    private final Path configDir;
    private final Path configFile;
    private List<String> servers; // 起動時に読み込んで保存
    private String discordToken; // Discord Bot のトークン
    private List<DiscordServer> discordServers; // Discord サーバーの情報
    private static boolean discordEnable;
    private static boolean messageCustom;// Discord 同期の有効化設定

    public ConfigManager(Logger logger) {
        this.logger = logger;
        this.configDir = Path.of("plugins", "velocityChatsync-velocity");
        this.configFile = configDir.resolve("config.yml");
        loadConfig();
    }

    public void loadConfig() {
        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            if (!Files.exists(configFile)) {
                logger.info("config.yml not found, creating default...");
                try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                    if (input != null) {
                        Files.copy(input, configFile, StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        logger.warn("Default config.yml not found in resources!");
                        return;
                    }
                }
            }

            try (InputStream input = new FileInputStream(configFile.toFile())) {
                Yaml yaml = new Yaml();
                Map<String, Object> configData = yaml.load(input);

                messageCustom = (boolean)configData.get("messageCustom");

                // Minecraft の `servers` を取得
                Object serversObj = configData.get("servers");
                if (serversObj instanceof List<?>) {
                    servers = (List<String>) serversObj;
                } else {
                    servers = List.of(); // 空リスト
                }

                // Discord の `token` を取得
                Object discordConfig = configData.get("discord");
                if (discordConfig instanceof Map<?, ?>) {
                    Map<String, Object> discordMap = (Map<String, Object>) discordConfig;
                    discordToken = (String) discordMap.getOrDefault("token", "");
                    discordEnable = (boolean) discordMap.getOrDefault("enable", false);

                    // Discord サーバーリストを取得
                    discordServers = new ArrayList<>();
                    Object discordServersObj = discordMap.get("discord_servers");
                    if (discordServersObj instanceof List<?>) {
                        for (Object obj : (List<?>) discordServersObj) {
                            if (obj instanceof Map<?, ?>) {
                                Map<String, String> serverMap = (Map<String, String>) obj;
                                String serverName = serverMap.get("server_name");
                                String channelId = serverMap.get("channel_id");
                                if (serverName != null && channelId != null) {
                                    discordServers.add(new DiscordServer(serverName, channelId));
                                }
                            }
                        }
                    }
                } else {
                    discordToken = "";
                    discordServers = List.of();
                }

                logger.info("config.yml loaded successfully.");
                logger.info("Loaded servers: " + servers);
                logger.info("Loaded Discord servers: " + discordServers);
            }
        } catch (IOException e) {
            logger.error("Failed to load config.yml", e);
        }
    }

    public List<String> getServers() {
        return servers;
    }

    public String getDiscordToken() {
        return discordToken;
    }

    public List<DiscordServer> getDiscordServers() {
        return discordServers;
    }

    public boolean isDiscordEnabled() {
        return discordEnable;
    }

    public boolean isMessageCustom() { return messageCustom;}

    public static class DiscordServer {
        private final String serverName;
        private final String channelId;

        public DiscordServer(String serverName, String channelId) {
            this.serverName = serverName;
            this.channelId = channelId;
        }

        public String getServerName() {
            return serverName;
        }

        public String getChannelId() {
            return channelId;
        }

        @Override
        public String toString() {
            return "DiscordServer{name='" + serverName + "', channelId='" + channelId + "'}";
        }
    }
}