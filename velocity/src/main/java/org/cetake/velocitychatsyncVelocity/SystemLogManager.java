package org.cetake.velocitychatsyncVelocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;

public class SystemLogManager {

    private static final String CHANNEL = "velocitychatsync:main"; // チャンネル名
    private final Logger logger;
    ChatManager chatManager;
    DiscordConnect discordConnect;

    public SystemLogManager(Logger logger ) {
        this.logger = logger;
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
                        // チャットメッセージを転送
                        chatManager.sendPluginMessage(sourceServer, message);
                        discordConnect.sendToOtherChannels("dummy", message);
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

    private void AdvancementsMessage(String serverName, String playerName, String message, RegisteredServer sourceServer){
        String FormatMessage = String.format("[%s] %s は 進歩 %s を達成した", serverName, playerName, message);
        chatManager.sendPluginMessage(sourceServer, FormatMessage);
        discordConnect.sendToOtherChannels("dummy", FormatMessage);
    }
}
