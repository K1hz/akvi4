package minecraft.game.display.clickgui.components;

import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
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
import minecraft.game.display.clickgui.Panel;
import minecraft.game.display.clickgui.components.builder.Component;
import minecraft.game.display.clickgui.components.settings.*;
import minecraft.game.operation.wamost.massa.api.Setting;
import minecraft.game.operation.wamost.massa.elements.*;
import minecraft.system.AG;
import minecraft.system.managers.Theme;
import minecraft.system.styles.Style;
import minecraft.system.styles.StyleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.glfw.GLFW;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;

import java.util.List;

@Getter
public class ModuleComponent extends Component {
    private final Vector4f ROUNDING_VECTOR = new Vector4f(5, 5, 5, 5);
    private final Vector4i BORDER_COLOR = new Vector4i(ColoringSystem.rgb(45, 46, 53), ColoringSystem.rgb(25, 26, 31), ColoringSystem.rgb(45, 46, 53), ColoringSystem.rgb(25, 26, 31));
    private final minecraft.game.operation.wamost.api.Module module;
    protected Panel panel1;
    public Animation expandAnim = new Animation();

    public Animation hoverAnim = new Animation();
    public Animation animation = new Animation();
    public Animation bindAnim = new Animation();
    public Animation noBindAnim = new Animation();

    public boolean open;

    public boolean bind;

    private double openAnimValue = 0.3, noOpenAnimValue = 0.4;

    private final ObjectArrayList<Component> components = new ObjectArrayList<>();

