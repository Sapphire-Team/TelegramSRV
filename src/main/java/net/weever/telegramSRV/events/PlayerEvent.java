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
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!ConfigUtil.getEnabledOrNot(false)) return;
        ConfigUtil.EventValue eventValue = ConfigUtil.getEventConfigValue(ConfigUtil.Events.PLAYER);
        if (eventValue.isEnabled() && !eventValue.isNullChatId()) {
            Player player = event.getPlayer();
            if (!event.getMessage().startsWith("/")) {
                String playerNick = player.getName();
                String message = event.getMessage().replaceAll("§.", "");
                if (!message.isEmpty()) {
                    String text = ConfigUtil.getLocalizedText(ConfigUtil.Events.PLAYER, "sendMessage").replace("%playerName%", playerNick).replace("%message%", message);
                    String threadId = null;
                    if (!eventValue.isNullThreadId()) {
                        threadId = eventValue.getThreadId();
                    }

                    TelegramSRV.telegramBot.sendMessage(text, eventValue.getChatId(), threadId, null);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDisconnect(PlayerQuitEvent event) {
        if (!ConfigUtil.getEnabledOrNot(false)) return;
        ConfigUtil.EventValue eventValue = ConfigUtil.getEventConfigValue(ConfigUtil.Events.PLAYER);
        if (eventValue.isEnabled() && !eventValue.isNullChatId()) {
            Player player = event.getPlayer();
            String playerNick = player.getName();
            String threadId = null;
            if (!eventValue.isNullThreadId()) {
                threadId = eventValue.getThreadId();
            }
            String text = ConfigUtil.getLocalizedText(ConfigUtil.Events.PLAYER, "leaveMessage").replace("%playerName%", playerNick);
            TelegramSRV.telegramBot.sendMessage(text, eventValue.getChatId(), threadId, null);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        if (!ConfigUtil.getEnabledOrNot(false)) return;
        ConfigUtil.EventValue eventValue = ConfigUtil.getEventConfigValue(ConfigUtil.Events.PLAYER);
        if (eventValue.isEnabled() && !eventValue.isNullChatId()) {
            Player player = event.getPlayer();
            String playerNick = player.getName();
            String text = ConfigUtil.getLocalizedText(ConfigUtil.Events.PLAYER, "joinMessage").replace("%playerName%", playerNick);
            String threadId = null;
            if (!eventValue.isNullThreadId()) {
                threadId = eventValue.getThreadId();
            }
            TelegramSRV.telegramBot.sendMessage(text, eventValue.getChatId(), threadId, null);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAchievement(PlayerAdvancementDoneEvent event) {
        if (!ConfigUtil.getEnabledOrNot(false)) return;
        ConfigUtil.EventValue eventValue = ConfigUtil.getEventConfigValue(ConfigUtil.Events.PLAYER);
        if (eventValue.isEnabled() && !eventValue.isNullChatId()) {
            String advancementKey = event.getAdvancement().getKey().getKey().replace("/", ".");
            if (advancementKey.startsWith("recipes.") || Boolean.FALSE.equals(event.getPlayer().getWorld().getGameRuleValue(GameRule.ANNOUNCE_ADVANCEMENTS)))
                return;
            String advancementText = TranslationRegistry.INSTANCE.translate("advancements." + advancementKey + ".title"); // idk, have some problems, like: advancements.death_trigger.title, but i think i can fix it, but i'm lazy ok da
            Player player = event.getPlayer();
            String playerNick = player.getName();
            String text = ConfigUtil.getLocalizedText(ConfigUtil.Events.PLAYER, "advancementDone")
                    .replace("%playerName%", playerNick).replace("%advancementName%", advancementText);
            String threadId = null;
            if (!eventValue.isNullThreadId()) {
                threadId = eventValue.getThreadId();
            }

            TelegramSRV.telegramBot.sendMessage(text, eventValue.getChatId(), threadId, null);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        if (!ConfigUtil.getEnabledOrNot(false)) return;
        ConfigUtil.EventValue eventValue = ConfigUtil.getEventConfigValue(ConfigUtil.Events.PLAYER);
        if (eventValue.isEnabled() && !eventValue.isNullChatId()) {
            Player player = event.getPlayer();
            String playerNick = player.getName();
            String text = ConfigUtil.getLocalizedText(ConfigUtil.Events.PLAYER, "death")
                    .replace("%playerName%", playerNick).replace("%deathMessage%", event.getDeathMessage());
            String threadId = null;
            if (!eventValue.isNullThreadId()) {
                threadId = eventValue.getThreadId();
            }

            TelegramSRV.telegramBot.sendMessage(text, eventValue.getChatId(), threadId, null);
        }
    }
}
