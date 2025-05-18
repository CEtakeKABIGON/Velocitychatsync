package org.cetake.velocitychatsyncVelocity;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import javax.inject.Inject;
import java.awt.*;
import java.util.List;
import java.util.Optional;

public class DiscordConnect extends ListenerAdapter {
    private final Logger logger;

    private final ConfigManager configManager;
    private JDA jda;
    // ChatManager はセッターで設定する
    private ChatManager chatManager;

    @Inject
    public DiscordConnect(Logger logger, ConfigManager configManager) {
        this.logger = logger;
        this.configManager = configManager;
    }

    /**
     * ChatManager のインスタンスを設定するセッター
     */
    public void setChatManager(ChatManager chatManager) {
        this.chatManager = chatManager;
    }

    /**
     * Discord Bot の起動メソッド
     */
    public void startDiscordBot() {
        if (!configManager.isDiscordEnabled()) {
            logger.info("Discord integration is disabled.");
            return;
        }
        String token = configManager.getDiscordToken();
        if (token == null || token.isEmpty()) {
            logger.error("Discord token is null or empty!");
            return;
        }
        try {
            jda = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.MESSAGE_CONTENT,
                            GatewayIntent.GUILD_MEMBERS)
                    .addEventListeners(this)
                    .setActivity(Activity.playing("Bot is online!"))
                    .setMemberCachePolicy(MemberCachePolicy.ALL) // ← これも！
                    .build();
            logger.info("Connected to Discord successfully!");
        } catch (Exception e) {
            logger.error("Failed to connect to Discord: " + e.getMessage(), e);
        }
    }

    @Override
    public void onReady(ReadyEvent event) {
        messageSendToOtherChannels("dummy", "✅ サーバー が起動しました");
    }

    /**
     * Discord Bot のシャットダウンメソッド
     */
    public void shutdownDiscordBot() {
        if (jda != null) {
            jda.shutdown();
            logger.info("Discord bot shutdown successfully.");
        }
    }

    /**
     * 指定されたチャンネルIDを除外してメッセージを送信するメソッド
     */
    public void sendToOtherChannels(String excludedChannelId, String message, String colorCode) {
        if (jda == null) return;
        Color color = Color.decode(colorCode);
        List<ConfigManager.DiscordServer> discordServers = configManager.getDiscordServers();
        for (ConfigManager.DiscordServer discordServer : discordServers) {
            String targetChannelId = discordServer.getChannelId();
            if (!targetChannelId.equals(excludedChannelId)) {
                TextChannel channel = jda.getTextChannelById(targetChannelId);
                if (channel != null) {
                    EmbedBuilder embed = new EmbedBuilder()
                            .setDescription(message)
                            .setColor(color);
                    channel.sendMessageEmbeds(embed.build()).queue();
                }
            }
        }
    }

    public void messageSendToOtherChannels(String excludedChannelId, String message) {
        if (jda == null) return;
        List<ConfigManager.DiscordServer> discordServers = configManager.getDiscordServers();
        for (ConfigManager.DiscordServer discordServer : discordServers) {
            String targetChannelId = discordServer.getChannelId();
            if (!targetChannelId.equals(excludedChannelId)) {
                TextChannel channel = jda.getTextChannelById(targetChannelId);
                if (channel != null) {
                    channel.sendMessage(message).queue();
                }
            }
        }
    }

    /**
     * Discord でメッセージを受信した際の処理
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // ボット自身のメッセージは無視する
        if (event.getAuthor().isBot()) {
            return;
        }

        if (event.isFromGuild()) {
            Message message = event.getMessage();
            String content = message.getContentRaw();
            User user = message.getAuthor();
            Guild guild = event.getGuild();
            Member member = guild.getMember(user);
            // ユーザーネーム（Discord全体でのユーザー名）
            String userName = user.getName();
            // サーバーネーム（サーバーで設定されたニックネーム）
            String serverUserName = (member != null) ? member.getEffectiveName() : "未設定";

            String channelId = event.getChannel().getId();

            // ユーザーメンションの置換
            for (Member mentionedMember : message.getMentions().getMembers()) {
                String mentionTag = "<@!" + mentionedMember.getId() + ">";
                String displayName = mentionedMember.getEffectiveName();
                content = content.replace(mentionTag, "@" + displayName);

                // 一部のクライアントでは <@ID> 形式になる場合もあるので対応
                mentionTag = "<@" + mentionedMember.getId() + ">";
                content = content.replace(mentionTag, "@" + displayName);
            }


            // ロールメンションの置換
            for (Role mentionedRole : message.getMentions().getRoles()) {
                String mentionTag = "<@&" + mentionedRole.getId() + ">";
                content = content.replace(mentionTag, "@" + mentionedRole.getName());
            }

// チャンネルメンションの置換
            for (GuildChannel mentionedChannel : message.getMentions().getChannels()) {
                String mentionTag = "<#" + mentionedChannel.getId() + ">";
                content = content.replace(mentionTag, "#" + mentionedChannel.getName());
            }


            // 設定されたチャンネルIDのリストを取得
            List<String> allowedChannelIds = configManager.getDiscordServers().stream()
                    .map(ConfigManager.DiscordServer::getChannelId)
                    .toList();

            // 許可されたチャンネルでなければ無視
            if (!allowedChannelIds.contains(channelId)) {
                return;
            }

            // 対応するサーバー名を取得
            Optional<String> serverNameOpt = configManager.getDiscordServers().stream()
                    .filter(discordServer -> discordServer.getChannelId().equals(channelId))
                    .map(ConfigManager.DiscordServer::getServerName)
                    .findFirst();
            String serverName = serverNameOpt.orElse("Unknown");

            // ChatManager のインスタンスが存在する場合のみブロードキャストを実行
            if (chatManager != null) {
                if (serverUserName.equals("未設定")) {
                    chatManager.discordBroadcastMessage(serverName, userName, content, channelId);
                }else{
                    chatManager.discordBroadcastMessage(serverName, serverUserName, content, channelId);
                }

            } else {
                logger.warn("chatManager is null. Unable to broadcast Discord message to Minecraft servers.");
            }
        }
    }


}
