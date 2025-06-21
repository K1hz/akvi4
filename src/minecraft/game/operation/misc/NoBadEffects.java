package minecraft.game.operation.misc;

import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.transactions.EventUpdate;
import net.minecraft.potion.Effects;

@Getter
@Defuse(name = "Potions Remover",description = "XUY", brand = Category.Player)
public class NoBadEffects extends Module {
    private final CheckBoxSetting plavka = new CheckBoxSetting("Плавное падение",true);
    private final CheckBoxSetting Levetation = new CheckBoxSetting("Левитация",true);
    private final CheckBoxSetting priguchka = new CheckBoxSetting("Прыгучесть",true);

    public NoBadEffects(){addSettings(plavka,Levetation,priguchka);};
    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (plavka.getValue()) {
            if (mc.player.isPotionActive(Effects.SLOW_FALLING)) {
                mc.player.removePotionEffect(Effects.SLOW_FALLING);
            }
        }
        if (Levetation.getValue()) {
            if (mc.player.isPotionActive(Effects.LEVITATION)) {
                mc.player.removePotionEffect(Effects.LEVITATION);
            }
        }
        if (priguchka.getValue()) {
            if (mc.player.isPotionActive(Effects.JUMP_BOOST)) {
                mc.player.removePotionEffect(Effects.JUMP_BOOST);
            }
        }
    }
}
