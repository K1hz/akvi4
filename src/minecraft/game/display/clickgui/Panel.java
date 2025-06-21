package minecraft.game.display.clickgui;

import com.mojang.blaze3d.matrix.MatrixStack;


import lombok.Getter;
import lombok.Setter;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.make.other.KawaseBlur;
import minecraft.game.advantage.make.other.Scissor;
import minecraft.game.advantage.make.other.Stencil;
import minecraft.game.advantage.words.font.ClientFonts;
import minecraft.game.display.clickgui.components.ModuleComponent;
import minecraft.game.display.clickgui.components.builder.Component;
import minecraft.game.display.clickgui.components.builder.IBuilder;
import minecraft.game.operation.visual.ClickGui;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.system.AG;
import minecraft.system.managers.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Getter
@Setter
public class Panel implements IBuilder {

    private final Category category;
    protected float x;
    protected float y;
    protected final float width = 120;
    protected final float height = 600 / 2f;
    private float scroll, animatedScrool;
    private boolean draggingScrollbar = false;
    private float lastMouseY;
    private final Vector4f ROUNDING_VECTOR = new Vector4f(8.0f, 8.0f, 8.0f, 8.0f);

    private List<ModuleComponent> modules = new ArrayList<>();
    private GraphicsSystem RectUtils;


    public Panel(Category category) {
        this.category = category;

        for (Module function : AG.getInst().getModuleManager().getModules()) {
            if (function.getCategory() == category) {
                ModuleComponent component = new ModuleComponent(function);
                component.setPanel(this);
                modules.add(component);
            }
        }

        Collections.sort(modules, new Comparator<ModuleComponent>() {
            @Override
            public int compare(ModuleComponent o1, ModuleComponent o2) {
                return o1.getModule().getName().compareToIgnoreCase(o2.getModule().getName());
            }
        });
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        ClickGui clickGui = AG.getInst().getModuleManager().getClickGui();
        animatedScrool = MathSystem.fast(animatedScrool, scroll, 10);
        float header = 55 / 2.3f;
        float headerFont = 9;
        GraphicsSystem.drawShadow(x, y-1, width, height+3, 10, ColoringSystem.reAlphaInt(ColoringSystem.rgba(30,30,30, 255), ColoringSystem.rgba(30,30,30, 255)));


        GraphicsSystem.drawRoundedRect(x, y, width, height, 7,
                ColoringSystem.rgba(15,15,15, 210));

        float textWidth = ClientFonts.msMedium[24].getWidth(category.name());
        float centerX = x + width / 2f;
        float centerY = y + header / 2f - ClientFonts.msMedium[18].getFontHeight() / 2f;

        ClientFonts.msMedium[24].drawString(stack, category.name(), centerX - textWidth / 2, centerY + 3, ColoringSystem.rgba(153, 153, 153, 255));

        drawComponents(stack, mouseX, mouseY);

        drawOutline();
    }

    protected void drawOutline() {
        Stencil.initStencilToWrite();

        GraphicsSystem.drawRoundedRect(x + 0.5f, y + 0.5f, width - 1, height - 1, new Vector4f(7, 7, 7, 7),
                ColoringSystem.rgba(21, 21, 21, (int) (255 * 0.33)));

        Stencil.readStencilBuffer(0);

        Stencil.uninitStencilBuffer();
    }

    float max = 0;

    private void drawComponents(MatrixStack stack, float mouseX, float mouseY) {
        float animationValue = (float) DropDown.getAnimation().getValue() * DropDown.scale;

        float halfAnimationValueRest = (1 - animationValue) / 2f;
        float height = getHeight();
        float testX = getX() + (getWidth() * halfAnimationValueRest);
        float testY = getY() + 55 / 2f + (height * halfAnimationValueRest);
        float testW = getWidth() * animationValue;
        float testH = height * animationValue;

        testX = testX * animationValue + ((Minecraft.getInstance().getMainWindow().getScaledWidth() - testW) *
                halfAnimationValueRest);

        Scissor.push();
        Scissor.setFromComponentCoordinates(testX, testY, testW, testH-34);
        float offset = 0;
        float header = 55 / 2f;

        if (max > height - header - 10) {
            scroll = MathHelper.clamp(scroll, -max + height - header - 10, 0);
            animatedScrool = MathHelper.clamp(animatedScrool, -max + height - header - 10, 0);
        }
        else {
            scroll = 0;
            animatedScrool = 0;
        }
        for (ModuleComponent component : modules) {

            component.setX(getX() + 6.5f);
            component.setY(getY() + header + offset  + animatedScrool);
            component.setWidth(getWidth() - 13);
            component.setHeight(20);
            component.animation.update();
            if (component.animation.getValue() > 0) {
                float componentOffset = 0;
                for (Component component2 : component.getComponents()) {
                    if (component2.isVisible())
                        componentOffset += component2.getHeight();
                }

                componentOffset *= component.animation.getValue();
                component.setHeight(component.getHeight() + componentOffset);
            }
            component.render(stack, mouseX, mouseY);
            offset += component.getHeight() + 3.5f;
            Scissor.setFromComponentCoordinates(testX, testY, testW, testH);
        }
        animatedScrool = MathSystem.fast(animatedScrool, scroll, 10);
        float scrollbarHeight = MathHelper.clamp((height - header - 10) * (height - header - 10) / max, 10, height - header - 10);
        float scrollbarY = getY() + header + (-getScroll() / (max - height + header + 4)) * (height - header - 4 - scrollbarHeight);
        scrollbarHeight = MathHelper.clamp(scrollbarHeight, 10, height - header - 10);
        scrollbarY = MathHelper.clamp(scrollbarY, getY() + header, getY() + height - scrollbarHeight - 4);
        if (max > height - header - 10) {
            setScroll(MathHelper.clamp(getScroll(), -max + height - header - 70, 0));
            setAnimatedScrool(MathHelper.clamp(animatedScrool, -max + height - header - 50, 0));

            if (scroll >= 0) {
                setScroll(0);
                setAnimatedScrool(0);
            }


        } else {
            setScroll(0);
            setAnimatedScrool(0);
        }

        max = offset;

        Scissor.unset();
        Scissor.pop();

    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int button) {

        if (button == 0) { // ЛКМ
            float header = 55 / 2f;
            float scrollbarHeight = MathHelper.clamp((height - header - 10) * (height - header - 10) / max, 10, height - header - 10);
            float scrollbarY = getY() + header + (-getScroll() / (max - height + header + 4)) * (height - header - 4 - scrollbarHeight);
            scrollbarHeight = MathHelper.clamp(scrollbarHeight, 20, height - header - 10);
            scrollbarY = MathHelper.clamp(scrollbarY, getY() + header, getY() + height - scrollbarHeight - 4);

            if (mouseX >= getX() + getWidth() - 2.5f && mouseX <= getX() + getWidth() + 1.0f && mouseY >= scrollbarY && mouseY <= scrollbarY + scrollbarHeight) {
                draggingScrollbar = true;
                lastMouseY = mouseY;
            }
        }
    }

    @Override
    public void keyPressed(int key, int scanCode, int modifiers) {
        for (ModuleComponent component : modules) {
            component.keyPressed(key, scanCode, modifiers);
        }
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
        for (ModuleComponent component : modules) {
            component.charTyped(codePoint, modifiers);
        }
    }

    @Override
    public void mouseRelease(float mouseX, float mouseY, int button) {
        for (ModuleComponent component : modules) {
            component.mouseRelease(mouseX, mouseY, button);
        }
        if (button == 0) { // ЛКМ
            draggingScrollbar = false;
        }

    }

}