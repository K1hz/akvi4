package minecraft.game.advantage.words.font;

import lombok.SneakyThrows;
import minecraft.game.advantage.words.font.common.Lang;
import minecraft.game.advantage.words.font.styled.StyledFont;


public class ClientFonts {
    public static final String FONT_DIR = "/assets/minecraft/render/fonts/normal/";

    public static volatile StyledFont[] msBold = new StyledFont[50];
    public static volatile StyledFont[] msMedium = new StyledFont[50];
    public static volatile StyledFont[] msLight = new StyledFont[50];
    public static volatile StyledFont[] msRegular = new StyledFont[50];
    public static volatile StyledFont[] msSemiBold = new StyledFont[50];
    public static volatile StyledFont[] small_pixel = new StyledFont[50];
    public static volatile StyledFont[] tech = new StyledFont[50];
    public static volatile StyledFont[] icon = new StyledFont[50];
    public static volatile StyledFont[] icons_wex = new StyledFont[50];
    public static volatile StyledFont[] settings_gui = new StyledFont[41];
    public static volatile StyledFont[] icons_nur = new StyledFont[50];
    public static volatile StyledFont[] icons_gui = new StyledFont[71];
    public static volatile StyledFont[] comfortaa = new StyledFont[50];
    public static volatile StyledFont[] interBold = new StyledFont[80];
    public static volatile StyledFont[] interMedium = new StyledFont[80];
    public static volatile StyledFont[] interRegular = new StyledFont[80];
    public static volatile StyledFont[] interSemiBold = new StyledFont[80];
    public static volatile StyledFont[] tenacity = new StyledFont[80];
    public static volatile StyledFont[] clickGui = new StyledFont[41];
    public static volatile StyledFont[] icons_hud = new StyledFont[81];
    public static volatile StyledFont[] social_icon = new StyledFont[81];
    public static volatile StyledFont[] category_icon = new StyledFont[81];
    public static volatile StyledFont[] settings = new StyledFont[81];
    public static volatile StyledFont[] tenacityBold = new StyledFont[80];

    @SneakyThrows
    public static void init() {

        // montserrat
        for (int i = 10; i < 71;i++) {
            icons_gui[i] = new StyledFont("icons_gui.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 50;i++) {
            msBold[i] = new StyledFont("Montserrat-Bold.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 50;i++) {
            msLight[i] = new StyledFont("Montserrat-Light.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 50;i++) {
            msMedium[i] = new StyledFont("Montserrat-Medium.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 50;i++) {
            msRegular[i] = new StyledFont("Montserrat-Regular.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 50;i++) {
            msSemiBold[i] = new StyledFont("Montserrat-SemiBold.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 10; i < 81;i++) {
            icons_hud[i] = new StyledFont("icons_hud.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 10; i < 81;i++) {
            category_icon[i] = new StyledFont("category_icon.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 10; i < 81;i++) {
            settings[i] = new StyledFont("settings.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 10; i < 81;i++) {
            social_icon[i] = new StyledFont("social_icon.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        // tenacity
        for (int i = 10; i < 41;i++) {
            clickGui[i] = new StyledFont("clickGui.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 10; i < 41;i++) {
            settings_gui[i] = new StyledFont("settings_gui.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 50;i++) {
            tenacity[i] = new StyledFont("tenacity.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }

        for (int i = 8; i < 50;i++) {
            tenacityBold[i] = new StyledFont("tenacitybold.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }

        // other
        for (int i = 8; i < 50;i++) {
            small_pixel[i] = new StyledFont("small_pixel.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 50;i++) {
            tech[i] = new StyledFont("tech.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }

        // icon
        for (int i = 8; i < 50;i++) {
            icon[i] = new StyledFont("icon.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 50;i++) {
            icons_wex[i] = new StyledFont("iconswex.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 50;i++) {
            icons_nur[i] = new StyledFont("iconsnur.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }

        for (int i = 8; i < 50;i++) {
            comfortaa[i] = new StyledFont("comfortaa-regular.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }

        // inter
        for (int i = 8; i < 80;i++) {
            interRegular[i] = new StyledFont("inter_regular.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 80;i++) {
            interMedium[i] = new StyledFont("inter_medium.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 80;i++) {
            interSemiBold[i] = new StyledFont("inter_semibold.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 80;i++) {
            interBold[i] = new StyledFont("inter_bold.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
    }
}