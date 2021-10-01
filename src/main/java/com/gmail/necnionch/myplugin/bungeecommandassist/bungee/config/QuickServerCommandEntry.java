package com.gmail.necnionch.myplugin.bungeecommandassist.bungee.config;

import java.util.Set;

public class QuickServerCommandEntry {

    private final String server;
    private final Set<String> commands;
    private final String permission;
    private final String sendCommandWhenConnected;

    public QuickServerCommandEntry(String server, Set<String> commands, String permission, String sendCommandWhenConnected) {
        this.server = server;
        this.commands = commands;
        this.sendCommandWhenConnected = sendCommandWhenConnected;
        this.permission = permission;
    }

    public String getServer() {
        return server;
    }

    public Set<String> getCommands() {
        return commands;
    }

    public String getPermission() {
        return permission;
    }

    public String getSendCommandWhenConnected() {
        return sendCommandWhenConnected;
    }

}
