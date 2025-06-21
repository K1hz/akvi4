package minecraft.game.operation.visual;

import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import minecraft.system.AG;
import minecraft.game.transactions.EventPacket;
import minecraft.game.transactions.EventUpdate;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.misc.SelfDestruct;
import minecraft.game.operation.wamost.massa.elements.*;
import minecraft.game.advantage.make.color.ColoringSystem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.server.SUpdateTimePacket;
import net.minecraft.util.ResourceLocation;

@Defuse(name = "Ambience",description = "Изменяет мир", brand = Category.Visual)
public class WorldTweaks extends Module {

    public static ModeListSetting options = new ModeListSetting("Опции",
            new CheckBoxSetting("Визуальный скин", true),
            new CheckBoxSetting("Изменять цвет неба", true),
            new CheckBoxSetting("Кастомный туман", true),
            new CheckBoxSetting("Своя дистанция тумана", true),
            new CheckBoxSetting("Физика предметов", true),
            new CheckBoxSetting("Время", true)
    );

    public static SliderSetting fogDistance = new SliderSetting("Дистация тумана", 0.4f, 0.1f, 1, 0.1f).visibleIf(() -> options.is("Своя дистанция тумана").getValue());

    public static ModeSetting mode = new ModeSetting("Вид", "Клиент", "Клиент", "Свой").visibleIf(() -> options.is("Кастомный туман").getValue());

    public static ColorSetting colorFog = new ColorSetting("Цвет тумана", ColoringSystem.rgb(255, 255, 255)).visibleIf(() -> mode.is("Свой"));
    public static ModeSetting skyMode = new ModeSetting("Вид", "Клиент", "Клиент", "Свой").visibleIf(() -> options.is("Изменять цвет неба").getValue());
    public static ColorSetting skyColor = new ColorSetting("Изменять цвет неба", ColoringSystem.rgb(255, 255, 255)).visibleIf(() -> skyMode.is("Свой"));

    private static ModeSetting selfSkinType = new ModeSetting("Текстура", "HZeed", "HZeed","Boy", "Boy2", "Boy3", "Boy4", "Girl", "Girl2", "Girl3", "Sonic", "Sonic2", "Sonic3", "Shadow", "Knuckles", "Pink", "Pink2", "Pink3", "Pink4", "Pink5", "Sora", "Cimol", "Cimol2", "Cimol3", "Cimol4", "Cimol5", "Cimol6", "Cimol7").visibleIf(() -> options.is("Визуальный скин").getValue());
    private static ModeSetting time = new ModeSetting("Время", "Ночь", "Ночь", "День").visibleIf(() -> options.is("Время").getValue());



    public WorldTweaks() {
        addSettings(options, fogDistance, selfSkinType, time, mode, colorFog);
    }

    public ResourceLocation updateSkin(ResourceLocation prevResource, Entity entity) {
        if (isEnabled() && entity instanceof PlayerEntity && entity == mc.player) {
            if (options.is("Визуальный скин").getValue() && !SelfDestruct.unhooked) {
                prevResource = new ResourceLocation("render/images/skins/" + selfSkinType.getValue().toLowerCase() + ".png");
            }
        }
        return prevResource;
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (e.getPacket() instanceof SUpdateTimePacket p) {
            if (isEnabled("Время")) {
                if (time.getValue().equalsIgnoreCase("День"))
                    p.worldTime = 1000L;
                else
                    p.worldTime = 18000L;
            }
        }
    }
    @Getter
    @RequiredArgsConstructor
    public enum AnimationMode {
        BELOW(0),
        ABOVE(1),
        HORIZONT(2),
        PLAYER_DIRECTION(3),
        DIRECTION(4);
        private final int mode;

        @Override
        public String toString() {
            String name = name().toLowerCase();
            String[] words = name.split("_");
            StringBuilder formattedName = new StringBuilder();

            for (String word : words) {
                formattedName.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
            return formattedName.toString().trim();
        }
    }
    @Subscribe
    public void onUpdate(EventUpdate e) {
    }

    public boolean isEnabled(String name) {
        return options.is(name).getValue();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
