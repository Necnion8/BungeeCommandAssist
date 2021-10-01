package com.gmail.necnionch.myplugin.bungeecommandassist.bungee.managers;

import com.gmail.necnionch.myplugin.bungeecommandassist.bungee.BungeeCommandAssist;
import com.gmail.necnionch.myplugin.bungeecommandassist.bungee.CommandWrapper;
import com.gmail.necnionch.myplugin.bungeecommandassist.bungee.config.QuickServerCommandEntry;
import com.google.common.collect.Sets;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.PluginManager;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class QuickServerCommandManager {
    private final BungeeCommandAssist plugin;
    private final PluginManager mgr;
    private final Set<CommandWrapper> commands = Sets.newHashSet();


    public QuickServerCommandManager(BungeeCommandAssist plugin) {
        this.plugin = plugin;
        mgr = plugin.getProxy().getPluginManager();
    }


    public void registerCommands(List<QuickServerCommandEntry> entries) {
        unregisterCommands();

        plugin.printDebug("Registering quick server commands...");

        for (QuickServerCommandEntry entry : entries) {
            ServerInfo server = plugin.getProxy().getServerInfo(entry.getServer());
            if (server == null)
                plugin.getLogger().warning(entry.getServer() + " server not found");

            for (String commandName : entry.getCommands()) {
                CommandWrapper.Executor executor = (sender, args) -> {
                    if (!(sender instanceof ProxiedPlayer))
                        return;
                    ProxiedPlayer p = (ProxiedPlayer) sender;

                    if (entry.getServer().equals(p.getServer().getInfo().getName())) {
                        if (entry.getSendCommandWhenConnected() != null) {
                            String argsLine = (args.length >= 1) ? " " + String.join(" ", args) : "";
                            p.chat("/" + entry.getSendCommandWhenConnected() + argsLine);
                        } else {
                            plugin.sendWithPrefix(p, "&4既に接続しています");
                        }
                    } else if (server != null) {
                        p.connect(server);
                    }
                };
                CommandWrapper command = CommandWrapper.create(plugin, commandName, entry.getPermission(), executor, (s, a) -> Collections.emptyList());
                plugin.printDebug("  register quick command: " + commandName + " (to " + entry.getServer() + " server)");
                commands.add(command);
            }
        }

        plugin.getLogger().info("Loaded quick server commands. (" + commands.size() + " entries)");

    }

    public void unregisterCommands() {
        if (!commands.isEmpty()) {
            for (CommandWrapper command : commands) {
                mgr.unregisterCommand(command);
            }
            commands.clear();
            plugin.getLogger().info("Unloaded quick server commands.");
        }
    }

}
