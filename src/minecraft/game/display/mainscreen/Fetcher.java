package minecraft.game.display.mainscreen;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Fetcher {
    public static String fetchText(String pastebinUrl) {
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL(pastebinUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return "Ошибка загрузки: " + responseCode;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Вывод ошибки в консоль
            return "Ошибка загрузки текста: " + e.getMessage();
        }
        return result.toString();
    }
}
