package minecraft.game.operation.visual;

import com.google.common.eventbus.Subscribe;
import minecraft.game.operation.combat.DDATTACK;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.transactions.EventMotion;
import minecraft.game.transactions.EventUpdate;
import minecraft.system.AG;
import net.minecraft.client.settings.PointOfView;

@Defuse(name = "Free Look",description = "XUY", brand = Category.Visual)
public class FreeLook extends Module {

    public CheckBoxSetting free = new CheckBoxSetting("Auto F5", false);
    public FreeLook(){
        addSettings( free);
    }

    private float startYaw, startPitch;
    @Override
    public void onEnable(){
        if(isFree()) {
            startYaw = mc.player.rotationYaw;
            startPitch = mc.player.rotationPitch;
        }
        super.onEnable();
    }

    @Override
    public void onDisable(){
        if(isFree()) {
            mc.player.rotationYawOffset = Integer.MIN_VALUE;
            mc.gameSettings.setPointOfView(PointOfView.FIRST_PERSON);
            mc.player.rotationYaw = startYaw;
            mc.player.rotationPitch = startPitch;
        }
        super.onDisable();
    }


    @Subscribe
    public void onUpdate(EventUpdate e) {
        DDATTACK aura = AG.getInst().getModuleManager().getDDATTACK();
        if (free.getValue()) {
            if (! aura.isEnabled() && aura.getTarget() == null) {
                mc.gameSettings.setPointOfView(PointOfView.THIRD_PERSON_BACK);
                mc.player.rotationYawOffset = startYaw;
            } else {
            }
        }
    }

    @Subscribe
    public void onMotion(EventMotion e){
        if(free.getValue()) {
            e.setYaw(startYaw);
            e.setPitch(startPitch);
            e.setOnGround(mc.player.isOnGround());
            mc.player.rotationYawHead = mc.player.rotationYawOffset;
            mc.player.renderYawOffset = mc.player.rotationYawOffset;
            mc.player.rotationPitchHead = startPitch;
        }
    }

    public boolean isFree(){
        return free.getValue();
    }
}