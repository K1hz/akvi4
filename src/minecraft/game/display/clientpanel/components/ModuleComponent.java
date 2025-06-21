package minecraft.game.display.clientpanel.components;

import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import minecraft.game.advantage.make.other.KawaseBlur;
import minecraft.game.operation.visual.ClickGui;
import minecraft.game.operation.visual.Hud;
import minecraft.system.AG;
import minecraft.system.styles.StyleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;
import minecraft.game.advantage.advisee.KeyStorage;
import minecraft.game.advantage.advisee.SoundPlayer;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.figures.Vector4i;
import minecraft.game.advantage.make.Cursors;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.make.font.Fonts;
import minecraft.game.advantage.make.other.Stencil;
import minecraft.game.advantage.words.BetterText;
import minecraft.game.advantage.words.MoreColorsSystem;
import minecraft.game.advantage.words.font.ClientFonts;
import minecraft.game.display.clientpanel.Panel;
import minecraft.game.display.clientpanel.components.builder.Component;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.massa.api.Setting;
import minecraft.game.display.clientpanel.components.settings.*;
import minecraft.game.operation.wamost.massa.elements.*;
import minecraft.system.managers.Theme;

import java.awt.*;
import java.util.List;

import static minecraft.game.advantage.advisee.IMinecraft.mc;

@Getter
public class ModuleComponent extends Component {
    private final Vector4f ROUNDING_VECTOR = new Vector4f(5, 5, 5, 5);
    private final Vector4i BORDER_COLOR = new Vector4i(ColoringSystem.rgb(13, 21, 36), ColoringSystem.rgb(13, 21, 36), ColoringSystem.rgb(13, 21, 36), ColoringSystem.rgb(13, 21, 36));
    private final Module module;
    public Panel panel;

    public Animation animation = new Animation();
    public boolean open;
    private boolean bind;
    public float animationToggle;
    private boolean isHovered;
    private final ObjectArrayList<Component> components = new ObjectArrayList<>();

