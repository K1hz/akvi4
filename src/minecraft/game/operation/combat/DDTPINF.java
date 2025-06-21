package minecraft.game.operation.combat;

import com.google.common.eventbus.Subscribe;
import minecraft.system.AG;
import minecraft.game.transactions.EventMotion;
import minecraft.game.transactions.EventRender3D;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.*;
import minecraft.game.advantage.make.color.ColoringSystem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.optifine.render.RenderUtils;

import static org.lwjgl.opengl.GL11.*;

@Defuse(name = "TPInfluеnсе",description = "Хуй знает че это", brand = Category.Misc)
public class DDTPINF extends Module {

    private Vector3d lastHandledVec = new Vector3d(0, 0, 0);

    public ModeSetting mode = new ModeSetting("Мод", "VanillaH", "VanillaH", "VanillaVH", "StepV", "StepH", "StepVH", "StepHG");
    public SliderSetting range = new SliderSetting("Дистанция", 15, 10, 100, 1);

    public CheckBoxSetting render = new CheckBoxSetting("Визуализация", true);

    public DDTPINF() {
        addSettings(mode, range, render);
    }

    @Subscribe
    public void onRender(EventRender3D e) {
        if (render.getValue() && AG.getInst().getModuleManager().getDDATTACK().getTarget() != null && AG.getInst().getModuleManager().getDDATTACK().isEnabled()) {
            float half = mc.player.getWidth() / 2;
            glPushMatrix();
            Vector3d renderOffset = mc.getRenderManager().info.getProjectedView();

            glTranslated(-renderOffset.x, -renderOffset.y, -renderOffset.z);

            RenderUtils.drawBox(new AxisAlignedBB(lastHandledVec.x - half, lastHandledVec.y, lastHandledVec.z - half, lastHandledVec.x + half, lastHandledVec.y + mc.player.getHeight(), lastHandledVec.z + half), ColoringSystem.setAlpha(-1, 130));

            glPopMatrix();
        }
    }
    
    public void DDATTACKTPPRe(LivingEntity target) {
        if (isEnabled() && !AG.getInst().getModuleManager().getDDFRCM().isEnabled()) teleportActionOfActionType(true, targetWhitePos(target, AG.getInst().getModuleManager().getDDATTACK().attackDistance()), mode.getValue());
    }

    public void DDATTACKTPPost(LivingEntity target) {
        if (isEnabled() && !AG.getInst().getModuleManager().getDDFRCM().isEnabled()) teleportActionOfActionType(false, targetWhitePos(target, AG.getInst().getModuleManager().getDDATTACK().attackDistance()), mode.getValue());
    }

