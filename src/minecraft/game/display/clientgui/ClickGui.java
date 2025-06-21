package minecraft.game.display.clientgui;

import com.mojang.blaze3d.matrix.MatrixStack;

import lombok.Getter;
import lombok.Setter;
import minecraft.game.advantage.advisee.ClientReceive;
import minecraft.game.advantage.advisee.IMinecraft;
import minecraft.game.advantage.alacrity.impl.DecelerateAnimation;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.figures.Vector2i;
import minecraft.game.advantage.make.Cursors;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.make.other.Scissor;
import minecraft.game.advantage.make.other.Stencil;
import minecraft.game.advantage.words.MoreColorsSystem;
import minecraft.game.advantage.words.font.ClientFonts;
import minecraft.game.display.clientgui.api.component.Component;
import minecraft.game.display.clientgui.component.ModuleComponent;
import minecraft.game.display.clientgui.component.impl.*;
import minecraft.game.display.clientpanel.Panel;
import minecraft.game.display.clientpanel.PanelStyle;
import minecraft.game.enjoin.feature.PassCommand;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.system.AG;
import minecraft.system.styles.Style;
import minecraft.system.styles.StyleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;



public class ClickGui extends Screen implements IMinecraft {
    boolean themeWindowOpen;
    private final DecelerateAnimation themeAnimation;
    private final DecelerateAnimation iconAnimation;
    private final List<Category> panels = new ArrayList<>();

    public ClickGui() {
        super(new StringTextComponent("GUI"));
        for (Module function : AG.getInst().getModuleManager().getModules()) {
            objects.add(new ModuleComponent(function));
        }
        themeWindowOpen = false;
        this.themeAnimation = new DecelerateAnimation(300, 1.0);
        this.iconAnimation = new DecelerateAnimation(200, 1.0);
    }

    private double xPanel, yPanel;
    private float animation;

    private Category current = Category.Combat;
    private ArrayList<ModuleComponent> objects = new ArrayList<>();
    @Setter
    @Getter
    private Category currentCategory = Category.Combat;

    private float scroll = 0;
    private float animateScroll = 0;

