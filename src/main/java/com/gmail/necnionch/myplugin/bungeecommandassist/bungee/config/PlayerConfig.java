package com.gmail.necnionch.myplugin.bungeecommandassist.bungee.config;

import com.gmail.necnionch.myplugin.bungeecommandassist.common.BungeeConfigDriver;
import com.google.common.collect.Sets;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerConfig extends BungeeConfigDriver {
    public PlayerConfig(Plugin plugin) {
        super(plugin, "players.yml", "empty.yml");
    }

    private Set<UUID> watchers = Sets.newHashSet();
    private Set<UUID> tabAssists = Sets.newHashSet();


    public Set<UUID> getWatchers() {
        return watchers;
    }

    public Set<UUID> getTabAssists() {
        return tabAssists;
    }


    @Override
    public boolean onLoaded(Configuration config) {
        if (super.onLoaded(config)) {
            watchers.clear();
            for (String str : config.getStringList("watchers")) {
                try {
                    watchers.add(UUID.fromString(str));
                } catch (IllegalArgumentException ignored) {}
            }

            tabAssists.clear();
            for (String str : config.getStringList("tab-assists")) {
                try {
                    tabAssists.add(UUID.fromString(str));
                } catch (IllegalArgumentException ignored) {}
            }

            return true;
        }
        return false;
    }

    @Override
    public boolean save() {
        config.set("watchers", watchers.stream().map(UUID::toString).collect(Collectors.toList()));
        config.set("tab-assists", tabAssists.stream().map(UUID::toString).collect(Collectors.toList()));
        return super.save();
    }

}
