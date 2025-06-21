package minecraft.system;

import com.google.common.eventbus.EventBus;

import com.google.common.eventbus.Subscribe;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.rpc.DiscordManager;
import minecraft.game.display.clientpanel2.ClickGuiScreen;
import minecraft.game.operation.visual.Hud;
import minecraft.game.operation.wamost.massa.elements.BindSetting;
import minecraft.game.transactions.EventUpdate;
import minecraft.system.capes.CapesBase;
import minecraft.game.advantage.figures.TimeCounterSetting;
import minecraft.game.display.clickgui.DropDown;
import minecraft.game.operation.misc.FakePlayer;
import minecraft.game.operation.misc.SelfDestruct;
import minecraft.system.via.VarMCP;
import net.minecraft.client.gui.screen.Screen;
import minecraft.game.display.musicplayer.MusicPlayerUI;
import minecraft.game.enjoin.api.*;
import minecraft.game.enjoin.feature.*;
import minecraft.game.enjoin.interfaces.*;
import minecraft.game.operation.visual.ClickGui;
import minecraft.system.managers.friend.FriendManager;
import minecraft.system.managers.StaffManager;
import minecraft.system.managers.config.ConfigManager;
import minecraft.system.managers.macro.MacroManager;
import minecraft.game.transactions.EventKey;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.ModuleManager;
import minecraft.system.managers.config.AltConfig;
import minecraft.game.display.mainscreen.AltScreen;
import minecraft.game.display.clientnotify.most.NotifyManager;
import minecraft.system.managers.Theme;
import minecraft.game.advantage.advisee.Time;
import minecraft.game.advantage.luvvy.TPSCalc;
import minecraft.game.advantage.advisee.ServerTPS;
import minecraft.system.managers.drag.DragManager;
import minecraft.system.managers.drag.Dragging;
import minecraft.game.advantage.luvvy.rotation.FreeLookHandler;
import minecraft.game.advantage.luvvy.rotation.RotationHandler;
import minecraft.game.advantage.words.font.ClientFonts;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;
import minecraft.system.styles.Style;
import minecraft.system.styles.StyleFactory;
import minecraft.system.styles.StyleFactoryImpl;
import minecraft.system.styles.StyleManager;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AG {

    public boolean playerOnServer = false;

    public static final String version = "1.16.5";

    @Getter
    private static AG inst;
    private final Time setBackTimer = new Time();

    public long getSetBackTime() {
        return setBackTimer.getPassedTimeMs();
    }
    private static final TimeCounterSetting TimeCounterSetting = null;
    private ModuleManager moduleManager;
    private ConfigManager configManager;
    private CommandDispatcher commandDispatcher;
    private ServerTPS serverTPS;
    private MacroManager macroManager;
    private FriendManager friendManager;

    private final EventBus eventBus = new EventBus();

    private final File clientDir = new File(Minecraft.getInstance().gameDir + "\\System\\Folder");
    private final File musicdir = new File(Minecraft.getInstance().gameDir + "\\System\\Folder\\Music");
    private final File filesDir = new File(Minecraft.getInstance().gameDir + "\\System\\Folder\\Files");

    private AltConfig altConfig;
    private DropDown dropDown;
    private minecraft.game.display.clientpanel.DropDown dropDownNEW;
    private NotifyManager notifyManager;
    private ClickGuiScreen clickGuiDropDown;
    private Screen musicPlayerUI;
    private VarMCP VarMCP;
    private TPSCalc tpsCalc;
    private minecraft.game.display.clientgui.ClickGui clickGui;
    private StyleManager styleManager;
    private DiscordManager discordManager;
    private CapesBase capesBase;

    private AltScreen altScreen;

    private Theme theme;

    public AG() {


        inst = this;

        if (!clientDir.exists()) {
            clientDir.mkdirs();
        }
        if (!filesDir.exists()) {
            filesDir.mkdirs();
        }
        if (!musicdir.exists()) {
            musicdir.mkdirs();
        }

        clientLoad();

        capesBase = new CapesBase();
        capesBase.init();
    }

    public Dragging createDrag(Module module, String name, float x, float y) {
        DragManager.draggables.put(name, new Dragging(module, name, x, y));
        return DragManager.draggables.get(name);
    }

    @SneakyThrows
    public void clientLoad() {
        ClientFonts.init();
        initStyles();
        discordManager = new DiscordManager();
        VarMCP = new VarMCP();
        serverTPS = new ServerTPS();
        configManager = new ConfigManager();
        altConfig = new AltConfig();

        moduleManager = new ModuleManager();
        moduleManager.init();
        discordManager.init();
        notifyManager = new NotifyManager();
        notifyManager.init();

        macroManager = new MacroManager();

        altScreen = new AltScreen();

        tpsCalc = new TPSCalc();
        initCommands();

        friendManager = new FriendManager();

        try {
            this.friendManager.init();
        } catch (IOException var5) {
        }
        try {
            altConfig.init();
        } catch (Exception e) {
        }
        try {
            configManager.init();
        } catch (IOException e) {
        }
        try {
            macroManager.init();
        } catch (IOException e) {
        }
        DragManager.load();
        friendManager.init();
        StaffManager.load();
        clickGui = new minecraft.game.display.clientgui.ClickGui();
        musicPlayerUI = new MusicPlayerUI(new StringTextComponent(""));
        dropDown = new DropDown(new StringTextComponent(""));
        clickGuiDropDown = new ClickGuiScreen(new StringTextComponent(""));
        dropDownNEW = new minecraft.game.display.clientpanel.DropDown(new StringTextComponent(""));
        theme = new Theme();
        new FreeLookHandler();
        new RotationHandler();
        eventBus.register(this);
    }

    public void clientShotdown() {
        if (SelfDestruct.unhooked) {
            DragManager.save();
            configManager.saveConfiguration("backup");
            AltConfig.updateFile();
            runFileOnClose();
            FakePlayer fakePlayer = AG.getInst().getModuleManager().getFakePlayer();
            if (fakePlayer.isEnabled()) {
                AG.getInst().getModuleManager().getFakePlayer().toggle();
                AG.getInst().getModuleManager().getFakePlayer().removeFakePlayer();
            }
        }
        if (!SelfDestruct.unhooked) {
            DragManager.save();
            configManager.saveConfiguration("backup");
            AltConfig.updateFile();
            FakePlayer fakePlayer = AG.getInst().getModuleManager().getFakePlayer();
            if (fakePlayer.isEnabled()) {
                AG.getInst().getModuleManager().getFakePlayer().toggle();
                AG.getInst().getModuleManager().getFakePlayer().removeFakePlayer();
            }
        }
    }

    public void runFileOnClose() {
        String filePath = System.getProperty("user.home") + "\\AppData\\Roaming\\.tlauncher\\legacy\\Minecraft\\TL.exe";
        File file = new File(filePath);

        if (file.exists()) {
            try {
                Runtime.getRuntime().exec(file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Файла нету ыыыы: " + filePath);
        }
    }

    private final EventKey eventKey = new EventKey(-1);

    public void onKeyPressed(int key) {
        if (moduleManager.getSelfDestruct().unhooked) return;
        eventKey.setKey(key);
        eventBus.post(eventKey);

        macroManager.onKeyPressed(key);

        if (ClickGui.mode.is("Панельная")) {
            if (key == ClickGui.bind.getValue()) {
                Minecraft.getInstance().displayGuiScreen(dropDownNEW);
            }
        }
        if (ClickGui.mode.is("Плиточная")) {
            if (key == ClickGui.bind.getValue()) {
                Minecraft.getInstance().displayGuiScreen(clickGuiDropDown);
            }
        }

        else  {
            if (key == ClickGui.bind.getValue()) {
                Minecraft.getInstance().displayGuiScreen(dropDownNEW);
            }
        }
            if (this.moduleManager.getMusicPlayerUI().isEnabled() && this.moduleManager.getMusicPlayerUI().setting.getValue() == key) {
            Minecraft.getInstance().displayGuiScreen(musicPlayerUI);
        }
    }
    public String randomNickname() {
        String[] names = new String[]{"AG", "Femboy", "RussianPirat", "Ladoga", "ny7oBKa", "IIIuPuHKa", "DataBase", "KoTuK", "nayk", "nykaH", "nykaJLKa", "IIIa7oMeP", "Ohtani", "Tango", "HardStyle", "GoToSleep", "Movietone", "7aIIIuK", "TpyCuKu", "TheKnight", "OnlySprint", "Press_W", "HowToWASD", "BloodFlow", "CutVeins", "Im_A_Cyber", "NextTime", "Killer", "Murauder", "AntiPetuh", "CMeTaHKa", "Enigma", "Doctor", "TheGhost", "GhostRunner", "Banana", "Ba3eJLuH", "MaCTyp6eK", "BaHTy3", "AliExpress", "Agressor", "Spasm", "SHAMAN", "optimist", "", "Banker", "JahMachine", "Cu7aPa", "nuBo", "CuM6uoT", "Venom", "Superman", "Supreme", "CeKcU_6ou", "SuperSpeed", "KnuckKnuck", "6o7aTbIPb", "SouthPark", "Simpson", "IIIaJLaIII", "3_Penetrate", "EmptySoul", "Firefly", "PlusTopka", "TryMe", "YouAreWeak", "MegaSexual", "Pikachu", "Pupsik", "Legenda", "SCP", "MyNumber", "YourToy", "SexShop", "Slayer", "Murderer", "CallMe", "PvpTeacher", "CrazyBoy", "4ynuK", "6aToH", "LongPenis", "Caxap", "Infernal", "Rerilo", "Remula", "Rarlin", "Devo4ka", "SexySister", "NakedBody", "PlusZ4", "ThiefInLaw", "StrongTea", "BlackTea", "SmallAss", "SmallBoobs", "CoffeeDEV", "FireRider", "MilkyWay", "PeacefulBoy", "Lambada", "MagicSpeed", "ThrowMom", "StopPlay", "KillMother", "XDeadForGay", "ALTf4", "HowAreYou", "GoSex", "Falas", "Sediment", "OpenDoor", "ShitInTrap", "SuckItUp", "NeuroNET", "BunnyHop", "BmxSport", "GiveCoord", "eHoTuK", "KucKa", "3auKa", "4aIIIa", "HykaHax", "Sweet", "MoHuTop", "Me7aMa4o", "Miner", "BonAqua", "COK", "BANK", "Lucky", "SPECTATE", "7OBHO", "MyXA", "Owner", "5opka", "JUK", "FaceBreak", "SnapBody", "Psycho", "EasyWin", "SoHard", "Panties", "SoloGame", "Robot", "Surgeon", "_IMBA", "ShakeMe", "EnterMe", "GoAway", "TRUE", "while", "Pinky", "Pickup", "Stack", "GL11", "GLSL", "Garbage", "NoBanMe", "WiFi", "Tally", "Dream", "Mommy", "6aTya", "Pivovar", "Alkash", "Gangsta", "Counter", "Clitor", "HentaUser", "BrowseHent", "LoadChunk", "Panical", "Kakashka", "MinusVse", "Pavlik", "RusskiPirat", "GoodTrip", "6A6KA", "3000IQ", "0IQ", "REMILO", "YOUR_BOSS", "CPacketGay", "4Bytes", "SinCos", "Yogurt", "SexInTrash", "TrashMyHome", "PenisUser", "Evulya", "Evochka", "Virginia", "NoReportMe", "Bluetouth", "PivoBak", "6AKJLAXA", "Opostrof", "Harming", "Cauldron", "Dripka", "Wurdulak", "Presedent", "Opstol", "Oposum", "Babayka", "O4KAPUK", "Dunozavr", "Cocacola", "Fantazy", "70JLA9I", "PedalKTLeave", "TolstoLobik", "nePDyH", "HABO3HUK", "KOT", "CKOT", "BISHOP", "4ukeH", "nanaxa", "Berkyt", "Matreshka", "HACBAU", "XAPEK", "Mopedik", "CKELET2013", "GodDrakona", "CoLHbiIIIKo", "HA77ETC", "PoM6uK", "PomaH", "6oM6UJLa", "MOH7OJl", "OutOfMemory", "PopkaDurak", "4nokVPupok", "Pinality", "Shaverma", "MOJLUCb", "MOJLuTBA", "CTEHA", "CKAJLA", "JohnWeak", "Plomba", "neKaPb", "Disconnect", "Oriflame", "Mojang", "TPPPPP", "EvilBoy", "DavaiEbaca", "TuMeuT", "Tapan", "600K7Puzo", "Poctelecom", "Interzet", "C_6oDUHA", "6yHTaPb", "Milka", "KOLBASA", "OhNo", "YesTea", "Mistik", "KuHDep", "Smippy", "Diamond", "KedpOBuK", "Lolikill", "CrazyGirl", "Kronos", "Kruasan", "MrProper", "HellCat", "Nameless", "Viper", "GamerYT", "slimer", "MrSlender", "Gromila", "BoomBoom", "Doshik", "BananaKenT", "NeonPlay", "Progibator", "Rubak", "MrMurzik", "Kenda", "DrShine", "cnacu6o", "Eclips", "ShadowFuse", "MrRem", "Bacardi", "UwU_FUF", "Exont", "Persik", "Mambl", "Rossamaha", "DrKraken", "MeWormy", "WaterBomb", "YourStarix", "nakeTuk", "Massik", "MineFOX", "BitCoin", "Avocado", "Chappi", "ECEQO", "Fondy", "StableFace", "JeBobby", "KrytoyKaka", "MagHyCEp", "I7evice", "LeSoulles", "EmptySoul", "KOMnOT", "MrPlay", "NGROK2009", "NoProblem", "MrPatric", "OkugAmas", "YaBuilder", "A7EHT007", "PussyGirl", "Triavan", "TyCoBKa", "UnsafeINV", "", "yKcyc_UFO", "Wendy", "Bendy", "XAOC", "ST6yP7", "XYNECI", "HENTAI", "YoutDaddy", "YouGurT", "EnumFacing", "HitVec3d", "JavaGypy", "VIWEBY", "ZamyPlay", "SUSUKI", "KPAX_TRAX", "Emiron", "UzeXilo", "Rembal", "Gejmer", "EvoNeon", "MrFazen", "ESHKERE", "FARADENZA", "EarWarm", "CMA3KA", "NaVi4oK", "A4_OMG", "YCYSAPO", "Booster", "BroDaga", "CastlePlay29", "DYWAHY", "Emirhan", "BezPontov", "Xilece", "Gigabait", "Griefer", "Goliaf", "Fallaut", "HERODOT", "KingKong", "NADOBNO", "ODIZIT", "Klawdy", "NCRurtler", "Fixik", "FINISHIST", "KPACOTA", "GlintEff", "Flexer", "NeverMore", "BludShaper", "PoSaN4Ik", "Goblin", "Aligator", "Zmeyka", "FieFoxe", "Homilion", "Locator", "kranpus", "HOLSON", "CocyD_ADA", "Anarant", "O6pUKoc", "MissTitan", "JellyKOT", "JellyCat", "LolGgYT", "MapTiNi", "GazVax", "Foxx4ever", "NaGiBaToP", "whiteKing", "KitKat", "VkEdAx", "Pro100Hy6", "Contex", "Durex", "Mr_Golem", "Moonlight", "CoolBoy", "6oTaH", "CaHa6uC", "MuJLaIIIKa", "AvtoBus", "ABOBA", "KanaTuK", "TpanoFob", "CAPSLOCK", "Sonic", "SONIK", "COHUK", "Tailss", "TAILSS", "TauJLC", "Ehidna", "exudHa", "Naklz", "HaKJL3", "coHuk", "parebuh", "nape6yx", "TEPOPNCT", "TPEHEP", "6OKCEP", "KARATE_TYAN", "Astolfo", "Itsuki", "Yotsuba", "Succub", "CyKKy6", "MuJLaIIIKa", "Chappie", "LeraPala4", "MegaSonic", "ME7A_COHUK", "SonicEzh", "IIaPe6yx", "Flamingo", "Pavlin", "VenusShop", "PinkRabbit", "EpicSonic", "EpicTailss", "Genius", "Valkiria"};
        String[] titles = new String[]{"DADA", "YA", "2001", "2002", "2003", "2004", "2005", "2006", "2007", "2008", "2009", "2010", "2011", "2012", "2013", "2014", "2015", "2016", "SUS", "SSS", "TAM", "TyT", "TaM", "Ok", "Pon", "LoL", "CHO", "4oo", "MaM", "Top", "PvP", "PVH", "DIK", "KAK", "SUN", "SIN", "COS", "FIT", "FAT", "HA", "AHH", "OHH", "UwU", "DA", "NaN", "RAP", "WoW", "SHO", "KA4", "Ka4", "AgA", "Fov", "LoVe", "TAN", "Mia", "Alt", "4el", "bot", "GlO", "Sir", "IO", "EX", "Mac", "Win", "Lin", "AC", "Bro", "6po", "6PO", "BRO", "mXn", "XiL", "TGN", "24", "228", "1337", "1488", "007", "001", "999", "333", "666", "111", "FBI", "FBR", "FuN", "FUN", "UFO", "OLD", "Old", "New", "OFF", "ON", "YES", "LIS", "NEO", "BAN", "OwO", "0_o", "0_0", "o_0", "IQ", "99K", "AK47", "SOS", "S0S", "SoS", "z0z", "zOz", "Zzz", "zzz", "ZZZ", "6y", "BU", "RAK", "PAK", "Pak", "MeM", "MoM", "M0M", "KAK", "TAK", "n0H", "BOSS", "RU", "ENG", "BAF", "BAD", "ZED", "oy", "Oy", "0y", "Big", "Air", "Dirt", "Dog", "CaT", "CAT", "KOT", "EYE", "CAN", "ToY", "ONE", "OIL", "HOT", "HoT", "VPN", "BnH", "Ty3", "GUN", "HZ", "XZ", "XYZ", "HZ", "XyZ", "HIS", "HER", "DOC", "COM", "DIS", "TOP", "1ST", "1st", "LORD", "DED", "ded", "HAK", "FUF", "IQQ", "KBH", "KVN", "HuH", "WWW", "RUN", "RuN", "run", "PRO", "100", "300", "3OO", "RAM", "DIR", "Yaw", "YAW", "TIP", "Tun", "Ton", "Tom", "Your", "AM", "FM", "YT", "yt", "Yt", "yT", "RUS", "KON", "FAK", "FUL", "RIL", "pul", "RW", "MST", "MEN", "MAN", "NO0", "SEX", "H2O", "H20", "LyT", "3000", "01", "KEK", "PUK", "nuk", "nyk", "nyK", "191", "192", "32O", "5OO", "320", "500", "777", "720", "480", "48O", "HUK", "BUS", "LUN", "LyH", "Fuu", "LaN", "LAN", "DIC", "HAA", "NON", "FAP", "4AK", "4on", "4EK", "4eK", "NVM", "BOG", "RIP", "SON", "XXL", "XXX", "GIT", "GAD", "8GB", "5G", "4G", "3G", "2G", "TX", "GTX", "RTX", "HOP", "TIR", "ufo", "MIR", "MAG", "ALI", "BOB", "GRO", "GOT", "ME", "SO", "Ay4", "MSK", "MCK", "RAY", "AEG", "AeG", "DEL", "ADD", "UP", "VK", "LOV", "AND", "AVG", "EGO", "YTY", "YoY", "I_I", "G_G", "D_D", "V_V", "F", "FF", "FFF", "LCM", "PCM", "CPS", "FPS", "GO", "G0", "70", "7UP", "JAZ", "GAZ", "7A3", "UFA", "HIT", "DAY", "DaY", "S00", "SCP", "FUK", "SIL", "COK", "SOK", "WAT", "WHO", "PUP", "PuP", "Py", "CPy", "SRU", "OII", "IO", "IS", "THE", "SHE", "nuc", "KXN", "VAL", "MIS", "HXI", "HI", "ByE", "WEB", "TNT", "BEE", "4CB", "III", "IVI", "POP", "C4", "BRUH", "Myp", "MyP", "NET", "CAR", "PET", "POV", "POG", "OKK", "ESP", "GOP", "G0P", "7on", "E6y", "BIT", "PIX", "AYE", "Aye", "PVP", "GAS", "REK", "rek", "PEK", "n0H", "RGB", "Hentai", "Dsc"};
        String name = names[(int)(((float)names.length - 1.0F) * (float)Math.random() * (((float)names.length - 1.0F) / (float)names.length))];
        String title = titles[(int)(((float)titles.length - 1.0F) * (float)Math.random() * (((float)titles.length - 1.0F) / (float)titles.length))];
        int size = (name + "_").length();
        return name + "_" + (16 - size == 0 ? "" : title);
    }



    private void initCommands() {
        Minecraft mc = Minecraft.getInstance();
        Logger logger = new MultiLogger(List.of(new ConsoleLogger(), new MinecraftLogger()));
        List<Command> commands = new ArrayList<>();
        Prefix prefix = new PrefixImpl();
        commands.add(new ListCommand(commands, logger));
        commands.add(new FriendCommand(prefix, logger, mc));
        commands.add(new BindCommand(prefix, logger));
        commands.add(new GPSCommand(prefix, logger));
        commands.add(new WayCommand(prefix, logger));
        commands.add(new ConfigCommand(configManager, prefix, logger));
        commands.add(new MacroCommand(macroManager, prefix, logger));
        commands.add(new VClipCommand(prefix, logger, mc));
        commands.add(new HClipCommand(prefix, logger, mc));
        commands.add(new StaffCommand(prefix, logger));
        commands.add(new ShutDownCommand(logger));
        commands.add(new SetPrefixCommand(logger, (PrefixImpl) prefix));
        commands.add(new MemoryCommand(logger));
        commands.add(new ToggleCommand(logger));
        commands.add(new DDBOXCommand(logger));
        commands.add(new PassCommand(logger));
        commands.add(new RCTCommand(logger, mc));
        commands.add(new ParseCommand(prefix, logger));
        commands.add(new LoginCommand(prefix, logger, mc));
        commands.add(new ReportCommand(logger));


        AdviceCommandFactory adviceCommandFactory = new AdviceCommandFactoryImpl(logger);
        ParametersFactory parametersFactory = new ParametersFactoryImpl();

        commandDispatcher = new StandaloneCommandDispatcher(commands, adviceCommandFactory, prefix, parametersFactory, logger);
    }
    private void initStyles() {





        StyleFactory styleFactory = new StyleFactoryImpl();
        List<Style> styles = new ArrayList<>();

        styles.add(styleFactory.createStyle("Основной", new Color(0, 123, 255), new Color(0, 60, 255)));
        styles.add(styleFactory.createStyle("Галактический", new Color(138, 79, 255), new Color(242, 64, 255)));
        styles.add(styleFactory.createStyle("Огненный", new Color(255, 10, 10), new Color(255, 140, 0)));
        styles.add(styleFactory.createStyle("Зеленый", new Color(24, 166, 2), new Color(2, 166, 84)));
        styles.add(styleFactory.createStyle("Белый", new Color(255, 255, 255), new Color(255, 255, 255)));
        styles.add(styleFactory.createStyle("Ночной", new Color(38, 38, 38), new Color(82, 82, 82)));
        styles.add(styleFactory.createStyle("Морской", new Color(0, 204, 255), new Color(0, 255, 170)));
        styles.add(styleFactory.createStyle("Весенний", new Color(102, 255, 102), new Color(255, 255, 102)));
        styles.add(styleFactory.createStyle("Персиковый", new Color(255, 183, 94), new Color(255, 127, 80)));
        styles.add(styleFactory.createStyle("Лазурный", new Color(135, 206, 250), new Color(0, 191, 255)));
        styles.add(styleFactory.createStyle("Лимонный", new Color(255, 250, 102), new Color(255, 215, 0)));
        styles.add(styleFactory.createStyle("Гламурный", new Color(54, 255, 148), new Color(242, 85, 158)));
        styles.add(styleFactory.createStyle("Бирюзовый", new Color(64, 224, 208), new Color(0, 206, 209)));
        styles.add(styleFactory.createStyle("Коралловый", new Color(255, 127, 80), new Color(255, 99, 71)));
        styles.add(styleFactory.createStyle("Сиреневый", new Color(216, 191, 216), new Color(238, 130, 238)));
        styles.add(styleFactory.createStyle("Небесный", new Color(135, 206, 250), new Color(176, 224, 230)));
        styles.add(styleFactory.createStyle("Мятный", new Color(152, 255, 152), new Color(102, 205, 170)));
        styles.add(styleFactory.createStyle("Зелёно-Синий", new Color(45, 232, 35), new Color(35, 97, 232)));
        styles.add(styleFactory.createStyle("Розово-Красный", new Color(199, 26, 150), new Color(199, 26, 26)));
        styles.add(styleFactory.createStyle("Фиолетово-белый", new Color(155, 66, 245), new Color(255, 255, 255)));
        styles.add(styleFactory.createStyle("Фиолетово-Синий", new Color(75, 14, 173), new Color(23, 26, 179)));
        styles.add(styleFactory.createStyle("Жёлто-Зелёный", new Color(245, 221, 66), new Color(66, 245, 72)));
        styles.add(styleFactory.createStyle("Жёлто-Розовый", new Color(221, 255, 0), new Color(255, 0, 217)));

        styles.add(styleFactory.createStyle("Радужный", new Color(ColoringSystem.rainbow(2, 1)), new Color(ColoringSystem.rainbow(2, 1))));
        styles.add(styleFactory.createStyle("Астольфо", new Color(ColoringSystem.astolfo(2, 1)), new Color(ColoringSystem.astolfo(2, 1))));

        styleManager = new StyleManager(styles, styles.get(0));

    }
}
