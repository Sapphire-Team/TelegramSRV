package net.weever.telegramSRV.api.modules;

import net.weever.telegramSRV.TelegramSRV;
import net.weever.telegramSRV.api.modules.commands.TelegramCommandImpl;
import net.weever.telegramSRV.api.modules.commands.TelegramCommandImpl.ReplyToCommand;

public interface ITelegramCommand {
    default void onCommand(ReplyToCommand sender) {
        sender.reply("Command do not have a handler. Please implement it.");
        TelegramCommandImpl.removeCommand(sender.commandName());
    }

    default boolean perform(String commandName, long userId) {
        return isCommandEnabled(commandName) && (!isAdminOnly(commandName) || isAdmin(userId));
    }

    private boolean isCommandEnabled(String commandName){
        return TelegramSRV.config().getBoolean("commands." + commandName + ".ENABLED");
    }

    private boolean isAdminOnly(String commandName){
        return TelegramSRV.config().getBoolean("commands." + commandName + ".FOR_ADMINS");
    }

    private boolean isAdmin(long userId){
        return TelegramSRV.config().getStringList("ADMINS").contains(String.valueOf(userId));
    }
}