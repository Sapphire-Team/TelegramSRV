package net.weever.telegramSRV.commands;

import net.weever.telegramSRV.util.ConfigUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LanguageCommand implements CommandExecutor {
    public static String removeEmojis(@NotNull String text) {
        String emojiRegex = "[\\p{So}\\p{Cn}\\p{Cs}]|[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+";

        Pattern emojiPattern = Pattern.compile(emojiRegex, Pattern.UNICODE_CASE);
        Matcher matcher = emojiPattern.matcher(text);

        return matcher.replaceAll("");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (commandSender.isOp()) {
            if (args.length > 1) {
                ConfigUtil.changeLanguage(args[1]);
                String text = removeEmojis(ConfigUtil.getLocalizedText("telegramCommands", "language.replySuccessful")
                        .replace("%language%", args[1]));
                commandSender.sendMessage(text);
            } else {
                commandSender.sendMessage(removeEmojis(ConfigUtil.getLocalizedText("telegramCommands", "language.replyFailed")));
            }
            return true;
        }
        return false;
    }
}
