package org.cetake.velocitychatsyncPaper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public class Velocitychatsync extends JavaPlugin implements Listener, PluginMessageListener {

    @Override
    public void onEnable() {
        // プラグインメッセージチャネルの登録
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "velocitychatsync:main");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "velocitychatsync:main", this);

        // イベントリスナーの登録
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onPluginMessageReceived(String channel, @NotNull Player player, byte @NotNull [] message) {
        if (!channel.equals("velocitychatsync:main")) return;

        String receivedMessage = new String(message, StandardCharsets.UTF_8);
        MiniMessage mm = MiniMessage.miniMessage();
        Component formatMessage = mm.deserialize(receivedMessage);
        Bukkit.broadcast(formatMessage); // Velocity から受け取ったメッセージをブロードキャスト
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
    }
}