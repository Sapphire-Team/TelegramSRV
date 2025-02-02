package net.weever.telegramSRV.api.modules.commands;

import net.weever.telegramSRV.api.modules.ITelegramCommand;
import net.weever.telegramSRV.util.ConfigUtil;

public class LanguageTelegramCommand implements ITelegramCommand {
    @Override
    public void onCommand(TelegramCommandImpl.ReplyToCommand replyToCommand) {
        if (replyToCommand.args().length > 1) {
            ConfigUtil.changeLanguage(replyToCommand.args()[1]);
            String text = ConfigUtil.getLocalizedText("telegramCommands", "language.replySuccessful")
                    .replace("%language%", replyToCommand.args()[1]);
            replyToCommand.reply(text);
        } else {
            replyToCommand.reply(ConfigUtil.getLocalizedText("telegramCommands", "language.replyFailed"));
        }
    }
}
