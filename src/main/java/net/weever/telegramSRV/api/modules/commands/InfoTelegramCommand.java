package net.weever.telegramSRV.api.modules.commands;

import net.weever.telegramSRV.api.modules.ITelegramCommand;
import net.weever.telegramSRV.util.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

public class InfoTelegramCommand implements ITelegramCommand {
    @Override
    public void onCommand(TelegramCommandImpl.ReplyToCommand replyToCommand) {
        double mspt = getAverageMSPT();
        String msptFormatted = String.format("%.1f", mspt);

        long maxMemory = Runtime.getRuntime().maxMemory() / 1024 / 1024;
        long freeMemory = Runtime.getRuntime().freeMemory() / 1024 / 1024;
        long totalMemory = Runtime.getRuntime().totalMemory() / 1024 / 1024;
        long usedMemory = totalMemory - freeMemory;
        String ramText = usedMemory + "/" + maxMemory + " MB";

        String uptime = formatUptime(ManagementFactory.getRuntimeMXBean().getUptime());

        int totalEntities = 0;
        int totalChunks = 0;
        for (World world : Bukkit.getWorlds()) {
            //? if >=1.17 {
            totalEntities += world.getEntityCount();
            //?} else {
            /*totalEntities += world.getEntities().size();
            *///?}
            totalChunks += world.getLoadedChunks().length;
        }

        String text = ConfigUtil.getLocalizedText("telegramCommands", "info.reply")
                .replace("%tps%", String.format("%.1f", Bukkit.getTPS()[0]))
                .replace("%mspt%", msptFormatted)
                .replace("%playersCount%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("%ram%", ramText)
                .replace("%uptime%", uptime)
                .replace("%entities%", String.valueOf(totalEntities))
                .replace("%chunks%", String.valueOf(totalChunks))
                .replace("%core%", Bukkit.getName() + " " + Bukkit.getBukkitVersion());
        replyToCommand.reply(text);
    }

    private double getAverageMSPT() {
        long[] times = Bukkit.getServer().getTickTimes();
        if (times == null) return 0.0;

        long total = 0;
        for (long time : times) {
            total += time;
        }

        return (total / (double) times.length) * 1.0E-6D;
    }

    private String formatUptime(long uptimeMillis) {
        long days = TimeUnit.MILLISECONDS.toDays(uptimeMillis);
        long hours = TimeUnit.MILLISECONDS.toHours(uptimeMillis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(uptimeMillis) % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        sb.append(minutes).append("m");
        return sb.toString();
    }
}
