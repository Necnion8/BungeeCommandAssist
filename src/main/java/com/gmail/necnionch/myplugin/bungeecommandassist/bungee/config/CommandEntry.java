package com.gmail.necnionch.myplugin.bungeecommandassist.bungee.config;

public class CommandEntry {
    private final String original;
    private final String changed;
    private final Type type;

    public CommandEntry(String original, String changed, Type type) {
        this.original = original;
        this.changed = changed;
        this.type = type;
    }

    public String getOriginal() {
        return original;
    }

    public String getChanged() {
        return changed;
    }

    public Type getType() {
        return type;
    }


    public enum Type {
        RENAME, ALIAS
    }

}

