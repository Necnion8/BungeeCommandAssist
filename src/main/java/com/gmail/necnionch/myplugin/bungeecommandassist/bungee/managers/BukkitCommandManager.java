package com.gmail.necnionch.myplugin.bungeecommandassist.bungee.managers;

import com.gmail.necnionch.myplugin.bungeecommandassist.bungee.BungeeCommandAssist;
import com.gmail.necnionch.myplugin.bungeecommandassist.bungee.config.CommandEntry;
import com.gmail.necnionch.myplugin.bungeecommandassist.bungee.events.BypassingBukkitCommandEvent;
import com.google.common.collect.Lists;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.event.EventHandler;

import java.util.ArrayList;
import java.util.List;

public class BukkitCommandManager implements Listener {
    private final BungeeCommandAssist plugin;
    private final PluginManager mgr;
    private final List<CommandEntry> entries = Lists.newArrayList();


    public BukkitCommandManager(BungeeCommandAssist plugin) {
        this.plugin = plugin;
        mgr = plugin.getProxy().getPluginManager();
    }

    public void registerCommands(List<CommandEntry> entries) {
        this.entries.clear();
        this.entries.addAll(entries);
        plugin.getLogger().info("Loaded bukkit commands. (" + entries.size() + " entries)");
    }

    public void unregisterCommands() {
        entries.clear();
    }


    @EventHandler
    public void onChat(ChatEvent event) {
        if (!event.isCommand() || event.isCancelled())
            return;

        String[] split = event.getMessage().substring(1).split(" ", -1);
        CommandEntry entry = null;
        for (CommandEntry e : entries) {
            if (e.getChanged().equalsIgnoreCase(split[0])) {
                entry = e;
                break;
            }
        }


        if (entry == null)
            return;

        String original = entry.getOriginal();
        ArrayList<String> args = Lists.newArrayList(split);
        args.remove(0);
        args.add(0, original);

        BypassingBukkitCommandEvent newEvent = new BypassingBukkitCommandEvent(event, entry, String.join(" ", args));

        if (!mgr.callEvent(newEvent).isCancelled() && event.getSender() instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) event.getSender();

            switch (plugin.getMainConfig().getBukkitCommandsHandling()) {
                case BUNGEE: {
                    try {
                        player.chat("/" + newEvent.getChangedLine());
                    } catch (Throwable e) {
                        plugin.getLogger().warning("Error in player.chat() (" + player.getName() + "): " + e.getMessage());
                    }
                    break;
                }
                case BUKKIT: {
                    plugin.sendCommandRequestToBukkit(player, newEvent.getChangedLine());
                    break;
                }
                default: {
                    try {
                        player.chat("/" + newEvent.getChangedLine());
                    } catch (UnsupportedOperationException e) {
                        plugin.sendCommandRequestToBukkit(player, newEvent.getChangedLine());
                    }
                }
            }
            event.setCancelled(true);
        }

    }

}
