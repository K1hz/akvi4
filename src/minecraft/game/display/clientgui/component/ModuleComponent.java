package minecraft.game.display.clientgui.component;


import com.mojang.blaze3d.matrix.MatrixStack;

import minecraft.game.advantage.advisee.KeyStorage;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.figures.Vector4i;
import minecraft.game.advantage.make.Cursors;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.words.MoreColorsSystem;
import minecraft.game.advantage.words.font.ClientFonts;
import minecraft.game.display.clientgui.ClickGui;
import minecraft.game.display.clientgui.api.component.Component;
import minecraft.game.display.clientgui.component.impl.*;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.massa.api.Setting;
import minecraft.game.operation.wamost.massa.elements.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleComponent extends Component {
    public Module module;
    public List<Component> components = new ArrayList<>();

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
                components.add(new KeyBindComponent(bind));
            }
            if (setting instanceof ModeSetting mode) {
                components.add(new ModeComponent(mode));
            }
            if (setting instanceof ModeListSetting mode) {
                components.add(new MultiListComponent(mode));
            }
            if (setting instanceof StringSetting string) {
                components.add(new StringComponent(string));
            }
            if (setting instanceof ColorSetting color) {
                components.add(new ColorComponent(color));
            }
        }
    }

    public float animationToggle;
    private boolean isHovered;
    public static ModuleComponent binding;

    public void drawComponent(MatrixStack matrixStack, int mouseX, int mouseY) {
        float totalHeight = 0;
        for (minecraft.game.display.clientgui.api.component.Component component : components) {
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

        renderBind(matrixStack, x, y, width);

        float moduleNameWidth = ClientFonts.clickGui[14].getWidth(module.getName());

        if (module.isEnabled()) {
            ITextComponent name = MoreColorsSystem.gradient(module.getName(), ColoringSystem.getColorTest4(1), ColoringSystem.rgb(20,20,20));
            ClientFonts.clickGui[14].drawString(matrixStack, name, x + 8, y + 9f, new Color(255, 255, 255).getRGB());
        } else {
            ClientFonts.clickGui[14].drawString(matrixStack, module.getName(), x + 8, y + 9f, ColoringSystem.rgb(128, 128, 128));
        }

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
                component.drawComponent(matrixStack, mouseX, mouseY);
                offsetY += component.height;
            }
        }
    }

    private void renderBind(MatrixStack matrixStack, float x, float y, float width) {
        String bind;

        if (binding == this) {
            bind = "...";
        } else {
            bind = module.getBind() == 0 ? "N/A" : KeyStorage.getKey(module.getBind());
        }

        if (bind == null || bind.isEmpty()) {
            bind = "N/A";
        }

        float keyWidth = ClientFonts.clickGui[14].getWidth(bind);

        GraphicsSystem.drawRoundedRect(x + width - 20 - keyWidth + 5, y + 5, 10 + keyWidth, 11, 4, ColoringSystem.rgb(20,20,20));
        GraphicsSystem.drawRoundedOutline(x + width - 20 - keyWidth + 5, y + 5, 10 + keyWidth, 11, 0.2f, new Vector4f(4, 4, 4, 4), Vector4i.copy(0xFF282932));

        ClientFonts.clickGui[14].drawCenteredString(matrixStack, bind, x + width - 20 - keyWidth + 5 + (10 + keyWidth) / 2, y + 9, -1);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isHovered && mouseButton == 0) {
            module.toggle();
        }

        if (binding == this) {
            if (mouseButton > 2) {
                module.setBind(-100 + mouseButton);
                binding = null;
                return;
            } else if (mouseButton == 2) {
                module.setBind(-98);
                binding = null;
                return;
            }
        }

        String bindText = (module.getBind() == 0 || module.getBind() == -1) ? "N/A" : KeyStorage.getKey(module.getBind());
        float keyWidth = ClientFonts.clickGui[14].getWidth(bindText);
        float bindX = x + width - 20 - keyWidth + 5;
        float bindY = y + 5;
        float bindWidth = 10 + keyWidth;
        float bindHeight = 11;

        if (MathSystem.isHovered(mouseX, mouseY, bindX, bindY, bindWidth, bindHeight)) {
            if (mouseButton == 0 || mouseButton == 2) {
                ClickGui.typing = false;
                binding = this;
                return;
            }
        }

        components.forEach(component -> component.mouseClicked(mouseX, mouseY, mouseButton));
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        components.forEach(component -> component.mouseReleased(mouseX, mouseY, mouseButton));
    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {
        if (binding == this) {
            if (keyCode == GLFW.GLFW_KEY_DELETE) {
                module.setBind(0);
            } else {
                module.setBind(keyCode);
            }
            binding = null;
        }
        components.forEach(component -> component.keyTyped(keyCode, scanCode, modifiers));
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
        components.forEach(component -> component.charTyped(codePoint, modifiers));
    }
}
