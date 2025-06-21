package minecraft.game.display.mainscreen;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.Setter;
import minecraft.game.advantage.advisee.ClientReceive;
import minecraft.game.advantage.advisee.IMinecraft;
import minecraft.game.advantage.figures.*;
import minecraft.game.advantage.luvvy.MouseManager;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.make.other.Scissor;
import minecraft.game.advantage.make.other.Stencil;
import minecraft.game.advantage.words.font.ClientFonts;
import minecraft.system.AG;
import minecraft.system.managers.config.AltConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Session;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.StringTextComponent;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;

public class AltScreen extends Screen implements IMinecraft {

    public AltScreen() {
        super(new StringTextComponent(""));
    }
    private final List<SnowParticle> snowParticles = new ArrayList<>();

    public final TimeCounterSetting timer = new TimeCounterSetting();
    public float o = 0;

    private Thread backspaceThread;
    @Getter
    @Setter
    private Account selectedAccount = null;
    public ArrayList<Account> accounts = new ArrayList<>();

    private int cursorPos = 0;
    public float scroll;
    public float scrollAn;

    private String altName = "";
    private boolean typing;
    private boolean selectAll = false;

    private boolean backspaceHeld = false;
    private long initialDelay = 300L;
    private long repeatRate = 50L;

    private String copyStatusText = "Чтобы скопировать ник, выберите аккаунт и нажмите на CTRL + C";
    private long copyStatusStartTime = 0L;
    private static final int STATUS_GraphicsSystem_DURATION = 2000;

    private String pasteStatusText = "Чтобы вставить ник, выберите ввод и нажмите на CTRL + V";
    private long pasteStatusStartTime = 0L;
    private static final int PASTE_STATUS_GraphicsSystem_DURATION = 2000;

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        mc.gameRenderer.setupOverlayRendering(2);

        scrollAn = MathSystem.lerp(scrollAn, scroll, 5);

        for (float i = 0; i < 1488; i++) {
            if (timer.isReached(10)) {
                o++;
                i = 0;
                timer.reset();
            }
        }

        int windowWidth = ClientReceive.calc(mc.getMainWindow().getScaledWidth());
        int windowHeight = ClientReceive.calc(mc.getMainWindow().getScaledHeight());

        GraphicsSystem.drawRectNEW(0,0,windowWidth,windowHeight, ColoringSystem.rgb(24,24,24));
//        GraphicsSystem.drawImage(
//        new ResourceLocation(BladeClient.getInstance().getClientInfo().getBasePath() + "замени на свое.png"),
//        0, 0, windowWidth, windowHeight, -1);

//        for (SnowParticle snowParticle : snowParticles) {
//            snowParticle.update(windowWidth, windowHeight);
//            int color = ColoringSystem.setAlpha(ColoringSystem.rgb(255, 255, 255), (int) (snowParticle.getAlpha() * 255));
//            GraphicsSystem.drawCircle(
//                    snowParticle.getX(),
//                    snowParticle.getY(),
//                    snowParticle.getRadius(),
//                    color
//            );
//        }

        float textWidthCopyNick = ClientFonts.clickGui[16].getWidth(copyStatusText);
        float centeredXCopyNick = (windowWidth / 2f) - (textWidthCopyNick / 2f);
        float bottomY = windowHeight - 15;

        float offset = 6f;
        float width = 320f, height = 250f;
        float x = (windowWidth / 2f) - (width / 2f);
        float y = (windowHeight / 2f) - (height / 2f);

        GraphicsSystem.drawRoundedRect(x - offset, y - offset, width + offset * 4f, height + offset * 2f, 4, ColoringSystem.rgba(45, 45, 45, 100));

        if (System.currentTimeMillis() - copyStatusStartTime > STATUS_GraphicsSystem_DURATION) {
            copyStatusText = "Чтобы скопировать ник, выберите аккаунт и нажмите на CTRL + C";
        }
        ClientFonts.clickGui[16].drawString(matrixStack, copyStatusText, centeredXCopyNick, bottomY, ColoringSystem.rgba(255, 255, 255, 100));

        Vector2i fixedMouse = ClientReceive.getMouse(mouseX, mouseY);
        drawButtons(matrixStack, x, y, fixedMouse.getX(), fixedMouse.getY());

        float size = 0f;
        float yOffset = 0;
        int maxColumns = 2;
        float columnWidth = 155f;
        float columnSpacing = 5f;
        float rowHeight = 60f;

