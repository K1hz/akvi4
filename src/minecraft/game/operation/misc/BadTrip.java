package minecraft.game.operation.misc;

import java.io.BufferedInputStream;
import java.io.InputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import com.google.common.eventbus.Subscribe;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.massa.api.Setting;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

@Defuse(name="Bad Trip", description="BadTrip", brand= Category.Visual)
public class BadTrip extends Module {
    public static boolean flexx = false;
    private Clip currentClip;
    SliderSetting volume = new SliderSetting("Громкость звука", 35.0f, 5.0f, 100.0f, 5.0f) {
        @Subscribe
        public void onChange(float newValue) {
            updateVolume();
        }
    };

    public BadTrip() {
        this.addSettings(volume);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        flexx = true;
        this.playSound();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        flexx = false;
        this.stopCurrentSound();
    }

    public void playSound() {
        if (this.currentClip != null && this.currentClip.isRunning()) {
            return;
        }
        try {
            Clip clip = AudioSystem.getClip();
            InputStream inputStream = mc.getResourceManager().getResource(new ResourceLocation("render/sounds/BadTrip.wav")).getInputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedInputStream);
            if (audioInputStream == null) {
                System.out.println("Нету звука пизда");
                return;
            }
            clip.open(audioInputStream);
            clip.start();
            this.currentClip = clip;
            updateVolume();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void updateVolume() {
        if (this.currentClip != null) {
            try {
                FloatControl floatControl = (FloatControl) this.currentClip.getControl(FloatControl.Type.MASTER_GAIN);
                float volumeValue = (float) volume.getValue();
                float gain = (float) (20.0 * Math.log10(volumeValue / 100.0)); // Конвертация в dB
                floatControl.setValue(gain);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void stopCurrentSound() {
        if (this.currentClip != null && this.currentClip.isRunning()) {
            this.currentClip.stop();
            this.currentClip.close();
        }
    }
}
