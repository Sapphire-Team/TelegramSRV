package net.weever.telegramSRV.api.modules.commands;

import net.weever.telegramSRV.TelegramSRV;
import net.weever.telegramSRV.api.modules.ITelegramCommand;
import net.weever.telegramSRV.util.ConfigUtil;

public class ReloadTelegramCommand implements ITelegramCommand {
    @Override
    public void onCommand(TelegramCommandImpl.ReplyToCommand replyToCommand) {
        TelegramSRV.plugin().reloadConfig();
        replyToCommand.reply(ConfigUtil.getLocalizedText("telegramCommands", "reload.reply"));
    }
}
