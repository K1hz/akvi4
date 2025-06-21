package minecraft.game.enjoin.feature;

import minecraft.game.enjoin.interfaces.Command;
import minecraft.game.enjoin.interfaces.Logger;
import minecraft.game.enjoin.interfaces.MultiNamedCommand;
import minecraft.game.enjoin.interfaces.Parameters;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PassCommand implements Command, MultiNamedCommand {

    final Logger logger;
    public static String currentUser = "None";
    public static String currentUid = "None";

    @Override
    public String name() {
        return "key";
    }

    public String getUsers() {
        return "User: " + currentUser + ", UID: " + currentUid;
    }

    @Override
    public String description() {
        return "Проверяет введенный ключ и обновляет данные User и UID.";
    }

    @Override
    public List<String> aliases() {
        return List.of("pass");
    }

    @Override
    public void execute(Parameters parameters) {

        String inputKey = parameters.asString(0).orElse("");
        if (inputKey.isEmpty()) {
            logger.log("Ошибка: введите ключ. Пример: (.key [или .pass] ваш ключ)");
            return;
        }


        List<PastebinEntry> validEntries = fetchKeysFromPastebin("https://pastebin.com/raw/ZercX41m");

        if (validEntries == null) {
            logger.log("Ошибка: не удалось загрузить данные.");
            return;
        }

        boolean keyFound = false;
        for (PastebinEntry entry : validEntries) {
            if (entry.getPass().equals(inputKey)) {

                currentUser = entry.getUser();
                currentUid = entry.getUid();
                logger.log("PASS верный, приятной игры. " + currentUser);
                keyFound = true;
                break;
            }
        }

        if (!keyFound) {

            currentUser = "None";
            currentUid = "None";
            logger.log("PASS не верный");
        }
    }


    private List<PastebinEntry> fetchKeysFromPastebin(String urlString) {
        List<PastebinEntry> entries = new ArrayList<>();
        try {

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);


            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {

                    if (line.contains("User:") && line.contains("Uid:") && line.contains("Pass:")) {
                        String user = extractValue(line, "User:");
                        String uid = extractValue(line, "Uid:");
                        String pass = extractValue(line, "Pass:");
                        if (!user.isEmpty() && !uid.isEmpty() && !pass.isEmpty()) {
                            entries.add(new PastebinEntry(user, uid, pass));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return entries;
    }

    private String extractValue(String line, String key) {

        Pattern pattern = Pattern.compile(key + "\\s*([^,]+)");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return ""; // Если не нашли - юзер люто сосет
    }


    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class PastebinEntry {
        String user;
        String uid;
        String pass;

        PastebinEntry(String user, String uid, String pass) {
            this.user = user;
            this.uid = uid;
            this.pass = pass;
        }

        public String getUser() {
            return user;
        }

        public String getUid() {
            return uid;
        }

        public String getPass() {
            return pass;
        }
    }
}
