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

import java.util.logging.Level;
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
        if (eventValue.isEnabled() && !eventValue.isNullChatId()) {
            String threadId = eventValue.isNullThreadId() ? null : eventValue.getThreadId();
            telegramBot.sendMessage(ConfigUtil.getLocalizedText(ConfigUtil.Events.SERVER, status), eventValue.getChatId(), threadId, null);
        }
    }

    private static void disablePlugin() {
        Bukkit.getPluginManager().disablePlugin(plugin);
    }

    private static void stopBotSession() {
        if (botSession != null) {
            try {
                botSession.stop();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to stop Telegram bot session: " + e.getMessage(), e);
            }
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
            logger.log(Level.SEVERE, "Failed to load plugin: " + e.getMessage(), e);
            disablePlugin();
            return;
        }
        if (startTelegramBot()) {
            Bukkit.getPluginManager().registerEvents(new PlayerEvent(), this);
        }
    }

    @Override
    public void onDisable() {
        try {
            if (botSession != null && botSession.isRunning()) {
                sendServerStatusMessage("stop");
                botSession.stop();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to send \"Disable\" message: " + e.getMessage(), e);
        }
    }
}