package com.gmail.necnionch.myplugin.bungeecommandassist.bungee.managers;

import com.gmail.necnionch.myplugin.bungeecommandassist.bungee.BungeeCommandAssist;
import com.gmail.necnionch.myplugin.bungeecommandassist.bungee.config.TabCompleteGenerator;
import com.gmail.necnionch.myplugin.bungeecommandassist.bungee.config.TabCompleteGeneratorEntry;
import com.gmail.necnionch.myplugin.bungeecommandassist.common.command.RootCommand;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TabCompleteGeneratorManager implements Listener {
    private final BungeeCommandAssist plugin;
    private Map<String, TabCompleteGenerator> entries = Collections.emptyMap();


    public TabCompleteGeneratorManager(BungeeCommandAssist plugin) {
        this.plugin = plugin;
    }


    public void update(Map<String, TabCompleteGenerator> entries) {
        this.entries = entries;
    }


    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        if (event.isCancelled())
            return;

        String[] split = event.getCursor().substring(1).split(" ", -1);

        if (split.length == 2) {
            TabCompleteGenerator generator = entries.get(split[0].toLowerCase(Locale.ROOT));
            if (generator == null)
                return;
            event.getSuggestions().addAll(RootCommand.generateSuggests(split[1], generator.generate()));
        }

    }


}
