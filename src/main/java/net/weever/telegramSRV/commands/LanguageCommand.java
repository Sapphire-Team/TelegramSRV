package net.weever.telegramSRV.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.weever.telegramSRV.util.ConfigUtil;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LanguageCommand implements BasicCommand
{
    private static final Pattern EMOJI_PATTERN = Pattern.compile("[\\p{So}\\p{Cn}\\p{Cs}]|[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+", Pattern.UNICODE_CASE);

    public static String removeEmojis(@NotNull String text)
    {
        Matcher matcher = EMOJI_PATTERN.matcher(text);
        return matcher.replaceAll("");
    }

    @Override
    public void execute(CommandSourceStack commandSourceStack, String[] args)
    {
        var sender = commandSourceStack.getSender();
        if (args.length != 1)
        {
            sender.sendMessage(removeEmojis(ConfigUtil.getLocalizedText("telegramCommands", "language.replyFailed")));
            return;
        }

        ConfigUtil.changeLanguage(args[0]);
        String text = removeEmojis(ConfigUtil.getLocalizedText("telegramCommands", "language.replySuccessful").replace("%language%", args[0])).replace("<b>", "").replace("</b>", ""); // 💀💀💀
        sender.sendMessage(text);
    }

    @Override
    public Collection<String> suggest(CommandSourceStack commandSourceStack, String[] args)
    {
        return ConfigUtil.getLoadedLanguages();
    }

    @Override
    public @Nullable String permission()
    {
        return "telegramSRV.admin";
    }
}