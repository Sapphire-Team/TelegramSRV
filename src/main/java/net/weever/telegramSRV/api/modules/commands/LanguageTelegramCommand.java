package net.weever.telegramSRV.api.modules.commands;

import net.weever.telegramSRV.api.modules.ITelegramCommand;
import net.weever.telegramSRV.util.ConfigUtil;

import java.util.Arrays;

public class LanguageTelegramCommand implements ITelegramCommand
{
    @Override
    public void onCommand(TelegramCommandImpl.ReplyToCommand replyToCommand)
    {
        System.out.println(Arrays.toString(replyToCommand.args()));
        System.out.println(replyToCommand.args().length);
        System.out.println(Arrays.stream(replyToCommand.args()).count());
        if (replyToCommand.args().length == 1)
        {
            ConfigUtil.changeLanguage(replyToCommand.args()[0]);
            String text = ConfigUtil.getLocalizedText("telegramCommands", "language.replySuccessful").replace("%language%", replyToCommand.args()[0]);
            replyToCommand.reply(text);
        }
        else
        {
            replyToCommand.reply(ConfigUtil.getLocalizedText("telegramCommands", "language.replyFailed"));
        }
    }
}
