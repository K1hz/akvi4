package net.minecraft.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;

import minecraft.game.operation.misc.DDBETTER;
import minecraft.game.operation.misc.SelfDestruct;
import minecraft.system.managers.drag.DragManager;
import minecraft.system.managers.drag.Dragging;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.make.Cursors;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.CommandSuggestionHelper;
import net.minecraft.client.gui.NewChatGui;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.atomic.AtomicBoolean;


public class ChatScreen extends Screen {
    private String historyBuffer = "";

    /**
     * keeps position of which chat message you will select when you press up, (does not increase for duplicated
     * messages sent immediately after each other)
     */
    private int sentHistoryCursor = -1;

    /**
     * Chat entry field
     */
    protected TextFieldWidget inputField;

    /**
     * is the text that appears when you press the chat key and the input box appears pre-filled
     */
    private String defaultInputFieldText = "";
    private CommandSuggestionHelper commandSuggestionHelper;

    public ChatScreen(String defaultText) {
        super(NarratorChatListener.EMPTY);
        this.defaultInputFieldText = defaultText;
    }

    public static boolean hide;
    Button button;

    @Override
    protected void init() {
        MainWindow mainWindow = Minecraft.getInstance().getMainWindow();
        if (!SelfDestruct.unhooked) {
            this.addButton(button = new Button(mainWindow.getScaledWidth() - 55 - 2, mainWindow.getScaledHeight() - 35, 55, 20, new StringTextComponent("Скрыть информацию: " + hide), (button) ->
            {
                hide = !hide;
            }));
        }
        this.minecraft.keyboardListener.enableRepeatEvents(true);
        this.sentHistoryCursor = this.minecraft.ingameGUI.getChatGUI().getSentMessages().size();
        this.inputField = new TextFieldWidget(this.font, 4, this.height - 12, this.width - 4, 12, new TranslationTextComponent("chat.editBox")) {
            @Override
            protected IFormattableTextComponent getNarrationMessage() {
                return super.getNarrationMessage().appendString(ChatScreen.this.commandSuggestionHelper.getSuggestionMessage());
            }
        };
        this.inputField.setMaxStringLength(256);
        this.inputField.setEnableBackgroundDrawing(false);
        this.inputField.setText(this.defaultInputFieldText);
        this.inputField.setResponder(this::func_212997_a);
        this.children.add(this.inputField);
        this.commandSuggestionHelper = new CommandSuggestionHelper(this.minecraft, this, this.inputField, this.font, false, false, 1, 10, true, -805306368);
        this.commandSuggestionHelper.init();
        this.setFocusedDefault(this.inputField);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        String s = this.inputField.getText();
        this.init(minecraft, width, height);
        this.setChatLine(s);
        this.commandSuggestionHelper.init();
    }

    @Override
    public void onClose() {
        this.minecraft.keyboardListener.enableRepeatEvents(false);
        this.minecraft.ingameGUI.getChatGUI().resetScroll();
        for (Dragging dragging : DragManager.draggables.values()) {
            if (dragging.getModule().isEnabled()) {
                dragging.onRelease(0);
            }
        }
        GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
    }

    @Override
    public void tick() {
        this.inputField.tick();
    }

