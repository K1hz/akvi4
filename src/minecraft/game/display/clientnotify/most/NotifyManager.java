package minecraft.game.display.clientnotify.most;

import com.google.common.eventbus.Subscribe;
import minecraft.system.AG;
import minecraft.game.transactions.EventRender2D;
import minecraft.game.operation.misc.SelfDestruct;
import minecraft.game.display.clientnotify.elements.NoNotify;
import minecraft.game.display.clientnotify.elements.SuccessNotify;
import minecraft.game.display.clientnotify.elements.WarningNotify;
import minecraft.game.advantage.advisee.IMinecraft;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

public class NotifyManager extends ArrayList<Notify> implements IMinecraft {

    public void init() {
        AG.getInst().getEventBus().register(this);
    }

    public void register(final String content, final NotifyType type, long delay) {
        final Notify notification = switch (type) {
            case YES -> new SuccessNotify(content, delay);
            case NO -> new NoNotify(content, delay);
            case WARN -> new WarningNotify(content, delay);
        };

        this.add(notification);
    }
    public float animX = 500;
    @Subscribe
    public void onRender(EventRender2D e) {
        if (SelfDestruct.unhooked) return;
        if (!AG.getInst().getModuleManager().getHud().elements.is("Notification").getValue() || !AG.getInst().getModuleManager().getHud().isEnabled()) return;

        if (this.size() == 0 || mc.player == null || mc.world == null) return;
        int i = 0;
        Iterator<Notify> iterator = this.iterator();
        try {
            while (iterator.hasNext()) {
                Notify notification = iterator.next();
                notification.render(e.getMatrixStack(), i);
                if (notification.hasExpired()) {
                    iterator.remove();
                }
                i++;
            }
        } catch (ConcurrentModificationException ignored) {
        }

        if (this.size() > 16) {
            this.clear();
        }
    }
}
