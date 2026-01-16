//? if modern_commands {
package net.weever.telegramSRV.api.registrar;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.weever.telegramSRV.TelegramSRV;
import net.weever.telegramSRV.api.registrar.base.ICommandRegistrar;
import net.weever.telegramSRV.commands.modern.LanguageCommandModern;

import java.util.logging.Level;

public class ModernCommandRegistrar implements ICommandRegistrar {
    @Override
    public void registerCommands(TelegramSRV plugin) {
        plugin.getLogger().info("Using modern command registration for Paper 1.21+.");
        try {
            plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
                event.registrar().register("tglanguage", new LanguageCommandModern());
            });
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to register command 'tglanguage' using modern method.", e);
        }
    }
}
//?}