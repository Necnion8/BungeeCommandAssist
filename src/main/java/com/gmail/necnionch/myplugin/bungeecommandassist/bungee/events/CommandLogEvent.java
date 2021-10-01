package com.gmail.necnionch.myplugin.bungeecommandassist.bungee.events;

import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Event;

public class CommandLogEvent extends Event {
    private boolean silent;

    private final ChatEvent chatEvent;

    public CommandLogEvent(ChatEvent chatEvent) {
        this.chatEvent = chatEvent;
    }


    public ChatEvent getChatEvent() {
        return chatEvent;
    }

    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

}