    public ModuleComponent(Module module) {
        this.module = module;
        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof CheckBoxSetting bool) {
                components.add(new BooleanComponent(bool));
            }
            if (setting instanceof SliderSetting slider) {
                components.add(new SliderComponent(slider));
            }
            if (setting instanceof BindSetting bind) {
                components.add(new BindComponent(bind));
            }
            if (setting instanceof ModeSetting mode) {
                components.add(new ModeComponent(mode));
            }
            if (setting instanceof ModeListSetting mode) {
                components.add(new MultiBoxComponent(mode));
            }
            if (setting instanceof StringSetting string) {
                components.add(new StringComponent(string));
            }
            if (setting instanceof ColorSetting color) {
                components.add(new ColorComponent(color));
            }


        }
        animation = animation.animate(open ? 1 : 0, 0.3);
    }

    public void drawComponents(MatrixStack stack, float mouseX, float mouseY) {
        if (animation.getValue() > 0) {
//            if (animation.getValue() > 0.1 && components.stream().filter(Component::isVisible).count() >= 1) {
//                GraphicsSystem.drawRectVerticalW(getX() + 5, getY() + 18, getWidth() - 10, 2f, Theme.rectColor, Theme.rectColor);
//            }
            Stencil.initStencilToWrite();
            GraphicsSystem.drawRoundedRect(getX() + 0.5f, getY() + 0.5f, getWidth() - 1, getHeight() - 1, ROUNDING_VECTOR, ColoringSystem.rgba(23, 23, 23, (int) (255 * 0.33)));
            Stencil.readStencilBuffer(1);
            float y = getY() + 20;
            for (Component component : components) {
                if (component.isVisible()) {
                    component.setX(getX());
                    component.setY(y);
                    component.setWidth(getWidth());
                    component.render(stack, mouseX, mouseY);
                    y += component.getHeight();
                }
            }
            Stencil.uninitStencilBuffer();

        }
    }
    public void drawComponent(MatrixStack matrixStack, int mouseX, int mouseY) {
        float totalHeight = 0;
        for (Component component : components) {
            if (component.setting != null && component.setting.visible.get()) {
                totalHeight += component.height;
            }
        }

        float off = 0f;

        components.forEach(c -> {
            c.module = module;
            c.parent = parent;
        });

        animationToggle += (module.isEnabled() ? 1 : -1) * 0.05f;
        animationToggle = MathHelper.clamp(animationToggle, 0, 1);

        GraphicsSystem.drawRoundedRect(x, y, width, height + totalHeight, 6, ColoringSystem.rgb(16, 16, 16));
        GraphicsSystem.drawRoundedOutline(x, y, width, height + totalHeight, 0.5f, Vector4f.copy(6), Vector4i.copy(ColoringSystem.rgb(16, 16, 16)));


        float moduleNameWidth = ClientFonts.clickGui[14].getWidth(module.getName());


        isHovered = MathSystem.isHovered(mouseX, mouseY, x + 6, y + 7f + off, moduleNameWidth + 4, 8);
        if (isHovered) {
            GLFW.glfwSetCursor(mc.getMainWindow().getHandle(), Cursors.HAND);
        } else {
            GLFW.glfwSetCursor(mc.getMainWindow().getHandle(), Cursors.ARROW);
        }

        float offsetY = 0;
        for (Component component : components) {
            if (component.setting != null && component.setting.visible.get()) {
                component.setPosition(x, y + height + offsetY, width, 20);
                offsetY += component.height;
            }
        }
    }
    @Override
    public void mouseRelease(float mouseX, float mouseY, int mouse) {
        // TODO Auto-generated method stub

        for (Component component : components) {
            component.mouseRelease(mouseX, mouseY, mouse);
        }

        super.mouseRelease(mouseX, mouseY, mouse);
    }

    private boolean hovered = false;

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        int color = ColoringSystem.interpolate(-1, ColoringSystem.rgb(161, 164, 177), (float) module.getAnimation().getValue());

        module.getAnimation().update();
        super.render(stack, mouseX, mouseY);

        drawOutlinedRect(mouseX, mouseY, color);
        drawText(stack, color);
        drawComponents(stack, mouseX, mouseY);

    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int button) {
        if (isHovered(mouseX, mouseY, 20)) {
            if (button == 0) module.toggle();
            if (button == 1) {
                if (!module.getSettings().isEmpty()) {
                    open = !open;
                    SoundPlayer.playSound(open ? "moduleopen.wav" : "moduleclose.wav");
                    animation = animation.animate(open ? 1 : 0, open ? 0.2 : 0.1, Easings.CIRC_OUT);
                }
            }
            if (button == 2) {
                bind = !bind;
            }
        }
        if (isHovered(mouseX, mouseY)) {
            if (open) {
                for (Component component : components) {
                    if (component.isVisible()) component.mouseClick(mouseX, mouseY, button);
                }
            }
        }
        super.mouseClick(mouseX, mouseY, button);
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
        for (Component component : components) {
            if (component.isVisible()) component.charTyped(codePoint, modifiers);
        }
        super.charTyped(codePoint, modifiers);
    }

    @Override
    public void keyPressed(int key, int scanCode, int modifiers) {
        for (Component component : components) {
            if (component.isVisible()) component.keyPressed(key, scanCode, modifiers);
        }
        if (bind) {
            if (key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_ESCAPE) {
                module.setBind(0);
                SoundPlayer.playSound("guibindreset.wav");
            } else module.setBind(key);
            bind = false;
        }
        super.keyPressed(key, scanCode, modifiers);
    }


    private void drawOutlinedRect(float mouseX, float mouseY, int color) {
        int alpha = (int) (1 * module.getAnimation().getValue());
        int alpha123 = 155;
        if (module.isEnabled()) {
            StyleManager styleManager = AG.getInst().getStyleManager();
            float color123 = ColoringSystem.rgb(24,24,24);
            float color1234 = ColoringSystem.rgb(44,44,44);
            GraphicsSystem.drawRoundedRect(getX() + 0.7f, getY() + 1, getWidth() - 2f, getHeight() - 2f, new Vector4f(5, 5, 5, 5), new Vector4i(
                    ColoringSystem.setAlpha(ColoringSystem.rgb((int) color123, (int) color123, (int) color123), (int) (115)),
                    ColoringSystem.setAlpha(ColoringSystem.rgb((int) color123, (int) color123, (int) color123), (int) (115)),
                    ColoringSystem.setAlpha(ColoringSystem.rgb((int) color123, (int) color123, (int) color123), (int) (115)),
                    ColoringSystem.setAlpha(ColoringSystem.rgb((int) color123, (int) color123, (int) color123), (int) (115))
            ));
        }
        else {

            float color123 = ColoringSystem.rgb(24,24,24);
            float color1234 = ColoringSystem.rgb(1,1,1);

            GraphicsSystem.drawRoundedRect(getX() + 0.7f, getY() + 1, getWidth() - 2f, getHeight() - 2f, new Vector4f(5, 5, 5, 5), new Vector4i(
                    ColoringSystem.setAlpha(ColoringSystem.rgb((int) color123, (int) color123, (int) color123), (int) (86)),
                    ColoringSystem.setAlpha(ColoringSystem.rgb((int) color123, (int) color123, (int) color123), (int) (86)),
                    ColoringSystem.setAlpha(ColoringSystem.rgb((int) color123, (int) color123, (int) color123), (int) (86)),
                    ColoringSystem.setAlpha(ColoringSystem.rgb((int) color123, (int) color123, (int) color123), (int) (86))
            ));
        }

        if (MathSystem.isHovered(mouseX, mouseY, getX(), getY(), getWidth(), 20.0F)) {
            if (!this.hovered) {
                GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
                this.hovered = true;
            }
        } else if (this.hovered) {
            GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
            this.hovered = false;
        }
    }

    private final BetterText gavno = new BetterText(List.of(
            "...", "...", "..."
    ), 100);

    private void drawText(MatrixStack stack, int color) {
        gavno.update();
        ITextComponent text = MoreColorsSystem.gradient(module.getName(), Hud.getColor(1), ColoringSystem.rgb(15,15,15));
        int i = ColoringSystem.interpolate(ColoringSystem.rgb(13, 21, 36), ColoringSystem.rgb(13, 21, 36), (float) module.getAnimation().getValue());
        float fontWidth = ClientFonts.clickGui[17].getWidth(module.getName());
        ITextComponent mode = MoreColorsSystem.gradient(module.getName());
        ITextComponent bind123 = MoreColorsSystem.gradient(KeyStorage.getReverseKey(module.getBind()));

        if (bind) {
            ClientFonts.clickGui[18].drawCenteredString(stack, module.getName(), (int)(getX() + 60), getY() + 7.5F, ColoringSystem.rgba(20, 20, 20, 0));
        } else {
            if (ClickGui.centerNAME.getValue()) {
                if (module.isEnabled()) {
                    if (isOpen()) {
                        ClientFonts.clickGui[18].drawCenteredString(stack, text, (int) (getX() + 60), getY() + 7.5F, ColoringSystem.rgb(98, 120, 168));
                    }
                    if (!isOpen()) {
                        ClientFonts.clickGui[18].drawCenteredString(stack, text, (int) (getX() + 60), getY() + 7.5F, ColoringSystem.rgb(98, 120, 168));
                    }
                } else {
                    if (isOpen()) {
                        ClientFonts.clickGui[18].drawCenteredString(stack, module.getName(), (int)(getX() + 60), getY() + 7.5F, ColoringSystem.rgb(255, 255, 255));
                    }
                    if (!isOpen()) {
                        ClientFonts.clickGui[18].drawCenteredString(stack, module.getName(), (int)(getX() + 60), getY() + 7.5F, ColoringSystem.rgb(255, 255, 255));
                    }
                }
            }
            if (!ClickGui.centerNAME.getValue()) {
                if (module.isEnabled()) {
                    if (isOpen()) {
                        ClientFonts.clickGui[18].drawString(stack, text, (int) (getX() + 6), getY() + 7.5F, ColoringSystem.rgb(98, 120, 168));
                    }
                    if (!isOpen()) {
                        ClientFonts.clickGui[18].drawString(stack, text, (int) (getX() + 6), getY() + 7.5F, ColoringSystem.rgb(98, 120, 168));
                    }
                } else {
                    if (isOpen()) {
                        ClientFonts.clickGui[18].drawString(stack, module.getName(), (int)(getX() + 6), getY() + 7.5F, ColoringSystem.rgb(255, 255, 255));
                    }
                    if (!isOpen()) {
                        ClientFonts.clickGui[18].drawString(stack, module.getName(), (int)(getX() + 6), getY() + 7.5F, ColoringSystem.rgb(255, 255, 255));
                    }
                }
            }
            if (ClickGui.helpers.getValue()) {
                if (module.getName().contains("FT Helper")) {
                    ClientFonts.interBold[18].drawString(stack, "F", (int)(getX() + 98), getY() + 7.5F, ColoringSystem.rgb(220, 20, 60));
                    ClientFonts.interBold[18].drawString(stack, "T", (int)(getX() + 104), getY() + 7.5F, ColoringSystem.rgb(255, 255, 224));
                }
                if (module.getName().contains("Water Speed")) {
                    ClientFonts.interBold[18].drawString(stack, "F", (int)(getX() + 98), getY() + 7.5F, ColoringSystem.rgb(220, 20, 60));
                    ClientFonts.interBold[18].drawString(stack, "T", (int)(getX() + 104), getY() + 7.5F, ColoringSystem.rgb(255, 255, 224));
                }
                if (module.getName().contains("Auto Farm")) {
                    ClientFonts.interBold[18].drawString(stack, "F", (int)(getX() + 98), getY() + 7.5F, ColoringSystem.rgb(220, 20, 60));
                    ClientFonts.interBold[18].drawString(stack, "T", (int)(getX() + 104), getY() + 7.5F, ColoringSystem.rgb(255, 255, 224));
                }
                if (module.getName().contains("RW Joiner")) {
                    ClientFonts.interBold[18].drawString(stack, "R", (int)(getX() + 94), getY() + 7.5F, ColoringSystem.rgb(255, 255, 224));
                    ClientFonts.interBold[18].drawString(stack, "W", (int)(getX() + 100), getY() + 7.5F, ColoringSystem.rgb(255, 165, 0));
                }
                if (module.getName().contains("RW Helper")) {
                    ClientFonts.interBold[18].drawString(stack, "R", (int)(getX() + 94), getY() + 7.5F, ColoringSystem.rgb(255, 255, 224));
                    ClientFonts.interBold[18].drawString(stack, "W", (int)(getX() + 100), getY() + 7.5F, ColoringSystem.rgb(255, 165, 0));
                }
                if (module.getName().contains("Auto Duеl")) {
                    ClientFonts.interBold[18].drawString(stack, "R", (int)(getX() + 94), getY() + 7.5F, ColoringSystem.rgb(255, 255, 224));
                    ClientFonts.interBold[18].drawString(stack, "W", (int)(getX() + 100), getY() + 7.5F, ColoringSystem.rgb(255, 165, 0));
                }
                if (module.getName().contains("Cat Strafe")) {
                    ClientFonts.interBold[18].drawString(stack, "R", (int)(getX() + 94), getY() + 7.5F, ColoringSystem.rgb(255, 255, 224));
                    ClientFonts.interBold[18].drawString(stack, "W", (int)(getX() + 100), getY() + 7.5F, ColoringSystem.rgb(255, 165, 0));
                }
                if (module.getName().contains("Cat Fly")) {
                    ClientFonts.interBold[18].drawString(stack, "R", (int)(getX() + 94), getY() + 7.5F, ColoringSystem.rgb(255, 255, 224));
                    ClientFonts.interBold[18].drawString(stack, "W", (int)(getX() + 100), getY() + 7.5F, ColoringSystem.rgb(255, 165, 0));
                }
                if (module.getName().contains("HW Helper")) {
                    ClientFonts.interBold[18].drawString(stack, "H", (int)(getX() + 96), getY() + 7.5F, ColoringSystem.rgb(255, 255, 255));
                    ClientFonts.interBold[18].drawString(stack, "W", (int)(getX() + 102), getY() + 7.5F, ColoringSystem.rgb(65, 206, 235));
                }
            }
            else {

            }
        }


        if (this.components.stream().filter(Component::isVisible).count() >= 1L) {
            if (bind) {

                Fonts.montserrat.drawCenteredText(stack, (module.getBind() == 0) ? "Нажмите любую клавишу" : "Клавиша: " + KeyStorage.getReverseKey(module.getBind()),
                        getX() + getWidth() - 63.5f, getY() + 6.5F, -1, 8, 0.03F);
            } else {

                if (ClickGui.settings.getValue()) {
                    ClientFonts.settings[16].drawCenteredString(stack, "A", getX() + getWidth() - 10, getY() +10 , ColoringSystem.rgba(255,255,255,55));
                }
                if (!ClickGui.settings.getValue()) {
                    ClientFonts.icons_nur[17].drawCenteredString(stack, "C", getX() + getWidth() - 10, getY() +9 , ColoringSystem.rgba(255,255,255,0));
                }
            }
        } else if (bind) {

            Fonts.montserrat.drawCenteredText(stack, (module.getBind() == 0) ? "Клавиша" : KeyStorage.getReverseKey(module.getBind()),
                    getX() + getWidth() / 2f, getY() + 7.5F, ColoringSystem.rgba(161, 164, 177, 255), 8.0F, 0.1F);
        }
    }
}
