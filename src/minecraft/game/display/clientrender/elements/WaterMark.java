package minecraft.game.display.clientrender.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import minecraft.game.advantage.advisee.AnimationNumbers;
import minecraft.game.advantage.advisee.ClientReceive;
import minecraft.game.advantage.advisee.PlayerPing;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.words.MoreColorsSystem;
import minecraft.game.advantage.words.font.ClientFonts;
import minecraft.game.display.clientrender.timeupdate.ElementRenderer;
import minecraft.game.enjoin.feature.PassCommand;
import minecraft.game.operation.visual.Hud;
import minecraft.game.transactions.EventRender2D;
import minecraft.system.AG;
import net.minecraft.util.text.ITextComponent;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class WaterMark implements minecraft.game.display.clientrender.timeupdate.ElementRenderer {
    public AnimationNumbers am = new AnimationNumbers();



    @Override
    public void render(EventRender2D eventRender2D) {
        MatrixStack matrixStack = eventRender2D.getMatrixStack();

        boolean showLogin = AG.getInst().getModuleManager().getHud().elementsWatermark.is("Login").getValue();
        boolean showFps = AG.getInst().getModuleManager().getHud().elementsWatermark.is("Fps").getValue();
        boolean showPing = AG.getInst().getModuleManager().getHud().elementsWatermark.is("Ping").getValue();
        boolean showTime = AG.getInst().getModuleManager().getHud().elementsWatermark.is("Time").getValue();
        boolean showServer = AG.getInst().getModuleManager().getHud().elementsWatermark.is("Server").getValue();

        String clientName = "Akvi4 Client";
        String roleSuffix = "Free";
        ITextComponent nameText = MoreColorsSystem.gradient(clientName, Hud.getColor(1, 25), ColoringSystem.rgb(25,25,25));

        float posX = 4;
        float posY = 4;
        float height = ClientFonts.clickGui[16].getFontHeight() + 5;

        float totalWidth = calculateTotalWidth(nameText.getString(), showLogin, showFps, showPing, showTime, showServer)  - 19;

        GraphicsSystem.drawRoundedRect(posX, posY, totalWidth, height, 3, ColoringSystem.rgb(20,20,20));

        float currentX = renderClientName(matrixStack, nameText, posX - 15, posY);
        if (showLogin) currentX = renderSeparatorAndElement(matrixStack, currentX, posY, "B", PassCommand.currentUser, 3, 0);
        if (showFps) currentX = renderSeparatorAndElement(matrixStack, currentX, posY, "C", am.FPSAnim() + " fps", 3, 0);
        if (showPing) currentX = renderSeparatorAndElement(matrixStack, currentX, posY, "L", PlayerPing.calculatePing() + " ms", 4, 1.5f);
        if (showTime) currentX = renderSeparatorAndElement(matrixStack, currentX, posY, "F", getCurrentTime(), 4, 2);
        if (showServer) renderSeparatorAndElement(matrixStack, currentX, posY, "E", ClientReceive.getServerIP(), 4, 2);
    }

    /**
     * Рассчитывает общую ширину прямоугольника, учитывая включенные элементы.
     */
    private float calculateTotalWidth(String clientName, boolean showLogin, boolean showFps, boolean showPing, boolean showTime, boolean showServer) {
        float baseWidth = ClientFonts.icons_hud[20].getWidth("A") + ClientFonts.clickGui[16].getWidth(clientName) + 12;
        float loginWidth = showLogin ? ClientFonts.icons_hud[20].getWidth("B") + ClientFonts.clickGui[16].getWidth(PassCommand.currentUser) + 8 : 0;
        float fpsWidth = showFps ? ClientFonts.icons_hud[20].getWidth("C") + ClientFonts.clickGui[16].getWidth(am.FPSAnim() + " fps") + 8 : 0;
        float pingWidth = showPing ? ClientFonts.icons_hud[20].getWidth("H") + ClientFonts.clickGui[16].getWidth(PlayerPing.calculatePing() + " ms") + 8 : 0;
        float timeWidth = showTime ? ClientFonts.icons_hud[20].getWidth("F") + ClientFonts.clickGui[16].getWidth(getCurrentTime()) + 10 : 0;
        float serverWidth = showServer ? ClientFonts.icons_hud[20].getWidth("E") + ClientFonts.clickGui[16].getWidth(ClientReceive.getServerIP()) + 10 : 0;

        return baseWidth + loginWidth + fpsWidth + pingWidth + timeWidth + serverWidth;
    }

    /**
     * Рендерит имя клиента и возвращает текущую X-координату.
     */
    private float renderClientName(MatrixStack matrixStack, ITextComponent nameText, float posX, float posY) {
//AVATARKA        ClientFonts.icons_hud[20].drawString(matrixStack, "A", posX + 5, posY + 5, getClientColor());
        ClientFonts.clickGui[16].drawString(matrixStack, nameText, posX + 8 + ClientFonts.icons_hud[20].getWidth("A"), posY + 5, -1);

        return posX + 10 + ClientFonts.icons_hud[20].getWidth("A") + ClientFonts.clickGui[16].getWidth(nameText.getString());
    }

    /**
     * Рендерит разделитель и элемент (иконку и текст) с указанным отступом и возвращает текущую X-координату.
     */
    private float renderSeparatorAndElement(MatrixStack matrixStack, float currentX, float posY, String icon, String text, float spacing, float iconSpacing) {
        float height = ClientFonts.clickGui[16].getFontHeight() + 5;

        if (currentX > 4) {
            GraphicsSystem.drawRoundedRect(currentX, posY + 2.5f, 1.0f, height - 4, 1, ColoringSystem.rgb(30, 30, 30));
            currentX += spacing;
        }

        ClientFonts.icons_hud[20].drawString(matrixStack, icon, currentX, posY + 5, -1);
        currentX += ClientFonts.icons_hud[20].getWidth(icon) + iconSpacing;

        ClientFonts.clickGui[16].drawString(matrixStack, text, currentX, posY + 5, -1);
        return currentX + ClientFonts.clickGui[16].getWidth(text) + 4;
    }

    /**
     * Получает текущее время в формате HH:mm.
     */
    private String getCurrentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
}
