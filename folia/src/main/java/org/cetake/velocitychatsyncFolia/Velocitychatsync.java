package org.cetake.velocitychatsyncFolia;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
    public void onPlayerDeath(PlayerDeathEvent event) {
        String deathMessage = event.getDeathMessage();

        getServer().getGlobalRegionScheduler().execute(this, () -> {
            sendToVelocity("DeathLog", deathMessage);
        });
    }

    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        String advancementKey = event.getAdvancement().getKey().getKey();

        if (advancementKey.startsWith("recipes/")) {return;}

        String playerName = event.getPlayer().getName();
        String sendData = advancementKey + "|" + playerName;

        getServer().getGlobalRegionScheduler().execute(this, () -> {
            sendToVelocity("Advancements", sendData);
        });
    }

    private void sendToVelocity(String Action, String data) {
        String message = Action + "|" + data;
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        Player player = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
        if (player != null) {
            player.sendPluginMessage(this, "velocitychatsync:main", messageBytes);
        } else {
            getLogger().warning("No online player to send plugin message: " + message);
        }
    }

}