package minecraft.game.operation.misc;

import com.google.common.eventbus.Subscribe;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.game.operation.wamost.massa.elements.StringSetting;
import minecraft.game.transactions.EventMotion;
import minecraft.game.transactions.EventUpdate;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.advantage.figures.MathSystem;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector2f;

import java.util.UUID;

@Defuse(name = "Fake Player",description = "Создает бота в мире", brand = Category.Misc)
public class FakePlayer extends Module {
    public CheckBoxSetting changename = new CheckBoxSetting("Изменять ник", false);
    final ModeSetting name = new ModeSetting("Ник бота", "Fake Player", "Fake Player", "Селёдка", "Ишак", "Голубь умер", "Дед инсульт", "Ебаклак", "1000-7 голубей", "picun_f6","Custom")
            .visibleIf(() -> (Boolean)this.changename.getValue());
    public final StringSetting customText = new StringSetting("Кастомный ник", "Абаюдна", "Укажите текст").visibleIf(() -> name.is("Custom"));
    private RemoteClientPlayerEntity fakePlayer;
    private String currentName = "";

    public FakePlayer() {
        setEnabled(false, true);
        this.addSettings(this.changename, this.name, this.customText);
    }

    @Subscribe
    public void onMotion(EventMotion e) {
        if (fakePlayer == null) return;
        Vector2f rot = MathSystem.rotationToEntity(mc.player);
        fakePlayer.rotationYaw = rot.x;
        fakePlayer.rotationPitch = rot.y;
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (fakePlayer == null) return;
        if (this.name.is("Custom")) {
            if (changename.getValue() && !currentName.equals(customText.getValue())) {
                removeFakePlayer();
                spawnFakePlayer();
                currentName = customText.getValue();
            }
        } else {
            if (changename.getValue() && !currentName.equals(name.getValue())) {
                removeFakePlayer();
                spawnFakePlayer();
                currentName = name.getValue();
            }
        }
    }


    private void spawnFakePlayer() {
        UUID var1 = UUID.nameUUIDFromBytes("1351355555".getBytes());
        if (this.name.is("Custom")) {
            fakePlayer = new RemoteClientPlayerEntity(FakePlayer.mc.world, new GameProfile(var1, customText.getValue()));
        }
        else {
            fakePlayer = new RemoteClientPlayerEntity(FakePlayer.mc.world, new GameProfile(var1, name.getValue()));
        }
        fakePlayer.copyLocationAndAnglesFrom(mc.player);
        fakePlayer.rotationYawHead = mc.player.rotationYawHead;
        fakePlayer.renderYawOffset = mc.player.renderYawOffset;
        fakePlayer.rotationPitchHead = mc.player.rotationPitchHead;
        fakePlayer.container = mc.player.container;
        fakePlayer.inventory = mc.player.inventory;
        fakePlayer.setHealth(1351355555);

        mc.world.addEntity(1351355555 , fakePlayer);

        if (this.name.is("Custom")) {
            currentName = customText.getValue();
        }
        else {
            currentName = name.getValue();
        }
    }

    @Override
    public void onDisable() {
        removeFakePlayer();
        super.onDisable();
        return;
    }

    @Override
    public void onEnable() {
        spawnFakePlayer();
        super.onEnable();
    }

    public void removeFakePlayer() {
        mc.world.removeEntityFromWorld(1351355555);
    }

}
