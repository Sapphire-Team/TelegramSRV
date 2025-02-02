package net.weever.telegramSRV.api.modules.commands;

import lombok.Getter;
import net.weever.telegramSRV.api.TelegramBot;
import net.weever.telegramSRV.api.modules.ITelegramCommand;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.HashMap;
import java.util.Map;

public class TelegramCommandImpl implements ITelegramCommand {
    @Getter
    private static final Map<String, ITelegramCommand> commands = new HashMap<>();

    public static void addCommand(String commandName, ITelegramCommand command) {
        commands.put(commandName, command);
    }

    public static void removeCommand(String commandName) {
        commands.remove(commandName);
    }

    public record ReplyToCommand(String commandName, TelegramBot bot, Message message, String[] args) {

        public void send(String text) {
            bot.sendMessage(text, message.getChatId().toString(), null, null);
        }

        public void reply(String text) {
            bot.replyMessage(text, message.getChatId().toString(), message.getMessageId());
        }
    }
}