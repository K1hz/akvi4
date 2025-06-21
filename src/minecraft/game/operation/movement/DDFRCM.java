package minecraft.game.operation.movement;

import com.google.common.eventbus.Subscribe;
import io.netty.handler.codec.spdy.SpdyDataFrame;
import minecraft.game.advantage.figures.GCDSensSystem;
import minecraft.game.advantage.luvvy.DDFCRMeraModule;
import minecraft.game.advantage.make.DDFRCMUtils;
import net.minecraft.client.MainWindow;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.client.gui.screen.DownloadTerrainScreen;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SConfirmTransactionPacket;
import net.minecraft.network.play.server.SEntityDDNVLCPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.network.play.server.SRespawnPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import minecraft.system.AG;
import minecraft.game.transactions.EventLivingUpdate;
import minecraft.game.transactions.EventMotion;
import minecraft.game.transactions.EventPacket;
import minecraft.game.transactions.EventRender2D;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.combat.DDATTACK;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.game.advantage.luvvy.MovementSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.words.font.ClientFonts;

import static minecraft.game.operation.combat.DDATTACK.rotateVector;

@Defuse(name = "Free Саm",description = "Возможность визуально летать сквозь блоки", brand = Category.Player)
public class DDFRCM extends Module {

    private final SliderSetting speed = new SliderSetting("Скорость по XZ", 1.0f, 0.1f, 5.0f, 0.05f);
    private final SliderSetting motionY = new SliderSetting("Скорость по Y", 0.5f, 0.1f, 1.0f, 0.05f);

    public DDFRCM() {
        addSettings(speed, motionY);
    }
    private Vector3d savedPosition = null;
    private Vector3d clientPosition = null;
    public DDFRCMUtils player = null;
    public RemoteClientPlayerEntity fakePlayer;


    @Subscribe
    private void onWalking(EventMotion e) {

        double diffX = mc.player.getPosX() - fakePlayer.getPosX();
        double diffZ = mc.player.getPosZ() - fakePlayer.getPosZ();
        double diffY = (mc.player.getPosY() + mc.player.getEyeHeight()) - (fakePlayer.getPosY() + fakePlayer.getEyeHeight());
        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
        double distance = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, distance));

        e.setYaw(yaw);
        e.setPitch(pitch);


    }
    @Subscribe
    public void onLivingUpdate(EventLivingUpdate e) {

        if (mc.player != null) {
            mc.player.noClip = true;
            mc.player.setOnGround(false);
            MovementSystem.setMotionOLD(speed.getValue());

            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                mc.player.motion.y = motionY.getValue();
            }
            if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                mc.player.motion.y = -motionY.getValue();
            }
            mc.player.abilities.isFlying = true;

            if (fakePlayer != null) {
                DDATTACK ddattack =  AG.getInst().getModuleManager().getDDATTACK();
                double diffX = mc.player.getPosX() - fakePlayer.getPosX();
                double diffZ = mc.player.getPosZ() - fakePlayer.getPosZ();
                double diffY = (mc.player.getPosY() + mc.player.getEyeHeight()) - (fakePlayer.getPosY() + fakePlayer.getEyeHeight());
                if (ddattack.isEnabled()) {
                    if (ddattack.getTarget() !=null) {
                        diffX = ddattack.getTarget().getPosX() - fakePlayer.getPosX();
                        diffZ = ddattack.getTarget().getPosZ() - fakePlayer.getPosZ();
                        diffY = (ddattack.getTarget().getPosY() + ddattack.getTarget().getEyeHeight()) - (fakePlayer.getPosY() + fakePlayer.getEyeHeight());
                    }
                }

                double distance = Math.sqrt(diffX * diffX + diffZ * diffZ);

                float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
                float pitch = (float) -Math.toDegrees(Math.atan2(diffY, distance));
                float yaw1 = Math.min(Math.max(Math.abs(6), 0), 85);
                float pitch1 = Math.min(Math.max(Math.abs(6), 6), 0);

            }
        }
    }

    @Subscribe
    public void render(EventRender2D e) {
        MainWindow resolution = mc.getMainWindow();

        int xPosition = (int) (fakePlayer.getPosX() - mc.player.getPosX());
        int yPosition = (int) (fakePlayer.getPosY() - mc.player.getPosY());
        int zPosition = (int) (fakePlayer.getPosZ() - mc.player.getPosZ());


        String position = "X:" + (float) -xPosition + " Y:" + (float) -yPosition + " Z:" + (float) -zPosition;
        ClientFonts.clickGui[14].drawCenteredStringWithOutline(e.getMatrixStack(), position, (double)((float)resolution.getScaledWidth() / 2.0F), (double)((float)resolution.getScaledHeight() / 2.0F + 47.0F), -1);
        ClientFonts.clickGui[14].drawCenteredStringWithOutline(e.getMatrixStack(), "Ваша визуальная позиция", (double)((float)resolution.getScaledWidth() / 2.0F), (double)((float)resolution.getScaledHeight() / 2.0F + 40.0F), -1);
    }

    @Subscribe
    public void onMotion(EventMotion e) {

        DDATTACK ddattack = AG.getInst().getModuleManager().getDDATTACK();


        if (fakePlayer == null) toggle();

        if (ddattack.isEnabled()) {
        }
        mc.player.motion.x = 0;
        mc.player.motion.z = 0;
        mc.player.motion.y = 0;
        e.cancel();
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (mc.player != null && mc.world != null && !(mc.currentScreen instanceof DownloadTerrainScreen)) {

            if (e.isReceive()) {
            } else if (e.getPacket() instanceof SPlayerPositionLookPacket packet) {
                if (fakePlayer != null) {
                    fakePlayer.setPosition(packet.getX(), packet.getY(), packet.getZ());
                }
            }
            if (e.getPacket() instanceof SRespawnPacket) {
                mc.player.abilities.isFlying = false;
                if (clientPosition != null) {
                    mc.player.setPositionAndRotation(clientPosition.x, clientPosition.y, clientPosition.z, mc.player.rotationYaw, mc.player.rotationPitch);
                }
                removeFakePlayer();
                mc.player.motion = Vector3d.ZERO;
            }
        }
    }

    @Override
    public void onDisable() {
        if (mc.player == null) {
            return;
        }
        mc.player.abilities.isFlying = false;
        if (clientPosition != null) {
            mc.player.setPositionAndRotation(clientPosition.x, clientPosition.y, clientPosition.z, mc.player.rotationYaw, mc.player.rotationPitch);
        }
        removeFakePlayer();
        mc.player.motion = Vector3d.ZERO;
        super.onDisable();
    }
    @Override
    public void onEnable() {
        if (mc.player == null) {
            return;
        }
        mc.player.moveForward = 0;
        mc.player.moveStrafing = 0;
        clientPosition = mc.player.getPositionVec();
        spawnFakePlayer();
        super.onEnable();
    }
    private void spawnFakePlayer() {
        fakePlayer = new RemoteClientPlayerEntity(mc.world, mc.player.getGameProfile());
        fakePlayer.copyLocationAndAnglesFrom(mc.player);
        fakePlayer.rotationYawHead = mc.player.rotationYawHead;
        fakePlayer.renderYawOffset = mc.player.renderYawOffset;
        fakePlayer.rotationPitchHead = mc.player.rotationPitchHead;
        fakePlayer.container = mc.player.container;
        fakePlayer.inventory = mc.player.inventory;
        mc.world.addEntity(1337, fakePlayer);
    }

    private void removeFakePlayer() {
        mc.world.removeEntityFromWorld(1337);
    }
}
