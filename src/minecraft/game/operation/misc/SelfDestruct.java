package minecraft.game.operation.misc;

import com.google.common.eventbus.Subscribe;
import minecraft.game.advantage.figures.Vector4i;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.make.other.KawaseBlur;
import minecraft.game.advantage.rpc.DiscordManager;
import minecraft.game.advantage.words.font.ClientFonts;
import minecraft.game.transactions.EventRender2D;
import minecraft.game.transactions.EventUpdate;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector4f;
import net.optifine.shaders.Shaders;
import org.apache.commons.lang3.RandomStringUtils;

import minecraft.system.AG;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.advantage.figures.TimeCounterSetting;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Defuse(name = "Un Hook",description = "Скрывает чит", brand = Category.Misc)
public class SelfDestruct extends Module {

    public static boolean unhooked = false;
    public String secret = RandomStringUtils.randomAlphabetic(3).toLowerCase(Locale.ROOT);
    public TimeCounterSetting TimeCounterSetting = new TimeCounterSetting();
    private long startTime;
    private final int selfDestructTime = 15;

    @Override
    public void onEnable() {
        super.onEnable();
        TimeCounterSetting.reset();
        startTime = System.currentTimeMillis();
        unhooked = true;
    }

    public List<Module> saved = new ArrayList<>();

    @Subscribe
    public void onUpdate(EventUpdate event) {
        if (!unhooked) return;

        long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
        long remainingTime = Math.max(0, selfDestructTime - elapsedTime);

        if (remainingTime <= 0) {
            mc.ingameGUI.getChatGUI().clearChatMessages(false);
            toggle();
            process();
        }
    }

    public void process() {
        AG.getInst().getDiscordManager().stopRPC();
        for (Module module : AG.getInst().getModuleManager().getModules()) {
            if (module == this) continue;
            if (module.isEnabled()) {
                saved.add(module);
                module.setEnabled(false, false);
            }
        }
        mc.fileResourcepacks = new File(System.getenv("appdata") + "\\.minecraft" + "\\resourcepacks");
        Shaders.shaderPacksDir = new File(System.getenv("appdata") + "\\.minecraft" + "\\shaderpacks");
        File folder = new File("C:\\system");
        hiddenFolder(folder, true);
    }

    @Subscribe
    public void onRender(EventRender2D r) {

        long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
        long remainingTime = Math.max(0, selfDestructTime - elapsedTime);
        if (remainingTime <= 0) return;
        int centerX = Minecraft.getInstance().getMainWindow().getScaledWidth() / 2;
        int centerY = Minecraft.getInstance().getMainWindow().getScaledHeight() / 2;

        KawaseBlur.blur.render(() -> {
            GraphicsSystem.drawRoundedRect(centerX - 126, centerY - 195 , 256, 100, new Vector4f(15, 15, 15, 15), new Vector4i(
                    ColoringSystem.setAlpha(ColoringSystem.rgb(11, 11, 11), 145),
                    ColoringSystem.setAlpha(ColoringSystem.rgb(34, 34, 34), 145),
                    ColoringSystem.setAlpha(ColoringSystem.rgb(11, 11, 11), 145),
                    ColoringSystem.setAlpha(ColoringSystem.rgb(34, 34, 34), 145)
            ));
        });
        GraphicsSystem.drawRoundedRect(centerX - 126, centerY - 195 , 256, 100, new Vector4f(5, 5, 5, 5), new Vector4i(
                ColoringSystem.setAlpha(ColoringSystem.rgb(11, 11, 11), 145),
                ColoringSystem.setAlpha(ColoringSystem.rgb(34, 34, 34), 145),
                ColoringSystem.setAlpha(ColoringSystem.rgb(11, 11, 11), 145),
                ColoringSystem.setAlpha(ColoringSystem.rgb(34, 34, 34), 145)
        ));
        ClientFonts.clickGui[23].drawCenteredString(r.getMatrixStack(), "В данный момент чит само-уничтожается", centerX + 3, centerY - 189, ColoringSystem.rgba(35,35,35, 175));
        ClientFonts.clickGui[23].drawCenteredString(r.getMatrixStack(), "До полной очистки осталось: " + remainingTime + " сек.", centerX + 3, centerY - 177, ColoringSystem.rgba(35,35,35, 175));
        ClientFonts.clickGui[23].drawCenteredString(r.getMatrixStack(), "Код чтобы вернуть чит: " + secret, centerX + 3, centerY - 165, ColoringSystem.rgba(35,35,35, 175));
        ClientFonts.clickGui[23].drawCenteredString(r.getMatrixStack(), "В данный момент чит само-уничтожается", centerX , centerY - 190, -1);
        ClientFonts.clickGui[23].drawCenteredString(r.getMatrixStack(), "До полной очистки осталось: " + remainingTime + " сек.", centerX, centerY - 178, -1);
        ClientFonts.clickGui[23].drawCenteredString(r.getMatrixStack(), "Код чтобы вернуть чит: " + secret, centerX, centerY - 166, -1);

        float angle = (System.currentTimeMillis() / 2) % 360;
        GraphicsSystem.drawCircleFilled(centerX + 3, centerY - 124, angle, angle + 180, 18, 7.0f, false, ColoringSystem.rgba(35,35,35, 175));
        GraphicsSystem.drawCircleFilled(centerX, centerY - 125, angle, angle + 180, 18, 7.0f, false, ColoringSystem.rgb(255, 255, 255));

    }




    public void hook() {
        AG.getInst().getDiscordManager().startRPC();
        for (Module module : saved) {
            if (module == this) continue;
            if (!module.isEnabled()) {
                module.setEnabled(true, false);
            }
        }
        File folder = new File(Minecraft.getInstance().gameDir, "\\saves\\files");
        hiddenFolder(folder, false);
        unhooked = false;
    }

    private void hiddenFolder(File folder, boolean hide) {
        if (folder.exists()) {
            try {
                Path folderPathObj = folder.toPath();
                DosFileAttributeView attributes = Files.getFileAttributeView(folderPathObj, DosFileAttributeView.class);
                attributes.setHidden(true);
            } catch (IOException e) {
                System.out.println("Не удалось скрыть папку: " + e.getMessage());
            }
        }
    }
}
