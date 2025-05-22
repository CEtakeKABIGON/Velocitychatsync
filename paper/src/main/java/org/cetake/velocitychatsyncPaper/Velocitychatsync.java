package org.cetake.velocitychatsyncPaper;

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

public class Velocitychatsync extends JavaPlugin implements Listener, PluginMessageListener {

    MiniMessage mm = MiniMessage.miniMessage();

    @Override
    public void onEnable() {
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "velocitychatsync:main");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "velocitychatsync:main", this);
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onPluginMessageReceived(String channel, @NotNull Player player, byte[] message) {
        String receivedMessage = new String(message, StandardCharsets.UTF_8);
        Component formatMessage = mm.deserialize(receivedMessage);

        switch (channel) {
            case "velocitychatsync:main" ->
                Bukkit.broadcast(formatMessage);

            case "velocitychatsync:setting" ->
                PaperPlayerSettingsManager.setPlayerSyncSettings(receivedMessage);

        }

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

        sendToVelocity("Advancements", sendData);
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