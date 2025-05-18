package org.cetake.velocitychatsyncVelocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class SystemLogManager {

    private static final String CHANNEL = "velocitychatsync:main"; // チャンネル名
    private final List<String> registeredServers;
    private final ProxyServer server;
    private final Logger logger;
    ChatManager chatManager;
    DiscordConnect discordConnect;
    ConfigManager configManager;
    MessageManager messageManager;
    private static final String DEFAULT_DISCORD_CHANNEL = "dummy";
    MiniMessage mm = MiniMessage.miniMessage();

    public SystemLogManager(ProxyServer server, Logger logger, ConfigManager configManager, MessageManager messageManager, DiscordConnect discordConnect) {
        this.server = server;
        this.logger = logger;
        this.registeredServers = configManager.getServers(); // 登録されたサーバーのリストを取得
        this.discordConnect = discordConnect;
        this.configManager = configManager;
        this.messageManager = messageManager;
    }

    public void setChatManager(ChatManager chatManager) {
        this.chatManager = chatManager;
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        logger.info("PluginMessageEvent triggered");

        if (!event.getIdentifier().getId().equals(CHANNEL)) {
            return;
        }

        try {
            String receivedMessage = new String(event.getData(), StandardCharsets.UTF_8);

            if (event.getSource() instanceof ServerConnection connection) {
                RegisteredServer sourceServer = connection.getServer();
                String serverName = sourceServer.getServerInfo().getName();

                // 送信されたデータを解析
                String[] parts = receivedMessage.split("\\|", 3);
                if (parts.length < 2) {
                    return;
                }

                String action = parts[0];
                String message = parts[1];

                switch (action) {
                    case "DeathLog":
                        DeathLogMessage(serverName, message, sourceServer);
                        break;

                    case "Advancements":
                        if (parts.length < 3) {
                            return;
                        }
                        String playerName = parts[2];
                        // チャットメッセージを転送
                        AdvancementsMessage(serverName, playerName, message, sourceServer);
                        break;
                    default:
                        logger.warn("Unknown action from {}: {}", serverName, action);
                }
            } else {
                logger.warn("Received message from unknown source: " + receivedMessage);
            }

        } catch (Exception e) {
            logger.error("Failed to handle plugin message", e);
        }
    }

    private void DeathLogMessage(String serverName, String message, RegisteredServer sourceerver) {
        // チャットメッセージを転送
        String FormatMessage;
        String DiscordMessage;
        String safeServerName = mm.escapeTags(serverName);
        String safeMessage = mm.escapeTags(message);
        if(configManager.isMessageCustom()) {
            FormatMessage = messageManager.getPlayerDeathLog()
                    .replace("{$Server}", safeServerName)
                    .replace("{$Message}", safeMessage);
            DiscordMessage = messageManager.getPlayerDeathLogToDiscord()
                    .replace("{$Server}", safeServerName)
                    .replace("{$Message}", safeMessage);
        }else {
            FormatMessage = String.format("<yellow>[</yellow><green>%s</green><yellow>]</yellow> %s", serverName, message);
            DiscordMessage = String.format("[%s] %s", serverName, message);
        }
        for (RegisteredServer server : server.getAllServers()) {
            if (registeredServers.contains(server.getServerInfo().getName()) && !server.getServerInfo().getName().equals(serverName)) {
                chatManager.sendPluginMessage(server, FormatMessage);
            }
        }
        if(configManager.isDiscordEnabled()) {
            if (configManager.isMessageCustom()) {
                discordConnect.sendToOtherChannels(DEFAULT_DISCORD_CHANNEL, DiscordMessage, messageManager.getPlayerDeathLogToDiscordColor());
            } else {
                discordConnect.sendToOtherChannels(DEFAULT_DISCORD_CHANNEL, DiscordMessage, "#FFFF00");
            }
        }
    }

    private void AdvancementsMessage(String serverName, String playerName, String message, RegisteredServer sourceServer){
        String FormatMessage;
        String DiscordMessage;
        String safeServerName = mm.escapeTags(serverName);
        String safePlayerName = mm.escapeTags(playerName);
        String safeMessage = mm.escapeTags(message);
        if(configManager.isMessageCustom()) {
            FormatMessage = messageManager.getPlayerAdvancements()
                    .replace("{$Player}", safePlayerName)
                    .replace("{$Server}", safeServerName)
                    .replace("{$Message}", safeMessage);
            DiscordMessage = messageManager.getPlayerAdvancementsToDiscord()
                    .replace("{$Player}", safePlayerName)
                    .replace("{$Server}", safeServerName)
                    .replace("{$Message}", safeMessage);
        }else{
            FormatMessage = String.format("<yellow>[</yellow><green>%s</green><yellow>]</yellow> <AQUA>%s</AQUA> は 進歩 %s を達成した", serverName, playerName, message);
            DiscordMessage = String.format("[%s] %s は 進歩 %s を達成した", serverName, playerName, message);
        }
        for (RegisteredServer server : server.getAllServers()) {
            if (registeredServers.contains(server.getServerInfo().getName()) && !server.getServerInfo().getName().equals(serverName)) {
                chatManager.sendPluginMessage(server, FormatMessage);
            }
        }

        if(configManager.isDiscordEnabled()){
            if (configManager.isMessageCustom()) {
                discordConnect.sendToOtherChannels(DEFAULT_DISCORD_CHANNEL, DiscordMessage, messageManager.getPlayerAdvancementsToDiscordColor());
            } else {
                discordConnect.sendToOtherChannels(DEFAULT_DISCORD_CHANNEL, DiscordMessage, "#FFFF00");
            }
        }
    }
}
