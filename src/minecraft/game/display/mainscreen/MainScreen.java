package minecraft.game.display.mainscreen;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.AllArgsConstructor;
import lombok.Getter;
import minecraft.game.advantage.advisee.ClientReceive;
import minecraft.game.advantage.advisee.IMinecraft;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.figures.Vec2i;
import minecraft.game.advantage.figures.Vector2i;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.words.MoreColorsSystem;
import minecraft.game.advantage.words.font.ClientFonts;
import minecraft.game.display.clientrender.timeupdate.ElementRenderer;
import minecraft.game.enjoin.feature.PassCommand;
import minecraft.game.operation.visual.Hud;
import minecraft.system.AG;
import minecraft.system.ClientInfo;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.ITextComponent;

import javax.lang.model.util.Elements;
import java.awt.*;
import java.net.URI;
import java.util.ArrayList;
import java.awt.Desktop;
import java.util.List;

public class MainScreen extends Screen implements IMinecraft {

    public MainScreen() {
        super(ITextComponent.getTextComponentOrEmpty(""));
    }
    private String pastebinText = "";
    private final List<Button> buttons = new ArrayList<>();
    private final List<SnowParticle> snowParticles = new ArrayList<>();

    @Override
    protected void init() {
//        snowParticles.clear();
//        for (int i = 0; i < 25; i++) {
//            snowParticles.add(new SnowParticle(
//                    mc.getMainWindow().getScaledWidth(),
//                    mc.getMainWindow().getScaledHeight()
//            ));
//        }
        new Thread(() -> {
            pastebinText = Fetcher.fetchText("https://pastebin.com/raw/xUb2vJpF");
        }).start();
        float widthButton = 400 / 2f;
        float heightButton = 25;
        float x = ClientReceive.calc(width) / 2f - widthButton / 2;
        float y = Math.round(ClientReceive.calc(height) / 2f - (heightButton) / 2);

        buttons.clear();

        buttons.add(new Button(x, y, widthButton, heightButton, "Singleplayer", () -> {
            mc.displayGuiScreen(new WorldSelectionScreen(this));
        }));

        y += 55 / 2f;
        buttons.add(new Button(x, y, widthButton, heightButton, "Multiplayer", () -> {
            mc.displayGuiScreen(new MultiplayerScreen(this));
        }));

        y += 55 / 2f;
        buttons.add(new Button(x, y, widthButton, heightButton, "AltManager", () -> {
            mc.displayGuiScreen(AG.getInst().getAltScreen());
        }));


        y += 55  / 2f;
        buttons.add(new Button(x + 101, y , widthButton - 101, heightButton, "Discord", () -> {
            Util.getOSType().openURI(java.net.URI.create("https://discord.gg/aegisclient"));
        }));



        buttons.add(new Button(x, y, widthButton - 101, heightButton, "Telegram", () -> {
            Util.getOSType().openURI(java.net.URI.create("https://t.me/aegisclient"));
        }));

        y += 55 / 2f;
        buttons.add(new Button(x, y, widthButton - 101, heightButton, "Options", () -> {
            mc.displayGuiScreen(new OptionsScreen(this, mc.gameSettings));
        }));

        buttons.add(new Button(x + 101, y, widthButton - 101, heightButton, "Exit game", mc::shutdownMinecraftApplet));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        mc.gameRenderer.setupOverlayRendering(2);

        ITextComponent nameClient = MoreColorsSystem.gradient(
                "Akvi4" + " Client 1.16.5",
                Hud.getColor(0),
                Hud.getColor(90)
        );

        String pcUsername = System.getProperty("user.name");
        ITextComponent username = MoreColorsSystem.gradient(
                pcUsername,
                Hud.getColor(0),
                Hud.getColor(90)
        );


        ITextComponent versionClient = MoreColorsSystem.gradient(
                ClientInfo.version,
                Hud.getColor(0),
                Hud.getColor(90)
        );

        String welcome = "Добро пожаловать, ";
        String version = "Текущая версия клиента: ";
        float windowWidth = ClientReceive.calc(mc.getMainWindow().getScaledWidth());
        float windowHeight = ClientReceive.calc(mc.getMainWindow().getScaledHeight());

        GraphicsSystem.drawRectNEW(0,0,windowWidth,windowHeight,ColoringSystem.rgb(24,24,24));
//        GraphicsSystem.drawImage(
//                new ResourceLocation("render/images/logo_orig.png"),
//                0, 0, 70, 70, -1
//        );

        for (SnowParticle snowParticle : snowParticles) {
            snowParticle.update(windowWidth, windowHeight);
            int color = ColoringSystem.setAlpha(ColoringSystem.rgb(255, 255, 255), (int) (snowParticle.getAlpha() * 255));
            GraphicsSystem.drawCircle(
                    snowParticle.getX(),
                    snowParticle.getY(),
                    snowParticle.getRadius(),
                    color
            );
        }

        float totalWidth = ClientFonts.icons_hud[40].getWidth("A") + 4 + ClientFonts.clickGui[32].getWidth(nameClient.getString());
        float textX = (windowWidth - totalWidth) / 2f;
        float textY = windowHeight / 2f - 35;

        float welcomeX = 4;
        float welcomeY = windowHeight - ClientFonts.clickGui[18].getFontHeight();
        float usernameX = welcomeX + ClientFonts.clickGui[18].getWidth(welcome);

        float versionX = windowWidth - ClientFonts.clickGui[18].getWidth(version + versionClient.getString()) - 2;
        float versionY = windowHeight - ClientFonts.clickGui[18].getFontHeight();

        ClientFonts.icons_hud[40].drawString(matrixStack, "A", textX, textY, AG.getInst().getStyleManager().getCurrentStyle().getSecondColor().getRGB());
        ClientFonts.clickGui[32].drawString(matrixStack, nameClient, textX + ClientFonts.icons_hud[40].getWidth("A") - 8.5f, textY, -1);

        // Рендер "Welcome, [Username]"
        ClientFonts.clickGui[18].drawString(matrixStack, welcome, welcomeX, welcomeY, -1);
        ClientFonts.clickGui[18].drawString(matrixStack, username, usernameX, welcomeY, -1);

        // Рендер "Version : [Version]"
        ClientFonts.clickGui[18].drawString(matrixStack, version, versionX, versionY, -1);
        ClientFonts.clickGui[18].drawString(matrixStack, versionClient, versionX + ClientFonts.clickGui[18].getWidth(version), versionY, -1);

        Vector2i fixed = ClientReceive.getMouse(mouseX, mouseY);

        drawButtons(matrixStack, fixed.getX(), fixed.getY(), partialTicks);
        ITextComponent changeLog = MoreColorsSystem.gradient(
                "Change Log",
                Hud.getColor(0),
                Hud.getColor(90)
        );
       ClientFonts.interBold[24].drawString(matrixStack, changeLog, 4, 4, ColoringSystem.rgb(255, 255, 255));


        float screenWidth = mc.getMainWindow().getWidth();
        float screenHeight = mc.getMainWindow().getHeight();
        String[] lines = pastebinText.split("\n");

        int startX = 5;
        int startY = 20;
        int lineHeight = (int) (ClientFonts.msMedium[14].getFontHeight() + 2);

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int defaultColor = ColoringSystem.rgb(255, 255, 255);
            int specialColor = defaultColor;

            if (line.startsWith("[+]")) {
                specialColor = ColoringSystem.rgb(89, 237, 66);
            } else if (line.startsWith("[/]")) {
                specialColor = ColoringSystem.rgb(255, 255, 0);
            }

            if (line.startsWith("[") && line.length() > 3) {

                float bracketWidth = ClientFonts.msMedium[16].getWidth("[");
                float symbolWidth = ClientFonts.msMedium[16].getWidth(line.substring(1, 2));
                float bracketSpacing = 2;


                ClientFonts.msMedium[16].drawString(matrixStack, "[", startX, startY + (i * lineHeight), defaultColor);
                ClientFonts.msMedium[17].drawString(matrixStack, line.substring(1, 2), startX + bracketWidth + bracketSpacing, startY + (i * lineHeight), specialColor);
                ClientFonts.msMedium[16].drawString(matrixStack, "]", startX + bracketWidth + bracketSpacing + symbolWidth + bracketSpacing, startY + (i * lineHeight), defaultColor);

                ClientFonts.msMedium[16].drawString(matrixStack, line.substring(3), startX + bracketWidth + bracketSpacing + symbolWidth + bracketSpacing + bracketWidth, startY + (i * lineHeight), defaultColor);
            } else {
                ClientFonts.msMedium[16].drawString(matrixStack, line, startX, startY + (i * lineHeight), defaultColor);
            }
        }
        renderSocial(matrixStack);
        mc.gameRenderer.setupOverlayRendering();
    }

    private void renderSocial(MatrixStack matrixStack) {

        float socX = (float) (scaled().x - 10);
        float socY = (10);
        float x = ClientReceive.calc(width) / 2f / 2;
        float y = Math.round(ClientReceive.calc(height) / 2f / 2);
        float telegramWidth = ClientFonts.social_icon[20].getWidth("B");
        float youtubeWidth = ClientFonts.social_icon[20].getWidth("C");

        float margin = 6;

        float socialWidth = telegramWidth + margin + youtubeWidth + margin + margin;

    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vector2i fixed = ClientReceive.getMouse((int) mouseX, (int) mouseY);
        buttons.forEach(b -> b.click(fixed.getX(), fixed.getY(), button));
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void drawButtons(MatrixStack stack, int mX, int mY, float pt) {
        buttons.forEach(b -> b.render(stack, mX, mY, pt));
    }

    private void openLink(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                System.out.println("Открытие ссылок не поддерживается на этой системе.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AllArgsConstructor
    private class Button {
        @Getter
        private final float x, y, width, height;
        private String text;
        private Runnable action;

        public void render(MatrixStack stack, int mouseX, int mouseY, float pt) {
            GraphicsSystem.drawRoundedRect(x, y + 3, width, height, 6,  ColoringSystem.rgba(45, 45, 45, 100));

            int color = ColoringSystem.rgb(255, 255, 255);
            if (MathSystem.isHovered(mouseX, mouseY, x, y, width, height)) {
                if ("Exit game".equalsIgnoreCase(text)) {
                    color = ColoringSystem.rgb(255, 0, 0);
                } else {
                    color = Hud.getColor(1);
                }
            }
            ClientFonts.clickGui[24].drawCenteredString(stack, text, x + width / 2f, y + height / 2f - 4 + 2, color);
        }

        public void click(int mouseX, int mouseY, int button) {
            if (MathSystem.isHovered(mouseX, mouseY, x, y, width, height)) {
                action.run();
            }
        }
    }
}
