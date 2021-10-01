package com.gmail.necnionch.myplugin.bungeecommandassist.bungee.config;

import java.util.Set;

public class WatcherConfig {

    private final String format;
    private final Set<String> blacklist;

    public WatcherConfig(String format, Set<String> blacklist) {
        this.format = format;
        this.blacklist = blacklist;
    }

    public String getFormat() {
        return format;
    }

    public Set<String> getBlacklist() {
        return blacklist;
    }

}
