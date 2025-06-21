// DropDown.java

package minecraft.game.display.clickgui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import lombok.Getter;
import minecraft.game.advantage.advisee.ClientReceive;
import minecraft.game.advantage.advisee.IMinecraft;
import minecraft.game.advantage.alacrity.easing.CompactAnimation;
import minecraft.game.advantage.alacrity.easing.Easing;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.figures.TimeCounterSetting;
import minecraft.game.advantage.figures.Vector2i;
import minecraft.game.advantage.make.Cursors;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.make.font.Fonts;
import minecraft.game.advantage.make.other.*;
import minecraft.game.advantage.words.MoreColorsSystem;
import minecraft.game.advantage.words.font.ClientFonts;
import minecraft.game.display.clickgui.components.SearchField;
import minecraft.game.operation.visual.ClickGui;
import minecraft.game.operation.wamost.api.Category;
import minecraft.system.AG;
import minecraft.system.managers.Theme;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class DropDown extends Screen implements IMinecraft {

    public SearchField searchField;


    private static final Animation gradientAnimation = new Animation();
    private final List<Panel> panels = new ArrayList<>();

    @Getter
    private static final Animation globalAnim = new Animation();
    @Getter
    private static Animation animation = new Animation();
    @Getter
    private static final Animation imageAnimation = new Animation();
    private boolean exit = false, open = false;

    private static final CompactAnimation scaleAnimation = new CompactAnimation(Easing.EASE_IN_QUAD, 200);
    private static final CompactAnimation psChanAnimation = new CompactAnimation(Easing.LINEAR, 700);
    private static final CompactAnimation psChanOverlayAnimation = new CompactAnimation(Easing.LINEAR, 1400);
    private final TimeCounterSetting psChatYAnimTimer = new TimeCounterSetting();
    private final TimeCounterSetting psChatOverlayAnimTimer = new TimeCounterSetting();

    public DropDown(ITextComponent titleIn) {
        super(titleIn);
        for (Category category : Category.values()) {
            panels.add(new Panel(category));
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        gradientAnimation.animate(1, 0.25f, Easings.EXPO_OUT);
        imageAnimation.animate(1, 0.5, Easings.BACK_OUT);
        int windowWidth = ClientReceive.calc(mc.getMainWindow().getScaledWidth());
        int windowHeight = ClientReceive.calc(mc.getMainWindow().getScaledHeight());

        float x = (windowWidth / 2f) - (60);
        float y = windowHeight / 2.1f + (510 / 2f) / 2.1f + 30;
        ClickGui clickGui = AG.getInst().getModuleManager().getClickGui();

        searchField = new SearchField((int) x, (int) y, 120, 20, "Поиск");

        exit = false;
        open = true;

        animation.animate(1, 0.25f, Easings.EXPO_OUT);
        super.init();
    }

    public static float scale = 1.0f;



    @Override
    public void closeScreen() {
        super.closeScreen();
        GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        Vector2i fixMouse = adjustMouseCoordinates((int) mouseX, (int) mouseY);

        Vector2i fix = ClientReceive.getMouse(fixMouse.getX(), fixMouse.getY());
        mouseX = fix.getX();
        mouseY = fix.getY();

        for (Panel panel : panels) {
            if (MathSystem.isHovered((float) mouseX, (float) mouseY, panel.getX(), panel.getY(), panel.getWidth(),
                    panel.getHeight())) {
                panel.setScroll((float) (panel.getScroll() + (delta * 20)));
            }
        }

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (searchField.charTyped(codePoint, modifiers)) {
            return true;
        }

        for (Panel panel : panels) {
            panel.charTyped(codePoint, modifiers);
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        ClickGui clickGui = AG.getInst().getModuleManager().getClickGui();
        KawaseBlur.blur.updateBlur(3, 3);
        mc.gameRenderer.setupOverlayRendering(2);
        Stream.of(animation, imageAnimation, gradientAnimation).forEach(Animation::update);
        boolean allow = !(animation.getValue() > 0.4);

        if (Stream.of(animation, imageAnimation, gradientAnimation).allMatch(anim -> anim.getValue() <= 0.1 && anim.isDone())) {
            closeScreen();
        }

        if (animation.getValue() < 0.1) {
            closeScreen();
        }


        final float off = 10;
        float width = panels.size() * (115 + off);

        updateScaleBasedOnScreenWidth();

        int windowWidth = ClientReceive.calc(mc.getMainWindow().getScaledWidth());
        int windowHeight = ClientReceive.calc(mc.getMainWindow().getScaledHeight());

        Vector2i fixMouse = adjustMouseCoordinates(mouseX, mouseY);

        Vector2i fix = ClientReceive.getMouse(fixMouse.getX(), fixMouse.getY());
        mouseX = fix.getX();
        mouseY = fix.getY();

        if (clickGui.gradient.getValue()) {
            GraphicsSystem.drawRectHorizontalW(0, 0 - scaled().y / 4, Minecraft.getInstance().getMainWindow().getScaledWidth(), (Minecraft.getInstance().getMainWindow().getScaledHeight() + scaled().y / 3) / gradientAnimation.getValue(), ColoringSystem.setAlpha(ColoringSystem.getColor(1), (int) (55 * gradientAnimation.getValue())),ColoringSystem.rgba(0,0,0,0 ));
        }





        Stencil.initStencilToWrite();
        GlStateManager.pushMatrix();
        GlStateManager.translatef(windowWidth / 2f, windowHeight / 2f, 0);
        GlStateManager.scaled(animation.getValue(), animation.getValue(), 1);
        GlStateManager.scaled(scale, scale, 1);
        GlStateManager.translatef(-windowWidth / 2f, -windowHeight / 2f, 0);



        GlStateManager.popMatrix();
        Stencil.readStencilBuffer(1);
        GlStateManager.bindTexture(KawaseBlur.blur.BLURRED.framebufferTexture);
        CustomFramebuffer.drawTexture();
        Stencil.uninitStencilBuffer();


        GlStateManager.pushMatrix();
        GlStateManager.translatef(windowWidth / 2f, windowHeight / 2f, 0);
        GlStateManager.scaled(animation.getValue(), animation.getValue(), 1);
        GlStateManager.scaled(scale, scale, 1);
        GlStateManager.translatef(-windowWidth / 2f, -windowHeight / 2f, 0);
        MainWindow mainWindow = mc.getMainWindow();

        for (Panel panel : panels) {
            panel.setY(windowHeight / 2f - (700 / 2) / 2f);
            panel.setX((windowWidth / 2f - 5) - (width / 2f - 390) + panel.getCategory().ordinal() *
                    (110 + off) + off / 2f);
            float animationValue = (float) animation.getValue() * scale;

            float halfAnimationValueRest = (1 - animationValue) / 2f;

            float testX = panel.getX() + (panel.getWidth() * halfAnimationValueRest);
            float testY = panel.getY() + (panel.getHeight() * halfAnimationValueRest);
            float testW = panel.getWidth() * animationValue;
            float testH = panel.getHeight() * animationValue;

            testX = testX * animationValue + ((windowWidth - testW) *
                    halfAnimationValueRest);

            Scissor.push();
            Scissor.setFromComponentCoordinates(testX - 9, testY-9, testW+20, testH+20);
            panel.render(matrixStack, mouseX, mouseY);
            Scissor.unset();
            Scissor.pop();

        }

            searchField.render(matrixStack, mouseX, mouseY, partialTicks);



        GlStateManager.popMatrix();
        mc.gameRenderer.setupOverlayRendering();


    }

    private void updateScaleBasedOnScreenWidth() {
        final float PANEL_WIDTH = 95;
        final float MARGIN = 10;
        float MIN_SCALE = 0.5f;
        float totalPanelWidth = panels.size() * (PANEL_WIDTH + MARGIN);
        float screenWidth = mc.getMainWindow().getScaledWidth();

        if (totalPanelWidth >= screenWidth) {
            scale = screenWidth / totalPanelWidth;
            scale = MathHelper.clamp(scale, MIN_SCALE, 1.0f);
        } else {
            scale = 1f;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

            if (searchField.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }

        for (Panel panel : panels) {
            panel.keyPressed(keyCode, scanCode, modifiers);
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            animation = animation.animate(0, 0.25f, Easings.EXPO_OUT);
            imageAnimation.animate(0.0, 0.3, Easings.BACK_OUT);
            return false;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private Vector2i adjustMouseCoordinates(int mouseX, int mouseY) {
        int windowWidth = mc.getMainWindow().getScaledWidth();
        int windowHeight = mc.getMainWindow().getScaledHeight();

        float adjustedMouseX = (mouseX - windowWidth / 2f) / scale + windowWidth / 2f;
        float adjustedMouseY = (mouseY - windowHeight / 2f) / scale + windowHeight / 2f;

        return new Vector2i((int) adjustedMouseX, (int) adjustedMouseY);
    }

    private double pathX(float mouseX, float scale) {
        if (scale == 1) return mouseX;
        int windowWidth = mc.getMainWindow().scaledWidth();
        int windowHeight = mc.getMainWindow().scaledHeight();
        mouseX /= (scale);
        mouseX -= (windowWidth / 2f) - (windowWidth / 2f) * (scale);
        return mouseX;
    }

    private double pathY(float mouseY, float scale) {
        if (scale == 1) return mouseY;
        int windowWidth = mc.getMainWindow().scaledWidth();
        int windowHeight = mc.getMainWindow().scaledHeight();
        mouseY /= scale;
        mouseY -= (windowHeight / 2f) - (windowHeight / 2f) * (scale);
        return mouseY;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vector2i fixMouse = adjustMouseCoordinates((int) mouseX, (int) mouseY);

        Vector2i fix = ClientReceive.getMouse(fixMouse.getX(), fixMouse.getY());
        mouseX = fix.getX();
        mouseY = fix.getY();

            if (searchField.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }

        for (Panel panel : panels) {
            panel.mouseClick((float) mouseX, (float) mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        Vector2i fixMouse = adjustMouseCoordinates((int) mouseX, (int) mouseY);

        Vector2i fix = ClientReceive.getMouse(fixMouse.getX(), fixMouse.getY());
        mouseX = fix.getX();
        mouseY = fix.getY();
        for (Panel panel : panels) {
            panel.mouseRelease((float) mouseX, (float) mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

}
