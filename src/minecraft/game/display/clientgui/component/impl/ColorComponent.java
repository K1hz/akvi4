package minecraft.game.display.clientgui.component.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import minecraft.game.advantage.advisee.SoundPlayer;
import minecraft.game.advantage.alacrity.AnimationManager;
import minecraft.game.advantage.alacrity.Direction;
import minecraft.game.advantage.alacrity.impl.EaseBackIn;
import minecraft.game.advantage.figures.Vector4i;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.words.font.ClientFonts;
import minecraft.game.display.clientgui.api.component.Component;
import minecraft.game.display.clientpanel2.ClickGuiScreen;
import minecraft.game.operation.wamost.massa.elements.ColorSetting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;

import java.awt.*;

public class ColorComponent extends Component {

    final ColorSetting setting;
    public static boolean opened;
    float colorRectX, colorRectY, colorRectWidth, colorRectHeight;
    float pickerX, pickerY, pickerWidth, pickerHeight;
    float sliderX, sliderY, sliderWidth, sliderHeight;

    final float padding = 5.5f;
    float textX, textY;
    private float[] hsb = new float[2];

    boolean panelOpened;
    boolean draggingHue, draggingPicker;

    final AnimationManager animation = new EaseBackIn(300, 1, 1);

    public ColorComponent(ColorSetting setting) {
        this.setting = setting;
        hsb = Color.RGBtoHSB(ColoringSystem.red(setting.getValue()), ColoringSystem.green(setting.getValue()), ColoringSystem.blue(setting.getValue()), null);

        animation.setDirection(!panelOpened ? Direction.BACKWARDS : Direction.FORWARDS);
        animation.setDuration(!panelOpened ? 400 : 300);
    }

    @Override
    public void drawComponent(MatrixStack stack, int mouseX, int mouseY) {
        renderTextAndColorRect(stack);

        animation.setDirection(!panelOpened ? Direction.BACKWARDS : Direction.FORWARDS);
        animation.setDuration(!panelOpened ? 400 : 300);

        GlStateManager.pushMatrix();
        GraphicsSystem.sizeAnimation(x+ (width / 2), (y +  height / 2), animation.getOutput());

        if (animation.getOutput() != 0.0) {
            this.setting.setValue(Color.getHSBColor(hsb[0], hsb[1], hsb[2]).getRGB());

            renderSlider(stack, mouseX, mouseY);
            renderPickerPanel(stack, mouseX, mouseY);

        }

        GlStateManager.popMatrix();

        super.render(stack, mouseX, mouseY);
    }

    private void renderTextAndColorRect(MatrixStack stack) {
        String settingName = setting.getName();
        int colorValue = ColoringSystem.setAlpha(setting.getValue(), (int) (255 * ClickGuiScreen.getGlobalAnim().getValue()));

        this.textX = this.x+ padding;
        this.textY = this.y + 3;

        // вывод цвета
        this.colorRectWidth = padding * 3f;
        this.colorRectHeight = padding * 1.5f;
        this.colorRectX = this.x+ width - colorRectWidth - padding;
        this.colorRectY = this.y + 2;

        // палитра
        this.pickerX = this.x+ padding;
        this.pickerY = this.y + (padding) + 8;
        this.pickerWidth = this.width - (padding * 2);
        this.pickerHeight = 30;

        // выбор цвета
        this.sliderX = pickerX;
        this.sliderY = pickerY + pickerHeight + padding;
        this.sliderWidth = pickerWidth;
        this.sliderHeight = 3;

        ClientFonts.tenacity[16].drawString(stack, settingName, textX, textY, ColoringSystem.setAlpha(-1, (int) (255 * ClickGuiScreen.getGlobalAnim().getValue())));
        GraphicsSystem.drawRoundedRect(this.colorRectX, this.colorRectY, this.colorRectWidth, this.colorRectHeight, 2f, colorValue);
        GraphicsSystem.drawShadow(this.colorRectX, this.colorRectY, this.colorRectWidth, this.colorRectHeight, 2, colorValue);
    }

