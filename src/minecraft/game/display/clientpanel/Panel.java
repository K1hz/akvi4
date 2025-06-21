package minecraft.game.display.clientpanel;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.Setter;
import minecraft.game.advantage.make.Cursors;
import minecraft.game.advantage.make.other.KawaseBlur;
import minecraft.game.operation.visual.Hud;
import minecraft.system.styles.Style;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.ITag;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.ITextComponent;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.figures.Vector4i;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.make.font.Fonts;
import minecraft.game.advantage.make.other.Scissor;
import minecraft.game.advantage.make.other.Stencil;
import minecraft.game.advantage.words.MoreColorsSystem;
import minecraft.game.advantage.words.font.ClientFonts;
import minecraft.game.display.clientpanel.components.ModuleComponent;
import minecraft.game.display.clientpanel.components.builder.Component;
import minecraft.game.display.clientpanel.components.builder.IBuilder;
import minecraft.game.operation.visual.ClickGui;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.system.AG;
import minecraft.system.managers.Theme;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
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
    protected final float width = 140;
    protected final float height = 686 / 2f;
    public float scroll, animatedScrool; // TODO: Параметры прокрутки и анимации прокрутки
    private boolean draggingScrollbar = false; // TODO: Флаг для отслеживания, удерживается ли скроллбар
    private float lastMouseY; // TODO: Последняя позиция мыши по Y для отслеживания движения скроллбара

    private List<ModuleComponent> modules = new ArrayList<>();
    private GraphicsSystem RectUtils;
    private ITextComponent description;


    public Panel(Category category) {
        this.category = category;

        // Добавляем все модули, относящиеся к текущей категории, в список modules
        for (Module function : AG.getInst().getModuleManager().getModules()) {
            if (function.getCategory() == category) {
                ModuleComponent component = new ModuleComponent(function);
                component.setPanel(this);
                modules.add(component);
            }
        }

        // Сортируем список modules по имени модуля (по алфавиту)
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
        String string = null;
        float f = getWidth() * 4;
        float f2 = 440;

        drawComponents(stack, mouseX, mouseY);

        for (ModuleComponent moduleComponent : this.modules) {
            if (!moduleComponent.isHovered(mouseX, mouseY) || !isModuleVisible(moduleComponent)) {
                continue;
            }
            string = moduleComponent.getModule().getDescription();
            ITextComponent stringi = MoreColorsSystem.gradient(moduleComponent.getModule().getDescription());
            if (string == null || string.isEmpty()) break;

            Scissor.pop();
            Scissor.push();
            break;
        }

        animatedScrool = MathSystem.fast(animatedScrool, scroll, 10);
        float header = 35 / 2.3f;
        float headerFont = 9;
        int alpha123 = 245;


        // **Начинаем рендеринг блюра перед панелью**
        KawaseBlur.blur.render(() -> {
            GraphicsSystem.drawRoundedRect(x + 0.5f, y, width - 1, height - 3.7f, 15, new Vector4i(
                    ColoringSystem.setAlpha(ColoringSystem.rgb(11, 11, 11), alpha123),
                    ColoringSystem.setAlpha(ColoringSystem.rgb(11, 11, 11), alpha123),
                    ColoringSystem.setAlpha(ColoringSystem.rgb(11, 11, 11), alpha123),
                    ColoringSystem.setAlpha(ColoringSystem.rgb(11, 11, 11), alpha123)
            ));
        });
        GraphicsSystem.drawRoundedRect(x + 0.5f, y, width - 1, height - 3.7f, 15, new Vector4i(
                ColoringSystem.setAlpha(ColoringSystem.rgb(11, 11, 11), 181),
                ColoringSystem.setAlpha(ColoringSystem.rgb(34, 34, 34), 181),
                ColoringSystem.setAlpha(ColoringSystem.rgb(11, 11, 11), 181),
                ColoringSystem.setAlpha(ColoringSystem.rgb(34, 34, 34), 181)
        ));


        // **Рисуем заголовок**
        String title = category.name().toUpperCase();
        String icon = String.valueOf(category.getIconChar());

        int titleWidth = (int) ClientFonts.interBold[26].getWidth(title);
        int iconWidth = (int) ClientFonts.category_icon[26].getWidth(icon);
        int totalWidth = iconWidth + 6 + titleWidth;

        float startX = x + width / 2f - totalWidth / 2f - 5;
        float centerY = y + header / 2f + 7 - Fonts.montserrat.getHeight(headerFont) / 2f - 1;

        ClientFonts.category_icon[26].drawString(stack, icon, startX, centerY , -1);
        ClientFonts.interBold[26].drawString(stack, title, startX + iconWidth + 6, centerY, -1);

        GraphicsSystem.drawRoundedRect(
                x + width / 2f - 60,
                y + header / 2f - 6 - Fonts.montserrat.getHeight(headerFont) / 2f + 18.3f,
                120, 1, 3, ColoringSystem.rgba(255, 255, 255, 1)
        );

        drawComponents(stack, mouseX, mouseY);
        drawOutline();
    }




    private boolean isModuleVisible(ModuleComponent module) {
        float moduleTop = module.getY();
        float moduleBottom = moduleTop + module.getHeight();

        float panelTop = this.getY() + 55 / 2f; // Верхняя граница панели (с учётом заголовка)
        float panelBottom = this.getY() + this.getHeight(); // Нижняя граница панели

        return moduleBottom > panelTop && moduleTop < panelBottom; // Модуль пересекает видимую область панели
    }

    protected void drawOutline() {
        Stencil.initStencilToWrite();

        GraphicsSystem.drawRoundedRect(x + 0.5f, y + 0.5f, width - 1, height - 1, new Vector4f(7, 7, 7, 7),
                ColoringSystem.rgba(1, 11, 11, (int) (255 * 0.33)));

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
        String string;
        for (ModuleComponent component : modules) {

            if(AG.getInst().getDropDownNEW().searchCheck(component.getModule().getName())){
                continue;
            }
            component.drawComponents(stack, mouseX, mouseY);
            component.setX(getX() + 6.5f);
            component.setY(getY() + header + offset  + animatedScrool);
            component.setWidth(getWidth() - 13);
            component.setHeight(22);
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
            offset += component.getHeight() + 1.6f;
            Scissor.setFromComponentCoordinates(testX, testY, testW, testH);
        }
        animatedScrool = MathSystem.fast(animatedScrool, scroll, 0);
        float scrollbarHeight = MathHelper.clamp((height - header - 10) * (height - header - 10) / max, 10, height - header - 10);
        float scrollbarY = getY() + header + (-getScroll() / (max - height + header + 4)) * (height - header - 4 - scrollbarHeight);
        scrollbarHeight = MathHelper.clamp(scrollbarHeight, 10, height - header - 10); // TODO: у меня мин высота скроллбара 20
        scrollbarY = MathHelper.clamp(scrollbarY, getY() + header, getY() + height - scrollbarHeight - 4);
        // TODO: Отрисовка скроллбара с учетом ограничения прокрутки
        if (max > height - header - 10) {
            setScroll(MathHelper.clamp(getScroll(), -max + height - header - 70, 0)); // TODO: Ограничение прокрутки (ставите свое значение если это надо)
            setAnimatedScrool(MathHelper.clamp(animatedScrool, -max + height - header - 50, 0));

// TODO: Если скролл на верху
            if (scroll >= 0) {
                setScroll(0);
                setAnimatedScrool(0);
            }

            if (ClickGui.scrollingg.getValue()) {
                RectUtils.drawRoundedRect(getX() + getWidth() - 4f, scrollbarY - 1, 2f, scrollbarHeight - 40, 0.5f, ColoringSystem.rgba(100, 100, 100, 200));
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
// TODO: Обработка кликов мыши по компонентам
        for (ModuleComponent component : modules) {
            if(AG.getInst().getDropDownNEW().searchCheck(component.getModule().getName())){
                continue;
            }
            component.mouseClick(mouseX, mouseY, button);
        }

// TODO: Обработка нажатия на скроллбар
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