package net.weever.telegramSRV.api.modules.events;

import lombok.Getter;
import net.weever.telegramSRV.api.modules.ITelegramEvent;

import java.util.ArrayList;
import java.util.List;

public class TelegramEventImpl implements ITelegramEvent {
    @Getter
    private static List<ITelegramEvent> events = new ArrayList<>();

    public static void addEvent(ITelegramEvent event) {
        events.add(event);
    }

    public static void removeEvent(ITelegramEvent event) {
        if (!events.contains(event)) return;
        events.remove(event);
    }
}
