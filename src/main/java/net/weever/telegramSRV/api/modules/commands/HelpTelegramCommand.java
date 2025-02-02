package net.weever.telegramSRV.api.modules.commands;

import net.weever.telegramSRV.api.modules.ITelegramCommand;
import net.weever.telegramSRV.util.ConfigUtil;

import java.util.Map;

public class HelpTelegramCommand implements ITelegramCommand {
    @Override
    public void onCommand(TelegramCommandImpl.ReplyToCommand sender) {
        StringBuilder text = new StringBuilder("Commands:\n");
        for (Map.Entry<String, ITelegramCommand> entry : TelegramCommandImpl.getCommands().entrySet()) {
            if (perform(entry.getKey(), sender.message().getFrom().getId())) {
                String commandInfo = "/" + entry.getKey() + " — " + ConfigUtil.getCommandDescription(entry.getKey()) + "\n";
                text.append(commandInfo);
            }
        }
        sender.reply(text.toString());
    }
}
