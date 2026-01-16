package net.weever.telegramSRV.events;

//? if modern_chat_event {
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
//?} else {
/*import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.md_5.bungee.chat.TranslationRegistry;
import org.bukkit.event.player.AsyncPlayerChatEvent;
*///?}
import net.weever.telegramSRV.TelegramSRV;
import net.weever.telegramSRV.util.ConfigUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerEvent implements Listener {
    private static void sendMessageToTelegram(String text, ConfigUtil.EventValue eventValue) {
        if (text.isEmpty()) {
            return;
        }

        if (eventValue.enabled() && !eventValue.isNullChatId()) {
            String threadId = eventValue.isNullThreadId() ? null : eventValue.threadId();
            TelegramSRV.telegramBot.sendMessage(text, eventValue.chatId(), threadId, null);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    //? if modern_chat_event {
    public void onChat(AsyncChatEvent event) {
    //?} else {
    /*public void onChat(AsyncPlayerChatEvent event) {
    *///?}
        if (!ConfigUtil.isForwardingEnabled(false)) {
            return;
        }

        ConfigUtil.EventValue eventValue = ConfigUtil.getEventConfigValue(ConfigUtil.Events.PLAYER);
        if (!eventValue.enabled() || eventValue.isNullChatId()) {
            return;
        }

        Player player = event.getPlayer();
        String rawMessage;

        //? if modern_chat_event {
        TextComponent message = ((TextComponent) event.originalMessage());
        rawMessage = removeStyles(message.content());
        //?} else {
        /*rawMessage = removeStyles(event.getMessage());
        *///?}

        if (ConfigUtil.isPrefixRequired(false)) {
            String prefix = ConfigUtil.getPrefix(false);
            if (rawMessage.startsWith(prefix)) {
                String messageWithoutPrefix = rawMessage.substring(prefix.length())
                                                     .trim();
                if (!messageWithoutPrefix.isEmpty()) {
                    String text = ConfigUtil.getLocalizedText(ConfigUtil.Events.PLAYER, "sendMessage")
                                            .replace("%playerName%", player.getName())
                                            .replace("%message%", messageWithoutPrefix);
                    sendMessageToTelegram(text, eventValue);
                }
            }
        } else {
            if (rawMessage.startsWith("/")) {
                return;
            }

            if (rawMessage.isEmpty()) {
                return;
            }

            String text = ConfigUtil.getLocalizedText(ConfigUtil.Events.PLAYER, "sendMessage")
                                    .replace("%playerName%", player.getName())
                                    .replace("%message%", rawMessage);
            sendMessageToTelegram(text, eventValue);
        }
    }

    private String removeStyles(String text) {
        return text.replaceAll("[§&].", "");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDisconnect(PlayerQuitEvent event) {
        ConfigUtil.EventValue eventValue = ConfigUtil.getEventConfigValue(ConfigUtil.Events.PLAYER);
        if (!eventValue.enabled() || eventValue.isNullChatId()) {
            return;
        }

        String text = ConfigUtil.getLocalizedText(ConfigUtil.Events.PLAYER, "leaveMessage").replace("%playerName%", event.getPlayer().getName());
        sendMessageToTelegram(text, eventValue);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        ConfigUtil.EventValue eventValue = ConfigUtil.getEventConfigValue(ConfigUtil.Events.PLAYER);
        if (!eventValue.enabled() || eventValue.isNullChatId()) {
            return;
        }
        String text = ConfigUtil.getLocalizedText(ConfigUtil.Events.PLAYER, "joinMessage").replace("%playerName%", event.getPlayer().getName());
        sendMessageToTelegram(text, eventValue);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAchievement(PlayerAdvancementDoneEvent event) {
        ConfigUtil.EventValue eventValue = ConfigUtil.getEventConfigValue(ConfigUtil.Events.PLAYER);
        if (!eventValue.enabled() || eventValue.isNullChatId()) {
            return;
        }

        String advancementName;

        //? if >=1.20.2 {
        
        if (event.getAdvancement().getDisplay() == null) return;
        advancementName = PlainTextComponentSerializer.plainText().serialize(event.getAdvancement().getDisplay().title())
                .replace("[", "").replace("]", "");
         
        //?} else {
        /*String key = event.getAdvancement().getKey().getKey();
        if (key.startsWith("recipes/")) return;

        String transKey = "advancements." + key.replace('/', '.') + ".title";

        String translated = TranslationRegistry.INSTANCE.translate(transKey);

        if (translated != null && !translated.equals(transKey)) {
            advancementName = translated;
        } else {
            advancementName = formatLegacyKey(key);
        }
        *///?}

        String text = ConfigUtil.getLocalizedText(ConfigUtil.Events.PLAYER, "advancementDone")
                .replace("%playerName%", event.getPlayer().getName())
                .replace("%advancementName%", advancementName);
        sendMessageToTelegram(text, eventValue);
    }

    private String formatLegacyKey(String key) {
        int lastSlash = key.lastIndexOf('/');
        String name = (lastSlash == -1) ? key : key.substring(lastSlash + 1);
        String[] words = name.split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return result.toString().trim();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        ConfigUtil.EventValue eventValue = ConfigUtil.getEventConfigValue(ConfigUtil.Events.PLAYER);

        boolean ignored = !eventValue.enabled() || eventValue.isNullChatId() || event.deathMessage() == null;
        if (ignored) {
            return;
        }

        String message;
        //? if modern_chat_event {
        message = ((TextComponent) event.deathMessage()).content();
        //?} else {
        /*message = event.getDeathMessage();
        *///?}

        String playerName = event.getEntity().getName();
        String text = ConfigUtil.getLocalizedText(ConfigUtil.Events.PLAYER, "death")
                .replace("%playerName%", playerName)
                .replace("%deathMessage%", message);
        sendMessageToTelegram(text, eventValue);
    }
}