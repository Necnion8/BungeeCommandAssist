package com.gmail.necnionch.myplugin.bungeecommandassist.bungee.managers;

import com.gmail.necnionch.myplugin.bungeecommandassist.bungee.BungeeCommandAssist;
import com.gmail.necnionch.myplugin.bungeecommandassist.bungee.config.PlayerConfig;
import com.gmail.necnionch.myplugin.bungeecommandassist.bungee.config.WatcherConfig;
import com.gmail.necnionch.myplugin.bungeecommandassist.bungee.events.CommandLogEvent;
import com.google.common.collect.Maps;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class WatcherManager implements Listener {
    public static final String PERMS_WATCHER = "bungeecommandassist.watcher";

    private final BungeeCommandAssist plugin;
    private final PlayerConfig playersConfig;
    private WatcherConfig config;

    private final Map<UUID, Boolean> tempWatchers = Maps.newHashMap();


    public WatcherManager(BungeeCommandAssist plugin, PlayerConfig config) {
        this.plugin = plugin;
        this.playersConfig = config;
    }


    public void update(WatcherConfig config) {
        this.config = config;
    }


    @EventHandler
    public void onChat(ChatEvent event) {
        if (!event.isCommand())
            return;

        ProxiedPlayer sender = (ProxiedPlayer) event.getSender();
        String[] split = event.getMessage().substring(1).split(" ", -1);

        if (config.getBlacklist().contains(split[0].toLowerCase(Locale.ROOT)))
            return;

        CommandLogEvent newEvent = plugin.getProxy().getPluginManager().callEvent(new CommandLogEvent(event));
        if (newEvent.isSilent())
            return;

        String formatted = ChatColor.translateAlternateColorCodes('&', config.getFormat())
                .replace("{player}", sender.getName())
                .replace("{command}", String.join(" ", split));

        plugin.getProxy().getPlayers().forEach(p -> {
            if (!p.equals(sender) && p.hasPermission(PERMS_WATCHER) && isWatching(p.getUniqueId()))
                p.sendMessage(formatted);
        });

    }


    public boolean isWatching(UUID player) {
        return (tempWatchers.containsKey(player)) ? tempWatchers.get(player) : playersConfig.getWatchers().contains(player);
    }


    public void setTempWatching(UUID player, boolean watch) {
        tempWatchers.put(player, watch);
        if (watch) {
            playersConfig.getWatchers().remove(player);
        } else {
            playersConfig.getWatchers().add(player);
        }
        playersConfig.save();
    }

    public void setWatching(UUID player, boolean watch) {
        tempWatchers.remove(player);
        if (watch) {
            playersConfig.getWatchers().add(player);
        } else {
            playersConfig.getWatchers().remove(player);
        }
        playersConfig.save();
    }


    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        tempWatchers.remove(event.getPlayer().getUniqueId());
    }

}
