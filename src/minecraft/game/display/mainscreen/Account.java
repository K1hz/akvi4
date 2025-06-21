package minecraft.game.display.mainscreen;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;

public class  Account {
    public String accountName = "";
    public long dateAdded;
    public ResourceLocation skin;
    public float x, y;

    public Account(String accountName) {
        this.accountName = accountName;
        this.dateAdded = System.currentTimeMillis();
        UUID uuid = resolveUUIDOrDefault(accountName);

        this.skin = DefaultPlayerSkin.getDefaultSkin(uuid);
        Minecraft.getInstance().getSkinManager().loadProfileTextures(new GameProfile(uuid, accountName), (type, loc, tex) -> {
            if (type == MinecraftProfileTexture.Type.SKIN) {
                skin = loc;
            }
        }, true);
    }

    public Account(String accountName, long dateAdded) {
        this.accountName = accountName;
        this.dateAdded = dateAdded;
        UUID uuid = resolveUUIDOrDefault(accountName);

        this.skin = DefaultPlayerSkin.getDefaultSkin(uuid);
        Minecraft.getInstance().getSkinManager().loadProfileTextures(new GameProfile(uuid, accountName), (type, loc, tex) -> {
            if (type == MinecraftProfileTexture.Type.SKIN) {
                skin = loc;
            }
        }, true);
    }

    public String getFormattedDateAdded() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        LocalDateTime dateTime = LocalDateTime.ofInstant(new Date(dateAdded).toInstant(), java.time.ZoneId.systemDefault());
        return dateTime.format(formatter);
    }

    private UUID resolveUUIDOrDefault(String accountName) {
        UUID uuid;
        try {
            uuid = resolveUUID(accountName);
        } catch (IOException e) {
            uuid = UUID.randomUUID();
        }
        return uuid;
    }

    public static UUID resolveUUID(String name) throws IOException {
        InputStreamReader in = new InputStreamReader(new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openStream(), StandardCharsets.UTF_8);
        UUID uuid;
        try {
            String id = new Gson().fromJson(in, JsonObject.class).get("id").getAsString();
            uuid = UUID.fromString(id.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
        } finally {
            in.close();
        }
        return uuid;
    }
}