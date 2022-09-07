package com.gmail.necnionch.myplugin.bungeecommandassist.bungee.managers;

import com.gmail.necnionch.myplugin.bungeecommandassist.bungee.BungeeCommandAssist;
import com.gmail.necnionch.myplugin.bungeecommandassist.bungee.CommandWrapper;
import com.gmail.necnionch.myplugin.bungeecommandassist.bungee.Lang;
import com.google.common.collect.Sets;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.event.EventHandler;

import java.util.Collections;
import java.util.Set;
import java.util.logging.Logger;

import static com.gmail.necnionch.myplugin.bungeecommandassist.bungee.BungeeCommandAssist.findEntries;
import static com.gmail.necnionch.myplugin.bungeecommandassist.bungee.BungeeCommandAssist.findOnes;


public class ShortServerCommandHandler implements Listener {
    private static final String SHORT_SERVER_PREFIX = "s/";
    private static final String SHORT_SERVER_PERMISSION = "bungeecord.command.server";
    private final Set<CommandWrapper> dynamics = Sets.newHashSet();
    private CommandWrapper fakeCommand;

    private final BungeeCommandAssist pl;
    private final PluginManager mgr;

    private ShortServerCommandHandler(BungeeCommandAssist pl, PluginManager mgr) {
        this.pl = pl;
        this.mgr = mgr;
    }


    public static ShortServerCommandHandler register(BungeeCommandAssist plugin, PluginManager pluginManager) {
        ShortServerCommandHandler self = new ShortServerCommandHandler(plugin, pluginManager);
        pluginManager.registerListener(plugin, self);
        self.registerCommands();
        self.fakeCommand = CommandWrapper.create(plugin, SHORT_SERVER_PREFIX, SHORT_SERVER_PERMISSION, (s, a) -> {}, self::unsafeCompleteServers);
        return self;
    }

    public void unregister() {
        mgr.unregisterListener(this);
        mgr.unregisterCommand(fakeCommand);
        unregisterCommands();
    }


    // methods

    public void registerCommands() {
        unregisterCommands();

        getServers().forEach(server -> {
            CommandWrapper command = CommandWrapper.create(
                    pl, SHORT_SERVER_PREFIX + server, SHORT_SERVER_PERMISSION,
                    (s, a) -> executeServer(s, server), null);
            dynamics.add(command);
        });
    }

    public void unregisterCommands() {
        for (CommandWrapper command : Sets.newHashSet(dynamics)) {
            mgr.unregisterCommand(command);
            dynamics.remove(command);
        }
    }


    // private methods

    private void executeServer(CommandSender sender, String name) {
        mgr.dispatchCommand(sender, "server " + name);
    }

    private Set<String> getServers() {
        return ProxyServer.getInstance().getServers().keySet();
    }

    public Iterable<String> unsafeCompleteServers(CommandSender s, String[] args) {
        if (args.length == 1) {
            if (args[0].isEmpty())
                return getServers();
            return findEntries(args[0], getServers());
        }
        return Collections.emptyList();
    }

    private Logger getLogger() {
        return pl.getLogger();
    }


    // listeners

    @EventHandler
    public void onCommand(ChatEvent event) {
        if (!event.isCommand() || event.isCancelled())
            return;

        ProxiedPlayer p = (ProxiedPlayer) event.getSender();
        if (!p.hasPermission(SHORT_SERVER_PERMISSION))
            return;

        String message = event.getMessage();
        if (!message.startsWith("/" + SHORT_SERVER_PREFIX))
            return;

        event.setCancelled(true);

        if (!p.hasPermission(SHORT_SERVER_PERMISSION)) {
            p.sendMessage(pl.getProxy().getTranslation("no_permission"));
            return;
        }

        getLogger().info(p.getName() + " executed command: " + message);

        String[] split = message.split(" ", 3);
        String input = split[0].substring(SHORT_SERVER_PREFIX.length() + 1);  // add '/'
        if (input.isEmpty() && split.length >= 2) {
            input = split[1];
        }

        if (input.isEmpty()) {
            pl.sendWithPrefix(p, Lang.SPECIFY_SERVER);
            return;
        }

        String suggest;
        try {
            suggest = findOnes(input, getServers());

        } catch (NullPointerException e) {
            pl.sendWithPrefix(p, Lang.FAILED_ASSIST);
            return;
        }

        if (suggest == null) {
            return;
        }

        executeServer(p, suggest);
    }


}
