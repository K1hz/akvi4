package minecraft.game.transactions;

public class CancelEvent {

    private boolean isCancel;

    public void cancel() {
        isCancel = true;
    }
    public void open() {
        isCancel = false;
    }
    public boolean isCancel() {
        return isCancel;
    }

}
