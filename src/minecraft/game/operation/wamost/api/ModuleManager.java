package minecraft.game.operation.wamost.api;

import com.google.common.eventbus.Subscribe;

import minecraft.game.advantage.make.font.Font;
import minecraft.game.operation.wamost.massa.elements.BindSetting;
import net.minecraft.util.text.StringTextComponent;
import minecraft.system.AG;
import minecraft.game.transactions.EventKey;
import minecraft.game.operation.combat.*;
import minecraft.game.operation.misc.*;
import minecraft.game.operation.movement.*;
import minecraft.game.operation.visual.*;
import minecraft.game.advantage.words.font.ClientFonts;
import lombok.Getter;
import org.lwjgl.system.CallbackI;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Getter
public class ModuleManager {
    private final List<Module> modules = new CopyOnWriteArrayList<>();

    private NoServerDesync noServerDesync;
    private ViewModel viewModel;
    private Hud hud;
    private DDGAPPLE DDGAPPLE;
    private AutoSprint autoSprint;
    private DDNVLC DDNVLC;
    private NoRender noRender;
    private InventoryPlus inventoryPlus;
    private ElytraHelper elytrahelper;
    private DDPOTION autopotion;
    private DDTBOT DDTBOT;
    private DDCLKFRIEND DDCLKFRIEND;
    private ESP esp;
    private FTHelper FTHelper;
    private DDCOLDWN DDCOLDWN;
    private DDCLCKPERL DDCLCKPERL;
    private DDSWAP DDSWAP;
    private DDBOXED DDBOXED;
    private DDPUSH DDPUSH;
    private DDFRCM DDFRCM;
    private DDCHSTSTEL DDCHSTSTEL;
    private Fly fly;
    private TargetStrafe targetStrafe;
    private DDCLNTTUNE DDCLNTTUNE;
    private DDTOTEM DDTOTEM;
    private DDEXPLOSION DDEXPLOSION;
    private DDATTACK DDATTACK;
    private DDBOT DDBOT;
    private Crosshair crosshair;
    private Strafe strafe;
    private Predictions predictions;
    private DDENTITYTR DDENTITYTR;
    private StorageEsp storageESP;
    private NameProtect nameProtect;
    private NoInteract noInteract;
    private SelfDestruct selfDestruct;
    private DDBETTER DDBETTER;
    private SeeInvisibles seeInvisibles;
    private Speed speed;
    private DDFRIEND DDFRIEND;
    private ClickGui clickGui;
    private DDBLICK DDBLICK;
    private Scaffold scaffold;
    private WorldTweaks worldTweaks;
    private Arrows arrows;
    private RWJoiner rwJoiner;
    private FakePlayer fakePlayer;
    private DDACTION DDACTION;
    private FullBright fullBright;
    private DDDUEL DDDUEL;
    private MoveHelper moveHelper;
    private HitBubbles hitBubbles;
    private FireFly fireFly;
    private Trails trails;
    private JumpCircle jumpCircle;
    private WallHack wallHack;
    private InventoryMove inventoryMove;
    private ChinaHat chinaHat;
    private AspectRatio aspectRatio;
    private ElytraBooster elytraBooster;
    private TargetEsp targetEsp;
    private MusicPlayerUI musicPlayerUI;
    private CustomClientBrand customClientBrand;
    private AntiTarget antiTarget;
    private FastEXP fastEXP;
    private NoDelay noDelay;
    private ClickSettings clickSettings;
    private HWHelper hwHelper;
    private Backtrack backtrack;
    private OreFinder oreFinder;
    private TrailsOG trailsOG;
    private BadTrip badTrip;
    private TapeMouse tapeMouse;
    private AntiCrash antiCrash;
    private AimAssist aimAssist;
    private LegitATTACK legitATTACK;
    private HitColor hitColor;
    private ElytraSpeed catStrafe;
    private ElytraFly elytraFly;
    private Nuker nuker;
    private CreeperFarm creeperFarm;
    private Animation animation;
    private AutoAnchor autoAnchor;
    private AntiAim antiAim;
    private Peregon peregon;
    private NoBadEffects noBadEffects;
    private DDBOW ddbow;
    private DDBOWSP ddbowsp;
    private DDCRIT ddcrit;
    private AutoTool autoTool;
    private Jesus jesus;
    private HighJump highJump;
    private Spider spider;
    private LootScrubber lootScrubber;
    private ContainerAura containerAura;
    private CustomModels customModels;
    public void init() {
        registerAll(chinaHat = new ChinaHat(), fakePlayer = new FakePlayer(),
                peregon = new Peregon(), noBadEffects = new NoBadEffects(),
                aimAssist = new AimAssist(), legitATTACK = new LegitATTACK(),
                musicPlayerUI = new MusicPlayerUI(), noDelay = new NoDelay(),
                clickSettings = new ClickSettings(), hwHelper = new HWHelper(), badTrip = new BadTrip(),
                trailsOG = new TrailsOG(), hitColor = new HitColor(),
                backtrack = new Backtrack(), oreFinder = new OreFinder(),
                antiAim = new AntiAim(), ddbow = new DDBOW(),
                antiTarget = new AntiTarget(), fastEXP = new FastEXP(),
                customClientBrand = new CustomClientBrand(), animation = new Animation(),
                targetEsp = new TargetEsp(), nuker = new Nuker(),
                spider = new Spider(), lootScrubber = new LootScrubber(),
                aspectRatio = new AspectRatio(), elytraBooster = new ElytraBooster(), antiCrash = new AntiCrash(),
                inventoryMove = new InventoryMove(), creeperFarm = new CreeperFarm(),
                tapeMouse = new TapeMouse(), wallHack = new WallHack(),
                fireFly = new FireFly(), trails = new Trails(), jumpCircle = new JumpCircle(),
                moveHelper = new MoveHelper(), hitBubbles = new HitBubbles(),
                fullBright = new FullBright(), DDDUEL = new DDDUEL(),
                DDACTION = new DDACTION(), autoAnchor = new AutoAnchor(),
                hud = new Hud(), arrows = new Arrows(), DDCHSTSTEL = new DDCHSTSTEL(),
                noServerDesync = new NoServerDesync(), ddcrit = new DDCRIT(),
                worldTweaks = new WorldTweaks(), scaffold = new Scaffold(),
                clickGui = new ClickGui(), ddbowsp = new DDBOWSP(),
                elytraFly = new ElytraFly(), catStrafe = new ElytraSpeed(), autoTool = new AutoTool(),
                DDFRIEND = new DDFRIEND(), jesus = new Jesus(), containerAura = new ContainerAura(),
                speed = new Speed(), DDGAPPLE = new DDGAPPLE(), autoSprint = new AutoSprint(),
                DDNVLC = new DDNVLC(), noRender = new NoRender(), inventoryPlus = new InventoryPlus(),
                seeInvisibles = new SeeInvisibles(), elytrahelper = new ElytraHelper(), autopotion = new DDPOTION(),
                DDTBOT = new DDTBOT(), DDCLKFRIEND = new DDCLKFRIEND(),
                esp = new ESP(), FTHelper = new FTHelper(), DDBOXED = new DDBOXED(), DDPUSH = new DDPUSH(),
                DDFRCM = new DDFRCM(), highJump = new HighJump(), customModels = new CustomModels(),
                fly = new Fly(), DDCLNTTUNE = new DDCLNTTUNE(), DDEXPLOSION = new DDEXPLOSION(),
                DDBOT = new DDBOT(), crosshair = new Crosshair(), DDTOTEM = new DDTOTEM(), DDCOLDWN = new DDCOLDWN(),
                DDATTACK = new DDATTACK(autopotion), DDCLCKPERL = new DDCLCKPERL(DDCOLDWN),
                DDSWAP = new DDSWAP(DDTOTEM), targetStrafe = new TargetStrafe(DDATTACK),
                strafe = new Strafe(targetStrafe, DDATTACK), viewModel = new ViewModel(DDATTACK),
                predictions = new Predictions(), DDENTITYTR = new DDENTITYTR(),
                storageESP = new StorageEsp(), nameProtect = new NameProtect(),
                noInteract = new NoInteract(), selfDestruct = new SelfDestruct(),
                DDBETTER = new DDBETTER(), new RWHelper()
        );

        sortModulesByWidth();


        AG.getInst().getEventBus().register(this);
    }

    private void registerAll(Module... modules) {
        this.modules.addAll(List.of(modules));
    }

    public List<Module> getSorted(Font font, float size) {
        return this.modules.stream().sorted((f1, f2) -> Float.compare(font.getWidth(f2.getName(), size), font.getWidth(f1.getName(), size))).toList();
    }

    public void sortModulesByWidth() {
        try {
            modules.sort(Comparator.comparingDouble(module ->
                    ClientFonts.tenacity[19].getWidth(module.getClass().getName())
            ).reversed());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Module> get(final Category category) {
        return modules.stream().filter(module -> module.getCategory() == category).collect(Collectors.toList());
    }

    public int countEnabledModules() {
        return (int) modules.stream().filter(Module::isEnabled).count();
    }

    @Subscribe
    private void onKey(EventKey e) {
        if (selfDestruct.unhooked) return;
        modules.stream().filter(module -> module.getBind() == e.getKey()).forEach(Module::toggle);
    }

}
