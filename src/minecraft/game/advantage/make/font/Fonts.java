package minecraft.game.advantage.make.font;

public class Fonts {

    public static Font montserrat, consolas, icons, damage, sfui, sfbold, sfMedium, wasafont, icon, nurik, icons2, ico;

    public static void register() {
        montserrat = new Font("Montserrat-Regular.ttf.png", "Montserrat-Regular.ttf.json");
        icons = new Font("icons.ttf.png", "icons.ttf.json");
        consolas = new Font("consolas.ttf.png", "consolas.ttf.json");
        damage = new Font("damage.ttf.png", "damage.ttf.json");
        sfui = new Font("sf_semibold.ttf.png", "sf_semibold.ttf.json");
        sfbold = new Font("sf_bold.ttf.png", "sf_bold.ttf.json");
        sfMedium = new Font("sf_medium.ttf.png", "sf_medium.ttf.json");
        wasafont = new Font("wasafont.ttf.png", "wasafont.ttf.json");
        icon = new Font("icon.ttf.png", "icon.ttf.json");
        nurik = new Font("nurik.png", "nurik.json");
        icons2 = new Font("icons2.png", "icons2.json");
        ico = new Font("icos.png", "icos.json");

    }

}
