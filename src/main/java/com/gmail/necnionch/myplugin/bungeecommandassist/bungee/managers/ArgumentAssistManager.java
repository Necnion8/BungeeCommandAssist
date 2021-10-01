package com.gmail.necnionch.myplugin.bungeecommandassist.bungee.managers;

import com.gmail.necnionch.myplugin.bungeecommandassist.bungee.BungeeCommandAssist;
import com.gmail.necnionch.myplugin.bungeecommandassist.bungee.config.PlayerConfig;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ArgumentAssistManager implements Listener {

    private final BungeeCommandAssist plugin;
    private final PlayerConfig playersConfig;
    private final Map<UUID, AssistCache> caches = Maps.newHashMap();

    public ArgumentAssistManager(BungeeCommandAssist plugin, PlayerConfig playersConfig) {
        this.plugin = plugin;
        this.playersConfig = playersConfig;
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTabComplete(TabCompleteEvent event) {
        if (event.isCancelled() || !(event.getSender() instanceof ProxiedPlayer))
            return;

        ProxiedPlayer sender = (ProxiedPlayer) event.getSender();

        if (!playersConfig.getTabAssists().contains(sender.getUniqueId()))
            return;

        String cursor = event.getCursor().substring(1);
        List<String> suggestions = Lists.newArrayList(event.getSuggestions());

        String[] split = cursor.split(" ", -1);
        String last = split[split.length - 1];

        AssistCache cache;

        if (last.isEmpty()) {
            cache = getCacheOrUpdate(sender.getUniqueId(), cursor, Lists.newArrayList(suggestions));
        } else {
            cache = getCache(sender.getUniqueId(), cursor);
        }

        plugin.printDebug("suggestions: " + String.join("|", event.getSuggestions()));

        if (cache == null)
            return;
        plugin.printDebug("cacheGet: \"" + cache.cursor + "\"");


        List<String> entries = BungeeCommandAssist.findEntries(last, cache.suggestions, .25f);

        entries.forEach(suggestions::remove);
        entries.addAll(suggestions);

        event.getSuggestions().clear();
        event.getSuggestions().addAll(entries);

        plugin.printDebug("reNew: " + String.join("|", event.getSuggestions()));
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        caches.remove(event.getPlayer().getUniqueId());
    }




    private String stripLast(String cursor) {
        String[] split = cursor.split(" ", -1);
        split[split.length - 1] = "";
        return String.join(" ", split);
    }

    private AssistCache getCacheOrUpdate(UUID sender, String cursor, List<String> suggestions) {
        cursor = stripLast(cursor);
        AssistCache cache = caches.get(sender);

        if (cache == null || !cache.cursor.equalsIgnoreCase(cursor)) {
            cache = new AssistCache(cursor, suggestions);
            caches.put(sender, cache);
        }
        return cache;
    }

    private AssistCache getCache(UUID sender, String cursor) {
        cursor = stripLast(cursor);

        AssistCache cache = caches.get(sender);
        if (cache != null && cache.cursor.equalsIgnoreCase(cursor)) {
            return cache;
        }
        return null;
    }




    public static class AssistCache {
        private final List<String> suggestions;
        private String cursor;

        public AssistCache(String cursor, List<String> suggestions) {
            this.cursor = cursor;
            this.suggestions = suggestions;
        }



    }

}
