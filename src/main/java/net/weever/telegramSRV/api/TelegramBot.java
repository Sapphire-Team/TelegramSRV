package net.weever.telegramSRV.api;

import net.weever.telegramSRV.TelegramSRV;
import net.weever.telegramSRV.api.modules.commands.*;
import net.weever.telegramSRV.api.modules.events.ChatEvent;
import net.weever.telegramSRV.api.modules.events.TelegramEventImpl;
import org.jetbrains.annotations.Nullable;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.LinkPreviewOptions;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;

public class TelegramBot extends TelegramLongPollingBot {
    public TelegramBot() {
        super(TelegramSRV.config().getString("BOT_TOKEN"));
        registerCommands();
        registerEvents();
    }

    private void registerCommands() {
        TelegramCommandImpl.addCommand("start", new StartTelegramCommand());
        TelegramCommandImpl.addCommand("info", new InfoTelegramCommand());
        TelegramCommandImpl.addCommand("language", new LanguageTelegramCommand());
        TelegramCommandImpl.addCommand("thread", new ThreadTelegramCommand());
        TelegramCommandImpl.addCommand("help", new HelpTelegramCommand());
        TelegramCommandImpl.addCommand("reload", new ReloadTelegramCommand());
    }

    private void registerEvents() {
        TelegramEventImpl.addEvent(new ChatEvent());
    }
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getText().startsWith("/")) {
            processCommand(update);
        }
        TelegramEventImpl.getEvents().forEach(event -> event.onUpdateReceived(update));
    }

    private void processCommand(Update update) {
        Message message = update.getMessage();
        long userId = message.getFrom().getId();
        String[] args = message.getText().split(" ");
        String commandText = args[0].substring(1);

        TelegramCommandImpl.getCommands().forEach((commandName, command) -> {
            if ((commandText.equals(commandName) || commandText.startsWith(commandName)) && command.perform(commandName, userId)) {
                command.onCommand(new TelegramCommandImpl.ReplyToCommand(commandName, this, message, Arrays.copyOfRange(args, 1, args.length)));
            }
        });
    }
    @Override
    public String getBotUsername() {
        return TelegramSRV.getPlugin(TelegramSRV.class).getConfig().getString("BOT_NAME");
    }

    public void sendMessage(String text, String chatId, @Nullable String threadId, @Nullable Integer replyMessageId) {
        text = text.replace("\\n", "\n");
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setParseMode("HTML");
        LinkPreviewOptions linkPreviewOptions = new LinkPreviewOptions();
        linkPreviewOptions.setIsDisabled(true);
        message.setLinkPreviewOptions(linkPreviewOptions);
        if (threadId != null) {
            message.setMessageThreadId(Long.valueOf(threadId).intValue());
        }
        if (replyMessageId != null) {
            message.setReplyToMessageId(replyMessageId);
        }

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void replyMessage(String text, String chatId, Integer messageId) {
        sendMessage(text, chatId, null, messageId);
    }
}