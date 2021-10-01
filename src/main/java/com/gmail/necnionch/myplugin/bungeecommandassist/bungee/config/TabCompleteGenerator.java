package com.gmail.necnionch.myplugin.bungeecommandassist.bungee.config;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;

import java.util.Collection;
import java.util.stream.Collectors;


public interface TabCompleteGenerator {
    Collection<String> generate();


    static TabCompleteGenerator makePlayers() {
        return () -> ProxyServer.getInstance().getPlayers().stream().map(CommandSender::getName).collect(Collectors.toList());
    }

    static TabCompleteGenerator makeServers() {
        return () -> ProxyServer.getInstance().getServers().keySet();
    }

}
