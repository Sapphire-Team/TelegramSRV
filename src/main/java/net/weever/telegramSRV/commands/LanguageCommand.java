package net.weever.telegramSRV.commands;

import net.weever.telegramSRV.util.ConfigUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LanguageCommand implements CommandExecutor {

    private static final Pattern EMOJI_PATTERN = Pattern.compile("[\\p{So}\\p{Cn}\\p{Cs}]|[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+", Pattern.UNICODE_CASE);

    public static String removeEmojis(@NotNull String text) {
        Matcher matcher = EMOJI_PATTERN.matcher(text);
        return matcher.replaceAll("");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.isOp()) return false;

        if (args.length != 1) {
            sender.sendMessage(removeEmojis(ConfigUtil.getLocalizedText("telegramCommands", "language.replyFailed")));
            return true;
        }

        ConfigUtil.changeLanguage(args[0]);
        String text = removeEmojis(ConfigUtil.getLocalizedText("telegramCommands", "language.replySuccessful")
                .replace("%language%", args[0]));
        sender.sendMessage(text);
        return true;
    }
}