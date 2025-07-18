package minecraft.system.managers.drag;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;

public class DragManager {
    public static LinkedHashMap<String, Dragging> draggables = new LinkedHashMap<>();

    private static final File DRAG_DATA = new File(Minecraft.getInstance().gameDir, "\\System\\Folder\\Files\\Dragging.ag");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();

    public static void save() {

        if (!DRAG_DATA.exists()) {
            DRAG_DATA.getParentFile().mkdirs();
        }
        try {
            Files.writeString(DRAG_DATA.toPath(), GSON.toJson(draggables.values()));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void load() {
        if (!DRAG_DATA.exists()) {
            DRAG_DATA.getParentFile().mkdirs();
            return;
        }
        Dragging[] draggings;

        try {
            draggings = GSON.fromJson(Files.readString(DRAG_DATA.toPath()), Dragging[].class);

        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        for (Dragging dragging : draggings) {
            if (dragging == null) return;
            Dragging currentDrag = draggables.get(dragging.getName());
            if (currentDrag == null) continue;
            currentDrag.setX(dragging.getX());
            currentDrag.setY(dragging.getY());
            draggables.put(dragging.getName(), currentDrag);
        }
    }

    public static void resetAllPositions() {
        for (Dragging dragging : draggables.values()) {
            dragging.resetPosition();
        }
        save();
    }


}