    private void renderPickerPanel(MatrixStack stack, float mouseX, float mouseY) {
        Vector4i vector4i = new Vector4i(
                ColoringSystem.setAlpha(Color.WHITE.getRGB(), (int) (255 * ClickGuiScreen.getGlobalAnim().getValue())),
                ColoringSystem.setAlpha(Color.BLACK.getRGB(), (int) (255 * ClickGuiScreen.getGlobalAnim().getValue())),
                ColoringSystem.setAlpha(Color.getHSBColor(hsb[0], 1, 1).getRGB(), (int) (255 * ClickGuiScreen.getGlobalAnim().getValue())),
                ColoringSystem.setAlpha(Color.BLACK.getRGB(), (int) (255 * ClickGuiScreen.getGlobalAnim().getValue()))
        );

        float offset = 2;
        float xRange = pickerWidth - offset * 2;
        float yRange = pickerHeight - offset * 2;

        if (draggingPicker) {
            float saturation = MathHelper.clamp((mouseX - pickerX - offset), 0, xRange) / (xRange);
            float brightness = MathHelper.clamp((mouseY - pickerY - offset), 0, yRange) / (yRange);
            hsb[1] = saturation;
            hsb[2] = 1 - brightness;
        }

        GraphicsSystem.drawRoundedRect(this.pickerX, this.pickerY, this.pickerWidth, this.pickerHeight, new Vector4f(2, 2, 2, 2), vector4i);

        float circleX = pickerX + offset + hsb[1] * (xRange);
        float circleY = pickerY + offset + (1 - hsb[2]) * (yRange);

        GraphicsSystem.drawCircle(circleX, circleY, 6, ColoringSystem.setAlpha(Color.BLACK.getRGB(), (int) (255 * ClickGuiScreen.getGlobalAnim().getValue())));
        GraphicsSystem.drawCircle(circleX, circleY, 4, ColoringSystem.setAlpha(Color.WHITE.getRGB(), (int) (255 * ClickGuiScreen.getGlobalAnim().getValue())));
    }


    private void renderSlider(MatrixStack stack, float mouseX, float mouseY) {
        float slH = 3;
        for (int i = 0; i < sliderWidth; i++) {
            float hue = i / sliderWidth;
            GraphicsSystem.drawRoundedRect(this.sliderX + i, sliderY - slH / 2f, slH, sliderHeight, 1, ColoringSystem.setAlpha(Color.HSBtoRGB(hue, 1, 1), (int) (255 * ClickGuiScreen.getGlobalAnim().getValue())));

        }
        GraphicsSystem.drawCircle(this.sliderX + (hsb[0] * sliderWidth), this.sliderY, 6, ColoringSystem.rgba(0, 0, 0, (int) (255 * ClickGuiScreen.getGlobalAnim().getValue())));
        GraphicsSystem.drawCircle(this.sliderX + (hsb[0] * sliderWidth), this.sliderY, 4, ColoringSystem.setAlpha(-1, (int) (255 * ClickGuiScreen.getGlobalAnim().getValue())));
        if (draggingHue) {
            float hue = (mouseX - sliderX) / sliderWidth;
            hsb[0] = MathHelper.clamp(hue, 0,1);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouse) {
        if (GraphicsSystem.isInRegion(mouseX, mouseY, colorRectX, colorRectY, colorRectWidth, colorRectHeight) && mouse == 1) {
            panelOpened = !panelOpened;
            SoundPlayer.playSound(panelOpened ? "guicoloropen.wav" : "guicolorclose.wav", 0.25);
        }

        if (panelOpened && mouse == 0) {
            if (GraphicsSystem.isInRegion(mouseX, mouseY, sliderX, sliderY - 1.5f, sliderWidth, sliderHeight)) {
                draggingHue = true;
            } else if (GraphicsSystem.isInRegion(mouseX, mouseY, pickerX, pickerY, pickerWidth, pickerHeight) && animation.isDone()) {
                draggingPicker = true;
            }
        }

    }


    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouse) {
        if (mouse == 0) {
            if (draggingHue) {
                SoundPlayer.playSound("guicolorselect.wav", 0.25);
                draggingHue = false;
            }
            if (draggingPicker) {
                SoundPlayer.playSound("guicolorselect.wav", 0.25);
                draggingPicker = false;
            }
        }

    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {

    }

    @Override
    public void charTyped(char codePoint, int modifiers) {

    }


    public boolean isVisible() {
        return setting.visible.get();
    }

    public void close() {
        opened = false;
    }
}
