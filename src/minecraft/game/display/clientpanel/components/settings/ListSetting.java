package minecraft.game.display.clientpanel.components.settings;


import com.google.common.eventbus.Subscribe;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.massa.api.Setting;

import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class ListSetting<T> extends Setting<T> {
    public List<T> values;
    private T cachedValue;

    @SafeVarargs
    public ListSetting(Module parent, String name, T... values) {
        super(parent, name, values[0]);
        this.values = List.of(values);
        this.set(values[0]);
        cachedValue = values[0];
    }

    public int getIndex() {
        int index = 0;
        for (T value : values) {
            if (value.equals(getValue())) {
                return index;
            }
            index++;
        }
        return 0;
    }

    public boolean is(T value) {
        return getValue().equals(value);
    }

    @Subscribe
    public ListSetting<T> set(T value) {
        ListSetting<T> set = (ListSetting<T>) super.set(value);
        this.cachedValue = super.getValue();
        return set;
    }

    public ListSetting<T> setAsObject(final Object value) {
        ListSetting<T> set = (ListSetting<T>) super.set((T) value);
        this.cachedValue = super.getValue();
        return set;
    }

    @Subscribe
    public ListSetting<T> setVisible(Supplier<Boolean> value) {
        return (ListSetting<T>) super.visibleIf(value);
    }

    @Subscribe
    public ListSetting<T> onAction(Runnable action) {
        return (ListSetting<T>) super.onAction(() -> {
            action.run();
            this.cachedValue = super.getValue();
        });
    }

    @Subscribe
    public ListSetting<T> onSetVisible(Runnable action) {
        return (ListSetting<T>) super.onSetVisible(action);
    }

    @Override
    public T getValue() {
        if (cachedValue == null) {
            cachedValue = super.getValue();
        }
        return cachedValue;
    }
}