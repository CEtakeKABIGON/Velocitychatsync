package org.cetake.velocitychatsyncFolia;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.entity.Player;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public class Velocitychatsync extends JavaPlugin implements Listener, PluginMessageListener {

    @Override
    public void onEnable() {
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "velocitychatsync:main");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "velocitychatsync:main", this);
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onPluginMessageReceived(String channel, @NotNull Player player, byte[] message) {
        if (!channel.equals("velocitychatsync:main")) return;

        String receivedMessage = new String(message, StandardCharsets.UTF_8);

        Bukkit.getLogger().info(receivedMessage);

        MiniMessage mm = MiniMessage.miniMessage();
        Component formatMessage = mm.deserialize(receivedMessage);

        getServer().getGlobalRegionScheduler().execute(this, () -> {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.sendMessage(formatMessage);
            }
        });

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
    }

    @EventHandler
    public void onAsyncChatEvent(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String plainMessage = PlainTextComponentSerializer.plainText().serialize(event.message());

        // Folia のスケジューリングを使用して Velocity へ送信
        getServer().getGlobalRegionScheduler().execute(this, () -> {
            sendToVelocity(player.getName() + "|" + plainMessage);
        });
    }

    private void sendToVelocity(String data) {
        String message = "Chat" + "|" + data;
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        Bukkit.getServer().sendPluginMessage(this, "velocitychatsync:main", messageBytes);
    }
}