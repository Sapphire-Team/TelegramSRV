package net.weever.telegramSRV.api.modules.commands;

import net.weever.telegramSRV.api.modules.ITelegramCommand;

public class StartTelegramCommand implements ITelegramCommand {
    @Override
    public void onCommand(TelegramCommandImpl.ReplyToCommand replyToCommand) {
        replyToCommand.reply("Bot done by: @Weever\nGithub: https://github.com/Sapphire-Team/TelegramSRV\n\nWith Love 💘");
    }
}
