package minecraft.game.operation.visual;

import com.google.common.eventbus.Subscribe;

import minecraft.game.transactions.EventRender3D;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.ColorSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.minecart.ChestMinecartEntity;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.BlockPos;
import net.optifine.render.RenderUtils;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

@Defuse(name = "Storage Esp",description = "Есп для сундуков и другого", brand = Category.Visual)
public class StorageEsp extends Module {

    final CheckBoxSetting chest = new CheckBoxSetting("Chest", true);
    final ColorSetting chestColor = new ColorSetting("ChestColor", new Color(243, 172, 82).getRGB()).visibleIf(() -> chest.getValue());

    final CheckBoxSetting trappedChest = new CheckBoxSetting("TrappedChest", true);
    final ColorSetting trappedChestColor = new ColorSetting("TrappedChestColor", new Color(143, 109, 62).getRGB()).visibleIf(() -> trappedChest.getValue());

    final CheckBoxSetting barrel = new CheckBoxSetting("Barrel", true);
    final ColorSetting barrelColor = new ColorSetting("BarrelColor", new Color(250, 225, 62).getRGB()).visibleIf(() -> barrel.getValue());

    final CheckBoxSetting hopper = new CheckBoxSetting("Hopper", true);
    final ColorSetting hopperColor = new ColorSetting("HopperColor", new Color(62, 137, 250).getRGB()).visibleIf(() -> hopper.getValue());

    final CheckBoxSetting dispenser = new CheckBoxSetting("Dispenser", true);
    final ColorSetting dispenserColor = new ColorSetting("DispenserColor", new Color(27, 64, 250).getRGB()).visibleIf(() -> dispenser.getValue());

    final CheckBoxSetting dropper = new CheckBoxSetting("Dropper", true);
    final ColorSetting dropperColor = new ColorSetting("DropperColor", new Color(0, 23, 255).getRGB()).visibleIf(() -> dropper.getValue());

    final CheckBoxSetting furnace = new CheckBoxSetting("Furnace", true);
    final ColorSetting furnaceColor = new ColorSetting("FurnaceColor", new Color(115, 115, 115).getRGB()).visibleIf(() -> furnace.getValue());

    final CheckBoxSetting enderChest = new CheckBoxSetting("EnderChest", true);
    final ColorSetting enderChestColor = new ColorSetting("EnderChestColor", new Color(82, 49, 238).getRGB()).visibleIf(() -> enderChest.getValue());

    final CheckBoxSetting shulkerBox = new CheckBoxSetting("ShulkerBox", true);
    final ColorSetting shulkerBoxColor = new ColorSetting("ShulkerBoxColor", new Color(246, 123, 123).getRGB()).visibleIf(() -> shulkerBox.getValue());

    final CheckBoxSetting mobSpawner = new CheckBoxSetting("MobSpawner", true);
    final ColorSetting mobSpawnerColor = new ColorSetting("MobSpawnerColor", new Color(112, 236, 166).getRGB()).visibleIf(() -> mobSpawner.getValue());

    final CheckBoxSetting mineCart = new CheckBoxSetting("MineCart", true);
    final ColorSetting mineCartColor = new ColorSetting("MineCartColor", new Color(255, 255, 255).getRGB()).visibleIf(() -> mineCart.getValue());


    public StorageEsp() {
        addSettings(chest, chestColor,
                trappedChest, trappedChestColor,
                barrel, barrelColor,
                hopper, hopperColor,
                dispenser, dispenserColor,
                dropper, dropperColor,
                furnace, furnaceColor,
                enderChest, enderChestColor,
                shulkerBox, shulkerBoxColor,
                mobSpawner, mobSpawnerColor,
                mineCart, mineCartColor
        );
    }

    @Subscribe
    private void onRender(EventRender3D e) {
        Map<TileEntityType<?>, Integer> tiles = new HashMap<>(Map.of(
                new ChestTileEntity().getType(), chestColor.getValue(),
                new TrappedChestTileEntity().getType(), trappedChestColor.getValue(),
                new BarrelTileEntity().getType(), barrelColor.getValue(),
                new HopperTileEntity().getType(), hopperColor.getValue(),
                new DispenserTileEntity().getType(), dispenserColor.getValue(),
                new DropperTileEntity().getType(), dropperColor.getValue(),
                new FurnaceTileEntity().getType(), furnaceColor.getValue(),
                new EnderChestTileEntity().getType(), enderChestColor.getValue(),
                new ShulkerBoxTileEntity().getType(), shulkerBoxColor.getValue(),
                new MobSpawnerTileEntity().getType(), mobSpawnerColor.getValue()
        ));

        for (TileEntity tile : mc.world.loadedTileEntityList) {
            if (!tiles.containsKey(tile.getType())) continue;

            BlockPos pos = tile.getPos();
            int color = tiles.get(tile.getType());

            if (tile instanceof ChestTileEntity && chest.getValue()) {
                RenderUtils.drawBlockBox(pos, color);
            } else if (tile instanceof TrappedChestTileEntity && trappedChest.getValue()) {
                RenderUtils.drawBlockBox(pos, color);
            } else if (tile instanceof BarrelTileEntity && barrel.getValue()) {
                RenderUtils.drawBlockBox(pos, color);
            } else if (tile instanceof HopperTileEntity && hopper.getValue()) {
                RenderUtils.drawBlockBox(pos, color);
            } else if (tile instanceof DispenserTileEntity && dispenser.getValue()) {
                RenderUtils.drawBlockBox(pos, color);
            } else if (tile instanceof DropperTileEntity && dropper.getValue()) {
                RenderUtils.drawBlockBox(pos, color);
            } else if (tile instanceof FurnaceTileEntity && furnace.getValue()) {
                RenderUtils.drawBlockBox(pos, color);
            } else if (tile instanceof EnderChestTileEntity && enderChest.getValue()) {
                RenderUtils.drawBlockBox(pos, color);
            } else if (tile instanceof ShulkerBoxTileEntity && shulkerBox.getValue()) {
                RenderUtils.drawBlockBox(pos, color);
            } else if (tile instanceof MobSpawnerTileEntity && mobSpawner.getValue()) {
                RenderUtils.drawBlockBox(pos, color);
            }
        }

        if (mineCart.getValue()) {
            for (Entity entity : mc.world.getAllEntities()) {
                if (entity instanceof ChestMinecartEntity) {
                    RenderUtils.drawBlockBox(entity.getPosition(), mineCartColor.getValue());
                }
            }
        }
    }

}
