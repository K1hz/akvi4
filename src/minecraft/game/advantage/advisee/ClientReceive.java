package minecraft.game.advantage.advisee;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import net.minecraft.client.gui.screen.*;
import minecraft.game.display.mainscreen.AltScreen;
import minecraft.game.advantage.figures.Vector2i;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.SUpdateBossInfoPacket;
import net.minecraft.util.StringUtils;

import minecraft.system.AG;
import minecraft.game.display.mainscreen.MainScreen;
import org.lwjgl.glfw.GLFW;

import java.time.LocalTime;
import java.util.UUID;

@UtilityClass
public class ClientReceive implements IMinecraft {
    private static boolean hasCheckedMM = false;
    private static float volumeMM = 0;
    public static String getHWID() {
        String hwid = System.getProperty("os.name") +
                System.getProperty("user.name") +
                Runtime.getRuntime().availableProcessors() +
                System.getProperty("os.arch") +
                System.getenv("COMPUTERNAME");

        return hashString(hwid);
    }
    public static boolean isMainMenuOpen() {
        Minecraft mc = Minecraft.getInstance();
        Screen currentScreen = mc.currentScreen;

        return currentScreen instanceof MainScreen;
    }
    public String getServerIP() {
        if (mc.world == null) return "mainmenu";

        if (mc.isSingleplayer()) return "local";

        if (mc.getCurrentServerData() != null) {
            return mc.getCurrentServerData().serverIP.toLowerCase();
        }

        return "";
    }

    private static String hashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getUsername() {
        return System.getProperty("user.name");
    }

    public String pasteString() {
        return GLFW.glfwGetClipboardString(mc.getMainWindow().getHandle());
    }

    public boolean ctrlIsDown() {
        return GLFW.glfwGetKey(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
    }

    public static String getGreetingMessage() {
        LocalTime currentTime = LocalTime.now();

        String greeting;
        if (currentTime.isBefore(LocalTime.NOON)) {
            greeting = "Доброе утро";
        } else if (currentTime.isBefore(LocalTime.of(18, 0))) {
            greeting = "Добрый день";
        } else if (currentTime.isBefore(LocalTime.of(22, 0))) {
            greeting = "Добрый вечер";
        } else {
            greeting = "Доброй ночи";
        }
        return greeting;
    }

    private static boolean pvpMode;
    private static UUID uuid;
    public static String state = "";

    public void updateBossInfo(SUpdateBossInfoPacket packet) {
        if (packet.getOperation() == SUpdateBossInfoPacket.Operation.ADD) {
            if (StringUtils.stripControlCodes(packet.getName().getString()).toLowerCase().contains("pvp")) {
                pvpMode = true;
                uuid = packet.getUniqueId();
            }
        } else if (packet.getOperation() == SUpdateBossInfoPacket.Operation.REMOVE) {
            if (packet.getUniqueId().equals(uuid))
                pvpMode = false;
        }
    }
    public boolean isConnectedToServer(String ip) {
        return mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP != null && mc.getCurrentServerData().serverIP.contains(ip);
    }
    public boolean isPvP() {
        return pvpMode;
    }

    public int calc(int value) {
        MainWindow rs = mc.getMainWindow();
        return (int) (value * rs.getGuiScaleFactor() / 2);
    }

    public Vector2i getMouse(int mouseX, int mouseY) {
        return new Vector2i((int) (mouseX * Minecraft.getInstance().getMainWindow().getGuiScaleFactor() / 2), (int) (mouseY * Minecraft.getInstance().getMainWindow().getGuiScaleFactor() / 2));
    }
}
