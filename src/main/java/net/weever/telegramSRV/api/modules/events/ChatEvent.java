package net.weever.telegramSRV.api.modules.events;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.weever.telegramSRV.TelegramSRV;
import net.weever.telegramSRV.api.modules.ITelegramEvent;
import net.weever.telegramSRV.util.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Objects;

public class ChatEvent implements ITelegramEvent {
    @Override
    public void onUpdateReceived(@NotNull Update update) {
        if (!isValidMessage(update) || !ConfigUtil.getEnabledOrNot(true)) return;

        Message message = update.getMessage();
        String chatId = String.valueOf(message.getChatId());
        String threadId = String.valueOf(message.getMessageThreadId());

        if (isConfiguredChat(chatId, threadId)) {
            String playerNick = formatUserName(message);
            String text = message.getText();
            boolean nullPrefix = Objects.equals(ConfigUtil.getLocalizedText("minecraft", "player.messagePrefix"), "");
            TextComponent component = new TextComponent();
            TextComponent telegramTag = new TextComponent(ConfigUtil.getLocalizedText("minecraft", "player.messagePrefix"));
            TextComponent messageComponent = new TextComponent(" " + ConfigUtil.getLocalizedText("minecraft", "player.message")
                    .replace("%playerName%", playerNick)
                    .replace("%message%", text));

            if (message.getReplyToMessage() != null) {
                if (TelegramSRV.config().getBoolean("text.minecraft.player.messageShowReplyInPrefix")) {
                    Message replyMessage = message.getReplyToMessage();
                    telegramTag.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(
                            net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
                            new Text(formatUserName(replyMessage) + ": " + replyMessage.getText())
                    ));
                    if (TelegramSRV.config().getBoolean("text.minecraft.player.messageAddUnderlineIfHaveAReplyInPrefix")) {
                        telegramTag.setUnderlined(true);
                    }
                }
            }
            if (!nullPrefix && TelegramSRV.config().getBoolean("text.minecraft.player.messageEnablePrefix")) {
                component.addExtra(telegramTag);
            }
            component.addExtra(messageComponent);

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.spigot().sendMessage(component);
            }
        }
    }

    private boolean isValidMessage(Update update) {
        if (!update.hasMessage()) return false;
        Message message = update.getMessage();
        return message.hasText() &&
                !message.getText().startsWith("/") &&
                !message.getFrom().getIsBot() &&
                TelegramSRV.config().getBoolean("PLAYER_STATUS");
    }

    private boolean isConfiguredChat(String chatId, String threadId) {
        if (threadId != null && !threadId.isEmpty() && !threadId.contains("null")) {
            return Objects.equals(TelegramSRV.config().getString("PLAYER_STATUS_THREAD_ID"), threadId) && Objects.equals(TelegramSRV.config().getString("PLAYER_STATUS_CHAT_ID"), chatId);
        }
        return Objects.equals(TelegramSRV.config().getString("PLAYER_STATUS_CHAT_ID"), chatId);
    }

    private @NotNull String formatUserName(Message message) {
        String username = message.getFrom().getUserName();
        if (username == null) {
            String firstName = message.getFrom().getFirstName();
            String lastName = message.getFrom().getLastName();
            username = (firstName != null ? firstName : "") + (lastName != null ? " " + lastName : "");
        }
        return username != null ? "@" + username : "Unknown";
    }
}