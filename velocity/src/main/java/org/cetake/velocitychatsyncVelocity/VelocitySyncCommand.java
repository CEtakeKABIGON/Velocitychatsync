package org.cetake.velocitychatsyncVelocity;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public class VelocitySyncCommand implements SimpleCommand {

    private final VelocityPlayerSettingsManager settingsManager;

    public VelocitySyncCommand(VelocityPlayerSettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!(source instanceof Player player)) {
            source.sendMessage(Component.text("このコマンドはプレイヤーのみ使用可能です"));
            return;
        }

        if (args.length != 1) {
            source.sendMessage(Component.text("使用法: /vcsync <chat|advancements|death>"));
            return;
        }

        String type = args[0].toLowerCase();
        UUID uuid = player.getUniqueId();

        if (!type.equals("chat") && !type.equals("advancements") && !type.equals("death")) {
            source.sendMessage(Component.text("有効な設定は: chat, advancements, death"));
            return;
        }

        settingsManager.toggleSetting(uuid, type);
        boolean newValue;
        switch (type) {
            case "chat" -> newValue = settingsManager.getSettings(uuid).chat;
            case "advancements" -> newValue = settingsManager.getSettings(uuid).advancements;
            case "death" -> newValue = settingsManager.getSettings(uuid).deathLog;
            default -> newValue = true;
        }

        source.sendMessage(Component.text(String.format("%s の同期設定を %s に変更しました", type, newValue ? "ON" : "OFF")));

        // PluginMessage を作成して送信（対象Paperサーバーに）
        String message = settingsManager.createPluginMessage(uuid);
        player.getCurrentServer().ifPresent(serverConn ->
                serverConn.sendPluginMessage(
                        Velocitychatsync.SETTINGS_CHANNEL,
                        message.getBytes()
                )
        );
    }
}

