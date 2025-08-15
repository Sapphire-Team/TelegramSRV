package net.weever.telegramSRV.commands.modern;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.weever.telegramSRV.commands.logic.LanguageCommandLogic;
import net.weever.telegramSRV.util.ConfigUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class LanguageCommandModern implements BasicCommand {
    @Override
    public void execute(CommandSourceStack commandSourceStack, String[] args) {
        LanguageCommandLogic.execute(commandSourceStack.getSender(), args);
    }

    @Override
    public Collection<String> suggest(CommandSourceStack commandSourceStack, String[] args) {
        if (args.length > 1) {
            return Collections.emptyList();
        }
        return ConfigUtil.getLoadedLanguages().stream()
                .filter(lang -> lang.toLowerCase().startsWith(args.length > 0 ? args[0].toLowerCase() : ""))
                .collect(Collectors.toList());
    }

    @Override
    public @Nullable String permission() {
        return "telegramSRV.admin";
    }
}