        Scissor.push();
        Scissor.setFromComponentCoordinates(
                x - offset + 0.5f,
                y - offset + 0.5f,
                width + offset * 4f - 0.5f,
                height + offset * 1.4f
        );

        for (int i = 0; i < accounts.size(); i++) {
            Account account = accounts.get(i);

            int currentColumn = i % maxColumns;
            int currentRow = i / maxColumns;

            float increasedSpacing = columnSpacing + 10;
            float colX = x - offset + currentColumn * (columnWidth + increasedSpacing) + 10;
            float rowY = y + offset + yOffset + currentRow * rowHeight + scroll;

            if (rowY + rowHeight > y - offset && rowY < y + height + offset) {
                GraphicsSystem.drawRoundedRect(colX, rowY, columnWidth, 45f, 8, ColoringSystem.rgba(45, 45, 45, 156));

                if (selectedAccount != null && selectedAccount.equals(account)) {
                    GraphicsSystem.drawRoundedRect(colX, rowY, columnWidth, 45f, 8, ColoringSystem.rgba(35, 35, 35, 156));
                }

                float centerY = rowY + 50f / 2f - 2;
                float nameYOffset = -6f;
                float timeYOffset = 6f;

                Stencil.initStencilToWrite();
                GraphicsSystem.drawRadiusHead(colX + 10, centerY + nameYOffset - 10, 32, 32, 6f, Color.BLACK.getRGB());
                Stencil.readStencilBuffer(1);
                mc.getTextureManager().bindTexture(account.skin);
                AbstractGui.drawScaledCustomSizeModalRect(
                        colX + 10,
                        centerY + nameYOffset - 10,
                        8F, 8F, 8F, 8F,
                        32, 32, 64, 64
                );
                Stencil.uninitStencilBuffer();
                GraphicsSystem.drawRadiusHead(colX + 10, centerY + nameYOffset - 10, 32, 32, 6f, Color.BLACK.getRGB());

                Scissor.push();
                Scissor.setFromComponentCoordinates(colX + 55, centerY + nameYOffset - 10, columnWidth - 60, 45f);
                ClientFonts.clickGui[18].drawString(matrixStack, account.accountName, colX + 55, centerY + nameYOffset - 2, -1);
                Scissor.unset();
                Scissor.pop();

                ClientFonts.clickGui[16].drawString(matrixStack, account.getFormattedDateAdded(), colX + 55, centerY + timeYOffset - 2, ColoringSystem.rgb(180, 180, 180));
            }
            size++;
        }

        float maxScrollableHeight = Math.max(0, (float) Math.ceil((double) accounts.size() / maxColumns) * rowHeight - height);
        scroll = MathHelper.clamp(scroll, -maxScrollableHeight, 0);

        Scissor.unset();
        Scissor.pop();

        if (System.currentTimeMillis() - pasteStatusStartTime > PASTE_STATUS_GraphicsSystem_DURATION) {
            pasteStatusText = "Чтобы вставить ник, выберите ввод и нажмите на CTRL + V";
        }
        float textWidthPasteNick = ClientFonts.clickGui[16].getWidth(pasteStatusText);
        float centeredXPasteNick = (windowWidth / 2f) - (textWidthPasteNick / 2f);
        ClientFonts.clickGui[16].drawString(matrixStack, pasteStatusText, centeredXPasteNick, bottomY - 12, ColoringSystem.rgba(255, 255, 255, 100));
        String GraphicsSystemedText = typing ? altName.substring(0, cursorPos) + ((System.currentTimeMillis() % 1000 > 500) ? "|" : "") + altName.substring(cursorPos) : "Никнейм";
        int textColor = typing ? ColoringSystem.rgb(255, 255, 255) : ColoringSystem.rgb(152, 152, 152);
        String addText = "Добавить";
        String randomText = "Рандом";

        float formWidth = 175f;
        float formHeight = 80f;
        float textWidth = ClientFonts.clickGui[22].getWidth("Введите желаемый ник");

        float addTextWidth = ClientFonts.clickGui[15].getWidth(addText);
        float randomTextWidth = ClientFonts.clickGui[15].getWidth(randomText);

        float addButtonWidth = addTextWidth + 25;
        float randomButtonWidth = randomTextWidth + 30;

        float centerX = x - formWidth - 10f;
        float centerY = y - 6;