    public void teleportActionOfActionType(boolean pre, Vector3d to, String actionType) {
        Vector3d self = mc.player.getPositionVec();
        double dx = positive(self.x - to.x);
        double dy = positive(self.y - to.y);
        double dz = positive(self.z - to.z);
        int grInt = mc.player.onGround ? 1 : 0;
        float distanceDensity = 1.0f;
        if (pre) {
            switch (actionType) {
                case "StepVH": {
                    double diffs = sqrtAt(dx, dy, dz);
                    for (int packetCount = (int)(diffs / (9.64 * (double)distanceDensity)) + 1; packetCount > 0; --packetCount) {
                        send(false);
                    }
                    if (grInt == 1) {
                        send(to.x, to.y + 0.08, to.z);
                    }
                    send(to.x, to.y + 0.01, to.z);
                    lastHandledVec = new Vector3d(to.x, to.y + 0.01, to.z);
                    break;
                }
                case "StepV": {
                    double diffs = positive(dy);
                    int packetCount = 1 + (int)(diffs / (9.73 * (double)distanceDensity));
                    if (grInt == 1) {
                        send(to.x, self.y + 0.08, to.z);
                    }
                    while (packetCount > 0) {
                        send(false);
                        --packetCount;
                    }
                    send(self.x, to.y + 0.01, self.z);
                    lastHandledVec = new Vector3d(self.x, to.y + 0.01, self.z);
                    break;
                }
                case "StepH": {
                    double diffs = sqrtAt(dx, dz);
                    for (int packetCount = (int)(diffs / (8.953 * (double)distanceDensity)) + grInt; packetCount > 0; --packetCount) {
                        send(false);
                    }
                    send(to.x, self.y, to.z);
                    lastHandledVec = new Vector3d(to.x, self.y, to.z);
                    break;
                }
                case "StepHG": {
                    int packetCount;
                    double diffs = sqrtAt(dx, dz);
                    for (packetCount = (int)(diffs / (8.317 * (double)distanceDensity)) + grInt; packetCount > 0; --packetCount) {
                        send(false);
                    }
                    send(to.x, self.y - (double)grInt * 1.0E-4 * (double)packetCount, to.z);
                    lastHandledVec = new Vector3d(to.x, self.y - (double)grInt * 1.0E-4 * (double)packetCount, to.z);
                    if (grInt != 0) break;
                    mc.player.setPosY(self.y);
                    break;
                }
                case "VanillaVH": {
                    send(false);
                    send(to.x, to.y, to.z);
                    lastHandledVec = new Vector3d(to.x, to.y, to.z);
                }
                case "VanillaH": {
                    send(false);
                    send(to.x, self.y, to.z);
                    lastHandledVec = new Vector3d(to.x, to.y, to.z);
                }
            }
            return;
        }
        switch (actionType) {
            case "StepVH": {
                send(self.x, self.y + 0.15, self.z);
                send(self.x, self.y, self.z);
                break;
            }
            case "StepV": {
                send(self.x, self.y, self.z);
                send(self.x, self.y + (grInt == 1 ? 0.1 : -1.0E-13), self.z);
                break;
            }
            case "StepH": {
                send(self.x, self.y, self.z);
                break;
            }
            case "StepHG": {
                send(self.x, self.y - positive(grInt - 1) * 1.0E-4 * 2.0, self.z);
                break;
            }
            case "VanillaVH": {
                send(self.x, self.y + 0.0016, self.z);
                break;
            }
            case "VanillaH": {
                send(self.x, self.y, self.z);
            }
        }
    }

    public Vector3d targetWhitePos(LivingEntity target, double distanceMin) {
        if ((distanceMin -= target.getHeight()) < 0.0) {
            distanceMin = 0.0;
        }
        Vector3d vec = target.getPositionVec();
        double selfX = mc.player.getPosX();
        double targetX = vec.x;
        double selfY = mc.player.getPosY();
        double targetY = vec.y;
        double yDst = positive(selfY - targetY);
        double targetW = (double)target.getHeight() / 2.0;
        double targetH = target.getHeight();
        double selfZ = mc.player.getPosZ();
        double targetZ = vec.z;
        if (yDst > distanceMin) {
            double tempAppend;
            double appendY = MathHelper.clamp(yDst - distanceMin, 0.0, distanceMin - sqrtAt(selfX - targetX, selfZ - targetZ));
            for (tempAppend = 0.0; tempAppend < appendY; tempAppend += 0.1) {
                AxisAlignedBB aabb = new AxisAlignedBB(targetX - targetW / 2.0, targetY + tempAppend, targetZ - targetW / 2.0, targetX + targetW / 2.0, targetY + targetH + tempAppend, targetZ + targetW / 2.0);
                if (aabb == null) {
                    tempAppend -= 0.1;
                    continue;
                }
                if (!(tempAppend > 0.0) || DDTPINF.mc.world.getCollisionShapes(target, aabb).toList().isEmpty()) continue;
                tempAppend -= 0.1;
                break;
            }
            if ((appendY = tempAppend) < 0.0) {
                appendY = 0.0;
            }
            vec = vec.add(0.0, appendY, 0.0);
        }
        return vec;
    }

    private void send(double x, double y, double z) {
        mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(x, y, z, false));
    }

    private void send(boolean ground) {
        mc.getConnection().sendPacket(new CPlayerPacket(ground));
    }

    private double sqrtAt(double val1, double val2) {
        return Math.sqrt(val1 * val1 + val2 * val2);
    }

    private double sqrtAt(double val1, double val2, double val3) {
        return Math.sqrt(val1 * val1 + val2 * val2 + val3 * val3);
    }

    private double positive(double val) {
        return val < 0.0 ? -val : val;
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
