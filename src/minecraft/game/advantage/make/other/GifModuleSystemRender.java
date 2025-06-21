package minecraft.game.advantage.make.other;

public class GifModuleSystemRender {
    public int getFrame(int totalFrames, int frameDelay, boolean countFromZero) {
        long currentTime = System.currentTimeMillis();
        int frameIndex = (int) (currentTime / frameDelay % totalFrames);
        return countFromZero ? frameIndex : frameIndex + 1;
    }
}