        GraphicsSystem.drawRoundedRect(centerX, centerY, formWidth, formHeight, 6, ColoringSystem.rgba(45, 45, 45, 120));
        ClientFonts.clickGui[22].drawString(matrixStack, "Введите желаемый ник", centerX + (formWidth / 2f) - (textWidth / 2f), centerY + 10f, -1);

        GraphicsSystem.drawRoundedRect(centerX + 2f, centerY + 30f, formWidth - 5, 22f, 4, ColoringSystem.rgba(45, 45, 45, 150));
        Scissor.push();
        Scissor.setFromComponentCoordinates(centerX + 2f, centerY + 30f, formWidth - 7, 22f);
        ClientFonts.clickGui[15].drawString(matrixStack, GraphicsSystemedText, centerX + 8, centerY + 40f, textColor);
        Scissor.unset();
        Scissor.pop();

        float addButtonX = centerX + formWidth - addButtonWidth - 110f;
        float randomButtonX = addButtonX + addButtonWidth + 45f;

        boolean isAddHovered = MathSystem.isHovered(mouseX, mouseY, addButtonX, centerY + 58, addButtonWidth, 18f);
        GraphicsSystem.drawRoundedRect(addButtonX, centerY + 58, addButtonWidth, 18f, 4f, ColoringSystem.rgba(45, 45, 45, 150));
        ClientFonts.clickGui[15].drawCenteredString(matrixStack, addText, addButtonX + addButtonWidth / 2f, centerY + 65f, isAddHovered ? -1 : ColoringSystem.rgb(180, 180, 180));

        boolean isRandomHovered = MathSystem.isHovered(mouseX, mouseY, randomButtonX, centerY + 58, randomButtonWidth, 18f);
        GraphicsSystem.drawRoundedRect(randomButtonX, centerY + 58, randomButtonWidth, 18f, 4f, ColoringSystem.rgba(45, 45, 45, 150));
        ClientFonts.clickGui[15].drawCenteredString(matrixStack, randomText, randomButtonX + randomButtonWidth / 2f, centerY + 65f, isRandomHovered ? -1 : ColoringSystem.rgb(180, 180, 180));

        float textWidthName = ClientFonts.clickGui[22].getWidth("Текущий ник: " + mc.session.getUsername());
        float textWidthInfo = ClientFonts.clickGui[22].getWidth("Кликните на любой ник,для входа в аккаунт");
        float centeredXName = (mc.getMainWindow().getScaledWidth() / 2f) - (textWidthName / 2f);
        float centeredXInfo = (mc.getMainWindow().getScaledWidth() / 2f) - (textWidthInfo / 2f);

