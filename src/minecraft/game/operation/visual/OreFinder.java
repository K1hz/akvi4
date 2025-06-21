package minecraft.game.operation.visual;

import com.google.common.eventbus.Subscribe;
import net.optifine.render.RenderUtils;
import minecraft.game.transactions.*;
import minecraft.system.managers.Theme;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.ModeListSetting;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.make.font.Fonts;
import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;

import java.util.concurrent.CopyOnWriteArrayList;

@Defuse(name = "xRay",description = "Иксрей через блоки", brand = Category.Visual)
public class OreFinder extends Module {

    private CopyOnWriteArrayList<BlockPos> foundOres = new CopyOnWriteArrayList<>();
    private SliderSetting radius = new SliderSetting("Радиус", 20, 0, 30, 1);
    final SliderSetting time = new SliderSetting("Скорость обновления", 5.5f, 1.0f, 10, 0.1f);
    private ModeListSetting ores = new ModeListSetting("Искать",
            new CheckBoxSetting("Алмазы", false),
            new CheckBoxSetting("Адское Золото", false),
            new CheckBoxSetting("Золото", false),
            new CheckBoxSetting("Железо", false),
            new CheckBoxSetting("Лазурит", false),
            new CheckBoxSetting("Незерит", false),
            new CheckBoxSetting("Редстоун", false),
            new CheckBoxSetting("Уголь", false),
            new CheckBoxSetting("Эмеральды", false),
            new CheckBoxSetting("Блок Алмаза", false),
            new CheckBoxSetting("Блок Эмеральда", false),
            new CheckBoxSetting("Блок Золота", false),
            new CheckBoxSetting("Блок Железа", false),
            new CheckBoxSetting("Блок Лазурита", false),
            new CheckBoxSetting("Блок Незерита", false),
            new CheckBoxSetting("Блок Редстоуна", false),
            new CheckBoxSetting("Блок Угля", false),
            new CheckBoxSetting("Блок Эмеральда", false)
    );

    public OreFinder() {
        addSettings(radius, time, ores);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        foundOres.clear();
        SCANING();
    }

    private long lastScanTime = 0; // Время последнего сканирования

    @Subscribe
    public void Update(EventUpdate e) {
        if (mc.world == null || mc.player == null) {
            System.out.println("Все нахуй сломалось еблан переделывай");
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastScanTime > this.time.getValue() * 100) { // Сканируем раз в 2.4 секунды
            SCANING();
            lastScanTime = currentTime;
        }
    }


    @Subscribe
    public void DISPLAYER(EventRender2D e) {
        String oreCount = "Найдено: " + foundOres.size() + " блоков";
        float width = Fonts.montserrat.getWidth(oreCount, 8) + 20;
        float height = 15;
        float x = mc.getMainWindow().getScaledWidth() / 2f - width / 2;
        GraphicsSystem.drawShadowFancyRect(x, 10, width, height);
        Fonts.montserrat.drawText(e.getMatrixStack(), oreCount, x + 10, 13, ColoringSystem.rgb(255,255,255), 8);
    }

    @Subscribe
    public void WORLD(EventRender3D e) {
        if (mc.world == null || mc.player == null) {return;}
        for (BlockPos pos : foundOres) {
            int color = OREBLOCKS(mc.world.getBlockState(pos).getBlock());if (color != -1) {RenderUtils.drawBlockBox(pos, color);}
        }
    }

