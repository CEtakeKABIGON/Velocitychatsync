package org.cetake.velocitychatsyncVelocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.slf4j.Logger;
import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ChatManager {
    private static final String CHANNEL = "velocitychatsync:main"; // チャンネル名
    private final Logger logger;
    private final ProxyServer server;
    private final List<String> registeredServers; // 登録されたサーバー
    private final DiscordConnect discordConnect;
    private final ConfigManager configManager;

    @Inject
    public ChatManager(ProxyServer server, Logger logger, ConfigManager configManager, DiscordConnect discordConnect) {
        this.server = server;
        this.logger = logger;
        this.registeredServers = configManager.getServers(); // 登録されたサーバーのリストを取得
        this.discordConnect = discordConnect;
        this.configManager = configManager;
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
                String playerName = parts[1];

                switch (action) {
                    case "Chat":
                        if (parts.length < 3) {
                            return;
                        }

                        String message = parts[2];
                        // チャットメッセージを転送
                        broadcastMessage(serverName, playerName, message, null);
                        break;

                    case "PlayerJoin":
                        broadcastJoinMessage(serverName, playerName);
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

    @Subscribe
    public void onPlayerJoin(ServerPostConnectEvent event) {
        String playerName = event.getPlayer().getUsername();
        RegisteredServer server = event.getPlayer().getCurrentServer().get().getServer();
        String serverName = server.getServerInfo().getName();
        broadcastJoinMessage(serverName, playerName);
    }

    // チャットの転送
    void broadcastMessage(String serverName, String playerName, String message, String channelId) {
        for (RegisteredServer server : server.getAllServers()) {
            // serverName と一致するサーバーには送信しない
            if (registeredServers.contains(server.getServerInfo().getName()) && !server.getServerInfo().getName().equals(serverName)) {
                String formattedMessage = String.format("[%s] <%s> %s", serverName, playerName, message);
                sendPluginMessage(server, formattedMessage);
            }
        }
        if (configManager.isDiscordEnabled()) {
            String formattedMessage = String.format("[%s] <%s> %s", serverName, playerName, message);
            discordConnect.sendToOtherChannels(channelId, formattedMessage);
        }
    }

    // 入室ログの転送
    private void broadcastJoinMessage(String serverName, String playerName) {
        if (registeredServers.contains(serverName)) {
            String message = String.format("%s に %s が入室しました", serverName, playerName);
            broadcastToRegisteredServers(message);
        }
        if (configManager.isDiscordEnabled()) {
            String message = String.format("%s に %s が入室しました", serverName, playerName);
            discordConnect.sendToOtherChannels("dummy", message);
        }
    }

    // Velocity から切断時の処理（登録されたサーバーのみ）
    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        String playerName = event.getPlayer().getUsername();
        for (RegisteredServer server : server.getAllServers()) {
            if (server.getPlayersConnected().contains(event.getPlayer()) && registeredServers.contains(server.getServerInfo().getName())) {
                String message = String.format("%sが退室しました", playerName);
                broadcastToRegisteredServers(message);
            }
        }
        if (configManager.isDiscordEnabled()) {
            String message = String.format("%sが退室しました", playerName);
            discordConnect.sendToOtherChannels("dummy", message);
        }
    }

    // 登録されたサーバーのみにメッセージを送信
    private void broadcastToRegisteredServers(String message) {
        for (RegisteredServer server : server.getAllServers()) {
            if (registeredServers.contains(server.getServerInfo().getName())) {
                sendPluginMessage(server, message);
            }
        }
    }

    // Plugin Message を送信
    private void sendPluginMessage(RegisteredServer server, String message) {
        MinecraftChannelIdentifier channelIdentifier = MinecraftChannelIdentifier.create("velocitychatsync", "main");
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        server.sendPluginMessage(channelIdentifier, messageBytes);
    }
}
