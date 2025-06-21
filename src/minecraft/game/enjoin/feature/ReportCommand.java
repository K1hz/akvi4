package minecraft.game.enjoin.feature;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import minecraft.game.advantage.advisee.ClientReceive;
import minecraft.game.enjoin.interfaces.Command;
import minecraft.game.enjoin.interfaces.Logger;
import minecraft.game.enjoin.interfaces.MultiNamedCommand;
import minecraft.game.enjoin.interfaces.Parameters;
import minecraft.system.AG;
import minecraft.system.ClientInfo;
import net.minecraft.util.text.TextFormatting;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReportCommand implements Command, MultiNamedCommand {
    Logger logger;
    private static final Map<String, Long> userLastReportTime = new HashMap<>();


    @Override
    public void execute(Parameters parameters) {
        String username = getUserName();

        if (userLastReportTime.containsKey(username)) {
            long lastReport = userLastReportTime.get(username);
            long timePassed = System.currentTimeMillis() - lastReport;

            if (timePassed < 120000) {
                long remainingTime = 120000 - timePassed;
                long secondsRemaining = remainingTime / 1000;
                logger.log(TextFormatting.RED + "Подождите " + TextFormatting.GREEN + secondsRemaining + TextFormatting.RED + " секунд(ы) перед повторной отправкой.");
                return;
            }
        }

        String message = parameters.collectMessage(0).trim();
        if (message.isEmpty()) {
            sendError();
            return;
        }

        String formattedMessage = formatMessage(message);
        sendReport(formattedMessage);

        userLastReportTime.put(username, System.currentTimeMillis());
    }



    private String formatMessage(String reason) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String username = getUserName();
        String pcName = System.getProperty("user.name");
        String userIp = getUserIp();
        String serverIp = ClientReceive.getServerIP();
        String version1 = ClientInfo.version;
        String versionMinecraft1 = ClientInfo.minecraft;
        String suggest1 = ClientInfo.suggest;



        return String.format(
                "**⏰ Время:** %s\n" +
                        "**Пользователь:** %s\n" +
                        "**Имя пк:** %s\n" +
                        "**IP Сервера:** %s\n" +
                        "**Версия клиента:** %s\n" +
                        "**Версия игры:** %s\n" +
                        "**Версия пользователя:** %s\n" +
                        "**Причина жалобы:** %s",
                timestamp, username, pcName, serverIp, version1, versionMinecraft1, suggest1, reason
        );
    }

    private String getUserName() {
        return PassCommand.currentUser != null ? PassCommand.currentUser : "Unknown";
    }

    private String getUserIp() {
        try {
            URL url = new URL("https://api.ipify.org");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            try (InputStreamReader reader = new InputStreamReader(conn.getInputStream())) {
                StringBuilder response = new StringBuilder();
                int charRead;
                while ((charRead = reader.read()) != -1) {
                    response.append((char) charRead);
                }
                return response.toString();
            }
        } catch (Exception e) {
            return "Не удалось определить";
        }
    }

    private String getServerIp() {
        return "localhost";
    }

    private void sendReport(String message) {
    }


    public void sendMessage(String webhookUrl, String message) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(webhookUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonPayload = String.format("{\"content\": \"%s\"}", message.replace("\"", "\\\"").replace("\n", "\\n"));

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                throw new RuntimeException("Ошибка: " + responseCode);
            }
        } catch (Exception e) {
            logger.log(TextFormatting.RED + "Ошибка при отправке запроса: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private void sendError() {
        logger.log(TextFormatting.RED + "Ошибка в использовании:");
        logger.log(TextFormatting.GRAY + "Используйте .report Текст");
        logger.log(TextFormatting.GREEN + "Пример: .report функция speed не работает!");
    }

    @Override
    public String name() {
        return "report";
    }

    @Override
    public String description() {
        return "Отправляет баги в клиенте.";
    }

    @Override
    public List<String> aliases() {
        return Collections.singletonList("bugreport");
    }
}
