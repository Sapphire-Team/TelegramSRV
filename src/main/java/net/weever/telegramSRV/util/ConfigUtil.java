package net.weever.telegramSRV.util;

import lombok.Getter;
import net.weever.telegramSRV.TelegramSRV;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static net.weever.telegramSRV.TelegramSRV.config;

public class ConfigUtil {
    @Getter
    private static final Set<String> loadedLanguages = new HashSet<>(Arrays.asList("en", "ru"));
    private static final String LANG_DIR = String.valueOf(TelegramSRV.plugin().getDataFolder());
    private static final String CONFIG_FILE = TelegramSRV.plugin().getDataFolder() + "/config.yml";
    private static final String OLD_CONFIG_FILE = TelegramSRV.plugin().getDataFolder() + "/config.old.yml";

    public static void copyDefaultTranslations() {
        File translationsFolder = new File(LANG_DIR);
        File langFolder = new File(translationsFolder, "lang");
        if (!langFolder.exists()) {
            langFolder.mkdir();
        }
        for (String lang : loadedLanguages) {
            String resourcePath = "lang/" + lang + ".yml";
            InputStream inputStream = TelegramSRV.plugin().getResource(resourcePath);
            if (inputStream != null) {
                File translationFile = new File(langFolder, lang + ".yml");
                try (FileOutputStream outputStream = new FileOutputStream(translationFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    TelegramSRV.logger.info("Copied translation file: " + translationFile.getName());
                } catch (IOException e) {
                    TelegramSRV.logger.severe("Error copying translation file for language '" + lang + "': " + e.getMessage());
                }
            } else {
                TelegramSRV.logger.warning("Translation file for language '" + lang + "' not found in resources.");
            }
        }
        File destFile = new File(CONFIG_FILE);
        if (!destFile.exists() || config().getKeys(false).isEmpty()) {
            try {
                FileUtils.copyFile(new File(langFolder, "en.yml"), destFile);
            } catch (IOException e) {
                TelegramSRV.logger.severe("Error copying en.yml to config.yml: " + e.getMessage());
                Bukkit.getPluginManager().disablePlugin(TelegramSRV.plugin());
            }
        }
    }


    public static void changeLanguage(String language, boolean trying) {
        File sourceFile = new File(LANG_DIR + "/lang/" + language + ".yml");
        File destFile = new File(CONFIG_FILE);
        File backupFile = new File(OLD_CONFIG_FILE);

        if (sourceFile.exists() && destFile.exists()) {
            try {
                FileUtils.copyFile(destFile, backupFile);
                TelegramSRV.logger.info("Backup created: " + backupFile.getName());

                Map<String, String> settingsMap = new HashMap<>(Map.of(
                        "BOT_TOKEN", config().getString("BOT_TOKEN"),
                        "BOT_NAME", config().getString("BOT_NAME"),
                        "ADMINS", config().getString("ADMINS"),
                        "SERVER_STATUS_CHAT_ID", config().getString("SERVER_STATUS_CHAT_ID"),
                        "SERVER_STATUS_THREAD_ID", config().getString("SERVER_STATUS_THREAD_ID"),
                        "PLAYER_STATUS_CHAT_ID", config().getString("PLAYER_STATUS_CHAT_ID"),
                        "PLAYER_STATUS_THREAD_ID", config().getString("PLAYER_STATUS_THREAD_ID")
//                        "CONSOLE_CHAT_ID", config().getString("CONSOLE_CHAT_ID"),
//                        "CONSOLE_STATUS_THREAD_ID", config().getString("CONSOLE_STATUS_THREAD_ID")
                ));

                List<String> langLines = FileUtils.readLines(sourceFile, StandardCharsets.UTF_8);
                FileUtils.writeLines(destFile, langLines);

                List<String> configLines = FileUtils.readLines(destFile, StandardCharsets.UTF_8);
                for (int i = 0; i < configLines.size(); i++) {
                    for (Map.Entry<String, String> entry : settingsMap.entrySet()) {
                        if (configLines.get(i).startsWith(entry.getKey()) && !configLines.get(i).contains(entry.getValue())) {
                            configLines.set(i, entry.getKey() + ": " + entry.getValue());
                        }
                    }
                }
                FileUtils.writeLines(destFile, configLines);
                TelegramSRV.plugin().reloadConfig();
                TelegramSRV.logger.info("Language changed to: " + language.toUpperCase());
            } catch (IOException e) {
                TelegramSRV.logger.severe("Problem with changing language: " + e.getMessage());
            }
        } else {
            TelegramSRV.logger.warning(String.format("Language file not found: %s.yml", language));
            if (trying) return;
            TelegramSRV.logger.warning("Retrying...");
            copyDefaultTranslations();
            changeLanguage(language, true);
        }
    }

    public static void changeLanguage(String language) {
        changeLanguage(language, false);
    }

    public static void changeThreadId(Events event, String threadId) {
        config().set(event.name().toUpperCase() + "_STATUS_THREAD_ID", Integer.valueOf(threadId));
        TelegramSRV.plugin().saveConfig();
    }

    public static String getCommandDescription(String commandName) {
        return config().getString("commands." + commandName.toLowerCase() + ".DESCRIPTION");
    }

    public static EventValue getEventConfigValue(Events event) {
        boolean status = config().getBoolean(event.name() + "_STATUS");
        String chatId = config().getString(event.name() + "_STATUS_CHAT_ID");
        String threadId = config().getString(event.name() + "_STATUS_THREAD_ID");
        return new EventValue(status, chatId, threadId);
    }

    public static boolean getEnabledOrNot(boolean aboutFromTelegramToMinecraft) {
        return aboutFromTelegramToMinecraft ? config().getBoolean("fromTelegramToMinecraft") : config().getBoolean("fromMinecraftToTelegram");
    }

    public static String getLocalizedText(String name, String key) {
        return config().getString("text." + name + "." + key);
    }

    public static String getLocalizedText(Events event, String key) {
        return getLocalizedText(event.name().toLowerCase(), key);
    }

    public enum Events {
        PLAYER,
        SERVER,
        CONSOLE
    }

    @Getter
    public static class EventValue {
        private final boolean enabled;
        private final String chatId;
        private final String threadId;

        public EventValue(boolean enabled, String chatId, String threadId) {
            this.enabled = enabled;
            this.chatId = chatId;
            this.threadId = threadId;
        }

        public boolean isNullThreadId() {
            return threadId == null || threadId.isEmpty() || threadId.equals("YOUR_THREAD_ID");
        }

        public boolean isNullChatId() {
            return chatId == null || chatId.isEmpty() || chatId.equals("YOUR_CHAT_ID");
        }
    }
}
