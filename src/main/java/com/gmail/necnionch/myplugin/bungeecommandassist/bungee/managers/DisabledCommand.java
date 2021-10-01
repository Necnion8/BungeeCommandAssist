package com.gmail.necnionch.myplugin.bungeecommandassist.bungee.managers;

import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;


public class DisabledCommand {
    private final Plugin plugin;
    private final Command command;

    public DisabledCommand(Plugin plugin, Command command) {
        this.plugin = plugin;
        this.command = command;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public Command getCommand() {
        return command;
    }

}
