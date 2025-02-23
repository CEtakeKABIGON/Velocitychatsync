これはvelocityプロキシ間のチャットを同期します  
DiscordBotを使ってDIscordチャンネルとの同期も可能です  
メッセージのカスタマイズや色の設定も可能です  
Velocityに導入し、起動するとconfig.yml MessageCustom.ymlが生成されるので設定して再起動してください  
  
messageCustom: false　#メッセージのカスタマイズの有効化  
  
discord:  
      enable: false #Discord連携の有効化  
      token: "YOUR_DISCORD_BOT_TOKEN" #DiscordBotのTokenを入力  
      discord_servers: #必要に応じて追加  
        - server_name: "Discordサーバー1" #チャンネルの表示名を設定  
          channel_id: "123456789012345678"  # Discord サーバー1のチャンネルID  
        - server_name: "Discordサーバー2" #チャンネルの表示名を設定  
          channel_id: "234567890123456789"  # Discord サーバー2のチャンネルID  
        - server_name: "Discordサーバー3" #チャンネルの表示名を設定  
          channel_id: "345678901234567890"  # Discord サーバー3のチャンネルID  
#サーバー名の配列  
#必要に応じて追加  
#velocity.tomlに設定したサーバー名と同じものを設定  
servers:  
  - server1  
  - server2  
  - server3  
