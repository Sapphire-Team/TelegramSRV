package net.weever.telegramSRV.api.modules.commands;

import net.weever.telegramSRV.api.modules.ITelegramCommand;
import net.weever.telegramSRV.util.ConfigUtil;

public class ThreadTelegramCommand implements ITelegramCommand {
    @Override
    public void onCommand(TelegramCommandImpl.ReplyToCommand replyToCommand) {
        String threadId = String.valueOf(replyToCommand.message().getMessageThreadId());
        if ("null".equals(threadId)) {
            replyToCommand.reply(ConfigUtil.getLocalizedText("telegramCommands", "thread.notFound"));
            return;
        }

        if (replyToCommand.args().length <= 1) {
            replyToCommand.reply(ConfigUtil.getLocalizedText("telegramCommands", "thread.reply").replace("%threadId%", threadId));
            return;
        }

        if ("set".equals(replyToCommand.args()[1]) && replyToCommand.args().length > 2) {
            try {
                ConfigUtil.Events eventEnum = ConfigUtil.Events.valueOf(replyToCommand.args()[2].toUpperCase());
                ConfigUtil.changeThreadId(eventEnum, threadId);
                String text = ConfigUtil.getLocalizedText("telegramCommands", "thread.set")
                        .replace("%eventName%", eventEnum.name())
                        .replace("%threadId%", threadId);
                replyToCommand.reply(text);
            } catch (IllegalArgumentException e) {
                replyToCommand.reply(ConfigUtil.getLocalizedText("telegramCommands", "thread.setError"));
            }
        } else {
            replyToCommand.reply(ConfigUtil.getLocalizedText("telegramCommands", "thread.setError"));
        }
    }
}