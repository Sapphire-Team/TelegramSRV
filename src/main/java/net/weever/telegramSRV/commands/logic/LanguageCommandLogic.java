package net.weever.telegramSRV.commands.logic;

import net.weever.telegramSRV.util.ConfigUtil;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LanguageCommandLogic {
    private static final Pattern EMOJI_PATTERN = Pattern.compile("[\\p{So}\\p{Cn}\\p{Cs}]|[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+", Pattern.UNICODE_CASE);

    public static String removeEmojis(@NotNull String text) {
        Matcher matcher = EMOJI_PATTERN.matcher(text);
        return matcher.replaceAll("");
    }

    public static void execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(removeEmojis(ConfigUtil.getLocalizedText("telegramCommands", "language.replyFailed")));
            return;
        }

        ConfigUtil.changeLanguage(args[0]);
        String text = removeEmojis(ConfigUtil.getLocalizedText("telegramCommands", "language.replySuccessful").replace("%language%", args[0])).replace("<b>", "").replace("</b>", "");
        sender.sendMessage(text);
    }
}