package com.gmail.necnionch.myplugin.bungeecommandassist.bungee.config;

public class TabCompleteGeneratorEntry {

    private final String commandLine;
    private final TabCompleteGenerator generator;

    public TabCompleteGeneratorEntry(String commandLine, TabCompleteGenerator generator) {
        this.commandLine = commandLine;
        this.generator = generator;
    }


    public String getCommandLine() {
        return commandLine;
    }

    public TabCompleteGenerator getGenerator() {
        return generator;
    }

}
