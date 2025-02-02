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
import java.util.stream.Collectors;

import static net.weever.telegramSRV.TelegramSRV.config;

public class ConfigUtil {
    @Getter
    private static final Set<String> loadedLanguages = new HashSet<>(Arrays.asList("en", "ru"));
    private static final String LANG_DIR = TelegramSRV.plugin().getDataFolder() + "/lang";
    private static final String CONFIG_FILE = TelegramSRV.plugin().getDataFolder() + "/config.yml";
    private static final String OLD_CONFIG_FILE = TelegramSRV.plugin().getDataFolder() + "/config.old.yml";

    public static void copyDefaultTranslations() {
        File langFolder = new File(LANG_DIR);
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }
        for (String lang : loadedLanguages) {
            copyTranslationFile(lang, langFolder);
        }
        copyDefaultConfigFile(langFolder);
    }

    private static void copyTranslationFile(String lang, File langFolder) {
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

    private static void copyDefaultConfigFile(File langFolder) {
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
        File sourceFile = new File(LANG_DIR + "/" + language + ".yml");
        File destFile = new File(CONFIG_FILE);
        File backupFile = new File(OLD_CONFIG_FILE);

        if (sourceFile.exists() && destFile.exists()) {
            try {
                FileUtils.copyFile(destFile, backupFile);
                TelegramSRV.logger.info("Backup created: " + backupFile.getName());
                updateConfigFileWithLanguage(language, destFile);
                TelegramSRV.plugin().reloadConfig();
                TelegramSRV.logger.info("Language changed to: " + language.toUpperCase());
            } catch (IOException e) {
                TelegramSRV.logger.severe("Problem with changing language: " + e.getMessage());
            }
        } else {
            handleMissingLanguageFile(language, trying);
        }
    }

    private static void handleMissingLanguageFile(String language, boolean trying) {
        TelegramSRV.logger.warning(String.format("Language file not found: %s.yml", language));
        if (trying) return;
        TelegramSRV.logger.warning("Retrying...");
        copyDefaultTranslations();
        changeLanguage(language, true);
    }

    private static void updateConfigFileWithLanguage(String language, File destFile) throws IOException {
        Map<String, String> settingsMap = getSettingsMap();
        List<String> langLines = FileUtils.readLines(new File(LANG_DIR + "/" + language + ".yml"), StandardCharsets.UTF_8);
        FileUtils.writeLines(destFile, langLines);

        List<String> configLines = FileUtils.readLines(destFile, StandardCharsets.UTF_8);
        List<String> updatedConfigLines = updateConfigLinesWithSettings(configLines, settingsMap);
        FileUtils.writeLines(destFile, updatedConfigLines);
    }

    private static List<String> updateConfigLinesWithSettings(List<String> configLines, Map<String, String> settingsMap) {
        return configLines.stream()
                .map(line -> {
                    for (Map.Entry<String, String> entry : settingsMap.entrySet()) {
                        if (line.startsWith(entry.getKey()) && !line.contains(entry.getValue())) {
                            return entry.getKey() + ": " + entry.getValue();
                        }
                    }
                    return line;
                })
                .collect(Collectors.toList());
    }

    private static Map<String, String> getSettingsMap() {
        return new HashMap<>(Map.of(
                "BOT_TOKEN", Objects.requireNonNull(config().getString("BOT_TOKEN")),
                "BOT_NAME", Objects.requireNonNull(config().getString("BOT_NAME")),
                "ADMINS", Objects.requireNonNull(config().getString("ADMINS")),
                "SERVER_STATUS_CHAT_ID", Objects.requireNonNull(config().getString("SERVER_STATUS_CHAT_ID")),
                "SERVER_STATUS_THREAD_ID", Objects.requireNonNull(config().getString("SERVER_STATUS_THREAD_ID")),
                "PLAYER_STATUS_CHAT_ID", Objects.requireNonNull(config().getString("PLAYER_STATUS_CHAT_ID")),
                "PLAYER_STATUS_THREAD_ID", Objects.requireNonNull(config().getString("PLAYER_STATUS_THREAD_ID"))
        ));
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