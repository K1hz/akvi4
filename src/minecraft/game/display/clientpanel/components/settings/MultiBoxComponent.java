package minecraft.game.display.clientpanel.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import minecraft.game.advantage.alacrity.impl.DecelerateAnimation;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.make.other.Scissor;
import minecraft.game.advantage.words.font.ClientFonts;
import minecraft.game.display.clientpanel.components.builder.Component;
import minecraft.game.operation.visual.Hud;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.ModeListSetting;
import minecraft.system.AG;

public class MultiBoxComponent extends Component {

    public static MultiBoxComponent currentOpened = null;
    public ModeListSetting option;
    public boolean opened;

    private final DecelerateAnimation heightAnimation;
    private final DecelerateAnimation elementAnimation;

    public MultiBoxComponent(ModeListSetting option) {
        this.option = option;
        this.setting = option;
        this.heightAnimation = new DecelerateAnimation(250, 1.0);
        this.elementAnimation = new DecelerateAnimation(200, 1.0);
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        float padding = 16;
        float yOffset = 3;
        float baseHeight = 14;
        float itemHeight = 16;

        heightAnimation.setDirection(opened);
        elementAnimation.setDirection(opened);

        
        double heightProgress = heightAnimation.getOutput();
        double elementProgress = elementAnimation.getOutput();

        ClientFonts.clickGui[14].drawString(stack, option.getName(), x + 8, y + yOffset, -1);

        float optionRectX = x + 6;
        float optionRectWidth = width / 1.1f;

        GraphicsSystem.drawRoundedRect(optionRectX, y + yOffset + 8, optionRectWidth, baseHeight, radius, ColoringSystem.rgba(16,16,16, 33));
        String displayText = !opened ? "Нажмите ПКМ Чтобы открыть" : "Нажмите ПКМ Чтобы закрыть";
        ClientFonts.clickGui[13].drawString(stack, displayText, optionRectX + 5, y + yOffset + 13, -1);

        float animatedHeight = (float) (option.getValue().size() * itemHeight * heightProgress);
        height = baseHeight + animatedHeight + (opened ? padding : 16);

        if (opened || !heightAnimation.isDone()) {
            float dropdownRectY = y + baseHeight + yOffset + 10;
            GraphicsSystem.drawRoundedRect(optionRectX, dropdownRectY, optionRectWidth, animatedHeight, radius, ColoringSystem.rgba(16,16,16, 33));

            int i = 0;
            for (CheckBoxSetting settingItem : option.getValue()) {
                float itemY = dropdownRectY + i * itemHeight;
                if (itemY > dropdownRectY + animatedHeight) break;
                int textColor = settingItem.getValue() ? -1 : 0xFFA3B0BC;
                int rectAlpha = (int) (255 * elementProgress);

                if (settingItem.getValue()) {
                    GraphicsSystem.drawRoundedRect(optionRectX, itemY, optionRectWidth, itemHeight, radiusOther, ColoringSystem.setAlpha(ColoringSystem.rgba(16,16,16, 1), 44));
                    float iconOffset = (float) (10 * (1 - elementProgress));
                    int iconAlpha = (int) (255 * elementProgress);
                    ClientFonts.settings_gui[30].drawString(stack, "A", optionRectX + optionRectWidth - 16 + iconOffset, itemY + 4, ColoringSystem.setAlpha(Hud.getColor(1), iconAlpha));
                }

                float textOffset = (float) (8 * (1 - elementProgress));
                int textAlpha = (int) (255 * elementProgress);
                float textX = optionRectX + 4 + textOffset;
                float textWidth = optionRectWidth - 20;
                ClientFonts.clickGui[14].drawString(stack, settingItem.getName(), textX + 2, itemY + 7, ColoringSystem.setAlpha(textColor, textAlpha));
                i++;
            }
        }
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {
        float yOffset = 3;
        float baseHeight = 14;
        float itemHeight = 16;

        float optionRectX = x + 6;
        float optionRectWidth = width / 1.1f;
        float optionRectY = y + yOffset + 8;

        if (MathSystem.isHovered(mouseX, mouseY, optionRectX, optionRectY, optionRectWidth, baseHeight)) {
            if (currentOpened != null && currentOpened != this) {
                currentOpened.close();
            }
            opened = !opened;
            currentOpened = opened ? this : null;
            return;
        }

        if (!opened) return;

        float dropdownStartY = y + baseHeight + yOffset + 10;

        int i = 0;
        for (CheckBoxSetting settingItem : option.getValue()) {
            float itemY = dropdownStartY + i * itemHeight;

            if (MathSystem.isHovered(mouseX, mouseY, optionRectX, itemY, optionRectWidth, itemHeight)) {
                settingItem.setValue(!settingItem.getValue());
                break;
            }
            i++;
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
    }


    public void keyTyped(int keyCode, int scanCode, int modifiers) {
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
    }
    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }
    public void close() {
        opened = false;
    }
}
