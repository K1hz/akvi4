package minecraft.game.operation.wamost.api;

import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.display.clientnotify.elements.NoNotify;
import minecraft.game.display.clientnotify.elements.SuccessNotify;
import minecraft.game.operation.misc.DDCLNTTUNE;
import minecraft.game.advantage.advisee.SoundPlayer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import minecraft.game.operation.wamost.massa.elements.BindSetting;
import net.minecraft.util.text.TextFormatting;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;

import java.util.List;

import minecraft.game.operation.visual.Hud;
import minecraft.system.AG;
import minecraft.game.operation.wamost.massa.api.Setting;
import minecraft.game.advantage.advisee.IMinecraft;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public abstract class Module implements IMinecraft {

    final String description;
    final String name;
    final Category category;
    public boolean enabled;
    @Setter
    int bind;
    final List<Setting<?>> settings = new ObjectArrayList<>();

    final Animation animation = new Animation();

    public Module() {
        this.description = getClass().getAnnotation(Defuse.class).description();
        this.name = getClass().getAnnotation(Defuse.class).name();
        this.category = getClass().getAnnotation(Defuse.class).brand();
        this.bind = getClass().getAnnotation(Defuse.class).key();
    }

    public void addSettings(Setting<?>... settings) {
        this.settings.addAll(List.of(settings));
    }
    public void betterComp(Module module) {
        if (getName().equals(module.getName())) {
            return;
        }

        if (module.isEnabled()) {
            module.setEnabled(false, false);
            module.setEnabled(true, false);
            print(getName() + ": улучшил совместимость с модулем " + module.getName());
        }
    }

    public void onEnable() {
        animation.animate(1, 0f, Easings.CIRC_OUT);
        AG.getInst().getEventBus().register(this);
    }

    public void onDisable() {
        animation.animate(0, 0f, Easings.CIRC_OUT);
        AG.getInst().getEventBus().unregister(this);
    }

    public String getDescription() {
        return this.description;
    }

    public int getWidthDESC() {
        return (this.description != null) ? this.description.length() : 0;
    }

    public int getLetterCount() {
        if (this.description == null) {
            return 0;
        }
        int count = 0;
        for (char c : this.description.toCharArray()) {
            if (Character.isLetter(c)) {
                count++;
            }
        }
        return count;
    }

    public void toggle() {
        setEnabled(!enabled, false);
    }

    public final void setEnabled(boolean newState, boolean config) {
        if (enabled == newState) {
            return;
        }

        enabled = newState;

        TextFormatting color12347 = TextFormatting.BLACK; // По умолчанию черный


        try {
            if (enabled) {
                onEnable();
                if (!name.equals("ClickGui")) AG.getInst().getNotifyManager().add(0, new SuccessNotify(this.name + " " + "включен. ", 600));
            } else {
                onDisable();
                if (!name.equals("ClickGui")) AG.getInst().getNotifyManager().add(0, new NoNotify(this.name + " "+ "выключен. ", 600));
            }
            if (!config) {
                ModuleManager moduleManager = AG.getInst().getModuleManager();
                DDCLNTTUNE DDCLNTTUNE = moduleManager.getDDCLNTTUNE();

                if (DDCLNTTUNE.modulesound.getValue()) {
                    String fileName = DDCLNTTUNE.getFileName(enabled);  // Get the sound file based on enabled state
                    float volume = DDCLNTTUNE.volume.getValue();  // Get the volume setting
                    SoundPlayer.playSound(fileName, volume, false);  // Play the sound
                }
            }
        } catch (Exception e) {
            handleException(enabled ? "onEnable" : "onDisable", e);
        }

    }

    private void handleException(String methodName, Exception e) {
        if (mc.player != null) {
            print("[" + name + "] Произошла ошибка " + TextFormatting.RED + methodName + TextFormatting.WHITE
                    + "() Сообщите о данной ошибке в дискорд сервере. Либо (.report (ссылка на скриншот))" + TextFormatting.GRAY + e.getMessage());
            e.printStackTrace();
        } else {
            System.out.println("[" + name + " Error" + methodName + "() Message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
