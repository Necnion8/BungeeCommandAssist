package com.gmail.necnionch.myplugin.bungeecommandassist.bungee.managers;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.Collections;

public class RenamedCommand extends Command implements TabExecutor {
    private final Command original;
    private final TabExecutor completer;

    public RenamedCommand(String newName, Command original, TabExecutor completer) {
        super(newName, original.getPermission());
        this.original = original;
        this.completer = completer;
    }

    public Command getOriginal() {
        return original;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        original.execute(sender, args);
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return original.hasPermission(sender);
    }


    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (completer != null)
            return completer.onTabComplete(sender, args);
        if (original instanceof TabExecutor)
            return ((TabExecutor) original).onTabComplete(sender, args);
        return Collections.emptyList();
    }

}
