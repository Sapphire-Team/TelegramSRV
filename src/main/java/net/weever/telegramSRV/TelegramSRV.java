package net.weever.telegramSRV;

import net.weever.telegramSRV.api.TelegramBot;
//? if modern_commands
import net.weever.telegramSRV.api.registrar.ModernCommandRegistrar;
//? if !modern_commands
/*import net.weever.telegramSRV.api.registrar.LegacyCommandRegistrar;*/
import net.weever.telegramSRV.api.registrar.base.ICommandRegistrar;
import net.weever.telegramSRV.events.PlayerEvent;
import net.weever.telegramSRV.util.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class TelegramSRV extends JavaPlugin {
    public static TelegramBot telegramBot;
    public static Logger logger;
    private static BotSession botSession;
    private static Plugin plugin;
    private ICommandRegistrar commandRegistrar;

    public static FileConfiguration config() {
        return plugin.getConfig();
    }

    public static TelegramSRV plugin() {
        return TelegramSRV.getPlugin(TelegramSRV.class);
    }

    public static boolean startTelegramBot() {
        try {
            telegramBot = new TelegramBot();
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botSession = botsApi.registerBot(telegramBot);
            logger.info("Telegram SRV is started: " + telegramBot.getBotUsername());
            sendServerStatusMessage("start");
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to start Telegram bot: " + e.getMessage(), e);
            disablePlugin();
            stopBotSession();
            return false;
        }
    }

    private static void sendServerStatusMessage(String status) {
        ConfigUtil.EventValue eventValue = ConfigUtil.getEventConfigValue(ConfigUtil.Events.SERVER);
        if (eventValue.enabled() && !eventValue.isNullChatId()) {
            String threadId = eventValue.isNullThreadId() ? null : eventValue.threadId();
            telegramBot.sendMessage(ConfigUtil.getLocalizedText(ConfigUtil.Events.SERVER, status), eventValue.chatId(), threadId, null);
        }
    }

    private static void disablePlugin() {
        Bukkit.getPluginManager().disablePlugin(plugin);
    }

    private static void stopBotSession() {
        if (botSession != null) {
            try {
                new Thread(TelegramSRV::stopBotSession).start();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to stop Telegram bot session: " + e.getMessage(), e);
            }
        }
    }

    private void setupCommandRegistrar() {
        //? if modern_commands {
        this.commandRegistrar = new ModernCommandRegistrar();
        //?} else {
        /*this.commandRegistrar = new LegacyCommandRegistrar();
        *///?}
    }

    @Override
    public void onEnable() {
        plugin = this;
        logger = getLogger();
        setupCommandRegistrar();

        try {
            saveDefaultConfig();
            ConfigUtil.copyDefaultTranslations();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load plugin: " + e.getMessage(), e);
            disablePlugin();
            return;
        }
        if (startTelegramBot()) {
            Bukkit.getPluginManager().registerEvents(new PlayerEvent(), this);
            if (commandRegistrar != null) {
                commandRegistrar.registerCommands(this);
            } else {
                logger.severe("Could not initialize command registrar. Commands will not work.");
            }
        }
    }

    @Override
    public void onDisable() {
        try {
            if (botSession != null && botSession.isRunning()) {
                sendServerStatusMessage("stop");
                stopBotSession();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to send \"Disable\" message: " + e.getMessage(), e);
        }
    }
}