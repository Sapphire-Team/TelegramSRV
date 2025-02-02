package net.weever.telegramSRV.events;

import net.md_5.bungee.chat.TranslationRegistry;
import net.weever.telegramSRV.TelegramSRV;
import net.weever.telegramSRV.util.ConfigUtil;
import org.bukkit.GameRule;
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
        if (eventValue.isEnabled() && !eventValue.isNullChatId()) {
            String threadId = eventValue.isNullThreadId() ? null : eventValue.getThreadId();
            TelegramSRV.telegramBot.sendMessage(text, eventValue.getChatId(), threadId, null);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!ConfigUtil.getEnabledOrNot(false)) return;
        ConfigUtil.EventValue eventValue = ConfigUtil.getEventConfigValue(ConfigUtil.Events.PLAYER);
        if (!eventValue.isEnabled() || eventValue.isNullChatId()) return;

        Player player = event.getPlayer();
        if (event.getMessage().startsWith("/")) return;
        String message = event.getMessage().replaceAll("§.", "");
        if (message.isEmpty()) return;

        String text = ConfigUtil.getLocalizedText(ConfigUtil.Events.PLAYER, "sendMessage")
                .replace("%playerName%", player.getName())
                .replace("%message%", message);
        sendMessageToTelegram(text, eventValue);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDisconnect(PlayerQuitEvent event) {
        if (!ConfigUtil.getEnabledOrNot(false)) return;
        ConfigUtil.EventValue eventValue = ConfigUtil.getEventConfigValue(ConfigUtil.Events.PLAYER);
        if (!eventValue.isEnabled() || eventValue.isNullChatId()) return;

        String text = ConfigUtil.getLocalizedText(ConfigUtil.Events.PLAYER, "leaveMessage")
                .replace("%playerName%", event.getPlayer().getName());
        sendMessageToTelegram(text, eventValue);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        if (!ConfigUtil.getEnabledOrNot(false)) return;
        ConfigUtil.EventValue eventValue = ConfigUtil.getEventConfigValue(ConfigUtil.Events.PLAYER);
        if (!eventValue.isEnabled() || eventValue.isNullChatId()) return;
        String text = ConfigUtil.getLocalizedText(ConfigUtil.Events.PLAYER, "joinMessage")
                .replace("%playerName%", event.getPlayer().getName());
        sendMessageToTelegram(text, eventValue);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAchievement(PlayerAdvancementDoneEvent event) {
        if (!ConfigUtil.getEnabledOrNot(false)) return;
        ConfigUtil.EventValue eventValue = ConfigUtil.getEventConfigValue(ConfigUtil.Events.PLAYER);
        if (!eventValue.isEnabled() || eventValue.isNullChatId()) return;

        String advancementKey = event.getAdvancement().getKey().getKey().replace("/", ".");
        if (advancementKey.startsWith("recipes.") || Boolean.FALSE.equals(event.getPlayer().getWorld().getGameRuleValue(GameRule.ANNOUNCE_ADVANCEMENTS)))
            return;
        String advancementText = TranslationRegistry.INSTANCE.translate("advancements." + advancementKey + ".title");
        String text = ConfigUtil.getLocalizedText(ConfigUtil.Events.PLAYER, "advancementDone")
                .replace("%playerName%", event.getPlayer().getName())
                .replace("%advancementName%", advancementText);
        sendMessageToTelegram(text, eventValue);

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        if (!ConfigUtil.getEnabledOrNot(false)) return;
        ConfigUtil.EventValue eventValue = ConfigUtil.getEventConfigValue(ConfigUtil.Events.PLAYER);
        if (!eventValue.isEnabled() || eventValue.isNullChatId()) return;
        String text = ConfigUtil.getLocalizedText(ConfigUtil.Events.PLAYER, "death")
                .replace("%playerName%", event.getPlayer().getName())
                .replace("%deathMessage%", event.getDeathMessage());
        sendMessageToTelegram(text, eventValue);
    }
}