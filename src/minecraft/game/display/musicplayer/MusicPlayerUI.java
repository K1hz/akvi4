package minecraft.game.display.musicplayer;

import com.mojang.blaze3d.matrix.MatrixStack;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import minecraft.game.advantage.figures.Vector4i;
import minecraft.game.advantage.words.font.ClientFonts;
import minecraft.game.operation.misc.SelfDestruct;
import minecraft.game.operation.visual.Hud;
import minecraft.system.AG;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.ITextComponent;
import minecraft.game.advantage.advisee.IMinecraft;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.make.font.Fonts;
import minecraft.game.advantage.make.other.Scissor;
import org.lwjgl.system.CallbackI;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MusicPlayerUI extends Screen implements IMinecraft {
    private List<File> musicFiles = new ArrayList<>();

    private File musicDirectory = new File("System/Folder/Music");

    private String message = "Папка с музыкой пуста";

    final ResourceLocation folder = new ResourceLocation("render/images/music/folder.png");

    final ResourceLocation play = new ResourceLocation("render/images/music/play.png");

    final ResourceLocation pause = new ResourceLocation("render/images/music/pause.png");

    final ResourceLocation repeat = new ResourceLocation("render/images/music/repeat.png");

    final ResourceLocation yes = new ResourceLocation("render/images/music/yes.png");
    private AdvancedPlayer mp3Player;

    private int selectedMusicIndex = -1;
    private boolean isPlaying = false;
    private boolean isRepeat = false;
    private Clip clip;
    private float volume = 0.3f;
    private MatrixStack matrixStack;
    private int scrollOffset = 0;
    private int maxVisibleItems = 10;
    private float itemHeight = 25f;
    float iconSizeX = 10;
    float iconSizeY = 10;

    private enum Category {
        LOCAL
    }

    private Category currentCategory = Category.LOCAL;

    public MusicPlayerUI(ITextComponent titleIn) {

        super(titleIn);

    }

    @Override
    protected void init() {

        super.init();

        loadMusicFiles();
    }

    private void loadMusicFiles() {
        musicFiles.clear();

        try {

            if (currentCategory == Category.LOCAL) {

                if (musicDirectory.exists() && musicDirectory.isDirectory()) {

                    File[] files = musicDirectory.listFiles((dir, name) -> name.endsWith(".wav") || name.endsWith(".mp3"));


                    if (files != null) {

                        for (File file : files) {

                            musicFiles.add(file);

                        }

                    }

                }

                if (!musicDirectory.exists()) {

                    musicDirectory.mkdir();

                }

                if (musicFiles.isEmpty()) {

                    message = "Папка с музыкой пуста\n";

                }

            }

        } catch (Exception e) {

            e.printStackTrace();

            message = "Произошла ошибка при загрузке треков.";

            musicFiles.clear();

        }

    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);

        int windowWidth = mc.getMainWindow().getScaledWidth();
        int windowHeight = mc.getMainWindow().getScaledHeight();
        float width = 200;


        float height;
        if (selectedMusicIndex != -1) {
            height = 340;
        } else {
            height = 230;
        }


        float x = windowWidth / 2f - width / 2f;
        float y = windowHeight / 2f - height / 2f;


        GraphicsSystem.drawRoundedRect(x, y, width, height, new Vector4f(7, 7, 7, 7), ColoringSystem.rgba(17, 17, 17, 255));

        GraphicsSystem.drawRoundedRect(x + width - 65 + 1 - 125, y + 8, 150, 20, 5, ColoringSystem.rgba(20, 20, 20, 1));
        ClientFonts.clickGui[28].drawCenteredString(matrixStack, "Music Player", x + width / 2f - 47, y + 18, ColoringSystem.rgb(255, 255, 255));

        float openFolderX = x + width - 65 + 1;

        float openFolderY = y + 8;//10;

        float folderTextWidth = Fonts.montserrat.getWidth("Папка", 10);
        ClientFonts.clickGui[14].drawString(matrixStack, "Folder", openFolderX + 34, openFolderY - 1 , ColoringSystem.rgb(165, 165, 165));

        GraphicsSystem.drawRoundedRect(openFolderX + 35, openFolderY + 4.5f, 20, 20, 3, ColoringSystem.rgba(20, 20, 20, 255));


        GraphicsSystem.drawImage(folder, openFolderX + 39, openFolderY + 8, 13f, 13f, ColoringSystem.rgb(255, 255, 255));

        if (musicFiles.isEmpty()) {

            String message2 = "чтобы добавить музыку\n";
            String message4 = "поместите её в папку";
            String message3 = "System/Folder/Music";
            String message5 = "в формате .wav";

            Fonts.montserrat.drawCenteredText(matrixStack, message, x + width / 2f, y + height / 2f - 32, ColoringSystem.rgb(255, 255, 255), 11);

            Fonts.montserrat.drawCenteredText(matrixStack, message2, x + width / 2f, y + height / 2f - 19.5f, ColoringSystem.rgb(255, 255, 255), 11);
            Fonts.montserrat.drawCenteredText(matrixStack, message4, x + width / 2f, y + height / 2f - 7f, ColoringSystem.rgb(255, 255, 255), 11);
            Fonts.montserrat.drawCenteredText(matrixStack, message3, x + width / 2f, y + height / 2f + 6.5f, ColoringSystem.rgb(255, 255, 255), 11);
            Fonts.montserrat.drawCenteredText(matrixStack, message5, x + width / 2f, y + height / 2f + 18.5f, ColoringSystem.rgb(255, 255, 255), 11);

        } else {
            Scissor.push();
            Scissor.setFromComponentCoordinates(x, y + 30, width + 300, height - 10);
            float listX = x + 20;
            float listY = y + 40;

            GraphicsSystem.drawRoundedRect(listX - 10, listY - 5, width - 20, 185, 2, ColoringSystem.rgba(20, 20, 20, 255));


            int visibleItems = 7;

            int maxScroll = Math.max(0, musicFiles.size() - visibleItems);

            int scrollOffset1 = Math.max(0, Math.min(scrollOffset, maxScroll));

            int startIndex = Math.max(0, scrollOffset1);

            int endIndex = Math.min(musicFiles.size(), startIndex + visibleItems);

            float visibleHeight = 175;

            for (int i = startIndex; i < endIndex; i++) {
                float itemY = listY + (i - startIndex) * itemHeight;

                if (itemY >= listY && itemY + itemHeight <= listY + visibleHeight) {
                    boolean hovered = MathSystem.isHovered(mouseX, mouseY, listX, itemY, width - 40, itemHeight);

                    if (hovered) {
                        GraphicsSystem.drawRoundedRect(listX, itemY, width - 40, itemHeight, 3, ColoringSystem.rgba(233, 233, 233, 55));
                    }

                    String songName = trimSongName( musicFiles.get(i).getName().replaceAll("\\.wav", ""), width - 80);
                    if (selectedMusicIndex != i) {
                        ClientFonts.clickGui[24].drawString(matrixStack, songName, listX + 5, itemY + 8, ColoringSystem.rgb(165, 165, 165));
                    }

                    if (selectedMusicIndex == i) {
                        ClientFonts.clickGui[18].drawString(matrixStack, "selected", listX + width - 80, itemY + 10, ColoringSystem.rgba(255, 255, 255, 255));
                        ClientFonts.clickGui[24].drawString(matrixStack, songName, listX + 5, itemY + 8, ColoringSystem.rgb(255, 255, 255));
                    }
                }
            }
            Scissor.unset();
            Scissor.pop();

            renderPlayerPanel(matrixStack, x, y, width, height);
        }

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    private void drawCategoryButton(MatrixStack matrixStack, float x, float y, String label, boolean selected) {
        int buttonColor = selected ? ColoringSystem.rgba(15, 15, 15, 215) : ColoringSystem.rgba(15, 15, 15, 215);


        GraphicsSystem.drawRoundedRect(x, y, Fonts.montserrat.getWidth(label, 10) + 22.5f, 20, 5, buttonColor);

        Fonts.montserrat.drawCenteredText(matrixStack, label, x + 45, y + 5, ColoringSystem.rgb(255, 255, 255), 10);
    }

    private String trimSongName(String name, float maxWidth) {

        float textWidth = Fonts.montserrat.getWidth(name, 10);

        if (textWidth > maxWidth) {

            while (textWidth > maxWidth && name.length() > 3) {

                name = name.substring(0, name.length() - 1);

                textWidth = Fonts.montserrat.getWidth(name + "...", 10);

            }

            name += "...";

        }

        return name;

    }

    private void renderPlayerPanel(MatrixStack matrixStack, float x, float y, float width, float height) {
        float panelHeight = 100;
        float panelY = y + height - panelHeight - 45;
        if (selectedMusicIndex != -1) {
            GraphicsSystem.drawRoundedRect(x + 10, panelY + 30, width - 20, panelHeight - 30, 2, ColoringSystem.rgba(20, 20, 20, 255));
            GraphicsSystem.drawRoundedRect(x + 10, panelY + 107, width - 20, 32, 2, ColoringSystem.rgba(20, 20, 20, 255));
            renderPlayer(matrixStack, x + 10, panelY, width - 20, panelHeight);
        }
    }

    private void renderPlayer(MatrixStack matrixStack, float x, float y, float width, float height) {
        String songName = trimSongName(musicFiles.get(selectedMusicIndex).getName().replaceAll("\\.wav", ""), width - 80);
        ClientFonts.clickGui[21].drawString(matrixStack, "Playing: " + songName, x + 10, y + 38, ColoringSystem.rgb(255, 255, 255));
        float buttonSize = 20;
        float buttonSpacing = 10;
        float prevButtonX = x + width / 2f - buttonSize - buttonSpacing - 5;
        float prevButtonY = y + height / 2f - buttonSize / 2f + 35;

        GraphicsSystem.drawRoundedRect(prevButtonX - 3, prevButtonY, buttonSize, buttonSize, 5, ColoringSystem.rgba(15, 15, 15, 215));

        ClientFonts.clickGui[21].drawCenteredString(matrixStack, "<", prevButtonX + buttonSize / 2f - 3, prevButtonY + 6, ColoringSystem.rgb(255, 255, 255));
        float playPauseButtonX = x + width / 2f - buttonSize / 2f;
        float playPauseButtonY = y + height / 2f - buttonSize / 2f + 35;
        GraphicsSystem.drawRoundedRect(isPlaying ? playPauseButtonX - 6 : playPauseButtonX - 2.5f, playPauseButtonY, isPlaying ? buttonSize + 12 : buttonSize + 4, buttonSize, 2, ColoringSystem.rgba(15, 15, 15, 255));


        GraphicsSystem.drawImage(isPlaying ? pause : play, playPauseButtonX + buttonSize / 2f - 4, playPauseButtonY + 5, 10, 10, ColoringSystem.rgb(255, 255, 255));
        ClientFonts.clickGui[20].drawCenteredString(matrixStack, isPlaying ? "Pause" : "Play", playPauseButtonX + buttonSize / 2f, playPauseButtonY + 6, ColoringSystem.rgb(255, 255, 255));

        float nextButtonX = x + width / 2f + buttonSpacing + 5;

        float nextButtonY = y + height / 2f - buttonSize / 2f + 35;

        GraphicsSystem.drawRoundedRect(nextButtonX +3 , nextButtonY, buttonSize, buttonSize, 5, ColoringSystem.rgba(15, 15, 15, 215));

        ClientFonts.clickGui[21].drawCenteredString(matrixStack, ">", nextButtonX + buttonSize / 2f + 3, nextButtonY + 6, ColoringSystem.rgb(255, 255, 255));

        float repeatButtonX = nextButtonX + buttonSize + buttonSpacing + 5;

        float repeatButtonY = nextButtonY;

        int repeatButtonColor = isRepeat ? ColoringSystem.rgba(255, 255, 255, 255) : ColoringSystem.rgba(15, 15, 15, 255);

        GraphicsSystem.drawRoundedRect(repeatButtonX, repeatButtonY, buttonSize, buttonSize, 5, repeatButtonColor);

        GraphicsSystem.drawImage(repeat, repeatButtonX + buttonSize / 2f - 7, repeatButtonY + 3, 14, 14, isRepeat ? ColoringSystem.rgba(1, 1, 1, 255) : ColoringSystem.rgba(255, 255, 255, 255));


        float closeButtonX = x + width / 2f - buttonSize / 2f - 60;

        GraphicsSystem.drawRoundedRect(closeButtonX, nextButtonY, buttonSize, buttonSize, 5, ColoringSystem.rgba(15, 15, 15, 215));

        Fonts.nurik.drawCenteredText(matrixStack, "U", closeButtonX + buttonSize / 2f - 1, repeatButtonY + 6f, ColoringSystem.rgb(255, 255, 255), 10);




        //VOLUME
        ClientFonts.clickGui[19].drawCenteredString(matrixStack, "Volume music", x + 41, y + 112, ColoringSystem.rgb(255, 255, 255));

        float volumeSliderX = x + 10;

        float volumeSliderY = y + height / 2f - 4 + 77;

        float volumeSliderWidth = width - 20 ;

        GraphicsSystem.drawRoundedRect(volumeSliderX, volumeSliderY, volumeSliderWidth, 10, 3, ColoringSystem.rgba(15, 15, 15, 255));

        GraphicsSystem.drawRoundedRect(volumeSliderX, volumeSliderY, volumeSliderWidth * volume, 10, 3, new Vector4i(
                ColoringSystem.setAlpha(Hud.getColor(1), 215),
                ColoringSystem.setAlpha(Hud.getColor(90), 215),
                ColoringSystem.setAlpha(Hud.getColor(180), 215),
                ColoringSystem.setAlpha(Hud.getColor(360), 215)));


        //TIME


        float timeSliderX = x + 10;

        float timeSliderY = y + height - 37;

        float timeSliderWidth = width - 20;



        if (clip != null) {
            long currentMicroseconds = clip.getMicrosecondPosition();
            long totalMicroseconds = clip.getMicrosecondLength();
            String currentTime = formatTime(currentMicroseconds);
            String totalTime = formatTime(totalMicroseconds);

            ClientFonts.clickGui[18].drawString(matrixStack, currentTime, timeSliderX, timeSliderY - 11, ColoringSystem.rgb(255, 255, 255));
            ClientFonts.clickGui[18].drawString(matrixStack, totalTime, timeSliderX + timeSliderWidth - ClientFonts.clickGui[18].getWidth(totalTime), timeSliderY - 11, ColoringSystem.rgb(255, 255, 255));

            GraphicsSystem.drawRoundedRect(timeSliderX, timeSliderY, timeSliderWidth, 8, 2f, ColoringSystem.rgba(15, 15, 15, 255));
            GraphicsSystem.drawRoundedRect(timeSliderX, timeSliderY, timeSliderWidth * getCurrentPlaybackPosition(), 8, 2f, new Vector4i(
                    ColoringSystem.setAlpha(Hud.getColor(1), 215),
                    ColoringSystem.setAlpha(Hud.getColor(90), 215),
                    ColoringSystem.setAlpha(Hud.getColor(180), 215),
                    ColoringSystem.setAlpha(Hud.getColor(360), 215)));
        }
    }


    private String formatTime(long microseconds) {
        int seconds = (int) (microseconds / 1_000_000) % 60;
        int minutes = (int) (microseconds / 60_000_000);
        return String.format("%02d:%02d", minutes, seconds);
    }


    public float getCurrentPlaybackPosition() {
        if (clip != null) {
            return clip.getMicrosecondPosition() / (float) clip.getMicrosecondLength();
        }
        return 0;
    }


    public String getCurrentSongName() {

        if (selectedMusicIndex != -1 && selectedMusicIndex < musicFiles.size()) {

            return musicFiles.get(selectedMusicIndex).getName().replace(".wav", "");

        }

        return "";

    }

    @Override

    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if (button == 0) {

            float windowWidth = mc.getMainWindow().getScaledWidth();

            float windowHeight = mc.getMainWindow().getScaledHeight();

            float width = 200;

            float height;
            if (selectedMusicIndex != -1) {
                height = 340;
            } else {
                height = 230;
            }

            float x = windowWidth / 2f - width / 2f;

            float y = windowHeight / 2f - height / 2f;

            float panelHeight = 100;

            float panelY = y + height - panelHeight - 45;

            float buttonSize = 20;

            float buttonSpacing = 10;

            float prevButtonX = x + width / 2f - buttonSize - buttonSpacing - 5;

            float prevButtonY = panelY + panelHeight / 2f - buttonSize / 2f + 35;

            if (MathSystem.isHovered((float) mouseX, (float) mouseY, prevButtonX, prevButtonY, buttonSize, buttonSize)) {

                selectPreviousMusic();

                return true;

            }

            float playPauseButtonX = x + width / 2f - buttonSize / 2f;

            float playPauseButtonY = panelY + panelHeight / 2f - buttonSize / 2f + 35;

            if (MathSystem.isHovered((float) mouseX, (float) mouseY, playPauseButtonX, playPauseButtonY, buttonSize, buttonSize)) {

                togglePlayPause();

                return true;

            }

            float nextButtonX = x + width / 2f + buttonSpacing + 5;

            float nextButtonY = panelY + panelHeight / 2f - buttonSize / 2f + 35;

            if (MathSystem.isHovered((float) mouseX, (float) mouseY, nextButtonX, nextButtonY, buttonSize, buttonSize)) {

                selectNextMusic();

                return true;

            }

            float repeatButtonX = nextButtonX + buttonSize + buttonSpacing + 5;

            float repeatButtonY = nextButtonY;

            if (MathSystem.isHovered((float) mouseX, (float) mouseY, repeatButtonX, repeatButtonY, buttonSize, buttonSize)) {

                isRepeat = !isRepeat;

                return true;

            }

            float volumeSliderX = x + 20;

            float volumeSliderY = panelY + panelHeight / 2f - 5 + 77;

            float volumeSliderWidth = width - 40;

            if (MathSystem.isHovered((float) mouseX, (float) mouseY, volumeSliderX, volumeSliderY, volumeSliderWidth, 10)) {

                volume = (float) ((mouseX - volumeSliderX) / volumeSliderWidth);

                setVolume(volume);

                return true;

            }

            float timeSliderX = x + 20;

            float timeSliderY = panelY + panelHeight - 37;

            float timeSliderWidth = width - 40;

            if (MathSystem.isHovered((float) mouseX, (float) mouseY, timeSliderX, timeSliderY, timeSliderWidth, 5)) {

                seek((float) ((mouseX - timeSliderX) / timeSliderWidth));

                return true;

            }

            float openFolderX = x + width - 30          + 1.5f;

            float openFolderY = y + 8;//10;

            float folderTextWidth = Fonts.montserrat.getWidth("Папка", 10);


            if (MathSystem.isHovered((float) mouseX, (float) mouseY, openFolderX, openFolderY, 20, 20)) {

                openMusicFolder();

                return true;

            }

            float listY = y + 40;
            float listHeight = 165;
            float listX = x + 20;

            int startIndex = scrollOffset;
            int endIndex = Math.min(startIndex + maxVisibleItems, musicFiles.size());

            for (int i = startIndex; i < endIndex; i++) {
                float itemY = listY + (i - startIndex) * itemHeight;

                if (itemY >= listY && itemY <= listY + listHeight) {
                    if (MathSystem.isHovered((float) mouseX, (float) mouseY, listX, itemY, width - 40, itemHeight)) {
                        selectedMusicIndex = i;
                        playSelectedMusic();
                        return true;
                    }
                }
            }
            float closeButtonX = x + width / 2f - buttonSize / 2f - 60;

            if (MathSystem.isHovered((float) mouseX, (float) mouseY, closeButtonX, prevButtonY, buttonSize, buttonSize)) {

                if (clip != null) {
                    clip.stop();
                    clip.close();
                    clip = null;
                }

                if (mp3Player != null) {
                    mp3Player.close();
                    mp3Player = null;
                }

                selectedMusicIndex = -1;

                return true;

            }


            for (int i = startIndex; i < endIndex; i++) {

                if (MathSystem.isHovered((float) mouseX, (float) mouseY, x + 20, listY + (i - startIndex) * itemHeight, width - 40, itemHeight) && scrollOffset > 0) {

                    selectedMusicIndex = i;

                    playSelectedMusic();

                    return true;

                }

            }

        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int maxOffset = Math.max(0, musicFiles.size() - maxVisibleItems);
        scrollOffset = Math.max(0, Math.min(scrollOffset - (int) delta, maxOffset));
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    public void handleMouseWheel(int delta) {
        int maxOffset = Math.max(0, musicFiles.size() - maxVisibleItems);
        scrollOffset = Math.max(0, Math.min(scrollOffset - delta * 3, maxOffset));
    }


    private void selectPreviousMusic() {

        if (musicFiles.isEmpty()) return;



        if (selectedMusicIndex > 0) {

            selectedMusicIndex--;

        } else {

            selectedMusicIndex = musicFiles.size() - 1;

        }

        playSelectedMusic();

    }

    private void selectNextMusic() {

        if (musicFiles.isEmpty()) return;



        if (selectedMusicIndex < musicFiles.size() - 1) {

            selectedMusicIndex++;

        } else {

            selectedMusicIndex = 0;

        }

        playSelectedMusic();

    }

    private void togglePlayPause() {

        if (clip == null) return;

        if (isPlaying) {

            clip.stop();

            isPlaying = false;

        } else {

            clip.setFramePosition(clip.getFramePosition());

            clip.start();

            isPlaying = true;

        }

    }

    private void openMusicFolder() {

        try {

            Runtime.getRuntime().exec("explorer " + musicDirectory.getAbsolutePath());

        } catch (IOException e) {

            e.printStackTrace();

        }

    }

    private void setVolume(float volume) {

        if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {

            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

            float dB = (float) (Math.log(volume) / Math.log(10) * 20);

            gainControl.setValue(dB);

        }

    }

    private void seek(float position) {

        if (clip != null) {

            clip.setMicrosecondPosition((long) (clip.getMicrosecondLength() * position));

        }

    }

    public boolean isVisible() {

        return this.minecraft.currentScreen == this;

    }

    public boolean isPlaying() {

        return isPlaying;

    }

    private void playSelectedMusic() {
        if (selectedMusicIndex == -1 || musicFiles.isEmpty()) return;

        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }

        if (mp3Player != null) {
            mp3Player.close();
            mp3Player = null;
        }

        File music = musicFiles.get(selectedMusicIndex);
        String fileName = music.getName().toLowerCase();

        try {
            if (fileName.endsWith(".wav")) {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(music);
                clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                setVolume(volume);
                isPlaying = true;
                clip.start();
            } else if (fileName.endsWith(".mp3")) {
                FileInputStream fis = new FileInputStream(music);
                BufferedInputStream bis = new BufferedInputStream(fis);
                mp3Player = new AdvancedPlayer(bis);
                new Thread(() -> {
                    try {
                        mp3Player.play();
                    } catch (JavaLayerException e) {
                        e.printStackTrace();
                    }
                }).start();
                isPlaying = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

}