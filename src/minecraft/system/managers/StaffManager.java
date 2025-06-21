package minecraft.system.managers;

import minecraft.game.advantage.advisee.SoundPlayer;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

import minecraft.game.advantage.advisee.IMinecraft;

import static java.io.File.separator;

@UtilityClass
public class StaffManager implements IMinecraft {

    @Getter
    private final Set<String> staffs = new HashSet<>();
    private final File file = new File(mc.gameDir + separator + "System" + separator + "Folder" + separator + "Files" + separator + "StaffList.ag");

    @SneakyThrows
    public void load() {
        if (file.exists()) {
            staffs.addAll(Files.readAllLines(file.toPath()));
        } else {
            file.createNewFile();
        }
    }

    public void add(String name) {
        staffs.add(name);
        SoundPlayer.playSound("friendadd.wav");
        save();
    }

    public void remove(String name) {
        staffs.remove(name);
        SoundPlayer.playSound("friendremove.wav");
        save();
    }

    public void clear() {
        staffs.clear();
        SoundPlayer.playSound("friendremove.wav");
        save();
    }

    public boolean isStaff(String name) {
        return staffs.contains(name);
    }

    @SneakyThrows
    private void save() {
        Files.write(file.toPath(), staffs);
    }
}
