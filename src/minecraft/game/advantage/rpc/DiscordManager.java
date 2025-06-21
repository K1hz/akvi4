package minecraft.game.advantage.rpc;

import lombok.Getter;
import minecraft.game.advantage.advisee.ClientReceive;
import minecraft.game.enjoin.feature.PassCommand;
import minecraft.system.AG;
import minecraft.system.ClientInfo;

@Getter
public class DiscordManager {

    private DiscordDaemonThread discordDaemonThread;
    private long APPLICATION_ID;
    private boolean running;
    private String imageKey;
    private String lastIP = "";
    private long startTime;

    private static final String GIF_IMAGE_KEY = "https://i.postimg.cc/63nG7gPn/0001-0220.gif";
    private static final String FUN_TIME_ICON_URL = "https://i.imgur.com/F0ZIcuD.png";
    private static final String REALLY_WORLD_ICON_URL = "https://i.imgur.com/9IRxmts.png";
    private static final String HOLY_WORLD_ICON_URL = "https://i.imgur.com/FqEcDAY.png";

    private void cppInit() {
        discordDaemonThread = new DiscordDaemonThread();
        APPLICATION_ID = 1298306537244069972L;
        running = true;
        imageKey = GIF_IMAGE_KEY;
        startTime = System.currentTimeMillis() / 1000;
    }

    public void init() {
        cppInit();
        startRPC();
        discordDaemonThread.start();
    }

    public void stopRPC() {
        DiscordRPC.INSTANCE.Discord_Shutdown();
        discordDaemonThread.interrupt();
        this.running = false;
    }

    public void startRPC() {
        DiscordRichPresence.Builder builder = new DiscordRichPresence.Builder();
        DiscordEventHandlers handlers = new DiscordEventHandlers.Builder().build();
        DiscordRPC.INSTANCE.Discord_Initialize(String.valueOf(APPLICATION_ID), handlers, true, "");
        builder.setStartTimestamp(startTime);

        updatePresence(builder);
    }

    public void updatePresence(DiscordRichPresence.Builder builder) {
        String username = PassCommand.currentUser;
        String uid = PassCommand.currentUid;
        String ipOrState = ClientReceive.getServerIP();
        String userInfo = "User: " + username + " | Role: " + uid;

        if (ipOrState.equals("mainmenu")) {
            builder.setDetails(userInfo);
            builder.setState("In Main Menu");
            builder.setLargeImage(imageKey, "Version " + ClientInfo.version);
            builder.setSmallImage(imageKey, "discord.gg/aegisclient");
        } else if (ipOrState.equals("local")) {
            builder.setDetails(userInfo);
            builder.setState("Playing on server: localhost");
            builder.setLargeImage(imageKey, "Version " + ClientInfo.version);
            builder.setSmallImage(imageKey, "discord.gg/aegisclient");
        } else if (!ipOrState.isEmpty()) {
            builder.setDetails(userInfo);
            builder.setState("Playing on Server: " + ipOrState);

            if (ipOrState.contains("funtime.su")) {
                builder.setSmallImage(FUN_TIME_ICON_URL, "FunTime Server");
            } else if (ipOrState.contains("mc.reallyworld.ru")) {
                builder.setSmallImage(REALLY_WORLD_ICON_URL, "ReallyWorld Server");
            } else if (ipOrState.contains("start.holyworld.ru")) {
                builder.setSmallImage(HOLY_WORLD_ICON_URL, "HolyWorld Server");
            } else {
                builder.setSmallImage(imageKey, "Default Icon");
            }

            builder.setLargeImage(imageKey, "Version " + ClientInfo.version);
        } else {
            builder.setDetails(userInfo);
            builder.setState("");
            builder.setLargeImage(imageKey, "Idle");
            builder.setSmallImage(imageKey, "Idle Icon");
        }

        builder.setStartTimestamp(startTime);
        DiscordRPC.INSTANCE.Discord_UpdatePresence(builder.build());
    }

    private class DiscordDaemonThread extends Thread {
        @Override
        public void run() {
            this.setName("Discord-RPC");

            try {
                while (running) {
                    String currentIP = ClientReceive.getServerIP();
                    if (currentIP == null) currentIP = "";
                    if (lastIP == null) lastIP = "";
                    if (!currentIP.equals(lastIP)) {
                        lastIP = currentIP;
                        DiscordRichPresence.Builder builder = new DiscordRichPresence.Builder();
                        updatePresence(builder);
                    }

                    DiscordRichPresence.Builder builder = new DiscordRichPresence.Builder();
                    updatePresence(builder);

                    DiscordRPC.INSTANCE.Discord_RunCallbacks();
                    Thread.sleep(150); // Обновление раз в 2 секунды

                }
            } catch (Exception exception) {
                stopRPC();
            }
        }
    }
}
