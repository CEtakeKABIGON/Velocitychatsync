package org.cetake.velocitychatsyncVelocity;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class VelocitySyncAdminCommand implements SimpleCommand {
    private final ProxyServer server;
    private final ConfigManager configManager;
    private final MessageManager messageManager;
    private final VelocityPlayerSettingsManager settingsManager;

    public VelocitySyncAdminCommand(ConfigManager configManager,
                                    MessageManager messageManager,
                                    VelocityPlayerSettingsManager settingsManager,
                                    ProxyServer server) {
        this.server = server;
        this.configManager = configManager;
        this.messageManager = messageManager;
        this.settingsManager = settingsManager;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length != 1) {
            source.sendMessage(Component.text("使用法: /vcsyncadmin <reload|status>"));
            return;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "reload" -> {
                configManager.loadConfig();
                messageManager.loadMessageCustom();
                settingsManager.load();
                source.sendMessage(Component.text("設定とメッセージを再読み込みしました。"));
            }

            case "status" -> {
                if (!(source instanceof Player player)) {
                    source.sendMessage(Component.text("プレイヤーのみが /vcsyncadmin status を実行できます。"));
                    return;
                }

                UUID uuid = player.getUniqueId();
                if (!settingsManager.has(uuid)) {
                    source.sendMessage(Component.text("まだ個別設定が存在しません。すべてONとして扱われます。"));
                    return;
                }

                var s = settingsManager.getSettings(uuid);
                source.sendMessage(Component.text(String.format("チャット: %s, 実績: %s, 死亡ログ: %s",
                        s.chat ? "ON" : "OFF",
                        s.advancements ? "ON" : "OFF",
                        s.deathLog ? "ON" : "OFF")));
            }

            case "apply" -> {
                int applied = 0;
                for (Player player : server.getAllPlayers()) {
                    UUID uuid = player.getUniqueId();
                    if (!settingsManager.has(uuid)) continue;

                    String message = settingsManager.createPluginMessage(uuid);
                    player.getCurrentServer().ifPresent(conn -> {
                        conn.sendPluginMessage(Velocitychatsync.SETTINGS_CHANNEL, message.getBytes(StandardCharsets.UTF_8));
                    });
                    applied++;
                }
                source.sendMessage(Component.text(String.format("%d人のプレイヤーに設定を送信しました。", applied)));
            }


            default -> {
                source.sendMessage(Component.text("不明なサブコマンドです。reload または status を指定してください。"));
            }
        }
    }
}
