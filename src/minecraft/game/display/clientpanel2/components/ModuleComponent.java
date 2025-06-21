package minecraft.game.display.clientpanel2.components;

import com.mojang.blaze3d.matrix.MatrixStack;
import minecraft.game.advantage.advisee.KeyStorage;
import minecraft.game.advantage.advisee.SoundPlayer;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.figures.Vector4i;
import minecraft.game.advantage.make.Cursors;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.make.engine2d.RectanglesSystem;
import minecraft.game.advantage.make.font.Fonts;
import minecraft.game.advantage.make.other.Stencil;
import minecraft.game.advantage.words.BetterText;
import minecraft.game.advantage.words.MoreColorsSystem;
import minecraft.game.advantage.words.font.ClientFonts;
import minecraft.game.display.clientpanel2.ClickGuiScreen;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import minecraft.game.display.clientpanel2.Panel;
import minecraft.game.display.clientpanel2.components.builder.Component;
import minecraft.game.display.clientpanel2.components.settings.*;
import minecraft.game.operation.visual.Hud;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.massa.api.Setting;
import minecraft.game.operation.wamost.massa.elements.*;
import minecraft.system.AG;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.glfw.GLFW;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;

import java.util.List;

@Getter
public class ModuleComponent extends Component {
    private final Vector4f ROUNDING_VECTOR = new Vector4f(3, 3, 3, 3);
    private final Vector4i BORDER_COLOR = new Vector4i(ColoringSystem.rgb(20, 20, 20), ColoringSystem.rgb(20, 20, 20), ColoringSystem.rgb(20, 20, 20), ColoringSystem.rgb(20, 20, 20));
    private final Module module;
    protected Panel panel;
    public Animation expandAnim = new Animation();

    public Animation hoverAnim = new Animation();

    public Animation bindAnim = new Animation();
    public Animation noBindAnim = new Animation();

    public boolean open;

    public boolean bind;

    private double openAnimValue = 0, noOpenAnimValue = 0;

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
        super.render(stack, mouseX, mouseY);

        module.getAnimation().update();

        betterText.update();
        if (AG.getInst().getClickGuiDropDown().getExpandedModule() != this) open = false;

        boolean hover = MathSystem.isHovered(mouseX, mouseY, getX() + 0.5f, getY() + 0.5f, getWidth() - 1, getHeight());

        if (hover) {
            GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
        }

        hoverAnim.animate(hover ? 1 : 0, 0.3, Easings.BACK_OUT);
        bindAnim.animate(bind ? 1 : 0, 0.3, Easings.BACK_OUT);
        noBindAnim.animate(!bind ? 1 : 0, 0.3, Easings.BACK_OUT);
        double posAnim = (1.5 * hoverAnim.getValue());

        int color = ColoringSystem.interpolate(ColoringSystem.setAlpha(-1, (int) (255 * ClickGuiScreen.getGlobalAnim().getValue())), ColoringSystem.rgba(161, 164, 177, (int) (255 * ClickGuiScreen.getGlobalAnim().getValue())), (float) module.getAnimation().getValue());
        int rectAlpha = (int) ((255 * module.getAnimation().getValue()) * ClickGuiScreen.getGlobalAnim().getValue());
        int hoverColorWhenNoActive = ColoringSystem.interpolateColor(ColoringSystem.rgba(33,33,33, 255), ColoringSystem.rgba(20,20,20, 255), (float) hoverAnim.getValue());
        int hoverColorWhenActive = ColoringSystem.interpolateColor(ColoringSystem.rgba(33,33,33, 255), ColoringSystem.rgba(28,28,28, 255), (float) hoverAnim.getValue());
        int rectColor = module.isEnabled() ? ColoringSystem.setAlpha(hoverColorWhenActive, rectAlpha) : ColoringSystem.setAlpha(hoverColorWhenNoActive, (int) (255 * ClickGuiScreen.getGlobalAnim().getValue()));
        int statusColor = ColoringSystem.setAlpha(ColoringSystem.interpolateColor(-1, -1, (float)module.getAnimation().getValue()), (int) (255 * ClickGuiScreen.getGlobalAnim().getValue()));

        float offMe = (float) (0.5f + 2 * hoverAnim.getValue());

        RectanglesSystem.getInstance().drawRoundedRectShadowed(stack, getX() + offMe, getY() + offMe, getX() + getWidth() - offMe, getY() + getHeight() - offMe, 3, (float) (3 * hoverAnim.getValue()), rectColor, rectColor, rectColor, rectColor, false, true, true, hoverAnim.getValue() > 0.1);