        ClientFonts.clickGui[22].drawString(matrixStack, "Текущий ник: " + mc.session.getUsername(), centeredXName, y + offset - 40, -1);
        ClientFonts.clickGui[22].drawString(matrixStack, "Кликните на любой ник,для входа в аккаунт", centeredXInfo, y + offset - 25, -1);
        mc.gameRenderer.setupOverlayRendering();
    }

    private void drawButtons(MatrixStack matrixStack, float x, float y, float mouseX, float mouseY) {
        String title = "Действия";
        String clear = "Очистить все";
        String exit = "Закрыть";

        float clearTextWidth = ClientFonts.clickGui[15].getWidth(clear);
        float exitTextWidth = ClientFonts.clickGui[15].getWidth(exit);

        float clearButtonWidth = clearTextWidth + 25f;
        float exitButtonWidth = exitTextWidth + 35f;

        float buttonHeight = 18f;
        float scaledWidth = mc.getMainWindow().getScaledWidth();

        float blockWidth = 160f;
        float blockHeight = 60f;

        float blockX = (scaledWidth / 2f) + (blockWidth + 22);
        float blockY = y - 6;

        GraphicsSystem.drawRoundedRect(blockX, blockY, blockWidth, blockHeight, 6, ColoringSystem.rgba(45, 45, 45, 120));
        ClientFonts.clickGui[22].drawCenteredString(matrixStack, title, blockX + blockWidth / 2f, blockY + 10f, -1);

        float buttonStartY = blockY + 37f;
        float clearButtonX = blockX + 6f;
        float exitButtonX = clearButtonX + clearButtonWidth + 7;

        boolean isClearHovered = MathSystem.isHovered(mouseX, mouseY, clearButtonX, buttonStartY, clearButtonWidth, buttonHeight);
        GraphicsSystem.drawRoundedRect(clearButtonX, buttonStartY, clearButtonWidth, buttonHeight, 4f, ColoringSystem.rgba(45, 45, 45, 100));
        ClientFonts.clickGui[15].drawCenteredString(matrixStack, clear, clearButtonX + clearButtonWidth / 2f, buttonStartY + 7f, isClearHovered ? -1 : ColoringSystem.rgb(180, 180, 180));

        boolean isExitHovered = MathSystem.isHovered(mouseX, mouseY, exitButtonX, buttonStartY, exitButtonWidth, buttonHeight);
        GraphicsSystem.drawRoundedRect(exitButtonX, buttonStartY, exitButtonWidth, buttonHeight, 4f, ColoringSystem.rgba(45, 45, 45, 100));
        ClientFonts.clickGui[15].drawCenteredString(matrixStack, exit, exitButtonX + exitButtonWidth / 2f, buttonStartY + 7f, isExitHovered ? -1 : ColoringSystem.rgb(180, 180, 180));
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (typing) {
            if (handleArrowKeys(keyCode)) return true;
            if (handleEnterKey(keyCode)) return true;
            if (handleBackspaceKey(keyCode)) return true;
        }

        if (handleClipboardActions(keyCode, modifiers)) return true;

        if (handleCopyAction(keyCode, modifiers)) return true;

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private boolean handleArrowKeys(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_LEFT && cursorPos > 0) {
            cursorPos--;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT && cursorPos < altName.length()) {
            cursorPos++;
            return true;
        }
        return false;
    }

    private boolean handleEnterKey(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_ENTER && !altName.trim().isEmpty()) {
            accounts.add(new Account(altName));
            AltConfig.updateFile();
            typing = false;
            altName = "";
            return true;
        }
        return false;
    }

    private boolean handleBackspaceKey(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (selectAll) {
                altName = "";
                cursorPos = 0;
                selectAll = false;
            } else if (cursorPos > 0) {
                altName = altName.substring(0, cursorPos - 1) + altName.substring(cursorPos);
                cursorPos--;
            }
            startBackspaceThread();
            return true;
        }
        return false;
    }

    private void startBackspaceThread() {
        backspaceHeld = true;
        if (backspaceThread == null || !backspaceThread.isAlive()) {
            backspaceThread = new Thread(() -> {
                try {
                    Thread.sleep(initialDelay);
                    while (backspaceHeld) {
                        if (cursorPos > 0) {
                            altName = altName.substring(0, cursorPos - 1) + altName.substring(cursorPos);
                            cursorPos--;
                        }
                        Thread.sleep(repeatRate);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            backspaceThread.start();
        }
    }

    private boolean handleClipboardActions(int keyCode, int modifiers) {
        if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
            if (keyCode == GLFW.GLFW_KEY_V) {
                pasteFromClipboard();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_A) {
                selectAll = true;
                return true;
            }
        }
        return false;
    }

    private void pasteFromClipboard() {
        if (!typing) {
            pasteStatusText = "Ошибка! Выберите поле ввода для вставки.";
            pasteStatusStartTime = System.currentTimeMillis();
            return;
        }

        String clipboardText = getClipboardText();
        if (clipboardText != null && !clipboardText.isEmpty()) {
            if (selectAll) {
                altName = clipboardText;
                selectAll = false;
            } else if (altName.length() + clipboardText.length() <= 20) {
                altName = altName.substring(0, cursorPos) + clipboardText + altName.substring(cursorPos);
            }
            cursorPos = altName.length();
            pasteStatusText = "Успешно!";
        } else {
            pasteStatusText = "Ошибка! Нет содержимого текста в буфере обмена.";
        }
        pasteStatusStartTime = System.currentTimeMillis();
    }

    private String getClipboardText() {
        return GLFW.glfwGetClipboardString(GLFW.glfwGetCurrentContext());
    }

    private boolean handleCopyAction(int keyCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_C && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
            if (selectedAccount != null) {
                copyAccountNameToClipboard(selectedAccount.accountName);
                copyStatusText = "Успешно!";
            } else {
                copyStatusText = "Ошибка! Аккаунт не выбран.";
            }
            copyStatusStartTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    private void copyAccountNameToClipboard(String accountName) {
        if (!GraphicsEnvironment.isHeadless()) {
            StringSelection stringSelection = new StringSelection(accountName);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        } else {
            GLFW.glfwSetClipboardString(GLFW.glfwGetCurrentContext(), accountName);
        }
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (typing) {
            if (selectAll) {
                altName = "";
                selectAll = false;
            }

            altName = altName.substring(0, cursorPos) + codePoint + altName.substring(cursorPos);
            cursorPos++;
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vector2d fixed = MathSystem.getMouse((int) mouseX, (int) mouseY);
        mouseX = fixed.x;
        mouseY = fixed.y;

        return handleInputFieldClick(mouseX, mouseY) ||
                handleAddButtonClick(mouseX, mouseY, button) ||
                handleRandomButtonClick(mouseX, mouseY, button) ||
                handleMenuButtonsClick(mouseX, mouseY, button) ||
                handleAccountClick(mouseX, mouseY, button) ||
                super.mouseClicked(mouseX, mouseY, button);
    }

    private float[] calculateBaseCoordinates() {
        float width = 320f, height = 250f;
        float x = ClientReceive.calc(mc.getMainWindow().getScaledWidth()) / 2f - width / 2f;
        float y = ClientReceive.calc(mc.getMainWindow().getScaledHeight()) / 2f - height / 2f;
        return new float[]{x, y};
    }

    private boolean handleInputFieldClick(double mouseX, double mouseY) {
        float formWidth = 175f;
        float[] baseCoords = calculateBaseCoordinates();
        float centerX = baseCoords[0] - formWidth - 10f;
        float centerY = baseCoords[1] - 6;

        Vector2i fixedMouse = ClientReceive.getMouse((int) mouseX, (int) mouseY);

        boolean isInputHovered = MathSystem.isHovered((float) fixedMouse.getX(), (float) fixedMouse.getY(),
                centerX + 2f, centerY + 30f, formWidth - 5, 22f);

        if (isInputHovered) {
            typing = true;
            cursorPos = calculateCursorPosition(fixedMouse.getX(), centerX + 8f, altName);
            return true;
        }

        typing = false;
        return false;
    }

    private int calculateCursorPosition(double mouseX, float textStartX, String text) {
        float relativeX = (float) mouseX - textStartX;
        float textWidthSoFar = 0;
        for (int i = 0; i < text.length(); i++) {
            textWidthSoFar += ClientFonts.clickGui[15].getWidth(String.valueOf(text.charAt(i)));
            if (textWidthSoFar >= relativeX) {
                return i;
            }
        }
        return text.length();
    }

    private boolean handleAddButtonClick(double mouseX, double mouseY, int button) {
        float formWidth = 175f;
        float addTextWidth = ClientFonts.clickGui[15].getWidth("Добавить");
        float addButtonWidth = addTextWidth + 25;

        float[] baseCoords = calculateBaseCoordinates();
        float centerX = baseCoords[0] - formWidth - 10f;
        float centerY = baseCoords[1] - 6;

        float addButtonX = centerX + formWidth - addButtonWidth - 110f;
        float addButtonY = centerY + 58f;

        Vector2i fixedMouse = ClientReceive.getMouse((int) mouseX, (int) mouseY);

        if (MathSystem.isHovered((float) fixedMouse.getX(), (float) fixedMouse.getY(), addButtonX, addButtonY, addButtonWidth, 18f) && button == 0) {
            if (!altName.trim().isEmpty()) {
                accounts.add(new Account(altName));
                AltConfig.updateFile();
                typing = false;
                altName = "";
            }
            return true;
        }
        return false;
    }

    private boolean handleRandomButtonClick(double mouseX, double mouseY, int button) {
        float formWidth = 175f;
        float randomTextWidth = ClientFonts.clickGui[15].getWidth("Рандом");
        float randomButtonWidth = randomTextWidth + 25;

        float[] baseCoords = calculateBaseCoordinates();
        float centerX = baseCoords[0] - formWidth - 10f;
        float centerY = baseCoords[1] - 6;

        float addButtonX = centerX + formWidth - randomButtonWidth - 110f;
        float randomButtonX = addButtonX + randomButtonWidth + 45f;
        float randomButtonY = centerY + 58f;

        Vector2i fixedMouse = ClientReceive.getMouse((int) mouseX, (int) mouseY);

        if (MathSystem.isHovered((float) fixedMouse.getX(), (float) fixedMouse.getY(), randomButtonX, randomButtonY, randomButtonWidth, 18f) && button == 0) {
            addRandomAccount();
            return true;
        }
        return false;
    }

    private boolean handleMenuButtonsClick(double mouseX, double mouseY, int button) {
        float[] baseCoords = calculateBaseCoordinates();
        float baseY = baseCoords[1];

        String[] buttonTexts = {"Очистить все", "Закрыть"};
        Runnable[] actions = {
                this::clearSelectedAccount,
                () -> mc.displayGuiScreen(null)
        };

        float clearTextWidth = ClientFonts.clickGui[15].getWidth(buttonTexts[0]);
        float exitTextWidth = ClientFonts.clickGui[15].getWidth(buttonTexts[1]);

        float clearButtonWidth = clearTextWidth + 25f;
        float exitButtonWidth = exitTextWidth + 35f;
        float buttonHeight = 18f;

        float blockWidth = 160f;
        float scaledWidth = ClientReceive.calc(mc.getMainWindow().getScaledWidth());
        float blockX = (scaledWidth / 2f) + (blockWidth + 22);
        float blockY = baseY - 6f;

        float buttonStartY = blockY + 37f;
        float clearButtonX = blockX + 6f;
        float exitButtonX = clearButtonX + clearButtonWidth + 7f;

        Vector2i fixedMouse = ClientReceive.getMouse((int) mouseX, (int) mouseY);

        if (MathSystem.isHovered((float) fixedMouse.getX(), (float) fixedMouse.getY(), clearButtonX, buttonStartY, clearButtonWidth, buttonHeight) && button == 0) {
            actions[0].run();
            return true;
        }

        if (MathSystem.isHovered((float) fixedMouse.getX(), (float) fixedMouse.getY(), exitButtonX, buttonStartY, exitButtonWidth, buttonHeight) && button == 0) {
            actions[1].run();
            return true;
        }

        return false;
    }

    private boolean handleAccountClick(double mouseX, double mouseY, int button) {
        float[] baseCoords = calculateBaseCoordinates();
        float x = baseCoords[0];
        float y = baseCoords[1];

        float columnWidth = 155f;
        float columnSpacing = 5f;
        float rowHeight = 60f;
        int maxColumns = 2;

        Vector2i fixedMouse = ClientReceive.getMouse((int) mouseX, (int) mouseY);

        for (int i = 0; i < accounts.size(); i++) {
            Account account = accounts.get(i);
            int currentColumn = i % maxColumns;
            int currentRow = i / maxColumns;

            float colX = x + currentColumn * (columnWidth + columnSpacing) - 6f + 10f;
            float rowY = y + currentRow * rowHeight + scroll + 6f;

            if (MathSystem.isHovered((float) fixedMouse.getX(), (float) fixedMouse.getY(), colX, rowY, columnWidth, 45f)) {
                if (button == 0) {
                    selectedAccount = account;
                    mc.session = new Session(account.accountName, "", "", "mojang");
                } else if (button == 1) {
                    accounts.remove(account);
                    AltConfig.updateFile();
                    selectedAccount = null;
                }
                return true;
            }
        }
        return false;
    }

    private void clearSelectedAccount() {
        if (!accounts.isEmpty()) {
            accounts.clear();
            AltConfig.updateFile();
            selectedAccount = null;
        }
    }

    public void addRandomAccount() {
        String randomAccountName = AccountGenerator.generateUsername();
        accounts.add(new Account(randomAccountName));
        AltConfig.updateFile();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        Vector2d fixed = MathSystem.getMouse((int) mouseX, (int) mouseY);
        mouseX = fixed.x;
        mouseY = fixed.y;

        float offset = 6f;
        float width = 320f, height = 250f;
        float x = mc.getMainWindow().getScaledWidth() / 2f - width / 2f;
        float y = mc.getMainWindow().getScaledHeight() / 2f - height / 2f;

        if (MouseManager.isHovered(mouseX, mouseY, x - offset, y - offset, width + offset * 4f, height + offset * 2f)) {
            float rowHeight = 60f;
            int maxColumns = 2;
            float maxScrollableHeight = Math.max(0, (float) Math.ceil((double) accounts.size() / maxColumns) * rowHeight - height);

            if (maxScrollableHeight > 0) {
                float scrollSpeed = 10f;
                scroll += delta * scrollSpeed;
                scroll = MathHelper.clamp(scroll, -maxScrollableHeight, 0);
            }

            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    protected void init() {
//        snowParticles.clear();
//        for (int i = 0; i < 25; i++) {
//            snowParticles.add(new SnowParticle(
//                    mc.getMainWindow().getScaledWidth(),
//                    mc.getMainWindow().getScaledHeight()
//            ));
//        }
        super.init();
    }

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            backspaceHeld = false;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public void tick() {
        super.tick();
    }
}