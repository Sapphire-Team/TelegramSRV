package net.weever.telegramSRV.api.modules;

import net.weever.telegramSRV.TelegramSRV;
import net.weever.telegramSRV.api.modules.commands.TelegramCommandImpl;
import net.weever.telegramSRV.api.modules.commands.TelegramCommandImpl.ReplyToCommand;

public interface ITelegramCommand {
    default void onCommand(ReplyToCommand sender) {
        sender.reply("Command do not have a handler. Please implement it.");
        TelegramCommandImpl.removeCommand(sender.commandName);
    }

    default boolean perform(String commandName, long userId) {
        boolean commandEnabled = TelegramSRV.config().getBoolean("commands." + commandName + ".ENABLED");
        boolean adminOnly = TelegramSRV.config().getBoolean("commands." + commandName + ".FOR_ADMINS");
        boolean isAdmin = TelegramSRV.config().getStringList("ADMINS").contains(String.valueOf(userId));

        return commandEnabled && (!adminOnly || isAdmin);
    }
}
