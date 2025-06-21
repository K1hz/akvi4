package minecraft.game.display.clientpanel.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;
import minecraft.game.advantage.figures.Vector4i;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.make.font.Fonts;
import minecraft.game.display.clientpanel.components.builder.Component;
import minecraft.game.operation.wamost.massa.elements.ColorSetting;

import java.awt.*;

public class ColorComponent extends Component {

    final ColorSetting colorSetting;

    float colorRectX, colorRectY, colorRectWidth, colorRectHeight;
    float pickerX, pickerY, pickerWidth, pickerHeight;
    float sliderX, sliderY, sliderWidth, sliderHeight;

    final float padding = 5;
    float textX, textY;
    final int textColor = -1;
    private float[] hsb = new float[2];

    boolean panelOpened;
    boolean draggingHue, draggingPicker;

    public ColorComponent(ColorSetting colorSetting) {
        this.colorSetting = colorSetting;
        hsb = Color.RGBtoHSB(ColoringSystem.IntColor.getRed(colorSetting.getValue()), ColoringSystem.IntColor.getGreen(colorSetting.getValue()), ColoringSystem.IntColor.getBlue(colorSetting.getValue()), null);
        setHeight(22);
    }


    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        renderTextAndColorRect(stack);

        if (panelOpened) {
            this.colorSetting.setValue(Color.getHSBColor(hsb[0], hsb[1], hsb[2]).getRGB());
            renderSlider(mouseX, mouseY);
            renderPickerPanel(mouseX, mouseY);
            setHeight(22 - 4 + pickerHeight + padding);
        } else {
            setHeight(22 - 4);
        }

        super.render(stack, mouseX, mouseY);
    }

    private void renderTextAndColorRect(MatrixStack stack) {
        String settingName = colorSetting.getName();
        int colorValue = colorSetting.getValue();

        // getX() + 5, getY() + 4.5f / 2f + 1
        this.textX = this.getX() + padding;
        this.textY = this.getY() + 6.5f / 2f;

        this.colorRectX = this.getX() + getWidth() - colorRectWidth - padding;
        this.colorRectY = this.getY() + (padding) - 4;
        this.colorRectWidth = (padding * 4);
        this.colorRectHeight = padding * 2;

        this.pickerX = this.getX() + padding;
        this.pickerY = this.getY() + (padding) + 16 - 4;
        this.pickerWidth = this.getWidth() - (padding * 4);
        this.pickerHeight = 60;

        this.sliderX = pickerX + pickerWidth + padding;
        this.sliderY = pickerY;
        this.sliderWidth = 3;
        this.sliderHeight = pickerHeight;


        Fonts.montserrat.drawText(stack, settingName, textX, textY, textColor, 6.5f, 0.02f);
        GraphicsSystem.drawRoundedRect(this.colorRectX, this.colorRectY, this.colorRectWidth, this.colorRectHeight, 3.5f, colorValue);
    }

    private void renderPickerPanel(float mouseX, float mouseY) {
        Vector4i vector4i = new Vector4i(Color.WHITE.getRGB(),
                Color.BLACK.getRGB(),
                Color.getHSBColor(hsb[0], 1, 1).getRGB(),
                Color.BLACK.getRGB());

        float offset = 4;
        float xRange = pickerWidth - 8;
        float yRange = pickerHeight - 8;

        if (draggingPicker) {
            float saturation = MathHelper.clamp((mouseX - pickerX - offset), 0, xRange) / (xRange);
            float brightness = MathHelper.clamp((mouseY - pickerY - offset), 0, yRange) / (yRange);
            hsb[1] = saturation;
            hsb[2] = 1 - brightness;
        }

        GraphicsSystem.drawRoundedRect(this.pickerX, this.pickerY, this.pickerWidth, this.pickerHeight, new Vector4f(6, 6, 6, 6), vector4i);

        float circleX = pickerX + offset + hsb[1] * (xRange);
        float circleY = pickerY + offset + (1 - hsb[2]) * (yRange);

        GraphicsSystem.drawCircle(circleX, circleY, 8, Color.BLACK.getRGB());
        GraphicsSystem.drawCircle(circleX, circleY, 6, Color.WHITE.getRGB());
    }


    private void renderSlider(float mouseX, float mouseY) {
        for (int i = 0; i < sliderHeight; i++) {
            float hue = i / sliderHeight;
            GraphicsSystem.drawCircle(this.sliderX + 1f, sliderY + i, 3, Color.HSBtoRGB(hue, 1, 1));

        }
        GraphicsSystem.drawCircle(this.sliderX + sliderWidth - 2F, this.sliderY + (hsb[0] * sliderHeight), 8, Color.BLACK.getRGB());
        GraphicsSystem.drawCircle(this.sliderX + sliderWidth - 2F, this.sliderY + (hsb[0] * sliderHeight), 6, -1);
        if (draggingHue) {
            float hue = (mouseY - sliderY) / sliderHeight;
            hsb[0] = MathHelper.clamp(hue, 0,1);
        }
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {
        if (GraphicsSystem.isInRegion(mouseX, mouseY, colorRectX, colorRectY, colorRectWidth, colorRectHeight) && mouse == 1) {
            panelOpened = !panelOpened;
        }

        if (panelOpened) {
            if (GraphicsSystem.isInRegion(mouseX, mouseY, sliderX - 2, sliderY, sliderWidth + 4, pickerHeight - 12)) {
                draggingHue = true;
            } else if (GraphicsSystem.isInRegion(mouseX, mouseY, pickerX, pickerY, pickerWidth, pickerHeight)) {
                draggingPicker = true;
            }
        }

        super.mouseClick(mouseX, mouseY, mouse);
    }


    @Override
    public void mouseRelease(float mouseX, float mouseY, int mouse) {
        if (draggingHue) {
            draggingHue = false;
        }
        if (draggingPicker) {
            draggingPicker = false;
        }
        super.mouseRelease(mouseX, mouseY, mouse);
    }
    @Override
    public boolean isVisible() {
        return colorSetting.visible.get();
    }
}
