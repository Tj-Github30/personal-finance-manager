package com.yourorg.finance.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EventBus {
    private static final EventBus INSTANCE = new EventBus();
    private final List<Consumer<String>> listeners = new ArrayList<>();

    private EventBus() {}

    public static EventBus get() {
        return INSTANCE;
    }

    /** topic could be "transactions:changed", etc. */
    public void subscribe(Consumer<String> listener) {
        listeners.add(listener);
    }

    public void publish(String topic) {
        for (var l : listeners) {
            try { l.accept(topic); }
            catch (Exception e) { e.printStackTrace(); }
        }
    }
}