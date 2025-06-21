import net.minecraft.client.main.Main;

import java.util.Arrays;

public class Start {
    public static void main(String[] args) {
        String assets = System.getenv().containsKey("assetDirectory") ? System.getenv("assetDirectory") : "assets";

        String[] jvmArgs = new String[]{
                "-Xmx4G"
        };
        Main.main(concat(jvmArgs, concat(
                new String[] {
                        "--version", "OptiFine 1.16.5",
                        "--accessToken", "0",
                        "--assetsDir", assets,
                        "--assetIndex", "1.16",
                        "--userProperties", "{}",
                },
                args
        )));
    }

    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