    private void func_212997_a(String p_212997_1_) {
        String s = this.inputField.getText();
        this.commandSuggestionHelper.shouldAutoSuggest(!s.equals(this.defaultInputFieldText));
        this.commandSuggestionHelper.init();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.commandSuggestionHelper.onKeyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (keyCode == 256) {
            this.minecraft.displayGuiScreen((Screen) null);
            return true;
        } else if (keyCode != 257 && keyCode != 335) {
            if (keyCode == 265) {
                this.getSentHistory(-1);
                return true;
            } else if (keyCode == 264) {
                this.getSentHistory(1);
                return true;
            } else if (keyCode == 266) {
                this.minecraft.ingameGUI.getChatGUI().addScrollPos((double) (this.minecraft.ingameGUI.getChatGUI().getLineCount() - 1));
                return true;
            } else if (keyCode == 267) {
                this.minecraft.ingameGUI.getChatGUI().addScrollPos((double) (-this.minecraft.ingameGUI.getChatGUI().getLineCount() + 1));
                return true;
            } else {
                return false;
            }
        } else {
            String s = this.inputField.getText().trim();

            if (!s.isEmpty()) {
                this.sendMessage(s);
            }

            this.minecraft.displayGuiScreen((Screen) null);
            return true;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta > 1.0D) {
            delta = 1.0D;
        }

        if (delta < -1.0D) {
            delta = -1.0D;
        }

        if (this.commandSuggestionHelper.onScroll(delta)) {
            return true;
        } else {
            if (!hasShiftDown()) {
                delta *= 7.0D;
            }

            this.minecraft.ingameGUI.getChatGUI().addScrollPos(delta);
            return true;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.commandSuggestionHelper.onClick((double) ((int) mouseX), (double) ((int) mouseY), button)) {
            return true;
        } else {
            if (button == 0) {
                NewChatGui newchatgui = this.minecraft.ingameGUI.getChatGUI();

                if (newchatgui.func_238491_a_(mouseX, mouseY)) {
                    return true;
                }

                Style style = newchatgui.func_238494_b_(mouseX, mouseY);

                if (style != null && this.handleComponentClicked(style)) {
                    return true;
                }
            }
            for (Dragging dragging : DragManager.draggables.values()) {
                if (dragging.getModule().isEnabled()) {

                    if (dragging.onClick(mouseX, mouseY, button)) {
                        break;
                    }
                }
            }
            return this.inputField.mouseClicked(mouseX, mouseY, button) ? true : super.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (Dragging dragging : DragManager.draggables.values()) {
            if (dragging.getModule().isEnabled()) {
                dragging.onRelease(button);
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected void insertText(String text, boolean overwrite) {
        if (overwrite) {
            this.inputField.setText(text);
        } else {
            this.inputField.writeText(text);
        }
    }

    /**
     * input is relative and is applied directly to the sentHistoryCursor so -1 is the previous message, 1 is the next
     * message from the current cursor position
     */
    public void getSentHistory(int msgPos) {
        int i = this.sentHistoryCursor + msgPos;
        int j = this.minecraft.ingameGUI.getChatGUI().getSentMessages().size();
        i = MathHelper.clamp(i, 0, j);

        if (i != this.sentHistoryCursor) {
            if (i == j) {
                this.sentHistoryCursor = j;
                this.inputField.setText(this.historyBuffer);
            } else {
                if (this.sentHistoryCursor == j) {
                    this.historyBuffer = this.inputField.getText();
                }

                this.inputField.setText(this.minecraft.ingameGUI.getChatGUI().getSentMessages().get(i));
                this.commandSuggestionHelper.shouldAutoSuggest(false);
                this.sentHistoryCursor = i;
            }
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (button != null) {
            button.setMessage(new StringTextComponent("Hide: " + (hide ? "ON" : "OFF")));
        }

        this.setListener(this.inputField);
        this.inputField.setFocused2(true);

        int textWidth = this.font.getStringWidth(this.inputField.getText());
        DDBETTER ddbetter = new DDBETTER();
        fill(matrixStack, 2, this.height - 14, this.width - 2, this.height - 2, this.minecraft.gameSettings.getChatBackgroundColor(Integer.MIN_VALUE));



        this.inputField.render(matrixStack, mouseX, mouseY, partialTicks);
        this.commandSuggestionHelper.drawSuggestionList(matrixStack, mouseX, mouseY);

        Style style = this.minecraft.ingameGUI.getChatGUI().func_238494_b_((double) mouseX, (double) mouseY);
        if (style != null && style.getHoverEvent() != null) {
            this.renderComponentHoverEffect(matrixStack, style, mouseX, mouseY);
        }

        AtomicBoolean anyHovered = new AtomicBoolean(false);
        DragManager.draggables.values().forEach(dragging -> {
            if (dragging.getModule().isEnabled()) {
                if (MathSystem.isHovered(mouseX, mouseY, dragging.getX(), dragging.getY(), dragging.getWidth(), dragging.getHeight())) {
                    anyHovered.set(true);
                }
                dragging.onDraw(mouseX, mouseY, Minecraft.getInstance().getMainWindow());
            }
        });

        if (anyHovered.get()) {
            GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.HAND);
        } else {
            GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
        }

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }


    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void setChatLine(String p_208604_1_) {
        this.inputField.setText(p_208604_1_);
    }
}
