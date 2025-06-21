package minecraft.game.display.clientpanel2;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.Setter;
import minecraft.game.advantage.advisee.ClientReceive;
import minecraft.game.advantage.advisee.IMinecraft;
import minecraft.game.advantage.advisee.SoundPlayer;
import minecraft.game.advantage.alacrity.easing.CompactAnimation;
import minecraft.game.advantage.alacrity.easing.Easing;
import minecraft.game.advantage.figures.TimeCounterSetting;
import minecraft.game.advantage.figures.Vector2i;
import minecraft.game.advantage.make.Cursors;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.make.other.KawaseBlur;
import minecraft.game.advantage.make.other.OpenGLSystem;
import minecraft.game.display.clientpanel.PanelStyle;
import minecraft.game.display.clientpanel2.components.ModuleComponent;
import minecraft.game.operation.visual.ClickGui;
import minecraft.game.operation.wamost.api.Category;
import minecraft.system.AG;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Getter
public class ClickGuiScreen extends Screen implements IMinecraft {
    private static final CompactAnimation yGuiAnimation = new CompactAnimation(Easing.EASE_OUT_EXPO, 1L);
    private static final CompactAnimation xGuiAnimation = new CompactAnimation(Easing.EASE_OUT_EXPO, 1L);

    @Getter
    private static final Animation globalAnim = new Animation();
    @Getter
    private static final Animation imageAnimation = new Animation();
    @Getter
    private static final Animation gradientAnimation = new Animation();
    private static final CompactAnimation scaleAnimation = new CompactAnimation(Easing.EASE_IN_QUAD, 200);
    private static final CompactAnimation psChanAnimation = new CompactAnimation(Easing.LINEAR, 700);
    private static final CompactAnimation psChanOverlayAnimation = new CompactAnimation(Easing.LINEAR, 1400);
    public static float scale = 1F;

    private final List<Panel> panels = new ArrayList<>();
    @Setter
    @Getter
    private ModuleComponent expandedModule = null;
    private float updownPanel = 40;
    private float movePanel = 0;
    public SearchField searchField;
    private boolean exit = false, open = false;

    private final TimeCounterSetting psChatYAnimTimer = new TimeCounterSetting();
    private final TimeCounterSetting psChatOverlayAnimTimer = new TimeCounterSetting();

