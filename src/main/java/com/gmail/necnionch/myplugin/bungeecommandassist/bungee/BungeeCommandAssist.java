package com.gmail.necnionch.myplugin.bungeecommandassist.bungee;

import com.gmail.necnionch.myplugin.bungeecommandassist.bungee.config.PlayerConfig;
import com.gmail.necnionch.myplugin.bungeecommandassist.bungee.managers.*;
import com.gmail.necnionch.myplugin.bungeecommandassist.common.command.CommandBungee;
import com.gmail.necnionch.myplugin.bungeecommandassist.common.command.CommandSender;
import com.gmail.necnionch.myplugin.bungeecommandassist.common.dataio.BukkitPlayerCommandPacket;
import com.gmail.necnionch.myplugin.bungeecommandassist.common.dataio.PluginMessaging;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.event.EventHandler;
import org.apache.lucene.search.spell.JaroWinklerDistance;

import java.util.*;
import java.util.stream.Collectors;


public final class BungeeCommandAssist extends Plugin implements Listener {
    private static BungeeCommandAssist instance;
    private final PluginManager mgr = getProxy().getPluginManager();
    private final MainConfig mainConfig = new MainConfig(this);
    private final PlayerConfig playersConfig = new PlayerConfig(this);
    private final Command mainCommand = CommandBungee.build(new MainCommand(this), "bcommandassist", null, "bacmd");
    private final PluginMessaging.BungeeSender messagingSender = new PluginMessaging.BungeeSender(this);

    private final BungeeCommandManager bungeeManager = new BungeeCommandManager(this);
    private final BukkitCommandManager bukkitManager = new BukkitCommandManager(this);
    private final QuickServerCommandManager quickManager = new QuickServerCommandManager(this);
    private final WatcherManager watcherManager = new WatcherManager(this, playersConfig);
    private final ArgumentAssistManager argumentAssistManager = new ArgumentAssistManager(this, playersConfig);
    private final TabCompleteGeneratorManager generatorManager = new TabCompleteGeneratorManager(this);
    private ShortServerCommandHandler shortServerCommandHandler;

    private boolean firstInit = true;


    @Override
    public void onLoad() {
        BungeeCommandManager.initReflections(this);
    }

    @Override
    public void onEnable() {
        instance = this;

        mgr.registerCommand(this, mainCommand);
        mgr.registerListener(this, this);
        mgr.registerListener(this, bungeeManager);
        mgr.registerListener(this, bukkitManager);
        mgr.registerListener(this, watcherManager);
        mgr.registerListener(this, argumentAssistManager);
        mgr.registerListener(this, generatorManager);

        messagingSender.registerChannel();

        reloadConfig();

        new MetricsLite(this, 16372);
    }

    @Override
    public void onDisable() {
        bungeeManager.unregisterCommands();
        bukkitManager.unregisterCommands();
        quickManager.unregisterCommands();

        mgr.unregisterCommands(this);
        mgr.unregisterListeners(this);
        messagingSender.unregisterChannel();
    }


    public void printDebug(String line, Object... args) {
        if (mainConfig.isDebug()) {
            if (args.length == 0) {
                getLogger().warning("[DEBUG] " + line);
            } else {
                getLogger().warning("[DEBUG] " + String.format(line, args));
            }
        }
    }


    public void reloadConfig() {
        mainConfig.load();
        playersConfig.load();
        bungeeManager.registerCommands(mainConfig.getBungeeCommands());
        bukkitManager.registerCommands(mainConfig.getBukkitCommands());
        quickManager.registerCommands(mainConfig.getServerCommands());
        watcherManager.update(mainConfig.getWatcherConfig());
        generatorManager.update(mainConfig.getCompleteGenerators());

        if (shortServerCommandHandler != null) {
            shortServerCommandHandler.unregister();
            shortServerCommandHandler = null;
        }
        if (mainConfig.isEnableShortServerCommand()) {
            shortServerCommandHandler = ShortServerCommandHandler.register(this, mgr);
        }

    }

    public MainConfig getMainConfig() {
        return mainConfig;
    }

    public PlayerConfig getPlayersConfig() {
        return playersConfig;
    }

    public void sendWithPrefix(CommandSender sender, String message) {
        message = ChatColor.translateAlternateColorCodes('&', mainConfig.getCommandPrefix() + message);
        sender.sendMessage(TextComponent.fromLegacyText(message));
    }

    public void sendWithPrefix(net.md_5.bungee.api.CommandSender sender, String message) {
        message = ChatColor.translateAlternateColorCodes('&', mainConfig.getCommandPrefix() + message);
        sender.sendMessage(TextComponent.fromLegacyText(message));
    }

    public void sendCommandRequestToBukkit(ProxiedPlayer player, String command) {
        messagingSender.sendData(player, "playerCommand", new BukkitPlayerCommandPacket(command));
    }


    // static

    public static String findOnes(String param, Collection<String> entries) {
        if (entries.contains(param))
            return param;
        if (entries.isEmpty())
            return null;

        JaroWinklerDistance dis = new JaroWinklerDistance();

        List<String> list = entries.stream()
                .collect(Collectors.toMap((s) -> s, (s) -> dis.getDistance(param, s)))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .filter(e -> e.getValue() >= 0.7)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (list.isEmpty())
            throw new NullPointerException("no match");

        return list.get(list.size() - 1);
    }

    public static List<String> findEntries(String param, Collection<String> entries) {
        return findEntries(param, entries, 0.5f);
    }

    public static List<String> findEntries(String param, Collection<String> entries, float level) {
        if (entries.isEmpty())
            return Collections.emptyList();

        JaroWinklerDistance dis = new JaroWinklerDistance();

        List<String> list = entries.stream()
                .collect(Collectors.toMap((s) -> s, (s) -> dis.getDistance(param, s)))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .filter(e -> e.getValue() >= level)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        Collections.reverse(list);
        return list;
    }

    public static BungeeCommandManager getBungeeCommandManager() {
        return Objects.requireNonNull(instance, "Plugin is not enabled").bungeeManager;
    }

    public static BukkitCommandManager getBukkitCommandManager() {
        return Objects.requireNonNull(instance, "Plugin is not enabled").bukkitManager;
    }

    public static QuickServerCommandManager getQuickManager() {
        return Objects.requireNonNull(instance, "Plugin is not enabled").quickManager;
    }

    public static WatcherManager getWatcherManager() {
        return Objects.requireNonNull(instance, "Plugin is not enabled").watcherManager;
    }

    // listeners

    @EventHandler
    public void onServerConnect(PostLoginEvent event) {
        if (firstInit) {
            firstInit = false;
            getLogger().info("Commands first reInitializing");
            bungeeManager.registerCommands(mainConfig.getBungeeCommands());
        }
    }

}