        int colorForModuleText = ColoringSystem.setAlpha(color, (int) ((255 * noBindAnim.getValue() * ClickGuiScreen.getGlobalAnim().getValue())));
        int colorForBindText = ColoringSystem.interpolateColor(ColoringSystem.rgba(161, 164, 177, (int) (255 * ClickGuiScreen.getGlobalAnim().getValue())), ColoringSystem.setAlpha(ColoringSystem.rgb(161, 164, 177), 0), (float) bindAnim.getValue());


        if (module.isEnabled()) {
            ClientFonts.comfortaa[17].drawString(stack, MoreColorsSystem.gradient(module.getName(), Hud.getColor(1), ColoringSystem.rgb(11,11,11)),  (float)(getX() + 6 + posAnim), getY() + 6.5F, ColoringSystem.rgba(255, 255, 255, (int) (255 * ClickGuiScreen.getGlobalAnim().getValue())));
        }
        if (!module.isEnabled()) {
            ClientFonts.comfortaa[17].drawString(stack, module.getName(),  (float)(getX() + 6 + posAnim), getY() + 6.5F, ColoringSystem.rgba(161, 164, 177, (int) (255 * ClickGuiScreen.getGlobalAnim().getValue())));
        }

        if (this.components.stream().filter(Component::isVisible).count() >= 1L) {
            GraphicsSystem.drawShadowCircle(getX() + getWidth() - 8, getY() + 10, 3, statusColor);
            GraphicsSystem.drawCircle(getX() + getWidth() - 8, getY() + 10 + 0.5f, 3f, ColoringSystem.setAlpha(statusColor, (int) (120 * ClickGuiScreen.getGlobalAnim().getValue())));
        }

        Fonts.montserrat.drawText(stack, "Bind" + (module.getBind() == 0 ? betterText.getOutput() : ": " + KeyStorage.getReverseKey(module.getBind())), (float)(getX() + 6 + posAnim), getY() + 6F, colorForBindText, 8F, 0.03F);

        if (expandAnim.getValue() > 0) {
            if (components.stream().filter(Component::isVisible).count() >= 1) {
                GraphicsSystem.drawRectVerticalW(getX() + 5, getY() + 18, getWidth() - 10, 0.5f, ColoringSystem.setAlpha(-1, (int) ((255 * expandAnim.getValue()) * ClickGuiScreen.getGlobalAnim().getValue())), ColoringSystem.setAlpha(Hud.getColor(1), (int) ((255 * expandAnim.getValue()) * ClickGuiScreen.getGlobalAnim().getValue())));
            }
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
    public void mouseClick(float mouseX, float mouseY, int button) {
        if (MathSystem.isHovered(mouseX, mouseY, getX() + 1, getY() + 1, getWidth() - 2, 18)) {
            ModuleComponent openModule = AG.getInst().getClickGuiDropDown().getExpandedModule();
            if (openModule != null && openModule != this && button == 1 && !module.getSettings().isEmpty()) {
                openModule.open = false;
                openModule.expandAnim.animate(0, noOpenAnimValue, Easings.EXPO_OUT);
            }
            if (button == 0 && !bind) module.toggle();
            if (button == 1 && !bind && expandAnim.isDone()) {
                if (!module.getSettings().isEmpty()) {
                    open = !open;
                    SoundPlayer.playSound(open ? "moduleonopen.wav" : "moduleonclose.wav");
                    if (expandAnim.isDone()) SoundPlayer.playSound(open ? "moduleopen.wav" : "moduleclose.wav");

                    if (open) {
                        AG.getInst().getClickGuiDropDown().setExpandedModule(this);
                        expandAnim = expandAnim.animate(1, openAnimValue, Easings.EXPO_OUT);
                    }

                    expandAnim = expandAnim.animate(open ? 1 : 0, open ? openAnimValue : noOpenAnimValue, Easings.EXPO_OUT);
                }
            }
            if (button == 2) {
                bind = !bind;

                SoundPlayer.playSound(bind ? "guibindingstart.wav" : "guibindingnull.wav");
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
            } else {
                module.setBind(key);
                SoundPlayer.playSound("guibinding.wav");
            }
            bind = false;
        }
        super.keyPressed(key, scanCode, modifiers);
    }

    private final BetterText betterText = new BetterText(List.of(
            "...", "...", "..."
    ), 100);
}