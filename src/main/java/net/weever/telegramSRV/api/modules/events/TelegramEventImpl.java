package net.weever.telegramSRV.api.modules.events;

import lombok.Getter;
import net.weever.telegramSRV.api.modules.ITelegramEvent;

import java.util.ArrayList;
import java.util.List;

public class TelegramEventImpl {
    @Getter
    private static final List<ITelegramEvent> events = new ArrayList<>();

    public static void addEvent(ITelegramEvent event) {
        events.add(event);
    }

    public static void removeEvent(ITelegramEvent event) {
        events.remove(event);
    }
}