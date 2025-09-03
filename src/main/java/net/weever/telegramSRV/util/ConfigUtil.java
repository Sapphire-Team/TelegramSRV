package net.weever.telegramSRV.util;

import lombok.Getter;
import net.weever.telegramSRV.TelegramSRV;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static net.weever.telegramSRV.TelegramSRV.config;

public class ConfigUtil {
    @Getter
    private static final Set<String> loadedLanguages = Set.of("en", "ru", "ua");
    private static final String langDirPath = "/lang";
    private static final String configFileName = "config.yml";
    private static final String oldConfigFileName = "config.old.yml";
    private static final String langFileExtension = ".yml";

    private static final File dataFolder = TelegramSRV.plugin().getDataFolder();
    private static final String langFile = dataFolder + langDirPath;
    private static final String configFile = dataFolder + "/" + configFileName;
    private static final String oldConfigFile = dataFolder + "/" + oldConfigFileName;

    public static void copyDefaultTranslations() {
        var langFolder = new File(langFile);
        if (!langFolder.exists()) {
            langFolder.mkdirs();
            TelegramSRV.logger.info("Created language directory: " + langFolder.getAbsolutePath());
        }
        for (var lang : loadedLanguages) {
            copyTranslationFile(lang, langFolder);
        }
        copyDefaultConfigFile(langFolder);
    }

    private static void copyTranslationFile(String lang, File langFolder) {
        var resourcePath = "lang/" + lang + langFileExtension;
        var inputStream = TelegramSRV.plugin().getResource(resourcePath);
        if (inputStream != null) {
            var translationFile = new File(langFolder, lang + langFileExtension);
            try (var outputStream = new FileOutputStream(translationFile)) {
                inputStream.transferTo(outputStream);
                TelegramSRV.logger.info("Copied translation file: " + translationFile.getName());
            } catch (IOException e) {
                TelegramSRV.logger.severe("Error copying translation file for language '" + lang + "': " + e.getMessage());
            }
        } else {
            TelegramSRV.logger.warning("Translation file for language '" + lang + "' not found in resources.");
        }
    }

    private static void copyDefaultConfigFile(File langFolder) {
        var destFile = new File(configFile);
        if (!destFile.exists() || config().getKeys(false).isEmpty()) {
            try {
                Files.copy(new File(langFolder, "en" + langFileExtension).toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                TelegramSRV.logger.info("Copied default config file: " + destFile.getName());
            } catch (IOException e) {
                TelegramSRV.logger.severe("Error copying en.yml to config.yml: " + e.getMessage());
                Bukkit.getPluginManager().disablePlugin(TelegramSRV.plugin());
            }
        }
    }

    public static void changeLanguage(String language) {
        var sourceFile = new File(langFile + "/" + language + langFileExtension);
        var destFile = new File(configFile);
        var backupFile = new File(oldConfigFile);

        if (sourceFile.exists() && destFile.exists()) {
            try {
                Files.copy(destFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                TelegramSRV.logger.info("Backup created: " + backupFile.getName());
                updateConfigFileWithLanguage(language, sourceFile, destFile);
                TelegramSRV.plugin().reloadConfig();
                TelegramSRV.logger.info("Language changed to: " + language.toUpperCase());
            } catch (IOException e) {
                TelegramSRV.logger.severe("Problem with changing language: " + e.getMessage());
            }
        } else {
            TelegramSRV.logger.warning(String.format("Language file not found: %s%s", language, langFileExtension));
            TelegramSRV.logger.info("Attempting to recover by copying default translations...");
            copyDefaultTranslations();
            if (sourceFile.exists()) {
                TelegramSRV.logger.info("Retry successful, changing language...");
                changeLanguage(language);
            } else {
                TelegramSRV.logger.severe("Failed to change language after retry.");
            }
        }
    }

    private static void updateConfigFileWithLanguage(String language, File langFile, File destFile) throws IOException {
        var settingsMap = getSettingsMap();
        YamlConfiguration newConfig = YamlConfiguration.loadConfiguration(langFile);
        for (Map.Entry<String, Object> entry : settingsMap.entrySet()) {
            newConfig.set(entry.getKey(), entry.getValue());
        }
        newConfig.save(destFile);
    }

    private static Map<String, Object> getSettingsMap() {
        var map = new HashMap<String, Object>();
        String[] keys = {
                "BOT_TOKEN", "BOT_NAME", "ADMINS",
                "forwarding.fromTelegramToMinecraft.enabled", "forwarding.fromTelegramToMinecraft.requirePrefix", "forwarding.fromTelegramToMinecraft.prefix",
                "forwarding.fromMinecraftToTelegram.enabled", "forwarding.fromMinecraftToTelegram.requirePrefix", "forwarding.fromMinecraftToTelegram.prefix",
                "SERVER_STATUS", "SERVER_STATUS_CHAT_ID", "SERVER_STATUS_THREAD_ID",
                "PLAYER_STATUS", "PLAYER_STATUS_CHAT_ID", "PLAYER_STATUS_THREAD_ID"
        };

        try {
            for (var key : keys) {
                var value = config().get(key);
                if (value != null) {
                    map.put(key, value);
                }
            }
        } catch (Exception e) {
            TelegramSRV.logger.severe("Error getting settings map: " + e.getMessage());
            return Collections.emptyMap();
        }
        return map;
    }

    public static void changeThreadId(Events event, String threadId) {
        config().set(event.name().toUpperCase() + "_STATUS_THREAD_ID", Integer.valueOf(threadId));
        TelegramSRV.plugin().saveConfig();
    }

    public static String getCommandDescription(String commandName) {
        return config().getString("commands." + commandName.toLowerCase() + ".DESCRIPTION");
    }

    public static EventValue getEventConfigValue(Events event) {
        var status = config().getBoolean(event.name() + "_STATUS");
        var chatId = config().getString(event.name() + "_STATUS_CHAT_ID");
        var threadId = config().getString(event.name() + "_STATUS_THREAD_ID");
        return new EventValue(status, chatId, threadId);
    }

    private static String getForwardingPath(boolean fromTelegramToMinecraft) {
        return fromTelegramToMinecraft ? "forwarding.fromTelegramToMinecraft" : "forwarding.fromMinecraftToTelegram";
    }

    public static boolean isForwardingEnabled(boolean fromTelegramToMinecraft) {
        return config().getBoolean(getForwardingPath(fromTelegramToMinecraft) + ".enabled", true);
    }

    public static boolean isPrefixRequired(boolean fromTelegramToMinecraft) {
        return config().getBoolean(getForwardingPath(fromTelegramToMinecraft) + ".requirePrefix", false);
    }

    public static String getPrefix(boolean fromTelegramToMinecraft) {
        return config().getString(getForwardingPath(fromTelegramToMinecraft) + ".prefix", fromTelegramToMinecraft ? "!" : "/tg");
    }

    public static String getLocalizedText(String name, String key) {
        var answer = config().getString("text." + name + "." + key);
        return answer == null ? "unknown" : answer;
    }

    public static String getLocalizedText(Events event, String key) {
        return getLocalizedText(event.name().toLowerCase(), key);
    }

    public enum Events {
        PLAYER, SERVER, CONSOLE
    }

    public record EventValue(boolean enabled, String chatId, String threadId) {
        public boolean isNullThreadId() {
            return threadId == null || threadId.isEmpty() || threadId.equals("YOUR_THREAD_ID");
        }

        public boolean isNullChatId() {
            return chatId == null || chatId.isEmpty() || chatId.equals("YOUR_CHAT_ID");
        }
    }
}