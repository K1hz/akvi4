// DropDown.java

package minecraft.game.display.clientpanel;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import lombok.Getter;
import minecraft.game.advantage.advisee.SoundPlayer;
import minecraft.game.advantage.make.font.Fonts;
import minecraft.game.operation.misc.SelfDestruct;
import minecraft.game.operation.visual.Hud;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;
import minecraft.game.advantage.advisee.ClientReceive;
import minecraft.game.advantage.advisee.IMinecraft;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.figures.Vec2i;
import minecraft.game.advantage.figures.Vector2i;
import minecraft.game.advantage.figures.Vector4i;
import minecraft.game.advantage.luvvy.UserPublic;
import minecraft.game.advantage.make.Cursors;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.make.other.CustomFramebuffer;
import minecraft.game.advantage.make.other.KawaseBlur;
import minecraft.game.advantage.make.other.Scissor;
import minecraft.game.advantage.make.other.Stencil;
import minecraft.game.advantage.words.font.ClientFonts;
import minecraft.game.display.clientpanel.components.DiscordUI;
import minecraft.game.display.clientpanel.components.SearchField;
import minecraft.game.operation.visual.ClickGui;
import minecraft.game.operation.wamost.api.Category;
import minecraft.system.AG;
import minecraft.system.managers.Theme;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DropDown extends Screen implements IMinecraft {


    ClickGui clickGui = AG.getInst().getModuleManager().getClickGui();
    public SearchField searchField;
    public DiscordUI discordUI;
    private static final Animation gradientAnimation = new Animation();
    private final List<Panel> panels = new ArrayList<>();
    @Getter

    private static Animation animation = new Animation();

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
        int windowWidth = ClientReceive.calc(mc.getMainWindow().getScaledWidth());
        int windowHeight = ClientReceive.calc(mc.getMainWindow().getScaledHeight());

        float x = (windowWidth / 2f) - (76);
        float y = windowHeight / 2.1f + (510 / 2f) / 2.1f + 30;
        ClickGui clickGui = AG.getInst().getModuleManager().getClickGui();


        discordUI = new DiscordUI((int)x,(int)y,50,50,5);
        searchField = new SearchField((int) x, (int) y + 45, 140, 20, "                   Нажмите для поиска");

        animation = animation.animate(1, 0f, Easings.EXPO_OUT);
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
        // TODO Auto-generated method stub
        Vec2i fixMouse = adjustMouseCoordinates((int) mouseX, (int) mouseY);

        Vector2i fix = ClientReceive.getMouse(fixMouse.getX(), fixMouse.getY());
        mouseX = fix.getX();
        mouseY = fix.getY();

        for (Panel panel : panels) {
            if (MathSystem.isHovered((float) mouseX, (float) mouseY, panel.getX(), panel.getY(), panel.getWidth(),
                    panel.getHeight())) {
                panel.setScroll((float) (panel.getScroll() + (delta * 20)));
            }
        }

        // System.out.println(delta + " " + scale + " " + mouseX + " " + mouseY))
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
        animation.update();
        gradientAnimation.update();
        if (animation.getValue() < 0.1 && gradientAnimation.getValue() < 0.1) {
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

        Vec2i fixMouse = adjustMouseCoordinates(mouseX, mouseY);

        Vector2i fix = ClientReceive.getMouse(fixMouse.getX(), fixMouse.getY());
        mouseX = fix.getX();
        mouseY = fix.getY();


        if (clickGui.gradient.getValue()) {
            GraphicsSystem.drawRectHorizontalW(0, 0 - scaled().y / 4, Minecraft.getInstance().getMainWindow().getScaledWidth(), (Minecraft.getInstance().getMainWindow().getScaledHeight() + scaled().y / 4) / gradientAnimation.getValue(), ColoringSystem.setAlpha(ColoringSystem.rgb(20, 20, 20), (int) (120 * gradientAnimation.getValue())), ColoringSystem.setAlpha(Hud.getColor(1), 120));
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
        if (ClickGui.info.getValue()) {
            float x = mainWindow.getScaledWidth() / 2 - (115);
            float y = mainWindow.getScaledHeight() / 2 - 220;
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String timeString = sdf.format(new Date());
            String time = timeString;
            SimpleDateFormat fff = new SimpleDateFormat("dd:MM:yyyy");
            String dateString = fff.format(new Date());
            String date = dateString;
        }








        for (Panel panel : panels) {
            float windowCenterX = windowWidth / 2f + 10; // Центр экрана
            float panelTotalWidth = panels.size() * (137.3f + off); // Общая ширина всех панелей

            panel.setY(windowHeight / 2f - (700 / 2) / 2f);

            // Координата X панели, чтобы она была выровнена по центру экрана
            panel.setX(windowCenterX - (panelTotalWidth / 2f) + panel.getCategory().ordinal() * (131 + off));

            float animationValue = (float) animation.getValue() * scale;
            float halfAnimationValueRest = (1 - animationValue) / 2f;

            float testX = panel.getX() + (panel.getWidth() * halfAnimationValueRest);
            float testY = panel.getY() + (panel.getHeight() * halfAnimationValueRest);
            float testW = panel.getWidth() * animationValue;
            float testH = panel.getHeight() * animationValue;

            testX = testX * animationValue + ((windowWidth - testW) * halfAnimationValueRest);

            Scissor.push();
            Scissor.setFromComponentCoordinates(testX - 9, testY - 9, testW + 20, testH + 20);
            panel.render(matrixStack, mouseX, mouseY);
            Scissor.unset();
            Scissor.pop();
        }
        float x = (windowWidth / 2f) - (76);
        float y = windowHeight / 2.1f + (510 / 2f) / 2.1f + 30;
        KawaseBlur.blur.render(() -> {
            GraphicsSystem.drawRoundedRect((int) x, (int) y - 345, 140, 20, new Vector4f(5, 5, 5, 5), new Vector4i(
                    ColoringSystem.setAlpha(ColoringSystem.rgb(11, 11, 11), 145),
                    ColoringSystem.setAlpha(ColoringSystem.rgb(34, 34, 34), 145),
                    ColoringSystem.setAlpha(ColoringSystem.rgb(11, 11, 11), 145),
                    ColoringSystem.setAlpha(ColoringSystem.rgb(34, 34, 34), 145)
            ));
        });
        GraphicsSystem.drawRoundedRect((int) x, (int) y - 345, 140, 20, new Vector4f(5, 5, 5, 5), new Vector4i(
                ColoringSystem.setAlpha(ColoringSystem.rgb(11, 11, 11), 145),
                ColoringSystem.setAlpha(ColoringSystem.rgb(34, 34, 34), 145),
                ColoringSystem.setAlpha(ColoringSystem.rgb(11, 11, 11), 145),
                ColoringSystem.setAlpha(ColoringSystem.rgb(34, 34, 34), 145)
        ));
        ClientFonts.clickGui[15].drawString(matrixStack, "                Безопасный режим", (int) x, (int) y - 337, ColoringSystem.rgb(255, 255, 255));
        searchField.render(matrixStack, mouseX, mouseY, partialTicks);
        GlStateManager.popMatrix();
        mc.gameRenderer.setupOverlayRendering();


    }

    private void updateScaleBasedOnScreenWidth() {
        final float PANEL_WIDTH = 130;
        final float MARGIN = 14;
        float MIN_SCALE = 0.76f;
        float DEFAULT_SCALE = 0.94f;

        float totalPanelWidth = panels.size() * (PANEL_WIDTH + MARGIN);
        float screenWidth = mc.getMainWindow().getWidth();
        float screenHeight = mc.getMainWindow().getHeight();

        if (screenWidth <= 1820 || screenHeight <= 980) {
            scale = MIN_SCALE;
        } else if (totalPanelWidth >= screenWidth) {
            scale = MathHelper.clamp(screenWidth / totalPanelWidth, MIN_SCALE, DEFAULT_SCALE);
        } else {
            scale = DEFAULT_SCALE;
        }
    }
    public boolean isSearching() {
        return !searchField.isEmpty();
    }

    public String getSearchText() {
        return searchField.text;
    }

    public boolean searchCheck(String text) {
        return isSearching() && !text
                .replaceAll(" ", "")
                .toLowerCase()
                .contains(getSearchText()
                        .replaceAll(" ", "")
                        .toLowerCase());
    }
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (Panel panel : panels) {
            panel.keyPressed(keyCode, scanCode, modifiers);
        }
        // TODO Auto-generated method stub
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            animation = animation.animate(0, 0f, Easings.EXPO_OUT);
            return false;
        }
        if (searchField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private Vec2i adjustMouseCoordinates(int mouseX, int mouseY) {
        int windowWidth = mc.getMainWindow().getScaledWidth();
        int windowHeight = mc.getMainWindow().getScaledHeight();

        float adjustedMouseX = (mouseX - windowWidth / 2f) / scale + windowWidth / 2f;
        float adjustedMouseY = (mouseY - windowHeight / 2f) / scale + windowHeight / 2f;

        return new Vec2i((int) adjustedMouseX, (int) adjustedMouseY);
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
        Vec2i fixMouse = adjustMouseCoordinates((int) mouseX, (int) mouseY);
        Vector2i fix = ClientReceive.getMouse(fixMouse.getX(), fixMouse.getY());
        mouseX = fix.getX();
        mouseY = fix.getY();

        float x = (mc.getMainWindow().getScaledWidth() / 2f) - 76;
        float y = mc.getMainWindow().getScaledHeight() / 2.1f + (510 / 2f) / 2.1f + 30 - 345;
        int width = 140;
        int height = 20;

        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            AG.getInst().getModuleManager().getSelfDestruct().toggle();
            closeScreen();
            SoundPlayer.playSound("warning.wav");
            return true;
        }

        if (searchField.isEmpty()) {
            for (Panel panel : panels) {
                boolean isHovered = MathSystem.isHovered((float) mouseX, (float) mouseY, panel.getX(), panel.getY(), panel.getWidth(), panel.getHeight());
                boolean isVisible = panel.getX() + panel.getWidth() > 0 && panel.getX() < mc.getMainWindow().getScaledWidth() &&
                        panel.getY() + panel.getHeight() > 0 && panel.getY() < mc.getMainWindow().getScaledHeight();

                if (!searchCheck(panel.getCategory().name()) && isHovered && isVisible) {
                    panel.mouseClick((float) mouseX, (float) mouseY, button);
                    return true;
                }
            }
        }

        if (!searchField.isEmpty()) {
            for (Panel panel : panels) {
                panel.mouseClick((float) mouseX, (float) mouseY, button);
            }
        }

        if (searchField.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        return false;
    }




    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        Vec2i fixMouse = adjustMouseCoordinates((int) mouseX, (int) mouseY);

        Vector2i fix = ClientReceive.getMouse(fixMouse.getX(), fixMouse.getY());
        mouseX = fix.getX();
        mouseY = fix.getY();

        // Проверяем попадание клика в области каждой панели
        for (Panel panel : panels) {
            if (isMouseWithinBounds(mouseX, mouseY, panel)) {
                panel.mouseRelease((float) mouseX, (float) mouseY, button);
                return true; // Если был клик в панели, прекращаем обработку
            }
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    /**
     * Проверяет, находится ли курсор мыши в границах панели.
     */
    private boolean isMouseWithinBounds(double mouseX, double mouseY, Panel panel) {
        return mouseX >= panel.getX() && mouseX <= panel.getX() + panel.getWidth()
                && mouseY >= panel.getY() && mouseY <= panel.getY() + panel.getHeight();
    }


}
