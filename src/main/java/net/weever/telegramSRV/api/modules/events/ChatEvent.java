package net.weever.telegramSRV.api.modules.events;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
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

public class ChatEvent implements ITelegramEvent {
    @Override
    public void onUpdateReceived(@NotNull Update update) {
        if (!isValidMessage(update)) {
            return;
        }

        if (!ConfigUtil.isForwardingEnabled(true)) {
            return;
        }

        Message message = update.getMessage();
        String text = message.getText();

        if (ConfigUtil.isPrefixRequired(true)) {
            String prefix = ConfigUtil.getPrefix(true);
            if (text.startsWith(prefix)) {
                text = text.substring(prefix.length()).trim();
                if (text.isEmpty()) {
                    return;
                }
            } else {
                return;
            }
        }

        String chatId = String.valueOf(message.getChatId());
        String threadId = message.isTopicMessage() ? String.valueOf(message.getMessageThreadId()) : null;

        if (isConfiguredChat(chatId, threadId)) {
            String playerNick = formatUserName(message);
            TextComponent finalComponent = new TextComponent();

            boolean prefixEnabled = TelegramSRV.config().getBoolean("text.minecraft.player.messageEnablePrefix", true);
            String rawPrefix = ConfigUtil.getLocalizedText("minecraft", "player.messagePrefix");

            if (prefixEnabled && !rawPrefix.isEmpty()) {
                String formattedPrefix = ChatColor.translateAlternateColorCodes('&', rawPrefix);
                BaseComponent[] prefixComponents = TextComponent.fromLegacyText(formattedPrefix);

                HoverEvent hoverEvent = null;
                boolean underline = false;
                if (message.getReplyToMessage() != null) {
                    if (TelegramSRV.config().getBoolean("text.minecraft.player.messageShowReplyInPrefix")) {
                        Message replyMessage = message.getReplyToMessage();
                        hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(formatUserName(replyMessage) + ": " + replyMessage.getText()));
                        if (TelegramSRV.config().getBoolean("text.minecraft.player.messageAddUnderlineIfHaveAReplyInPrefix")) {
                            underline = true;
                        }
                    }
                }

                for (BaseComponent part : prefixComponents) {
                    if (hoverEvent != null) {
                        part.setHoverEvent(hoverEvent);
                    }
                    if (underline) {
                        part.setUnderlined(true);
                    }
                    finalComponent.addExtra(part);
                }
            }

            String messageFormat = " " + ConfigUtil.getLocalizedText("minecraft", "player.message");
            String messageWithContent = messageFormat.replace("%playerName%", playerNick).replace("%message%", text);
            String formattedMessage = ChatColor.translateAlternateColorCodes('&', messageWithContent);
            BaseComponent[] messageComponents = TextComponent.fromLegacyText(formattedMessage);

            for(BaseComponent part : messageComponents) {
                finalComponent.addExtra(part);
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.spigot().sendMessage(finalComponent);
            }
        }
    }

    private boolean isValidMessage(Update update) {
        if (!update.hasMessage()) {
            return false;
        }
        Message message = update.getMessage();
        return message.hasText() && !message.getText().startsWith("/") && !message.getFrom().getIsBot() && TelegramSRV.config().getBoolean("PLAYER_STATUS");
    }

    private boolean isConfiguredChat(String chatId, String threadId) {
        ConfigUtil.EventValue playerConfig = ConfigUtil.getEventConfigValue(ConfigUtil.Events.PLAYER);

        if (playerConfig.isNullChatId() || !playerConfig.chatId().equals(chatId)) {
            return false;
        }

        if (threadId != null) {
            return !playerConfig.isNullThreadId() && playerConfig.threadId().equals(threadId);
        }

        return true;
    }

    private @NotNull String formatUserName(Message message) {
        String username = message.getFrom().getUserName();
        if (username == null || username.isEmpty()) {
            String firstName = message.getFrom().getFirstName() != null ? message.getFrom().getFirstName() : "";
            String lastName = message.getFrom().getLastName() != null ? " " + message.getFrom().getLastName() : "";
            return (firstName + lastName).trim();
        }
        return "@" + username;
    }
}