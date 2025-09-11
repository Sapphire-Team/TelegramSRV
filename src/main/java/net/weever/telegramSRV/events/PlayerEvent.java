package net.weever.telegramSRV.events;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.weever.telegramSRV.TelegramSRV;
import net.weever.telegramSRV.util.ConfigUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerEvent implements Listener {
    private static void sendMessageToTelegram(String text, ConfigUtil.EventValue eventValue) {
        if (text == null || text.isEmpty()) {
            return;
        }

        if (eventValue.enabled() && !eventValue.isNullChatId()) {
            String threadId = eventValue.isNullThreadId() ? null : eventValue.threadId();
            TelegramSRV.telegramBot.sendMessage(text, eventValue.chatId(), threadId, null);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!ConfigUtil.isForwardingEnabled(false)) {
            return;
        }

        ConfigUtil.EventValue eventValue = ConfigUtil.getEventConfigValue(ConfigUtil.Events.PLAYER);
        if (!eventValue.enabled() || eventValue.isNullChatId()) {
            return;
        }

        Player player = event.getPlayer();
        String message = event.getMessage();

        if (ConfigUtil.isPrefixRequired(false)) {
            String prefix = ConfigUtil.getPrefix(false);
            if (message.startsWith(prefix)) {
                String messageToSend = message.substring(prefix.length()).trim();
                if (!messageToSend.isEmpty()) {
                    String text = ConfigUtil.getLocalizedText(ConfigUtil.Events.PLAYER, "sendMessage")
                            .replace("%playerName%", player.getName())
                            .replace("%message%", messageToSend.replaceAll("§.", ""));
                    sendMessageToTelegram(text, eventValue);
                }
            }
        } else {
            if (message.startsWith("/")) {
                return;
            }

            String cleanedMessage = message.replaceAll("§.", "");
            if (cleanedMessage.isEmpty()) {
                return;
            }

            String text = ConfigUtil.getLocalizedText(ConfigUtil.Events.PLAYER, "sendMessage")
                    .replace("%playerName%", player.getName())
                    .replace("%message%", cleanedMessage);
            sendMessageToTelegram(text, eventValue);
        }
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
        if (!eventValue.enabled() || eventValue.isNullChatId() || event.getAdvancement().getDisplay() == null) {
            return;
        }

        String advancementText = PlainTextComponentSerializer.plainText().serialize(event.getAdvancement().displayName()).replace("[", "").replace("]", "");
        String text = ConfigUtil.getLocalizedText(ConfigUtil.Events.PLAYER, "advancementDone").replace("%playerName%", event.getPlayer().getName()).replace("%advancementName%", advancementText);
        sendMessageToTelegram(text, eventValue);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        ConfigUtil.EventValue eventValue = ConfigUtil.getEventConfigValue(ConfigUtil.Events.PLAYER);
        if (!eventValue.enabled() || eventValue.isNullChatId()) {
            return;
        }
        String text = ConfigUtil.getLocalizedText(ConfigUtil.Events.PLAYER, "death").replace("%playerName%", event.getPlayer().getName()).replace("%deathMessage%", event.getDeathMessage());
        sendMessageToTelegram(text, eventValue);
    }
}