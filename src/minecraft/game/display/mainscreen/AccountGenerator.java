package minecraft.game.display.mainscreen;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Random;

public class AccountGenerator {

    private static final String[] NAMES = new String[]{
            "Penis", "Femboy", "Shadow", "Eclipse", "Phantom", "Storm", "Thunder", "Nova", "Cosmos", "Pixel", "Lynx", "Wolf", "Predator",
            "Venom", "Draco", "Hydra", "Phoenix", "Inferno", "Vortex", "Cyber", "Neo", "Ghost", "Reaper", "Hunter", "Raven", "Falcon",
            "Jaguar", "Titan", "Zephyr", "Aurora", "Specter", "Mercury", "Orion", "Apollo", "Viper", "Axel", "Cipher", "Zero", "Delta",
            "Omega", "Sigma", "Zeta", "Alpha", "Beta", "Drift", "Glitch", "Echo", "Frost", "Blaze", "Comet", "Astro", "Nimbus", "Meteor",
            "Lunar", "Solar", "Stellar", "Quasar", "Pulsar", "Gravity", "Magnet", "Fusion", "Atom", "Electron", "Proton", "Neutron",
            "Quantum", "Particle", "Plasma", "Flare", "Bolt", "Spark", "Shock", "Voltage", "Current", "Tesla", "Edison", "Newton",
            "Einstein", "Curie", "Galileo", "Hawk", "Eagle", "Sparrow", "HawkEye", "IronWolf", "Silver", "Gold", "Platinum", "Diamond",
            "Ruby", "Sapphire", "Emerald", "Obsidian", "Onyx", "Topaz", "Crystal", "Jade", "Garnet", "Amethyst", "Pearl", "Moonstone",
            "Midnight", "Dusk", "Dawn", "Twilight", "Aurora", "Glacier", "Blizzard", "Hurricane", "Typhoon", "Tornado", "Quake", "Lava",
            "Magma", "Volcano", "Cinder", "Ash", "Smoke", "Flame", "Pyro", "Ignis", "Aether", "Void", "Chaos", "Entropy", "Harmony",
            "Zenith", "Nadir", "Peak", "Summit", "Apex", "Base", "Core", "Axis", "Vector", "Matrix", "Catalyst", "Focus", "Prism",
            "Spectrum", "Orbit", "Satellite", "Galaxy", "Universe", "Infinity", "Eternity", "Time", "Chronos", "Kairos", "Aeon",
            "Myth", "Legend", "Fable", "Saga", "Epic", "Rune", "Glyph", "Scroll", "Cipher", "Key", "Lock", "Code", "Script", "Text",
            "Verse", "Lyric", "Song", "Melody", "Harmony", "Chord", "Scale", "Tone", "Beat", "Rhythm", "Tempo", "Pulse", "Bass",
            "Treble", "Synth", "Wave", "Sound", "Echo", "Voice", "Chime", "Bell", "Ring", "Alarm", "Signal", "Beacon", "Light",
            "Ray", "Beam", "Glow", "Shine", "Sparkle", "Glimmer", "Glisten", "Twinkle", "Flicker", "Flash", "Burst", "Explosion",
            "Blast", "Impact", "Crash", "Bang", "Boom", "Pop", "Snap", "Crackle", "Fizz", "Bubble", "Sizzle", "Steam", "Fog",
            "Mist", "Cloud", "Rain", "Storm", "Hail", "Snow", "Ice", "Frost", "Freeze", "Cold", "Chill", "Winter", "Spring", "Summer",
            "Autumn", "Season", "Year", "Month", "Week", "Day", "Hour", "Minute", "Second", "Moment", "Tick", "Tock", "Clock",
            "Watch", "Timer", "Alarm", "Calendar", "Date", "Time", "Past", "Present", "Future", "History", "Story", "Tale", "Myth",
            "Fable", "Legend", "Epic", "Saga", "Chronicle", "Journal", "Diary", "Log", "Record", "File", "Document", "Note", "Memo",
            "Message", "Letter", "Word", "Sentence", "Paragraph", "Page", "Book", "Volume", "Library", "Archive", "Database",
            "Server", "Network", "System", "Machine", "Robot", "Android", "Cyborg", "Drone", "Bot", "AI", "Algorithm", "Program",
            "App", "Software", "Hardware", "Chip", "Circuit", "Board", "Module", "Unit", "Device", "Gadget", "Tool", "Instrument",
            "Machine", "Engine", "Motor", "Gear", "Cog", "Wheel", "Axle", "Frame", "Body", "Shell", "Core", "Base", "Structure",
            "Building", "Tower", "Skyscraper", "Bridge", "Tunnel", "Road", "Path", "Trail", "Route", "Track", "Line", "Network",
            "Grid", "Web", "Net", "Mesh", "Chain", "Link", "Bond", "Knot", "Loop", "Twist", "Turn", "Curve", "Arc", "Circle",
            "Ring", "Sphere", "Ball", "Orb", "Disk", "Plate", "Tile", "Brick", "Block", "Cube", "Box", "Case", "Container", "Chest",
            "Locker", "Safe", "Vault", "Closet", "Cabinet", "Shelf", "Rack", "Table", "Desk", "Chair", "Seat", "Couch", "Sofa",
            "Bed", "Bunk", "Hammock", "Mat", "Rug", "Carpet", "Curtain", "Blind", "Shade", "Screen", "Projector", "Display",
            "Monitor", "Panel", "Board", "Board", "Sheet", "Card", "Paper", "File", "Folder", "Bag", "Sack", "Pouch", "Pocket",
            "Pack", "Bundle", "Box", "Case", "Kit", "Set", "Group", "Team", "Squad", "Crew", "Gang", "Band", "Tribe", "Clan",
            "Family", "House", "Line", "Branch", "Tree", "Root", "Stem", "Leaf", "Flower", "Blossom", "Bloom", "Fruit", "Berry",
            "Nut", "Seed", "Pod", "Shell", "Husk", "Case", "Skin", "Peel", "Rind", "Core", "Pit", "Stone", "Kernel", "Meat",
            "Flesh", "Juice", "Oil", "Fat", "Grease", "Wax", "Soap", "Foam", "Cream", "Lotion", "Gel", "Paste", "Powder", "Dust",
            "Sand", "Gravel", "Stone", "Rock", "Boulder", "Mountain", "Hill", "Cliff", "Canyon", "Valley", "Plain", "Field",
    };

