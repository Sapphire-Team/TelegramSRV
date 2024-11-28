package net.weever.telegramSRV.api.modules;

import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface ITelegramEvent {
    default void onUpdateReceived(@NotNull Update update) {
    }
}
