package com.gmail.necnionch.myplugin.bungeecommandassist.bungee.events;

import com.gmail.necnionch.myplugin.bungeecommandassist.bungee.config.CommandEntry;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

public class BypassingBukkitCommandEvent extends Event implements Cancellable {
    private final ChatEvent chatEvent;
    private final CommandEntry entry;
    private String changedLine;
    private boolean cancelled;

    public BypassingBukkitCommandEvent(ChatEvent chatEvent, CommandEntry entry, String changedLine) {
        this.chatEvent = chatEvent;
        this.entry = entry;
        this.changedLine = changedLine;
    }

    public ChatEvent getChatEvent() {
        return chatEvent;
    }

    public CommandEntry getEntry() {
        return entry;
    }

    public String getChangedLine() {
        return changedLine;
    }

    public void setChangedLine(String changedLine) {
        this.changedLine = changedLine;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}