    public ClickGuiScreen(ITextComponent titleIn) {
        super(titleIn);
        Category[] categories = Category.values();
        for (Category category : categories) {
            panels.add(new Panel(category));

        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        globalAnim.animate(1, 0.1f, Easings.EXPO_OUT);
        gradientAnimation.animate(1, 0f, Easings.EXPO_OUT);
        imageAnimation.animate(1, 0, Easings.BACK_OUT);

        exit = false;
        open = true;

        searchField = new SearchField(3, ClientReceive.calc(mc.getMainWindow().getScaledHeight()) - 19, 70, 16, "Поиск");
        GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
        SoundPlayer.playSound("guiyes.wav", .02);

        super.init();
    }

    @Override
    public void closeScreen() {
        GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
        super.closeScreen();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        SoundPlayer.playSound("guiscroll.wav");

        if (ClientReceive.ctrlIsDown()) {
            movePanel += (float) (delta * 5);
        } else {
            updownPanel -= (float) (delta * 20);
        }

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        Stream.of(globalAnim, imageAnimation, gradientAnimation).forEach(Animation::update);

        scaleAnimation.run(exit ? 1.5 : 1);
        scaleAnimation.setDuration(exit ? 500 : 150);
        scaleAnimation.setEasing(exit ? Easing.EASE_IN_QUAD : Easing.EASE_OUT_QUAD);
        boolean allow = !(globalAnim.getValue() > 0);

        if (Stream.of(globalAnim, imageAnimation, gradientAnimation).allMatch(anim -> anim.getValue() <= 0.3 && anim.isDone())) {
            closeScreen();
        }

        float off = 10.0F;
        float width = (float) panels.size() * 130;
        updateScaleBasedOnScreenWidth();
        int windowWidth = ClientReceive.calc(mc.getMainWindow().getScaledWidth());
        int windowHeight = ClientReceive.calc(mc.getMainWindow().getScaledHeight());
        Vector2i fixMouse = adjustMouseCoordinates(mouseX, mouseY);
        Vector2i fix = ClientReceive.getMouse(fixMouse.getX(), fixMouse.getY());
        mouseX = fix.getX();
        mouseY = fix.getY();

        ClickGui clickGui = AG.getInst().getModuleManager().getClickGui();

        mc.gameRenderer.setupOverlayRendering(2);


        OpenGLSystem.scaleStart(mc.getMainWindow().getScaledWidth() / 2F, mc.getMainWindow().getScaledHeight() / 2f, (float) scaleAnimation.getValue());

        for (Panel panel : panels) {
            xGuiAnimation.run(movePanel);
            yGuiAnimation.run(!allow ? (windowHeight / 2.0F - 110.0F - updownPanel) : (panel.getY() - (exit ? 0 : 10)));
            panel.setY((float) yGuiAnimation.getValue());
            panel.setX((float) (((windowWidth / 2f) - (width / 2f) + panel.getCategory().ordinal() * (130 + off / 2) - off / 1.5) - xGuiAnimation.getValue()));

            panel.render(matrixStack, (float) mouseX, (float) mouseY);
        }

        OpenGLSystem.scaleEnd();

        searchField.render(matrixStack, mouseX, mouseY, partialTicks);
        mc.gameRenderer.setupOverlayRendering();
    }

    public boolean isSearching() {
        return !searchField.isEmpty();
    }

    public String getSearchText() {
        return searchField.getText();
    }

    public boolean searchCheck(String text) {
        return isSearching() && !text.replaceAll(" ", "").toLowerCase().contains(getSearchText().replaceAll(" ", "").toLowerCase());
    }

    private void updateScaleBasedOnScreenWidth() {
        float totalPanelWidth = (float) panels.size() * 125;
        float screenWidth = (float) mc.getMainWindow().getScaledWidth();
        if (totalPanelWidth >= screenWidth) {
            scale = screenWidth / totalPanelWidth;
            scale = MathHelper.clamp(scale, 0.5F, 1.0F);
        } else {
            scale = 1F;
        }
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean ruleBind = false;

        for (Panel panel : panels) {
            panel.keyPressed(keyCode, scanCode, modifiers);
        }

        if (searchField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        for (Panel panel : panels) {
            if (panel.binding) {
                ruleBind = true;
            }
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE && !exit && !searchField.isTyping() && !ruleBind) {
            globalAnim.animate(0.0, 0, Easings.EXPO_OUT);
            gradientAnimation.animate(0.0, 0, Easings.EXPO_OUT);
            imageAnimation.animate(0.0, 0, Easings.BACK_OUT);

            SoundPlayer.playSound("guino.wav", .03);
            exit = true;
            open = false;
            return false;
        } else {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) return false;
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    private Vector2i adjustMouseCoordinates(int mouseX, int mouseY) {
        int windowWidth = mc.getMainWindow().getScaledWidth();
        int windowHeight = mc.getMainWindow().getScaledHeight();
        float adjustedMouseX = ((float) mouseX - (float) windowWidth / 2.0F) / scale + (float) windowWidth / 2.0F;
        float adjustedMouseY = ((float) mouseY - (float) windowHeight / 2.0F) / scale + (float) windowHeight / 2.0F;
        return new Vector2i((int) adjustedMouseX, (int) adjustedMouseY);
    }

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

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        Vector2i fixMouse = adjustMouseCoordinates((int) mouseX, (int) mouseY);
        Vector2i fix = ClientReceive.getMouse(fixMouse.getX(), fixMouse.getY());
        mouseX = (double) fix.getX();
        mouseY = (double) fix.getY();

        for (Panel panel : panels) {
            panel.mouseRelease((float) mouseX, (float) mouseY, button);
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (searchField.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

}
