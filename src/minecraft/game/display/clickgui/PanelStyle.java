package minecraft.game.display.clickgui;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import minecraft.game.operation.visual.ClickGui;
import minecraft.game.operation.visual.Hud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.glfw.GLFW;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.figures.Vector4i;
import minecraft.game.advantage.make.Cursors;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.make.font.Fonts;
import minecraft.game.advantage.make.other.Scissor;
import minecraft.game.advantage.make.other.Stencil;
import minecraft.game.operation.wamost.api.Category;
import minecraft.system.AG;
import minecraft.system.styles.Style;

@Getter
public class PanelStyle extends Panel {

    public PanelStyle(Category category) {
        super(category);
        // TODO Auto-generated constructor stub
    }

    float max = 0;
    protected final float width = 120;
    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        float header = 50 / 2f;
        float headerFont = 9;
        setAnimatedScrool(MathSystem.fast(getAnimatedScrool(), getScroll(), 5));

        GraphicsSystem.drawRoundedRect(x, y, width, header, new Vector4f(7, 0, 7, 0), ColoringSystem.rgba(255, 255, 255, 0));

        GraphicsSystem.drawRoundedRect(x, y, width, height - 15, new Vector4f(7, 7, 7, 7), ColoringSystem.rgba(15, 15, 15, (int) (0)));

        GraphicsSystem.drawRectHorizontalW(x, y, width, header, ColoringSystem.rgba(17, 17, 17, 0), ColoringSystem.rgba(17, 17, 17, 0));

        GraphicsSystem.drawRectVerticalW(x, y + header, width, 0.5f, ColoringSystem.rgba(24, 24, 30, 0), ColoringSystem.rgba(32, 34, 40, 0));
        Fonts.montserrat.drawCenteredText(stack, getCategory().name(), x + width / 2f, y + header / 2f - Fonts.montserrat.getHeight(headerFont) / 2f - 1, ColoringSystem.rgba(153, 153, 153, 0), headerFont, 0.1f);
        drawOutline();

        // Ограничение скролла по высоте
        if (max > height - header - 10) {
            setScroll(MathHelper.clamp(getScroll(), -max + height - header - 10, 0));
            setAnimatedScrool(MathHelper.clamp(getAnimatedScrool(), -max + height - header - 10, 0));
        } else {
            setScroll(0);
            setAnimatedScrool(0);
        }

        // Анимация
        float animationValue = (float) DropDown.getAnimation().getValue() * DropDown.scale;
        float halfAnimationValueRest = (1 - animationValue) / 2f;
        float height = getHeight();
        float testX = getX() + (getWidth() * halfAnimationValueRest);
        float testY = getY() + 65 / 2f + (height * halfAnimationValueRest);
        float testW = getWidth() * animationValue;
        float testH = height * animationValue;

        testX = testX * animationValue + ((Minecraft.getInstance().getMainWindow().getScaledWidth() - testW) * halfAnimationValueRest);
        Scissor.push();
        Scissor.setFromComponentCoordinates(testX, testY, testW, testH);

        int offset = 0;
        boolean hovered = false;
        for (Style style : AG.getInst().getStyleManager().getStyleList()) {
            float x = this.x + 6;
            float y = this.y + header + 5 + offset * (57 / 2f + 5) + getAnimatedScrool();

            // Рендерим только видимые элементы
            if (y + (57 / 2f) < this.y + header || y > this.y + height - 23) {
                offset++;
                continue;
            }

            if (MathSystem.isHovered(mouseX, mouseY, x + 5, y + 13, width - 10 - 40, 23 / 2f)) {
                hovered = true;
            }
            int alpha123 = ColoringSystem.rgb(0, 0, 0);

            GraphicsSystem.drawRoundedRect(x + 0.5f, y + 0.5f, width - 10, 55 / 2f - 1, new Vector4f(7, 7, 7, 7), ColoringSystem.setAlpha(ColoringSystem.rgb(15,15,15), (int) (alpha123 * 0.33)));
            Stencil.initStencilToWrite();

            GraphicsSystem.drawRoundedRect(x + 0.5f, y + 0.5f, width - 10, 55 / 2f - 1, new Vector4f(7, 7, 7, 7), ColoringSystem.setAlpha(ColoringSystem.rgb(15,15,15), (int) (alpha123 * 0.33)));

            Stencil.readStencilBuffer(0);

            Stencil.uninitStencilBuffer();

            Fonts.montserrat.drawText(stack, style.getStyleName(), x  +3 , y + 12, ColoringSystem.rgba(153,153,153, 255), 6f, 0.05f);

            y += 1;

            if (AG.getInst().getStyleManager().getCurrentStyle() == style) {
                GraphicsSystem.drawShadow(x + 55, y + 10, width  - 70, 15 / 2f, 8, style.getFirstColor().getRGB(), style.getSecondColor().getRGB());
            }
            GraphicsSystem.drawRoundedRect(x + 55, y + 10, width - 70, 15 / 2f, new Vector4f(4, 4, 4, 4), new Vector4i(style.getFirstColor().getRGB(), style.getFirstColor().getRGB(), style.getSecondColor().getRGB(), style.getSecondColor().getRGB()));
            offset++;
        }
        if (MathSystem.isHovered(mouseX, mouseY, x, y, width + 50, height)) {
            if (hovered) {
                GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
            } else {
                GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.HAND);
            }
        }
        Scissor.unset();
        Scissor.pop();
        max = offset * (57 / 2f + 5); // Размер области для прокрутки
    }

    @Override
    public void keyPressed(int key, int scanCode, int modifiers) {

    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int button) {
        float header = 55 / 2f;
        int offset = 0;
        for (Style style : AG.getInst().getStyleManager().getStyleList()) {
            float x = this.x + 5;
            float y = this.y + header + 5 + offset * (57 / 2f + 5) + getAnimatedScrool();
            if (MathSystem.isHovered(mouseX, mouseY, x + 5, y + 5, width - 10 - 10, 40 / 2f)) {
                AG.getInst().getStyleManager().setCurrentStyle(style);
            }
            offset++;
        }

    }

    @Override
    public void mouseRelease(float mouseX, float mouseY, int button) {

    }

}
