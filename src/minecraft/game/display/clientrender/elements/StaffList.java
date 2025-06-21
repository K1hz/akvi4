package minecraft.game.display.clientrender.elements;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import minecraft.game.advantage.alacrity.easing.CompactAnimation;
import minecraft.game.advantage.alacrity.easing.Easing;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.words.font.ClientFonts;
import minecraft.game.display.clientrender.timeupdate.ElementRenderer;
import minecraft.game.display.clientrender.timeupdate.ElementUpdater;
import minecraft.game.transactions.EventRender2D;
import minecraft.game.transactions.EventRender3D;
import minecraft.game.transactions.EventUpdate;
import minecraft.system.AG;
import minecraft.system.managers.StaffManager;
import minecraft.system.managers.drag.Dragging;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class StaffList implements ElementRenderer, ElementUpdater {

    final Dragging dragging;

    public CompactAnimation staffListWidth = new CompactAnimation(Easing.EASE_OUT_EXPO, 350);
    public CompactAnimation staffListHeight = new CompactAnimation(Easing.EASE_OUT_EXPO, 350);
    public CompactAnimation animation = new CompactAnimation(Easing.EASE_OUT_CUBIC, 400);

    float width;
    float height;

    private final List<Staff> staffPlayers = new ArrayList<>();
    private final Pattern namePattern = Pattern.compile("^\\w{3,16}$");
    private final Pattern prefixMatches = Pattern.compile(".*(mod|der|adm|help|wne|хелп|адм|поддержка|кура|own|taf|curat|dev|supp|yt|сотруд).*");

    @Subscribe
    public void update(EventUpdate e) {
        staffPlayers.clear();

        for (ScorePlayerTeam team : mc.world.getScoreboard().getTeams().stream().sorted(Comparator.comparing(Team::getName)).toList()) {
            String name = team.getMembershipCollection().toString().replaceAll("[\\[\\]]", "");
            boolean vanish = true;
            for (NetworkPlayerInfo info : mc.getConnection().getPlayerInfoMap()) {
                if (info.getGameProfile().getName().equals(name)) {
                    vanish = false;
                }
            }
            if (namePattern.matcher(name).matches() && !name.equals(mc.player.getName().getString())) {
                Status status = Status.ONLINE;

                if (!vanish && isPlayerNear(mc.player, name)) {
                    status = Status.NEAR;
                }

                if (!vanish) {
                    if (prefixMatches.matcher(team.getPrefix().getString().toLowerCase(Locale.ROOT)).matches() || StaffManager.isStaff(name)) {
                        Staff staff = new Staff(team.getPrefix(), name, false, status);
                        staffPlayers.add(staff);
                    }
                }
                if (vanish && !team.getPrefix().getString().isEmpty()) {
                    Staff staff = new Staff(team.getPrefix(), name, true, Status.VANISHED);
                    staffPlayers.add(staff);
                }
            }
        }
    }


    @Subscribe
    public void render(EventRender2D EventRender2D) {
        boolean shouldRender = mc.ingameGUI.getChatGUI().getChatOpen() || !staffPlayers.isEmpty();

        animation.run(shouldRender ? 1 : 0);
        animation.update();

        float animationValue = (float) animation.getValue();
        if (animationValue <= 0) return;

        MatrixStack matrixStack = EventRender2D.getMatrixStack();

        float minWidth = 66;
        float initialHeight = ClientFonts.clickGui[16].getFontHeight() + 7;
        float fontSize = 6f;
        float padding = 5;

        float posX = dragging.getX();
        float posY = dragging.getY();

        String name = "Staff List";

        float maxWidth = ClientFonts.clickGui[18].getWidth(name) + padding * 3 + ClientFonts.icons_hud[24].getWidth("L");
        for (Staff staff : staffPlayers) {
            ITextComponent prefix = staff.getPrefix();
            float prefixWidth = ClientFonts.clickGui[16].getWidth(prefix.getString());
            float nameWidth = ClientFonts.clickGui[16].getWidth(staff.getName());

            float localWidth = padding + 15 + prefixWidth + 10 + nameWidth + 30;
            maxWidth = Math.max(maxWidth, localWidth);
        }

        staffListWidth.run(Math.max(maxWidth, minWidth));
        width = staffListWidth.getNumberValue().floatValue();
        height = initialHeight;

        int alpha = (int) (255 * animationValue);

        GraphicsSystem.drawRoundedRect(posX, posY, width, height, 3, ColoringSystem.setAlpha(ColoringSystem.rgb(20,20,20), alpha));

        float iconHeight = ClientFonts.icons_hud[22].getFontHeight();
        float textHeight = ClientFonts.clickGui[18].getFontHeight();
        float centerY = posY + padding + (Math.max(iconHeight, textHeight) - iconHeight) / 2 + 1;

        ClientFonts.clickGui[18].drawString(matrixStack, name, posX + padding + ClientFonts.icons_hud[22].getWidth("L") + 4, centerY, ColoringSystem.setAlpha(-1, alpha));
        ClientFonts.icons_hud[22].drawString(matrixStack, "L", posX + padding, centerY, ColoringSystem.setAlpha(-1, alpha));

        float contentStartY = posY + initialHeight;

        float textAlpha = (float) animation.getValue();

        for (Staff staff : staffPlayers) {
            ITextComponent prefix = staff.getPrefix();
            float prefixWidth = prefix != null ? ClientFonts.clickGui[16].getWidth(prefix.getString()) : 0;
            String statusText = staff.getStatus().string;
            float statusWidth = ClientFonts.clickGui[16].getWidth(statusText);

            float rowHeight = fontSize + padding + 3;
            height += rowHeight;

            float rectAlpha = (float) animation.getValue();
            float rectHeight = rowHeight * rectAlpha;
            float rectWidth = width * rectAlpha;

            GraphicsSystem.drawRoundedRect(posX, contentStartY, rectWidth, rectHeight, 3, ColoringSystem.rgb(20,20,20));

            float circleRadius = 6;
            float circleX = posX + padding + 3;
            float circleY = contentStartY + rowHeight / 2 - circleRadius + 6;

            float circleAlpha = (float) animation.getValue();
            int circleColor;
            if (staff.getStatus() == Status.NEAR) {
                circleColor = ColoringSystem.rgb(255, 255, 0);
            } else if (staff.isSpec) {
                circleColor = ColoringSystem.rgb(255, 0, 0);
            } else if (staff.getStatus() == Status.VANISHED) {
                circleColor = ColoringSystem.rgb(255, 0, 0);
            } else {
                circleColor = ColoringSystem.rgb(0, 255, 0);
            }

            GraphicsSystem.drawCircle(circleX, circleY, circleRadius * rectAlpha, ColoringSystem.setAlpha(circleColor, (int) (255 * circleAlpha)));

            float nameX = posX + padding + circleRadius * 1.3f;
            if (prefix != null) {
                ClientFonts.clickGui[16].drawString(matrixStack, prefix, nameX, contentStartY + 5, ColoringSystem.setAlpha(-1, (int) (255 * textAlpha)));
                ClientFonts.clickGui[16].drawString(matrixStack, staff.getName(), nameX + prefixWidth, contentStartY + 5, ColoringSystem.setAlpha(-1, (int) (255 * textAlpha)));
            } else {
                ClientFonts.clickGui[16].drawString(matrixStack, staff.getName(), nameX, contentStartY + 5, ColoringSystem.setAlpha(-1, (int) (255 * textAlpha)));
            }

            float separatorX = posX + width - statusWidth - padding - 4;
            GraphicsSystem.drawRoundedRect(separatorX, contentStartY + 3, 1.0f, rowHeight - 5, 1, ColoringSystem.rgb(30, 30, 30));
            ClientFonts.clickGui[16].drawString(matrixStack, statusText, posX + width - padding - statusWidth, contentStartY + 5, staff.status.color);

            contentStartY += rowHeight;
        }

        staffListHeight.run(height);
        height = staffListHeight.getNumberValue().floatValue();
        dragging.setWidth(width);
        dragging.setHeight(height);
    }

    private boolean isPlayerNear(PlayerEntity player, String staffName) {
        for (NetworkPlayerInfo info : mc.getConnection().getPlayerInfoMap()) {
            if (info.getGameProfile().getName().equals(staffName)) {
                PlayerEntity staffPlayer = mc.world.getPlayerByUuid(info.getGameProfile().getId());
                if (staffPlayer != null) {
                    double distance = player.getDistance(staffPlayer);
                    return distance <= 50.0;
                }
            }
        }
        return false;
    }

    @Data
    @AllArgsConstructor
    public static class Staff {
        ITextComponent prefix;
        String name;
        boolean isSpec;
        Status status;
    }

    public enum Status {
        VANISHED("Vanished", ColoringSystem.rgb(255, 0, 0)),
        ONLINE("Online", ColoringSystem.rgb(0, 255, 0)),
        NEAR("Near", ColoringSystem.rgb(255, 255, 0));

        String string;
        int color;

        Status(String string, int color) {
            this.string = string;
            this.color = color;
        }
    }
}
