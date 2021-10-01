package com.gmail.necnionch.myplugin.bungeecommandassist.bungee;

import com.gmail.necnionch.myplugin.bungeecommandassist.bungee.config.*;
import com.gmail.necnionch.myplugin.bungeecommandassist.common.BungeeConfigDriver;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

import java.util.*;
import java.util.stream.Collectors;


public class MainConfig extends BungeeConfigDriver {
    private final List<CommandEntry> bungeeCommands = Lists.newArrayList();
    private final List<CommandEntry> bukkitCommands = Lists.newArrayList();
    private final List<QuickServerCommandEntry> serverCommands = Lists.newArrayList();
    private final Map<String, TabCompleteGenerator> completeGenerators = Maps.newHashMap();
    private WatcherConfig watcherConfig = new WatcherConfig("", Collections.emptySet());


    public MainConfig(Plugin plugin) {
        super(plugin);
    }


    public boolean isDebug() {
        return config.getBoolean("debug", false);
    }

    public String getCommandPrefix() {
        String string = config.getString("command-prefix", null);
        return (string != null) ? string : "&8[&3C;Assist&8] ";
    }

    public boolean isEnableShortServerCommand() {
        return config.getBoolean("enable-short-server-command", false);
    }

    public boolean isEnableShortPlayerTeleportCommand() {
        return config.getBoolean("enable-short-player-teleport-command", false);
    }

    public List<CommandEntry> getBungeeCommands() {
        return bungeeCommands;
    }

    public List<CommandEntry> getBukkitCommands() {
        return bukkitCommands;
    }

    public List<QuickServerCommandEntry> getServerCommands() {
        return serverCommands;
    }

    public WatcherConfig getWatcherConfig() {
        return watcherConfig;
    }

    public Map<String, TabCompleteGenerator> getCompleteGenerators() {
        return completeGenerators;
    }

    @Override
    public boolean onLoaded(Configuration config) {
        if (super.onLoaded(config)) {
            bungeeCommands.clear();
            Configuration section = config.getSection("bungee-commands.remapping");
            for (String orig : section.getKeys()) {
                bungeeCommands.add(new CommandEntry(
                        orig.toLowerCase(Locale.ROOT),
                        section.getString(orig).toLowerCase(Locale.ROOT),
                        CommandEntry.Type.RENAME
                ));
            }
            section = config.getSection("bungee-commands.aliases");
            for (String alias : section.getKeys()) {
                bungeeCommands.add(new CommandEntry(
                        section.getString(alias).toLowerCase(Locale.ROOT),
                        alias.toLowerCase(Locale.ROOT),
                        CommandEntry.Type.ALIAS
                ));
            }


            bukkitCommands.clear();
            section = config.getSection("bukkit-commands.aliases");
            for (String alias : section.getKeys()) {
                bukkitCommands.add(new CommandEntry(
                        section.getString(alias).toLowerCase(Locale.ROOT),
                        alias.toLowerCase(Locale.ROOT),
                        CommandEntry.Type.ALIAS
                ));
            }


            serverCommands.clear();
            section = config.getSection("server-quick-command");
            for (String server : section.getKeys()) {
                Set<String> commands = Sets.newHashSet();
                commands.addAll(section.getStringList(server + ".commands"));
                serverCommands.add(new QuickServerCommandEntry(
                        server,
                        commands,
                        section.getString(server + ".permission", null),
                        section.getString(server + ".execute-command-when-connected", null)
                ));
            }


            String format = config.getString("watcher.format", null);
            if (format == null)
                format = "&4* &7{player} &8[&7/{command}&8]";
            Set<String> blacklist = config.getStringList("watcher.blacklist").stream()
                    .map(s -> s.toLowerCase(Locale.ROOT))
                    .collect(Collectors.toSet());
            watcherConfig = new WatcherConfig(format, blacklist);


            completeGenerators.clear();
            section = config.getSection("tabcomplete-generator");
            if (section != null) {
                for (String command : section.getKeys()) {
                    TabCompleteGenerator generator;
                    String string = section.getString(command, null);
                    if (string == null) {
                        List<String> completes = section.getStringList(command);
                        generator = () -> completes;
                    } else if (string.equalsIgnoreCase("players")) {
                        generator = TabCompleteGenerator.makePlayers();
                    } else if (string.equalsIgnoreCase("servers")) {
                        generator = TabCompleteGenerator.makeServers();
                    } else {
                        continue;
                    }
                    completeGenerators.put(command.toLowerCase(Locale.ROOT), generator);
                }
            }

            return true;
        }
        return false;
    }

}