    @Override
    public void onClose() {
        super.onClose();
        themeWindowOpen = false;
        typing = false;
        searchText = "";
        selectAll = false;

        for (ModuleComponent moduleComponent : objects) {
            for (Component settingComp : moduleComponent.components) {
                if (settingComp instanceof ModeComponent) {
                    ((ModeComponent) settingComp).close();
                } else if (settingComp instanceof MultiListComponent) {
                    ((MultiListComponent) settingComp).close();
                } else if (settingComp instanceof SliderComponent) {
                    ((SliderComponent) settingComp).close();
                }
            }
        }
        ModuleComponent.binding = null;
        KeyBindComponent.binding = null;
        ModeComponent.currentOpened = null;
        MultiListComponent.currentOpened = null;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        float scale = 2f;
        float width = 800 / scale;
        float height = 526 / scale;
        float x = MathSystem.calculateXPosition(mc.getMainWindow().scaledWidth() / 2f, width);
        float y = MathSystem.calculateXPosition(mc.getMainWindow().scaledHeight() / 2f, height);

        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            if (!themeAnimation.isDone()) return true;
            scroll += delta * 15;
            return true;
        }
        return false;
    }

    @Override
    public void closeScreen() {
        if (typing || !searchText.isEmpty()) {
            typing = false;
            searchText = "";
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private boolean selectAll = false;
    private boolean searchBarHovered = false;
    private long lastBlinkTime = System.currentTimeMillis();
    private boolean showCursor = true;

    private String searchText = "";
    public static boolean typing;

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.minecraft.keyboardListener.enableRepeatEvents(true);
        float scale = 2f;
        float width = 800 / scale;
        float height = 526 / scale;
        float leftPanel = 200 / scale;

// Получаем размеры экрана
        float screenWidth = mc.getMainWindow().scaledWidth();
        float screenHeight = mc.getMainWindow().scaledHeight();

// Вычисляем x и y так, чтобы они всегда были по центру
        float x = (screenWidth - width) / 2f;
        float y = (screenHeight - height) / 2f;

        xPanel = x;
        yPanel = y;
        animation = MathSystem.lerp(animation, 0, 5);

        Vector2i fixed = ClientReceive.getMouse(mouseX, mouseY);
        mouseX = fixed.getX();
        mouseY = fixed.getY();

        int finalMouseX = mouseX;
        int finalMouseY = mouseY;

        mc.gameRenderer.setupOverlayRendering(2);
        renderBackground(matrixStack, x, y, width, height, leftPanel, finalMouseX, finalMouseY);
        renderLines(matrixStack, x, y, width, height, leftPanel, finalMouseX, finalMouseY);
        renderCategories(matrixStack, x, y, width, height, leftPanel, finalMouseX, finalMouseY);
        renderComponents(matrixStack, x, y, width, height, leftPanel, finalMouseX, finalMouseY);

        renderThemes(matrixStack, x, y, width, height, leftPanel, finalMouseX, finalMouseY);
        renderSearchBar(matrixStack, x, y, width, height, leftPanel, finalMouseX, finalMouseY);
        mc.gameRenderer.setupOverlayRendering();
    }


    void renderBackground(MatrixStack matrixStack, float x, float y, float width, float height, float leftPanel, int mouseX, int mouseY) {
        GraphicsSystem.drawRoundedRect(x, y, width, height, 6, ColoringSystem.rgba(12, 12, 14, 255));

        ITextComponent name = MoreColorsSystem.gradient(
                "Akvi4 Client",
                ColoringSystem.getColorTest4(1),
                ColoringSystem.rgb(20,20,20)
        );

        ClientFonts.icons_hud[20].drawString(
                matrixStack,
                "A",
                x + leftPanel / 2f - ClientFonts.icons_hud[20].getWidth("A") - 16,
                y + 15,
                ColoringSystem.getColorTest4(1)
        );

        ClientFonts.clickGui[19].drawCenteredString(
                matrixStack,
                name,
                x + leftPanel / 2f - 6,
                y + 14,
                -1
        );

        Category currentCategory = AG.getInst().getClickGui().getCurrentCategory();
        if (currentCategory != null) {
            String categoryName = currentCategory.name();

            float categoryX = x + leftPanel - 5;
            float categoryY = y + 15;

            ClientFonts.clickGui[18].drawString(
                    matrixStack,
                    categoryName,
                    categoryX,
                    categoryY,
                    -1
            );
        }

        Stencil.initStencilToWrite();
       GraphicsSystem.drawRoundedRect(x + 5, y + 240, 18, 18, 4, -1);
        Stencil.readStencilBuffer(1);
        Stencil.uninitStencilBuffer();

        ClientFonts.clickGui[14].drawString(matrixStack, PassCommand.currentUser, x + 27, y + 244, -1);
        ClientFonts.clickGui[14].drawString(matrixStack, "UID " + PassCommand.currentUid, x + 27, y + 252, ColoringSystem.rgb(180, 180, 180));
    }

    void renderLines(MatrixStack matrixStack, float x, float y, float width, float height, float leftPanel, int mouseX, int mouseY) {
        GraphicsSystem.drawRoundedRect(x + 86, y, 1, height, 1, ColoringSystem.rgb(30,30,30));
        GraphicsSystem.drawRoundedRect(x, y + 30, width, 1, 1, ColoringSystem.rgb(30,30,30));
        GraphicsSystem.drawRoundedRect(x - 1, y + 234, 88, 1, 1, ColoringSystem.rgb(30,30,30));
    }

    void renderCategories(MatrixStack matrixStack, float x, float y, float width, float height, float leftPanel, int mouseX, int mouseY) {
        float heightCategory = 38 / 2f;

        for (Category Category : Category.values()) {
            char iconChar = Category.getIconChar();
            String iconString = String.valueOf(iconChar);

            String categoryName = Category.name();
            float textWidth = ClientFonts.clickGui[14].getWidth(categoryName);

            float iconWidth = ClientFonts.icons_gui[18].getWidth(iconString);
            float dynamicWidth = iconWidth + textWidth + 20;

            if (Category == current) {
                GraphicsSystem.drawRoundedRect(
                        x + 10,
                        y + 55f + Category.ordinal() * heightCategory,
                        dynamicWidth,
                        1.0f,
                        1,
                        ColoringSystem.getColorTest4(1)
                );
            }

            ClientFonts.icons_gui[18].drawString(
                    matrixStack,
                    iconString,
                    x + 12,
                    y + 40f + Category.ordinal() * heightCategory + (heightCategory / 2f) - (ClientFonts.icons_gui[18].getFontHeight() / 2f),
                    Category == current ? ColoringSystem.getColorTest4(1) : -1
            );

            ITextComponent gradient = MoreColorsSystem.gradient(
                    categoryName,
                    ColoringSystem.getColorTest4(1),
                    ColoringSystem.rgb(20,20,20)
            );

            if (Category == current) {
                ClientFonts.clickGui[14].drawString(
                        matrixStack,
                        gradient,
                        x + 20 + iconWidth,
                        y + 45 + Category.ordinal() * heightCategory,
                        -1
                );
            } else {
                ClientFonts.clickGui[14].drawString(
                        matrixStack,
                        categoryName,
                        x + 20 + iconWidth,
                        y + 45 + Category.ordinal() * heightCategory,
                        Category == current ? ColoringSystem.getColorTest4(1) : -1
                );
            }
        }
    }

    void renderComponents(MatrixStack matrixStack, float x, float y, float width, float height, float leftPanel, int mouseX, int mouseY) {
        drawComponents(matrixStack, mouseX, mouseY);
    }

    void renderThemes(MatrixStack matrixStack, float x, float y, float width, float height, float leftPanel, int mouseX, int mouseY) {
        float themeIconX = x + 71;
        float themeIconY = y + 231;
        String iconText = "F";
        float iconWidth = ClientFonts.icons_gui[20].getWidth(iconText);
        float iconHeight = ClientFonts.icons_gui[20].getFontHeight();

        int clientColor = ColoringSystem.getColorTest4(1);
        int hoverColor = ColoringSystem.rgb(200, 200, 200);
        int defaultColor = ColoringSystem.rgb(100, 100, 100);

        int iconColor;
        if (themeWindowOpen) {
            iconColor = clientColor;
        } else if (MathSystem.isHovered(mouseX, mouseY, themeIconX - 1, themeIconY + 12, iconWidth, iconHeight)) {
            iconColor = hoverColor;
        } else {
            iconColor = defaultColor;
        }

        ClientFonts.icons_gui[20].drawString(
                matrixStack,
                iconText,
                themeIconX,
                themeIconY + 15,
                iconColor
        );

        themeAnimation.setDirection(themeWindowOpen);
        double themeProgress = themeAnimation.getOutput();

        if (themeWindowOpen || !themeAnimation.isDone()) {
            renderThemeWindow(matrixStack, x + width + 5, y, mouseX, mouseY, themeProgress);
        }
    }

    private void renderThemeWindow(MatrixStack matrixStack, float x, float y, int mouseX, int mouseY, double progress) {
        float themeWindowWidth = 150;
        float themeWindowHeight = 263;

        float animatedHeight = (float) (themeWindowHeight * progress);

        float scrollAmount = Math.max(0, scroll);
        scrollAmount = Math.min(scrollAmount, animatedHeight - themeWindowHeight);

        GraphicsSystem.drawRoundedRect(x, y, themeWindowWidth, animatedHeight, 6, ColoringSystem.rgb(12, 12, 14));

        if (progress > 0.1) {
            ClientFonts.clickGui[16].drawString(matrixStack, "Выбор Темы", x + 10, y + 10, -1);
            GraphicsSystem.drawRoundedRect(x, y + 21, themeWindowWidth, 1.0f, 1, ColoringSystem.rgb(30, 30, 30));
        }

        float offsetY = y + 30 - scrollAmount;
        int clientColor = ColoringSystem.getColorTest4(1);
        Style currentTheme = AG.getInst().getStyleManager().getCurrentStyle();
        for (Style theme : AG.getInst().getStyleManager().getStyleList()) {
            if (offsetY > y + animatedHeight) break;

            String themeName = theme.getStyleName();
            float textWidth = ClientFonts.clickGui[14].getWidth(themeName);
            float textHeight = ClientFonts.clickGui[14].getFontHeight();

            boolean isHovered = MathSystem.isHovered(mouseX, mouseY, x + 10, offsetY - 2, textWidth, textHeight);
            int textColor = isHovered ? clientColor : -1;

            if (theme.equals(current)) {
                ITextComponent gradient = MoreColorsSystem.gradient(themeName, ColoringSystem.getColorTest4(1), ColoringSystem.rgb(20,20,20));
                ClientFonts.clickGui[14].drawString(matrixStack, gradient, x + 10, offsetY, -1);
            } else {
                ClientFonts.clickGui[14].drawString(matrixStack, themeName, x + 10, offsetY, textColor);
            }

            float circleXOffset = x + themeWindowWidth - 20;
            float circleYOffset = offsetY + (textHeight / 2) - 8;

            if (theme.equals(currentTheme)) {
                ClientFonts.settings_gui[30].drawString(matrixStack, "A", x + textWidth + 15, offsetY - 4, clientColor);
            }

            int color = theme.getSecondColor().getRGB();
            GraphicsSystem.drawRoundedRect(circleXOffset, circleYOffset, 10, 10, 3, color);
            offsetY += 20;
        }
    }


    void renderSearchBar(MatrixStack matrixStack, float x, float y, float width, float height, float leftPanel, int mouseX, int mouseY) {
        float searchBarWidth = 80f;
        float searchBarHeight = 15f;
        float searchBarX = x + width - searchBarWidth - 10;
        float searchBarY = y + 9;

        boolean currentlyHovered = MathSystem.isHovered(mouseX, mouseY, searchBarX, searchBarY, searchBarWidth, searchBarHeight);
        if (currentlyHovered && !searchBarHovered) {
            GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.IBEAM);
            searchBarHovered = true;
        } else if (!currentlyHovered && searchBarHovered) {
            GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
            searchBarHovered = false;
        }

        if (typing && System.currentTimeMillis() - lastBlinkTime > 500) {
            showCursor = !showCursor;
            lastBlinkTime = System.currentTimeMillis();
        }

        GraphicsSystem.drawRoundedRect(
                searchBarX,
                searchBarY + 15,
                searchBarWidth,
                1.0f,
                1,
                typing ? ColoringSystem.getColorTest4(1) : ColoringSystem.rgb(30, 30, 30)
        );

        String displayText = searchText + (typing && showCursor ? "_" : "");
        float maxTextWidth = searchBarWidth - 8;

        if (ClientFonts.clickGui[16].getWidth(displayText) > maxTextWidth) {
            displayText = displayText.substring(0, Math.min(displayText.length(), displayText.length() - 3)) + "...";
            while (ClientFonts.clickGui[16].getWidth(displayText) > maxTextWidth && displayText.length() > 3) {
                displayText = displayText.substring(0, displayText.length() - 4) + "...";
            }
        }

        ClientFonts.clickGui[16].drawString(
                matrixStack,
                displayText,
                searchBarX + 5,
                searchBarY + (searchBarHeight / 2) - 3,
                0xFF878894
        );

        if (searchText.isEmpty() && !typing) {
            ClientFonts.clickGui[16].drawString(
                    matrixStack,
                    "Поиск",
                    searchBarX + 5,
                    searchBarY + (searchBarHeight / 2) - 3,
                    0xFF878894
            );

            GraphicsSystem.drawImage(
                    new ResourceLocation(AG.getInst().getClientDir().getPath() + "icons/gui/search.png"),
                    searchBarX + searchBarWidth - 15,
                    searchBarY + (searchBarHeight / 2) - 5.5f,
                    10, 10,
                    0xFF878894
            );
        }
    }

    void drawComponents(MatrixStack stack, int mouseX, int mouseY) {

        List<ModuleComponent> moduleComponentList = objects.stream().filter(moduleObject -> {
            if (!searchText.isEmpty()) {
                return moduleObject.module.getName().toLowerCase().contains(searchText.toLowerCase());
            } else {
                return moduleObject.module.getCategory() == current;
            }
        }).toList();

        int halfSize = (moduleComponentList.size() + 1) / 2;
        List<ModuleComponent> first = moduleComponentList.subList(0, halfSize);
        List<ModuleComponent> second = moduleComponentList.subList(halfSize, moduleComponentList.size());

        float scale = 2f;
        animateScroll = MathSystem.lerp(animateScroll, scroll, 10);
        float height = 526 / scale;

        Scissor.push();
        float firstColumnX = (float) (xPanel + (80f + 12));
        float firstColumnY = (float) (yPanel + (64 / 2f) + 4);
        float firstColumnWidth = 130;
        float firstColumnHeight = height - 38;

        Scissor.setFromComponentCoordinates(firstColumnX, firstColumnY, firstColumnWidth, firstColumnHeight);

        float offset = firstColumnY + animateScroll;
        float size1 = 0;
        for (ModuleComponent component : first) {
            component.parent = this;
            component.setPosition(firstColumnX, offset, 260 / 2f, 20);
            component.drawComponent(stack, mouseX, mouseY);

            if (!component.components.isEmpty()) {
                for (Component settingComp : component.components) {
                    if (settingComp.setting != null && settingComp.setting.visible.get()) {
                        offset += settingComp.height;
                        size1 += settingComp.height;
                    }
                }
            }
            offset += component.height + 6;
            size1 += component.height + 6;
        }
        Scissor.unset();
        Scissor.pop();

        Scissor.push();
        float secondColumnX = (float) (xPanel + (60f + 12) + 314 / 2f + 4);
        float secondColumnY = (float) (yPanel + (64 / 2f) + 4);
        float secondColumnWidth = 160;
        float secondColumnHeight = height - 38;

        Scissor.setFromComponentCoordinates(secondColumnX, secondColumnY, secondColumnWidth, secondColumnHeight);

        float offset2 = secondColumnY + animateScroll;
        float size2 = 0;
        for (ModuleComponent component : second) {
            component.parent = this;
            component.setPosition(secondColumnX, offset2, 320 / 2f, 20);
            component.drawComponent(stack, mouseX, mouseY);

            if (!component.components.isEmpty()) {
                for (Component settingComp : component.components) {
                    if (settingComp.setting != null && settingComp.setting.visible.get()) {
                        offset2 += settingComp.height;
                        size2 += settingComp.height;
                    }
                }
            }
            offset2 += component.height + 6;
            size2 += component.height + 6;
        }
        Scissor.unset();
        Scissor.pop();

        float max = Math.max(size1, size2);
        if (max < height) {
            scroll = 0;
        } else {
            scroll = MathHelper.clamp(scroll, -(max - height + 50), 0);
        }
    }

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);

        typing = false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {

        Vector2i fixed = ClientReceive.getMouse((int) mouseX, (int) mouseY);
        mouseX = fixed.getX();
        mouseY = fixed.getY();

        for (ModuleComponent m : objects) {
            if (searchText.isEmpty()) {
                if (m.module.getCategory() != current) continue;
            } else {
                if (!m.module.getName().toLowerCase().contains(searchText.toLowerCase())) continue;
            }
            m.mouseReleased((int) mouseX, (int) mouseY, button);
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            mc.displayGuiScreen(null);
            this.minecraft.keyboardListener.enableRepeatEvents(false);
            return true;
        }

        boolean ctrlDown = Screen.hasControlDown();
        if (typing) {
            if (ctrlDown && keyCode == GLFW.GLFW_KEY_A) {
                selectAll = true;
                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (selectAll) {
                    searchText = "";
                    selectAll = false;
                } else if (!searchText.isEmpty()) {
                    searchText = searchText.substring(0, searchText.length() - 1);
                }
                return true;
            }

            if (ctrlDown && keyCode == GLFW.GLFW_KEY_V) {
                String pasteText = GLFW.glfwGetClipboardString(Minecraft.getInstance().getMainWindow().getHandle());
                searchText += pasteText;
                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_DELETE) {
                searchText = "";
                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                typing = false;
                return true;
            }
        }

        for (ModuleComponent m : objects) {
            if (searchText.isEmpty()) {
                if (m.module.getCategory() != current) continue;
            } else {
                if (!m.module.getName().toLowerCase().contains(searchText.toLowerCase())) continue;
            }
            m.keyTyped(keyCode, scanCode, modifiers);
        }

        if (ModuleComponent.binding != null) {
            if (keyCode == GLFW.GLFW_KEY_DELETE) {
                ModuleComponent.binding .module.setBind(0);
            } else {
                ModuleComponent.binding .module.setBind(keyCode);
            }
            ModuleComponent.binding  = null;
            return true;
        }

        if (themeWindowOpen && typing) {
            return false;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!themeWindowOpen) {
            if (typing) {
                searchText += codePoint;
            }
        }

        for (ModuleComponent m : objects) {
            if (searchText.isEmpty()) {
                if (m.module.getCategory() != current) continue;
            } else {
                if (!m.module.getName().toLowerCase().contains(searchText.toLowerCase())) continue;
            }
            m.charTyped(codePoint, modifiers);
        }

        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        Vector2i fixed = ClientReceive.getMouse((int) mouseX, (int) mouseY);
        mouseX = fixed.getX();
        mouseY = fixed.getY();

        float scale = 2f;
        float width = 800 / scale;
        float height = 526 / scale;
        float leftPanel = 165 / scale;
        float x = MathSystem.calculateXPosition(mc.getMainWindow().scaledWidth() / 2f, width);
        float y = MathSystem.calculateXPosition(mc.getMainWindow().scaledHeight() / 2f, height);


        if (processThemeWindow(mouseX, mouseY, x, y, width, height, button)) {
            return true;
        }

        if (processCategorySelection(mouseX, mouseY, x, y, leftPanel)) {
            return true;
        }

        processModuleComponents(mouseX, mouseY, x, y, width, height, button);

        if (processSearchBar(mouseX, mouseY, x, width, y)) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }


    private boolean processThemeWindow(double mouseX, double mouseY, float x, float y, float width, float height, int button) {
        float themeIconX = x + 71;
        float themeIconY = y + 242;

        String iconText = "F";
        float iconWidth = ClientFonts.icons_gui[20].getWidth(iconText);
        float iconHeight = ClientFonts.icons_gui[20].getFontHeight();

        float themeWindowWidth = 150;
        float themeWindowX = x + width + 5;
        float themeWindowY = y;
        float themeWindowHeight = 263;
        float offsetY = themeWindowY + 30;

        if (MathSystem.isHovered((float) mouseX, (float) mouseY, themeIconX - 1, themeIconY, iconWidth , iconHeight + 2)) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
                themeWindowOpen = !themeWindowOpen;
                typing = false;
                return true;
            }
        }

        if (themeWindowOpen && MathSystem.isHovered((float) mouseX, (float) mouseY, themeWindowX, themeWindowY, themeWindowWidth, themeWindowHeight)) {
            for (Style theme : AG.getInst().getStyleManager().getStyleList()) {
                if (MathSystem.isHovered((float) mouseX, (float) mouseY, themeWindowX + 10, offsetY - 2, ClientFonts.clickGui[14].getWidth(theme.getStyleName()), ClientFonts.clickGui[14].getFontHeight())) {
                    AG.getInst().getStyleManager().setCurrentStyle(theme);
                    return true;
                }
                offsetY += 20;
            }
        }
        return false;

    }

    private boolean processCategorySelection(double mouseX, double mouseY, float x, float y, float leftPanel) {
        float heightCategory = 38 / 2f;

        for (Category Category : Category.values()) {
            char iconChar = Category.getIconChar();

            String iconString = String.valueOf(iconChar);

            String categoryName = Category.name();
            float textWidth = ClientFonts.clickGui[14].getWidth(categoryName);
            float iconWidth = ClientFonts.icons_gui[18].getWidth(iconString);
            float dynamicWidth = iconWidth + textWidth + 20;
            float categoryX = x + 10;
            float categoryY = y + 35 + Category.ordinal() * heightCategory;

            if (MathSystem.isHovered((float) mouseX, (float) mouseY, categoryX, categoryY, dynamicWidth, heightCategory)) {
                if (current == Category) return false;
                current = Category;
                setCurrentCategory(Category);
                animation = 1;
                scroll = 0;
                searchText = "";

                typing = false;
                return true;
            }
        }
        return false;
    }

    private void processModuleComponents(double mouseX, double mouseY, float x, float y, float width, float height, int button) {
        if (MathSystem.isHovered((float) mouseX, (float) mouseY, x, y + 64 / 2f, width, height - 64 / 2f)) {
            for (ModuleComponent m : objects) {
                if (searchText.isEmpty()) {
                    if (m.module.getCategory() != current) continue;
                } else {
                    if (!m.module.getName().toLowerCase().contains(searchText.toLowerCase())) continue;
                }
                m.mouseClicked((int) mouseX, (int) mouseY, button);
            }
        }
    }

    private boolean processSearchBar(double mouseX, double mouseY, float x, float width, float y) {
        float searchBarWidth = 80f;
        float searchBarHeight = 15f;
        float searchBarX = x + width - searchBarWidth - 10;
        float searchBarY = y + 9;

        if (themeWindowOpen) {
            typing = false;
            return false;
        }

        if (MathSystem.isHovered((float) mouseX, (float) mouseY, searchBarX, searchBarY, searchBarWidth, searchBarHeight)) {
            typing = true;
            showCursor = true;
            lastBlinkTime = System.currentTimeMillis();
            return true;
        } else {
            typing = false;
        }
        return false;
    }
}
