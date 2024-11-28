package net.weever.telegramSRV.api.modules.commands;

import net.weever.telegramSRV.api.modules.ITelegramCommand;
import net.weever.telegramSRV.util.ConfigUtil;
import org.bukkit.Bukkit;

public class InfoTelegramCommand implements ITelegramCommand {
    @Override
    public void onCommand(TelegramCommandImpl.ReplyToCommand replyToCommand) {
        String text = ConfigUtil.getLocalizedText("telegramCommands", "info.reply")
                .replace("%tps%", String.valueOf((int) Bukkit.getTPS()[0]))
                .replace("%playersCount%", String.valueOf(Bukkit.getOnlinePlayers().size()));
        replyToCommand.reply(text);
    }
}