    public ModuleComponent(minecraft.game.operation.wamost.api.Module module) {
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
            Stencil.initStencilToWrite();
            GraphicsSystem.drawRoundedRect(getX() + 0.5f, getY() + 0.5f, getWidth() - 1, getHeight() - 1, ROUNDING_VECTOR, ColoringSystem.rgba(23, 23, 23, (int) (255 * 0.33)));
            Stencil.readStencilBuffer(1);
            float y = getY() + 20;
            for (Component component : components) {
                if (component.isVisible()){
                    component.setX(getX());
                    component.setY(y);
                    component.setWidth(getWidth());
                    component.render(stack, mouseX, mouseY );
                    y += component.getHeight();
                }
            }
            Stencil.uninitStencilBuffer();

        }
    }

    @Override
    public void mouseRelease(float mouseX, float mouseY, int mouse) {

        for (Component component : components) {
            component.mouseRelease(mouseX, mouseY, mouse);
        }

        super.mouseRelease(mouseX, mouseY, mouse);
    }

    private boolean hovered = false;

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        int color = ColoringSystem.interpolate(Theme.textColor, ColoringSystem.rgb(161, 164, 177), (float) module.getAnimation().getValue());

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
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                module.setBind(0);
            }
            if (key == GLFW.GLFW_KEY_DELETE) {
                module.setBind(0);
            }
            if (key == GLFW.GLFW_KEY_LEFT_CONTROL) {
                module.setBind(0);
            }
            else module.setBind(key);
            bind = false;
        }
        super.keyPressed(key, scanCode, modifiers);
    }


    private void drawOutlinedRect(float mouseX, float mouseY, int color) {
        int alpha = (int) (20 * module.getAnimation().getValue());
        int i = module.isEnabled() ? ColoringSystem.setAlpha(Theme.mainRectColor, alpha + 55) : ColoringSystem.setAlpha(ColoringSystem.rgb(153,153,153), 10);
        StyleManager style = AG.getInst().getStyleManager();
        Vector4i z = new Vector4i(
                ColoringSystem.setAlpha(ColoringSystem.rgb(153,153,153), 65),
                ColoringSystem.setAlpha(ColoringSystem.rgb(153,153,153), 65),
                ColoringSystem.setAlpha(Theme.darkMainRectColor, 0),
                ColoringSystem.setAlpha(Theme.darkMainRectColor, 0)
        );
        Vector4i disabledColor = new Vector4i(
                ColoringSystem.setAlpha(ColoringSystem.rgb(153, 153, 153), 15),
                ColoringSystem.setAlpha(ColoringSystem.rgb(153, 153, 153), 15),
                ColoringSystem.setAlpha(ColoringSystem.rgb(153, 153, 153), 0),
                ColoringSystem.setAlpha(ColoringSystem.rgb(153, 153, 153), 0)
        );

        GraphicsSystem.drawRoundedRect(
                getX() + 0.7f,
                getY() + 1,
                getWidth() - 2f,
                getHeight() - 2f,
                this.ROUNDING_VECTOR,
                module.isEnabled() ? z : disabledColor
        );


        if (MathSystem.isHovered(mouseX, mouseY, getX(), getY(), getWidth(), 20.0F)) {
            if (!this.hovered) {
                GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.HAND);
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
        int i = ColoringSystem.interpolate(Theme.mainRectColor, ColoringSystem.rgba(153, 153, 153,15), (float) module.getAnimation().getValue());
        int i2 = module.isEnabled() ? ColoringSystem.setAlpha(Theme.textColor,  255) : ColoringSystem.setAlpha(ColoringSystem.rgb(153,153,153), 255);

        float fontWidth = ClientFonts.msMedium[17].getWidth(module.getName());

        if (bind) {
            ClientFonts.msMedium[18].drawString(stack, module.getName(), (int)(getX() + 6), getY() + 7.5F, ColoringSystem.rgba(20, 20, 20, 0)); // Fully transparent
        } else {
            if (module.isEnabled()) {
                // Если модуль включен, рисуем его название в ярком цвете
                ClientFonts.msMedium[18].drawString(stack, module.getName(), (int)(getX() + 6), getY() + 7.5F, ColoringSystem.rgb(255, 255, 255));
            } else {
                // Если модуль выключен, рисуем название модуля в сером цвете
                ClientFonts.msMedium[18].drawString(stack, module.getName(), (int)(getX() + 6), getY() + 7.5F, ColoringSystem.rgb(153, 153, 153));
            }
            if (module.getName().contains("FT Helper")) {
                ClientFonts.msMedium[18].drawString(stack, "F", (int)(getX() + 76), getY() + 7.5F, ColoringSystem.rgb(220, 20, 60));
                ClientFonts.msMedium[18].drawString(stack, "T", (int)(getX() + 82), getY() + 7.5F, ColoringSystem.rgb(255, 255, 224));
            }
            if (module.getName().contains("Water Speed")) {
                ClientFonts.msMedium[18].drawString(stack, "F", (int)(getX() + 88), getY() + 7.5F, ColoringSystem.rgb(220, 20, 60));
                ClientFonts.msMedium[18].drawString(stack, "T", (int)(getX() + 94), getY() + 7.5F, ColoringSystem.rgb(255, 255, 224));
            }
            if (module.getName().contains("Auto Farm")) {
                ClientFonts.msMedium[18].drawString(stack, "F", (int)(getX() + 76), getY() + 7.5F, ColoringSystem.rgb(220, 20, 60));
                ClientFonts.msMedium[18].drawString(stack, "T", (int)(getX() + 82), getY() + 7.5F, ColoringSystem.rgb(255, 255, 224));
            }
            if (module.getName().contains("RW Joiner")) {
                ClientFonts.msMedium[18].drawString(stack, "R", (int)(getX() + 74), getY() + 7.5F, ColoringSystem.rgb(255, 255, 224));
                ClientFonts.msMedium[18].drawString(stack, "W", (int)(getX() + 80), getY() + 7.5F, ColoringSystem.rgb(255, 165, 0));
            }
            if (module.getName().contains("RW Helper")) {
                ClientFonts.msMedium[18].drawString(stack, "R", (int)(getX() + 74), getY() + 7.5F, ColoringSystem.rgb(255, 255, 224));
                ClientFonts.msMedium[18].drawString(stack, "W", (int)(getX() + 80), getY() + 7.5F, ColoringSystem.rgb(255, 165, 0));
            }
            if (module.getName().contains("Auto Duеl")) {
                ClientFonts.msMedium[18].drawString(stack, "R", (int)(getX() + 74), getY() + 7.5F, ColoringSystem.rgb(255, 255, 224));
                ClientFonts.msMedium[18].drawString(stack, "W", (int)(getX() + 80), getY() + 7.5F, ColoringSystem.rgb(255, 165, 0));
            }
            if (module.getName().contains("Cat Strafe")) {
                ClientFonts.msMedium[18].drawString(stack, "R", (int)(getX() + 74), getY() + 7.5F, ColoringSystem.rgb(255, 255, 224));
                ClientFonts.msMedium[18].drawString(stack, "W", (int)(getX() + 80), getY() + 7.5F, ColoringSystem.rgb(255, 165, 0));
            }
            if (module.getName().contains("Cat Fly")) {
                ClientFonts.msMedium[18].drawString(stack, "R", (int)(getX() + 74), getY() + 7.5F, ColoringSystem.rgb(255, 255, 224));
                ClientFonts.msMedium[18].drawString(stack, "W", (int)(getX() + 80), getY() + 7.5F, ColoringSystem.rgb(255, 165, 0));
            }
            if (module.getName().contains("HW Helper")) {
                ClientFonts.msMedium[18].drawString(stack, "H", (int)(getX() + 74), getY() + 7.5F, ColoringSystem.rgb(0, 191, 255));
                ClientFonts.msMedium[18].drawString(stack, "W", (int)(getX() + 80), getY() + 7.5F, ColoringSystem.rgb(135, 206, 235));
            }
        }

        if (this.components.stream().filter(Component::isVisible).count() >= 1L) {
            if (bind) {
                Fonts.montserrat.drawCenteredText(stack, (module.getBind() == 0) ? "Bind" + gavno.getOutput().toString() : KeyStorage.getReverseKey(module.getBind()), getX() + getWidth() / 2f, getY() + 7.5F, ColoringSystem.rgb(153, 153, 153), 8.0F, 0.1F);
            } else {
                ClientFonts.icons_nur[17].drawCenteredString(stack, "C", getX() + getWidth() - 10, getY() +9 , i);
            }
        } else if (bind) {
            Fonts.montserrat.drawCenteredText(stack, (module.getBind() == 0) ? "Bind" + gavno.getOutput().toString() : KeyStorage.getReverseKey(module.getBind()), getX() + getWidth() / 2f, getY() + 7.5F, ColoringSystem.rgb(153, 153, 153), 8.0F, 0.1F);
        }
    }
}