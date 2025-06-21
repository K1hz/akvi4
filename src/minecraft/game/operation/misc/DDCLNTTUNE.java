package minecraft.game.operation.misc;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.game.transactions.AttackEvent;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.BufferedInputStream;
import java.io.InputStream;

import static java.lang.Math.signum;
import static net.minecraft.util.math.MathHelper.wrapDegrees;

@Defuse(name = "Cliеnt Sound",description = "Добавляет звук вкл и выкл модулей", brand = Category.Misc)
public class DDCLNTTUNE extends Module {
    public CheckBoxSetting hitsound = new CheckBoxSetting("HitSounds", false);
    public CheckBoxSetting modulesound = new CheckBoxSetting("ModuleSounds", true);
    public ModeSetting mode = new ModeSetting("Тип", "VL", "Default", "Bubbles", "Pop", "Heavy", "Windows", "Slide", "Droplet", "VL").visibleIf(() -> this.modulesound.getValue());;;
    public SliderSetting volume = new SliderSetting("Громкость", 60.0f, 0.0f, 100.0f, 1.0f).visibleIf(() -> this.modulesound.getValue());;;
    private final ModeSetting sound = new ModeSetting("Звук", "bell", new String[]{"bell", "metallic", "rust", "bubble", "bonk", "crime"}).visibleIf(() -> this.hitsound.getValue());;;
    SliderSetting volume1 = new SliderSetting("Громкость Удара", 35.0F, 5.0F, 100.0F, 5.0F).visibleIf(() -> this.hitsound.getValue());;

    public DDCLNTTUNE() {
        addSettings(modulesound,mode, volume,hitsound, sound, volume1);
        toggle();
    }

    @Subscribe
    public void onPacket(AttackEvent e) {
        Minecraft var10000 = mc;
        if (Minecraft.player != null) {
            var10000 = mc;
            if (Minecraft.world != null) {
                // Check if hitsound is enabled before playing the sound
                if (hitsound.getValue()) {
                    this.playSound(e.entity);
                }
                return;
            }
        }
    }


    public String getFileName(boolean state) {
        switch (mode.getValue()) {
            case "Default" -> {
                return state ? "enable" : "disable";
            }
            case "Bubbles" -> {
                return state ? "enableBubbles" : "disableBubbles";
            }
            case "Pop" -> {
                return state ? "popenable" : "popdisable";
            }
            case "Heavy" -> {
                return state ? "heavyenable" : "heavydisable";
            }
            case "Windows" -> {
                return state ? "winenable" : "windisable";
            }
            case "Droplet" -> {
                return state ? "dropletenable" : "dropletdisable";
            }
            case "Slide" -> {
                return state ? "slideenable" : "slidedisable";
            }
            case "VL" -> {
                return state ? "enablevl" : "disablevl";
            }
        }
        return "";
    }
    public void playSound(Entity e) {
        try {
            Clip clip = AudioSystem.getClip();
            InputStream is = mc.getResourceManager().getResource(new ResourceLocation("render/sounds/" + (String)this.sound.getValue() + ".wav")).getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bis);
            if (audioInputStream == null) {
                System.out.println("Sound not found!");
                return;
            }

            clip.open(audioInputStream);
            clip.start();
            FloatControl floatControl = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
            if (e != null) {
                FloatControl balance = (FloatControl)clip.getControl(FloatControl.Type.BALANCE);
                Vector3d var10000 = e.getPositionVec();
                Minecraft.getInstance();
                Vector3d vec = var10000.subtract(Minecraft.player.getPositionVec());
                double yaw = MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(vec.z, vec.x)) - (double)90.0F);
                Minecraft var10001 = mc;
                double delta = MathHelper.wrapDegrees(yaw - (double)Minecraft.player.rotationYaw);
                if (Math.abs(delta) > (double)180.0F) {
                    delta -= Math.signum(delta) * (double)360.0F;
                }

                try {
                    balance.setValue((float)delta / 180.0F);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            Minecraft var16 = mc;
            floatControl.setValue(-(Minecraft.player.getDistance(e) * 5.0F) - this.volume.max / (Float)this.volume1.getValue());
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }
}