    private void SCANING() {
        foundOres.clear();
        if (mc.world == null || mc.player == null) {
            return;
        }

        BlockPos start = mc.player.getPosition();
        int searchRadius = radius.getValue().intValue();
        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -searchRadius; y <= searchRadius; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    BlockPos pos = start.add(x, y, z);
                    if (pos.getY() <= 0) continue;
                    Block block = mc.world.getBlockState(pos).getBlock();
                    if (CUSTOMORE(block)) {foundOres.add(pos);}
                }
            }
        }
    }

    private boolean CUSTOMORE(Block block) {
        return (block == Blocks.COAL_ORE && ores.is("Уголь").getValue())
                || (block == Blocks.COAL_BLOCK && ores.is("Блок Угля").getValue())
                || (block == Blocks.IRON_ORE && ores.is("Железо").getValue())
                || (block == Blocks.IRON_BLOCK && ores.is("Блок Железа").getValue())
                || (block == Blocks.REDSTONE_ORE && ores.is("Редстоун").getValue())
                || (block == Blocks.REDSTONE_BLOCK && ores.is("Блок Редстоуна").getValue())
                || (block == Blocks.GOLD_ORE && ores.is("Золото").getValue())
                || (block == Blocks.GOLD_BLOCK && ores.is("Блок Золота").getValue())
                || (block == Blocks.EMERALD_ORE && ores.is("Эмеральды").getValue())
                || (block == Blocks.EMERALD_BLOCK && ores.is("Блок Эмеральда").getValue())
                || (block == Blocks.DIAMOND_ORE && ores.is("Алмазы").getValue())
                || (block == Blocks.DIAMOND_BLOCK && ores.is("Блок Алмаза").getValue())
                || (block == Blocks.ANCIENT_DEBRIS && ores.is("Незерит").getValue())
                || (block == Blocks.NETHERITE_BLOCK && ores.is("Блок Незерита").getValue())
                || (block == Blocks.LAPIS_ORE && ores.is("Лазурит").getValue())
                || (block == Blocks.LAPIS_BLOCK && ores.is("Блок Лазурита").getValue())
                || (block == Blocks.NETHER_GOLD_ORE && ores.is("Адское Золото").getValue());
    }

    private int OREBLOCKS(Block block) {
        if (block == Blocks.COAL_ORE && ores.is("Уголь").getValue()) {
            return ColoringSystem.rgba(0, 0, 0, 255);
        }
        if (block == Blocks.COAL_BLOCK && ores.is("Блок Угля").getValue()) {
            return ColoringSystem.rgba(0, 0, 0, 255);
        }
        if (block == Blocks.IRON_ORE && ores.is("Железо").getValue()) {
            return ColoringSystem.rgba(122, 122, 122, 255);
        }
        if (block == Blocks.IRON_BLOCK && ores.is("Блок Железа").getValue()) {
            return ColoringSystem.rgba(122, 122, 122, 255);
        }
        if (block == Blocks.REDSTONE_ORE && ores.is("Редстоун").getValue()) {
            return ColoringSystem.rgba(255, 82, 82, 255);
        }
        if (block == Blocks.REDSTONE_BLOCK && ores.is("Блок Редстоуна").getValue()) {
            return ColoringSystem.rgba(255, 82, 82, 255);
        }
        if (block == Blocks.GOLD_ORE && ores.is("Золото").getValue()) {
            return ColoringSystem.rgba(247, 255, 102, 255);
        }
        if (block == Blocks.GOLD_BLOCK && ores.is("Блок Золота").getValue()) {
            return ColoringSystem.rgba(247, 255, 102, 255);
        }
        if (block == Blocks.EMERALD_ORE && ores.is("Эмеральды").getValue()) {
            return ColoringSystem.rgba(116, 252, 101, 255);
        }
        if (block == Blocks.EMERALD_BLOCK && ores.is("Блок Эмеральда").getValue()) {
            return ColoringSystem.rgba(116, 252, 101, 255);
        }
        if (block == Blocks.DIAMOND_ORE && ores.is("Алмазы").getValue()) {
            return ColoringSystem.rgba(77, 219, 255, 255);
        }
        if (block == Blocks.DIAMOND_BLOCK && ores.is("Блок Алмаза").getValue()) {
            return ColoringSystem.rgba(77, 219, 255, 255);
        }
        if (block == Blocks.ANCIENT_DEBRIS && ores.is("Незерит").getValue()) {
            return ColoringSystem.rgba(105, 60, 12, 255);
        }
        if (block == Blocks.NETHERITE_BLOCK && ores.is("Блок Незерита").getValue()) {
            return ColoringSystem.rgba(5, 2, 2, 255);
        }
        if (block == Blocks.LAPIS_ORE && ores.is("Лазурит").getValue()) {
            return ColoringSystem.rgba(41, 41, 255, 255);
        }
        if (block == Blocks.LAPIS_BLOCK && ores.is("Блок Лазурита").getValue()) {
            return ColoringSystem.rgba(41, 41, 255, 255);
        }
        if (block == Blocks.NETHER_GOLD_ORE && ores.is("Адское Золото").getValue()) {
            return ColoringSystem.rgba(247, 255, 102, 255);
        }
        return -1;
    }
}
