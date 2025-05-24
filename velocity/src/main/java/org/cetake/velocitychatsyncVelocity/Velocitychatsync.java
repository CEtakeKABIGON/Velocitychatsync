package org.cetake.velocitychatsyncVelocity;

import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.slf4j.Logger;

import javax.inject.Inject;

@Plugin(id = "velocitychatsync", name = "VelocityChatSync", version = "4.0")
public class Velocitychatsync {
    private final Logger logger;
    private final ConfigManager configManager;
    private final ProxyServer server;
    private DiscordConnect discordConnect;
    private ChatManager chatManager;
    private final MessageManager messageManager;
    private SystemLogManager systemLogManager;
    public static final MinecraftChannelIdentifier SETTINGS_CHANNEL =
            MinecraftChannelIdentifier.create("velocitychatsync", "settings");

    @Inject
    public Velocitychatsync(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
        this.configManager = new ConfigManager(logger);
        this.messageManager = new MessageManager(logger);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("VelocityChatsync Plugin Enabled!");

        // ConfigManager を生成
        this.discordConnect = new DiscordConnect(logger, configManager);
        this.chatManager = new ChatManager(server, logger, configManager, messageManager, discordConnect);
        this.systemLogManager = new SystemLogManager(server, logger, configManager, messageManager, discordConnect);
        server.getChannelRegistrar().register(MinecraftChannelIdentifier.create("velocitychatsync", "main"));
        server.getChannelRegistrar().register(SETTINGS_CHANNEL);

        VelocityPlayerSettingsManager settingsManager = new VelocityPlayerSettingsManager(logger);
        server.getCommandManager().register("vcsync", new VelocitySyncCommand(settingsManager));
        server.getCommandManager().register("vcsyncadmin", new VelocitySyncAdminCommand(configManager, messageManager, settingsManager, server));
        // イベントリスナーの登録
        server.getEventManager().register(this, discordConnect);
        server.getEventManager().register(this, chatManager);
        server.getEventManager().register(this, systemLogManager);

        // DiscordConnect に ChatManager をセットして循環依存を解決
        discordConnect.setChatManager(chatManager);
        systemLogManager.setChatManager(chatManager);

        // DiscordBotの起動
        if (configManager.isDiscordEnabled()) {
            discordConnect.startDiscordBot();
        }
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (discordConnect != null) {
            discordConnect.messageSendToOtherChannels("dummy", "🛑 サーバー が停止しました");
            discordConnect.shutdownDiscordBot();
        }
    }
}
