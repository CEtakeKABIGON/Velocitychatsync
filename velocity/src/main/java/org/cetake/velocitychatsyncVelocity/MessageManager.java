package org.cetake.velocitychatsyncVelocity;

import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public class MessageManager {
    private final Logger logger;
    private final Path messageDir;
    private final Path MessageFile;
    private String PlayerChatMessage;
    private String PlayerJoinMessage;
    private String PlayerQuitMessage;
    private String PlayerChatMessageToDiscord;
    private String PlayerJoinMessageToDiscord;
    private String PlayerQuitMessageToDiscord;
    private String DiscordMessageToPlayer;
    private String DiscordMessageToDiscord;

    public MessageManager(Logger logger) {
        this.logger = logger;
        this.messageDir = Path.of("plugins", "velocityChatsync-velocity");
        this.MessageFile = messageDir.resolve("MessageCustom.yml");
        loadMessageCustom();
    }

    private void loadMessageCustom() {
        try {

            if (!Files.exists(MessageFile)) {
                logger.info("MessageCustom.yml not found, creating default...");
                try (InputStream input = getClass().getClassLoader().getResourceAsStream("MessageCustom.yml")) {
                    if (input != null) {
                        Files.copy(input, MessageFile, StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        logger.warn("Default MessageCustom.yml not found in resources!");
                        return;
                    }
                }
            }

            try (InputStream input = new FileInputStream(MessageFile.toFile())) {
                Yaml yaml = new Yaml();
                Map<String, Object> messageData = yaml.load(input);
                PlayerChatMessage = (String) messageData.get("PlayerChatMessage");
                PlayerJoinMessage = (String) messageData.get("PlayerJoinMessage");
                PlayerQuitMessage = (String) messageData.get("PlayerQuitMessage");
                PlayerChatMessageToDiscord = (String) messageData.get("PlayerChatMessageToDiscord");
                PlayerJoinMessageToDiscord = (String) messageData.get("PlayerJoinMessageToDiscord");
                PlayerQuitMessageToDiscord = (String) messageData.get("PlayerQuitMessageToDiscord");
                DiscordMessageToPlayer = (String) messageData.get("DiscordMessageToPlayer");
                DiscordMessageToDiscord = (String) messageData.get("DiscordMessageToDiscord");
                logger.info("MessageCustom.yml loaded successfully.");
            }
        } catch (IOException e) {
            logger.error("Failed to load MessageCustom.yml", e);
        }
    }

    public String getPlayerChatMessage() { return PlayerChatMessage; }
    public String getPlayerJoinMessage() { return PlayerJoinMessage; }
    public String getPlayerQuitMessage() { return PlayerQuitMessage; }
    public String getPlayerChatMessageToDiscord() { return PlayerChatMessageToDiscord; }
    public String getPlayerJoinMessageToDiscord() { return PlayerJoinMessageToDiscord; }
    public String getPlayerQuitMessageToDiscord() { return PlayerQuitMessageToDiscord; }
    public String getDiscordMessageToPlayer() { return DiscordMessageToPlayer;}
    public String getDiscordMessageToDiscord() { return DiscordMessageToDiscord;}
}