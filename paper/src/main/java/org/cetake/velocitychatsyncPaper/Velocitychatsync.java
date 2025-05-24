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
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "velocitychatsync:settings");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "velocitychatsync:settings", this);
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onPluginMessageReceived(String channel, @NotNull Player player, byte[] message) {
        String receivedMessage = new String(message, StandardCharsets.UTF_8);
        switch (channel) {
            case "velocitychatsync:main" ->
                messageBroadcast(receivedMessage);

            case "velocitychatsync:settings" ->
                PaperPlayerSettingsManager.setPlayerSyncSettings(receivedMessage);

            default -> throw new IllegalStateException("Unexpected value: " + channel);
        }

    }

    private void messageBroadcast(String message) {
        String[] parts = message.split("\\|", 2);

        Component formatMessage = mm.deserialize(message);
        if (parts.length == 2) {
            formatMessage = mm.deserialize(parts[1]);
        }

        switch (parts[0]) {
            case "chat" -> {
                for(Player player : Bukkit.getOnlinePlayers()){
                    if(PaperPlayerSettingsManager.getChatEnable(player.getUniqueId())){
                        player.sendMessage(formatMessage);
                    }
                }
            }

            case "deathLog" -> {
                for(Player player : Bukkit.getOnlinePlayers()){
                    if(PaperPlayerSettingsManager.getDeathLogEnable(player.getUniqueId())){
                        player.sendMessage(formatMessage);
                    }
                }
            }

            case "advancement" -> {
                for(Player player : Bukkit.getOnlinePlayers()){
                    if(PaperPlayerSettingsManager.getAdvancementsEnable(player.getUniqueId())){
                        player.sendMessage(formatMessage);
                    }
                }
            }

            default -> {
                Bukkit.broadcast(formatMessage);
            }
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