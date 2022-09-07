package com.gmail.necnionch.myplugin.bungeecommandassist.bungee;

import java.util.Locale;

public enum Lang {
    RELOAD_CONFIG("&7Reloaded configuration and commands"),

    NON_PLAYER("&4Can't execute command from console"),
    SPECIFY_YES_OR_NO("&4Please specify &cOn &7OR &cOff"),
    SPECIFY_SERVER("&4Please specify server name"),
    ALREADY_CONNECTED("&4Already connected"),
    FAILED_ASSIST("&4Assist failed"),

    ENABLED_WATCHER("&2Enabled &7Command Watcher"),
    DISABLED_WATCHER("&4Disabled &7Command Watcher"),
    ENABLED_WATCHER_TEMP("&2Temporary enabled &7Command Watcher"),
    DISABLED_WATCHER_TEMP("&4Temporary disabled &7Command Watcher"),

    ENABLED_ASSIST("&2Enabled &7Assist Tab-Completion"),
    DISABLED_ASSIST("&4Disabled &7Assist Tab-Completion"),
    ;

    private final String key;
    private final String defaultMessage;

    Lang(String defaultMessage) {
        this.key = name().toLowerCase(Locale.ROOT).replace("_", "-");
        this.defaultMessage = defaultMessage;
    }

    public String getKey() {
        return key;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

}
