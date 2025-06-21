package minecraft.game.display.clientpanel2;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.Setter;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.make.font.Fonts;
import minecraft.game.advantage.words.font.ClientFonts;
import minecraft.game.display.clientpanel2.components.ModuleComponent;
import minecraft.game.display.clientpanel2.components.builder.Component;
import minecraft.game.display.clientpanel2.components.builder.IBuilder;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.system.AG;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
@Setter
public class Panel implements IBuilder {

    private final Category category;
    protected float x;
    protected float y;
    protected final float width = 125;
    protected float height;

    public boolean binding;

    private List<ModuleComponent> modules = new ArrayList<>();
    private float scroll, animatedScrool;

    public Panel(Category category) {
        this.category = category;

        for (Module module : AG.getInst().getModuleManager().getModules()) {
            if (module.getCategory() == category) {
                ModuleComponent component = new ModuleComponent(module);
                component.setPanel(this);
                modules.add(component);
            }
        }

        modules.sort(Comparator.comparing(m -> m.getModule().getName()));

        updateHeight();
    }

    double base = 20;
    double biba = 28.5;
    double boba = 8.5;

    public void updateHeight() {
        final double additionalHeight = modules.stream().filter(ModuleComponent::isOpen).mapToDouble(ModuleComponent::getHeight).sum();

        this.height = (float) Math.max(biba, base + additionalHeight + boba);
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        animatedScrool = MathSystem.fast(animatedScrool, scroll, 10);
        float headerFont = 9;

        updateHeight();
        height = (float) Math.max(biba, modules.stream().filter(component -> !AG.getInst().getClickGuiDropDown().searchCheck(component.getModule().getName())).mapToDouble(ModuleComponent::getHeight).sum() + base + boba);

        GraphicsSystem.drawShadowFancyRectNoOutline(stack, x, y, width, height, (int) (255 * ClickGuiScreen.getGlobalAnim().getValue()));
        GraphicsSystem.drawRoundedRect(x - 4.5f, y - 5, width + 9, height + 9, 6, ColoringSystem.rgba(20,20,20, (int) (210 * ClickGuiScreen.getGlobalAnim().getValue())));

        GraphicsSystem.drawRoundedRect(x + 2, y + 2, ClientFonts.icons_wex[35].getWidth(String.valueOf(category.getIconChar())) + 5, ClientFonts.icons_wex[35].getFontHeight() + 2, 4, ColoringSystem.setAlpha(ColoringSystem.rgba(20,20,20, 255)   , (int) (255 * ClickGuiScreen.getGlobalAnim().getValue())));

        ClientFonts.tenacity[24].drawString(stack, category.name(), x + 9 + ClientFonts.icons_wex[35].getWidth(String.valueOf(category.getIconChar())), y + 12 - ClientFonts.tenacity[18].getFontHeight() / 2f, ColoringSystem.rgba(255, 255, 255, (int) (255 * ClickGuiScreen.getGlobalAnim().getValue())));
        ClientFonts.icons_wex[35].drawString(stack, String.valueOf(category.getIconChar()), x + 5, y + 14 - ClientFonts.icons_wex[30].getFontHeight() / 2f, ColoringSystem.rgba(255, 255, 255, (int) (255 * ClickGuiScreen.getGlobalAnim().getValue())));

        GraphicsSystem.drawRectHorizontalW(x + 0.5f, y + 18 + Fonts.montserrat.getHeight(headerFont) / 2f, width - (0.5f * 2), 2.5f, ColoringSystem.rgba(0,0,0,0), ColoringSystem.rgba(0,0,0, (int) (65 * ClickGuiScreen.getGlobalAnim().getValue())));

        drawComponents(stack, mouseX, mouseY);
    }

    float max = 0;

    private void drawComponents(MatrixStack stack, float mouseX, float mouseY) {
        float offset = -1;
        float header = 25;

        if (max > height - header - 10) {
            scroll = MathHelper.clamp(scroll, -max + height - header - 10, 0);
            animatedScrool = MathHelper.clamp(animatedScrool, -max + height - header - 10, 0);
        } else {
            scroll = 0;
            animatedScrool = 0;
        }

        for (ModuleComponent component : modules) {
            component.setX(getX() + 0.5f);
            component.setY(getY() + header + offset + 0.5f + animatedScrool);
            component.setWidth(getWidth() - 1);
            component.setHeight(18.5f);
            component.expandAnim.update();
            component.hoverAnim.update();
            component.bindAnim.update();
            component.noBindAnim.update();
            binding = component.bind;

            if (AG.getInst().getClickGuiDropDown().searchCheck(component.getModule().getName())){
                continue;
            }

            if (component.expandAnim.getValue() > 0 && AG.getInst().getClickGuiDropDown().getExpandedModule() == component) {
                float componentOffset = 0;
                for (Component component2 : component.getComponents()) {
                    if (component2.isVisible())
                        componentOffset += component2.getHeight();
                }
                componentOffset *= (float) component.expandAnim.getValue();
                component.setHeight(component.getHeight() + componentOffset);
            }

            component.render(stack, mouseX, mouseY);
            offset += component.getHeight() + 0.1f;
        }

        max = offset;
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int button) {
        for (ModuleComponent component : modules) {
            if (AG.getInst().getClickGuiDropDown().searchCheck(component.getModule().getName())){
                continue;
            }
            component.mouseClick(mouseX, mouseY, button);
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
    }

}