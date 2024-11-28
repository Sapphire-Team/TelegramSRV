package net.weever.telegramSRV;

import net.weever.telegramSRV.api.TelegramBot;
import net.weever.telegramSRV.events.PlayerEvent;
import net.weever.telegramSRV.util.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.logging.Logger;

public final class TelegramSRV extends JavaPlugin {
    public static TelegramBot telegramBot;
    public static Logger logger;
    private static BotSession botSession;
    private static Plugin plugin;

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
            ConfigUtil.EventValue eventValue = ConfigUtil.getEventConfigValue(ConfigUtil.Events.SERVER);
            if (eventValue.isEnabled() && !eventValue.isNullChatId()) {
                String threadId = null;
                if (!eventValue.isNullThreadId()) {
                    threadId = eventValue.getThreadId();
                }
                telegramBot.sendMessage(ConfigUtil.getLocalizedText(ConfigUtil.Events.SERVER, "start"), eventValue.getChatId(), threadId, null);
            }
            return true;
        } catch (Exception e) {
            logger.severe("Failed to start Telegram bot: " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(plugin);
            if (botSession != null) {
                botSession.stop();
            }
            return false;
        }
    }

    @Override
    public void onEnable() {
        plugin = this;
        logger = getLogger();
        try {
            saveDefaultConfig();
            ConfigUtil.copyDefaultTranslations();
        } catch (Exception e) {
            logger.severe("Failed to load plugin: " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        if (startTelegramBot()) {
            Bukkit.getPluginManager().registerEvents(new PlayerEvent(), this);
//            try {
//                PluginCommand languageCommand = getCommand("tglanguage");
//                languageCommand.setExecutor(new LanguageCommand());
//                languageCommand.setTabCompleter(new LanguageTabCompletion());
//            } catch (NullPointerException e) {
//                logger.severe("Error with registering this command: " + e.getMessage());
//                Arrays.stream(e.getStackTrace()).forEach(line -> logger.severe(line.toString()));
//            }
        }
    }

    @Override
    public void onDisable() {
        try {
            if (botSession != null && botSession.isRunning()) {
                ConfigUtil.EventValue eventValue = ConfigUtil.getEventConfigValue(ConfigUtil.Events.SERVER);
                if (eventValue.isEnabled() && !eventValue.isNullChatId()) {
                    String threadId = null;
                    if (!eventValue.isNullThreadId()) {
                        threadId = eventValue.getThreadId();
                    }
                    telegramBot.sendMessage(ConfigUtil.getLocalizedText(ConfigUtil.Events.SERVER, "stop"), eventValue.getChatId(), threadId, null);
                }
                botSession.stop();
            }
        } catch (Exception e) {
            logger.severe("Failed to send \"Disable\" message: " + e.getMessage());
        }
    }
}
