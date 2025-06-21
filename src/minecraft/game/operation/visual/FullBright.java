package minecraft.game.operation.visual;

import com.google.common.eventbus.Subscribe;
import minecraft.game.transactions.EventUpdate;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.game.advantage.alacrity.easing.CompactAnimation;
import minecraft.game.advantage.alacrity.easing.Easing;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;

@Defuse(name = "Full Bright",description = "Увеличивает яркость игры", brand = Category.Visual)
public class FullBright extends Module {
    private final ModeSetting mode = new ModeSetting("Мод", "Gamma", "Gamma", "Potion");
    private final CompactAnimation animation = new CompactAnimation(Easing.EASE_OUT_QUART, 500);
    private final CheckBoxSetting dynamic = new CheckBoxSetting("Динамический", false).visibleIf(() -> mode.is("Gamma"));
    private final SliderSetting bright = new SliderSetting("Яркость", 2.5f, 1, 5, 0.1f).visibleIf(() -> !dynamic.getValue() && mode.is("Gamma"));

    private float originalGamma;
    private boolean isGammaChanged = false;

    public FullBright() {
        addSettings(mode, dynamic, bright);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        saveGamma();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        restoreGamma();
        mc.player.removeActivePotionEffect(new EffectInstance(Effects.NIGHT_VISION).getPotion());
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mode.is("Gamma")) {
            mc.player.removeActivePotionEffect(new EffectInstance(Effects.NIGHT_VISION).getPotion());
            if (dynamic.getValue()) {
                float lightLevel = mc.player.getBrightness();
                animation.run(calculateGamma(lightLevel));
                float gamma = (float) animation.getValue();
                setGamma(gamma);
            } else {
                setGamma(bright.getValue());
            }
        } else {
            mc.player.addPotionEffect(new EffectInstance(Effects.NIGHT_VISION, 16360, 0));
        }
    }

    private float calculateGamma(float lightLevel) {
        float minGamma = 0.5f;
        float maxGamma = 5.0f;
        float gammaRange = maxGamma - minGamma;
        float lightRange = 1;
        float gamma = minGamma + (gammaRange * (1.0f - lightLevel / lightRange));
        return gamma;
    }

    public void saveGamma() {
        originalGamma = (float) mc.gameSettings.gamma;
    }

    public void setGamma(float newGamma) {
        saveGamma();
        mc.gameSettings.gamma = newGamma;
        isGammaChanged = true;
    }

    public void restoreGamma() {
        if (isGammaChanged) {
            mc.gameSettings.gamma = originalGamma;
            isGammaChanged = false;
        }
    }
}
