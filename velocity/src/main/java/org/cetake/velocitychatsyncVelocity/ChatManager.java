package org.cetake.velocitychatsyncVelocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;
import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ChatManager { // チャンネル名
    private final Logger logger;
    private final ProxyServer server;
    private final List<String> registeredServers; // 登録されたサーバー
    private final DiscordConnect discordConnect;
    private final ConfigManager configManager;
    private final MessageManager messageManager;

    @Inject
    public ChatManager(ProxyServer server, Logger logger, ConfigManager configManager, MessageManager messageManager, DiscordConnect discordConnect) {
        this.server = server;
        this.logger = logger;
        this.registeredServers = configManager.getServers(); // 登録されたサーバーのリストを取得
        this.discordConnect = discordConnect;
        this.configManager = configManager;
        this.messageManager = messageManager;
    }

    @Subscribe
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        // 発言したサーバーを取得
        player.getCurrentServer().ifPresent(server -> {
            String serverName = server.getServerInfo().getName();
            String playerName = player.getUsername();
            broadcastMessage(serverName, playerName, message);
        });
    }

    @Subscribe
    public void onPlayerJoin(ServerPostConnectEvent event) {
        String playerName = event.getPlayer().getUsername();
        RegisteredServer server = event.getPlayer().getCurrentServer().get().getServer();
        String serverName = server.getServerInfo().getName();
        broadcastJoinMessage(serverName, playerName);
    }

    // チャットの転送
    public void broadcastMessage(String serverName, String playerName, String message) {
        for (RegisteredServer server : server.getAllServers()) {
            // serverName と一致するサーバーには送信しない
            if (registeredServers.contains(server.getServerInfo().getName()) && !server.getServerInfo().getName().equals(serverName)) {
                MiniMessage mm = MiniMessage.miniMessage();
                String safeServerName = mm.escapeTags(serverName);
                String safePlayerName = mm.escapeTags(playerName);
                String safeMessage = mm.escapeTags(message);
                String formattedMessage;
                if (configManager.isMessageCustom()) {
                    // プレイヤーの入力をエスケープ
                    formattedMessage = messageManager.getPlayerChatMessage()
                            .replace("{$Server}",serverName)
                            .replace("{$Player}",safePlayerName)
                            .replace("{$Message}",safeMessage);
                }else {
                    // プレイヤーの入力をエスケープ
                    formattedMessage = String.format("<yellow>[</yellow><AQUA>%s</AQUA><yellow>]</yellow> <%s> %s", safeServerName, safePlayerName, safeMessage);
                }
                sendPluginMessage(server, formattedMessage);
            }
        }
        if (configManager.isDiscordEnabled()) {
            String formattedMessage = String.format("[%s] <%s> %s", serverName, playerName, message);
            discordConnect.sendToOtherChannels(null, formattedMessage);
        }
    }

    void DiscordBroadcastMessage(String serverName, String playerName, String message, String channelId) {
        for (RegisteredServer server : server.getAllServers()) {
            // serverName と一致するサーバーには送信しない
            MiniMessage mm = MiniMessage.miniMessage();
            String safeServerName = mm.escapeTags(serverName);
            String safePlayerName = mm.escapeTags(playerName);
            String safeMessage = mm.escapeTags(message);
            String formattedMessage;
            if (configManager.isMessageCustom()) {

                // プレイヤーの入力をエスケープ

                formattedMessage = messageManager.getDiscordMessageToPlayer()
                        .replace("{$Server}",serverName)
                        .replace("{$Player}",safePlayerName)
                        .replace("{$Message}",safeMessage);

            }else {
                // プレイヤーの入力をエスケープ

                formattedMessage = String.format("<yellow>[</yellow><green>%s</green><yellow>]</yellow> <%s> %s", safeServerName, safePlayerName, safeMessage);
            }
            sendPluginMessage(server, formattedMessage);
        }
        if (configManager.isDiscordEnabled()) {
            String formattedMessage = String.format("[%s] <%s> %s", serverName, playerName, message);
            discordConnect.sendToOtherChannels(channelId, formattedMessage);
        }
    }

    // 入室ログの転送
    private void broadcastJoinMessage(String serverName, String playerName) {
        if (registeredServers.contains(serverName)) {
            MiniMessage mm = MiniMessage.miniMessage();
            String safeServerName = mm.escapeTags(serverName);
            String safePlayerName = mm.escapeTags(playerName);
            String message;
            if (configManager.isMessageCustom()) {

                message = messageManager.getPlayerJoinMessage()
                        .replace("{$Server}",serverName)
                        .replace("{$Player}",safePlayerName);

            }else {

                message = String.format("<AQUA>%s</AQUA> に <AQUA>%s</AQUA> が入室しました", safeServerName, safePlayerName);
            }
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
                MiniMessage mm = MiniMessage.miniMessage();
                String safePlayerName = mm.escapeTags(playerName);
                String message;
                if (configManager.isMessageCustom()){
                    message = messageManager.getPlayerQuitMessage()
                            .replace("{$Player}",safePlayerName);
                    broadcastToRegisteredServers(message);
                }else {
                    message = String.format("<yellow>%sが退室しました</yellow>", safePlayerName);
                    broadcastToRegisteredServers(message);
                }
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
    public void sendPluginMessage(RegisteredServer server, String message) {
        MinecraftChannelIdentifier channelIdentifier = MinecraftChannelIdentifier.create("velocitychatsync", "main");
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        server.sendPluginMessage(channelIdentifier, messageBytes);
    }
}
