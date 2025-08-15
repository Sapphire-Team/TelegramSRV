package net.weever.telegramSRV.api.registrar;

import net.weever.telegramSRV.TelegramSRV;
import net.weever.telegramSRV.api.registrar.base.ICommandRegistrar;
import net.weever.telegramSRV.commands.legacy.LanguageCommandLegacy;
import org.bukkit.command.PluginCommand;

import java.util.logging.Level;

public class LegacyCommandRegistrar implements ICommandRegistrar {
    @Override
    public void registerCommands(TelegramSRV plugin) {
        plugin.getLogger().info("Using legacy command registration for Paper 1.20.x or older.");
        try {
            PluginCommand languageCommand = plugin.getCommand("tglanguage");
            if (languageCommand != null) {
                LanguageCommandLegacy executor = new LanguageCommandLegacy();
                languageCommand.setExecutor(executor);
                languageCommand.setTabCompleter(executor);
            } else {
                plugin.getLogger().severe("Command 'tglanguage' not found in plugin.yml!");
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to register command 'tglanguage' using legacy method.", e);
        }
    }
}