    private static final String[] TITLES = new String[]{
            "DADA", "YA", "2001", "2002", "2003", "2004", "2005", "2006", "2007", "2008", "2009", "2010", "2011", "2012", "2013",
            "2014", "2015", "2016", "SUS", "SSS", "TAM", "TyT", "TaM", "Ok", "Pon", "LoL", "CHO", "4oo", "MaM", "Top", "PvP", "PVH",
            "DIK", "KAK", "SUN", "SIN", "COS", "FIT", "FAT", "HA", "AHH", "OHH", "UwU", "DA", "NaN", "RAP", "WoW", "SHO", "KA4", "Ka4",
            "AgA", "Fov", "LoVe", "TAN", "Mia", "Alt", "4el", "bot", "GlO", "Sir", "IO", "EX", "Mac", "Win", "Lin", "AC", "Bro", "6po",
            "6PO", "BRO", "mXn", "XiL", "TGN", "24", "228", "1337", "1488", "007", "001", "999", "333", "666", "111", "FBI", "FBR", "FuN",
            "FUN", "UFO", "OLD", "Old", "New", "OFF", "ON", "YES", "LIS", "NEO", "BAN", "OwO", "0_o", "0_0", "o_0", "IQ", "99K", "AK47",
            "SOS", "S0S", "SoS", "z0z", "zOz", "Zzz", "zzz", "ZZZ", "6y", "BU", "RAK", "PAK", "Pak", "MeM", "MoM", "M0M", "KAK", "TAK",
            "n0H", "BOSS", "RU", "ENG", "BAF", "BAD", "ZED", "oy", "Oy", "0y", "Big", "Air", "Dirt", "Dog", "CaT", "CAT", "KOT", "EYE",
            "CAN", "ToY", "ONE", "OIL", "HOT", "HoT", "VPN", "BnH", "Ty3", "GUN", "HZ", "XZ", "XYZ", "HZ", "XyZ", "HIS", "HER", "DOC",
            "COM", "DIS", "TOP", "1ST", "1st", "LORD", "DED", "ded", "HAK", "FUF", "IQQ", "KBH", "KVN", "HuH", "WWW", "RUN", "RuN", "run",
            "PRO", "100", "300", "3OO", "RAM", "DIR", "Yaw", "YAW", "TIP", "Tun", "Ton", "Tom", "Your", "AM", "FM", "YT", "yt", "Yt", "yT",
            "RUS", "KON", "FAK", "FUL", "RIL", "pul", "RW", "MST", "MEN", "MAN", "NO0", "SEX", "H2O", "H20", "LyT", "3000", "01", "KEK",
            "PUK", "nuk", "nyk", "nyK", "191", "192", "32O", "5OO", "320", "500", "777", "720", "480", "48O", "HUK", "BUS", "LUN", "LyH",
            "Fuu", "LaN", "LAN", "DIC", "HAA", "NON", "FAP", "4AK", "4on", "4EK", "4eK", "NVM", "BOG", "RIP", "SON", "XXL", "XXX", "GIT",
            "GAD", "8GB", "5G", "4G", "3G", "2G", "TX", "GTX", "RTX", "HOP", "TIR", "ufo", "MIR", "MAG", "ALI", "BOB", "GRO", "GOT", "ME",
            "SO", "Ay4", "MSK", "MCK", "RAY", "DEL", "ADD", "UP", "VK", "LOV", "AND", "AVG", "EGO", "YTY", "YoY", "I_I", "G_G",
            "D_D", "V_V", "F", "FF", "FFF", "LCM", "PCM", "CPS", "FPS", "GO", "G0", "70", "7UP", "JAZ", "GAZ", "7A3", "UFA", "HIT", "DAY",
            "DaY", "S00", "SCP", "FUK", "SIL", "COK", "SOK", "WAT", "WHO", "PUP", "PuP", "Py", "CPy", "SRU", "OII", "IO", "IS", "THE", "SHE",
            "nuc", "KXN", "VAL", "MIS", "HXI", "HI", "ByE", "WEB", "TNT", "BEE", "4CB", "III", "IVI", "POP", "C4", "BRUH", "Myp", "MyP",
            "NET", "CAR", "PET", "POV", "POG", "OKK", "ESP", "GOP", "G0P", "7on", "E6y", "BIT", "PIX", "AYE", "Aye", "PVP", "GAS", "REK",
            "rek", "PEK", "n0H", "RGB"
    };

    private static Random random = new Random();

    public static String generateUsername() {
        int length = random.nextInt(12) + 5;
        StringBuilder username = new StringBuilder();

        String name = NAMES[random.nextInt(NAMES.length)];
        String title = TITLES[random.nextInt(TITLES.length)];
        username.append(name);
        if (random.nextInt(100) < 40) {
            username.append("_");
        }

        username.append(title);

        if (username.length() > length) {
            username.setLength(length);
        } else if (username.length() < length) {
            username.append(RandomStringUtils.randomAlphanumeric(length - username.length()));
        }

        if (username.length() > 16) {
            username.setLength(16);
        }

        return username.toString();
    }
}
