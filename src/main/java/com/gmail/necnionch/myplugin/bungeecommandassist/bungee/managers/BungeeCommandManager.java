package com.gmail.necnionch.myplugin.bungeecommandassist.bungee.managers;

import com.gmail.necnionch.myplugin.bungeecommandassist.bungee.BungeeCommandAssist;
import com.gmail.necnionch.myplugin.bungeecommandassist.bungee.config.CommandEntry;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.*;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class BungeeCommandManager implements Listener {
    private static Field commandsByPluginField;
    private final BungeeCommandAssist plugin;
    private final PluginManager mgr;
    private final Set<DisabledCommand> disabledCommands = Sets.newHashSet();  // 無効化したオリジナルのコマンド
    private final Set<RenamedCommand> newCommands = Sets.newHashSet();  // 新たに登録したコマンド
    private final Set<TabCompleteEvent> wrappedEvents = Sets.newHashSet();  // TabCompleteイベントの内部処理
    private final Set<EventContext> cancellingEvents = Sets.newHashSet();  // オリジナルによるイベント処理をバイパスする


    public BungeeCommandManager(BungeeCommandAssist plugin) {
        this.plugin = plugin;
        mgr = plugin.getProxy().getPluginManager();
    }


    public void registerCommands(List<CommandEntry> entries) {
        long processTime = System.currentTimeMillis();

        unregisterCommands();

        entries = Lists.newArrayList(entries);
        Map<String, Command> bungeeCommands = getBungeeCommands();

        plugin.printDebug("Registering bungee commands...");

        for (Iterator<CommandEntry> it = entries.iterator(); it.hasNext(); ) {
            CommandEntry entry = it.next();
            Command origCommand = bungeeCommands.get(entry.getOriginal());

            if (origCommand == null)
                continue;

            it.remove();
            Plugin owner;
            String ownerName;
            try {
                owner = searchCommandOwner(mgr, origCommand);
                ownerName = (owner != null) ? owner.getDescription().getName() : "null";
            } catch (Throwable e) {
                plugin.getLogger().warning("Failed to search command owner (/" + entry.getOriginal() + "): " + e.getMessage());
                owner = plugin;
                ownerName = "N/A";
            }
            plugin.printDebug("  ENTRY TYPE: " + entry.getType().name() + ", ORIG: /" + origCommand.getName() + " (by " + ownerName + ")");


            if (CommandEntry.Type.ALIAS.equals(entry.getType())) {
                registerClone(entry, owner, origCommand);

            } else if (CommandEntry.Type.RENAME.equals(entry.getType())) {
                disable(owner, origCommand);
                registerClone(entry, owner, origCommand);

            } else {
                plugin.getLogger().warning("    ..." + entry.getType().name().toLowerCase() + " type? don't know");
            }

        }


        processTime = System.currentTimeMillis() - processTime;
        plugin.getLogger().info("Loaded bungee commands. (" + processTime + "ms / " + "done: " + newCommands.size() + ", fail: " + entries.size() + ")");

        if (!entries.isEmpty()) {
            Set<String> unknownCommands = entries.stream()
                    .map(CommandEntry::getOriginal)
                    .collect(Collectors.toSet());
            plugin.getLogger().warning("Could not get commands: " + String.join(", ", unknownCommands));
        }

    }

    public void unregisterCommands() {
        if (disabledCommands.size() + newCommands.size() <= 0)
            return;

        plugin.printDebug("Unregistering commands...");

        for (DisabledCommand disabled : disabledCommands) {
            Plugin owner = disabled.getPlugin();
            if (owner == null || mgr.getPlugins().contains(owner)) {  // check enable state
                mgr.registerCommand(owner, disabled.getCommand());
                plugin.printDebug("  (Re)registering original command: " + disabled.getCommand().getName());
            } else {
                plugin.printDebug("  (Re)registering original command: " + disabled.getCommand().getName() + " (owner's disabled, skip)");
            }
        }
        disabledCommands.clear();

        for (RenamedCommand command : newCommands) {
            mgr.unregisterCommand(command);
            plugin.printDebug("  unregistering remap/renamed command: " + command.getName());
        }
        newCommands.clear();

        plugin.getLogger().info("Unloaded bungee commands.");
    }



    // methods

    private void registerClone(CommandEntry entry, Plugin owner, Command original) {
        RenamedCommand renamedCommand;
        if (original instanceof TabExecutor) {
            renamedCommand = new RenamedCommand(entry.getChanged(), original, null);
            plugin.printDebug("    register renamed : " + entry.getChanged() + "  (with TabExecutor)");
        } else {
            renamedCommand = new RenamedCommand(entry.getChanged(), original, ((sender, args) -> {
                if (!(sender instanceof Connection))
                    return Collections.emptyList();
                String cursor = "/" + original.getName() + " " + String.join(" ", args);
                Connection receiver = (sender instanceof ProxiedPlayer) ? ((ProxiedPlayer) sender).getServer() : null;
                TabCompleteEvent newEvent = new TabCompleteEvent((Connection) sender, receiver, cursor, Lists.newArrayList());
                wrappedEvents.add(newEvent);
                mgr.callEvent(newEvent);
                wrappedEvents.remove(newEvent);
                return newEvent.getSuggestions();
            }));
            plugin.printDebug("    register renamed : " + entry.getChanged() + "  (non TabExecutor, event handling mode)");
        }
        mgr.registerCommand(owner, renamedCommand);
        newCommands.add(renamedCommand);
    }

    private void disable(Plugin owner, Command command) {
        DisabledCommand disabledEntry = new DisabledCommand(owner, command);
        disabledCommands.add(disabledEntry);
        plugin.printDebug("    unregister original : " + command.getName());
        mgr.unregisterCommand(command);
    }


    private Map<String, Command> getBungeeCommands() {
        return mgr.getCommands().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public RenamedCommand[] getRenamedCommands() {
        return newCommands.toArray(new RenamedCommand[0]);
    }

    public DisabledCommand[] getDisabledCommands() {
        return disabledCommands.toArray(new DisabledCommand[0]);
    }


    // statics

    public static void initReflections(BungeeCommandAssist owner) {
        try {
            commandsByPluginField = owner.getProxy().getPluginManager().getClass().getDeclaredField("commandsByPlugin");
            commandsByPluginField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            owner.getLogger().warning("PluginManager.commandsByPlugin reflection error: " + e.getMessage());
            return;
        }

        try {
            //noinspection unchecked
            Multimap<Plugin, Command> tmp = (Multimap<Plugin, Command>) commandsByPluginField.get(owner.getProxy().getPluginManager());
        } catch (Throwable e) {
            owner.getLogger().warning("PluginManager.commandsByPlugin testing error: " + e.getMessage());
        }

    }

    public static Plugin searchCommandOwner(PluginManager mgr, Command command) {
        if (commandsByPluginField == null)
            return null;

        Multimap<Plugin, Command> commandsByPlugin;
        try {
            //noinspection unchecked
            commandsByPlugin = (Multimap<Plugin, Command>) commandsByPluginField.get(mgr);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }

        for (Map.Entry<Plugin, Command> e : commandsByPlugin.entries()) {
            if (e.getValue().equals(command))
                return e.getKey();
        }
        return null;
    }


    private static class EventContext {
        private final TabCompleteEvent event;
        private final List<String> suggestions;

        public EventContext(TabCompleteEvent event, List<String> suggestions) {
            this.event = event;
            this.suggestions = suggestions;
        }
    }


    // listeners

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTabCompleteFirst(TabCompleteEvent event) {
        if (event.isCancelled())
            return;

        if (wrappedEvents.contains(event))
            return;

        String[] split = event.getCursor().substring(1).split(" ", -1);

        for (DisabledCommand disabled : disabledCommands) {
            if (disabled.getCommand().getName().equalsIgnoreCase(split[0])) {
                cancellingEvents.add(new EventContext(event, Lists.newArrayList(event.getSuggestions())));
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTabCompleteFinal(TabCompleteEvent event) {
        for (EventContext context : cancellingEvents) {
            if (event.equals(context.event)) {
                cancellingEvents.remove(context);
                event.getSuggestions().clear();
                event.getSuggestions().addAll(context.suggestions);
                break;
            }
        }
    }





}
