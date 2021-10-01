package com.gmail.necnionch.myplugin.bungeecommandassist.bungee;

import com.gmail.necnionch.myplugin.bungeecommandassist.bungee.managers.WatcherManager;
import com.gmail.necnionch.myplugin.bungeecommandassist.common.command.Command;
import com.gmail.necnionch.myplugin.bungeecommandassist.common.command.CommandSender;
import com.gmail.necnionch.myplugin.bungeecommandassist.common.command.RootCommand;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class MainCommand extends RootCommand {
    private final BungeeCommandAssist pl;
    public static final String PERMS_RELOAD = "bungeecommandassist.command.reload";
    public static final String PERMS_WATCH = "bungeecommandassist.command.logwatch";
    public static final String PERMS_ASSIST = "bungeecommandassist.command.tabassist";


    public MainCommand(BungeeCommandAssist owner) {
        pl = owner;

        addCommand("reload", PERMS_RELOAD, this::execReload, (s, c, a) -> Collections.emptyList());
        addCommand("logwatch", PERMS_WATCH, this::execWatch, this::completeYesOrNo);
        addCommand("tabassist", PERMS_ASSIST, this::execAssist, this::completeYesOrNo);

    }



    private void execReload(CommandSender s, List<String> a) {
        pl.reloadConfig();
        pl.sendWithPrefix(s, "&7設定ファイルとコマンドを再読み込みしました");
    }


    private void execWatch(CommandSender s, List<String> a) {
        if (!(s.getSender() instanceof ProxiedPlayer)) {
            pl.sendWithPrefix(s, "&4プレイヤーコマンドです");
            return;
        }
        ProxiedPlayer p = (ProxiedPlayer) s.getSender();
        WatcherManager mgr = BungeeCommandAssist.getWatcherManager();
        if (a.isEmpty()) {
            boolean watching = !mgr.isWatching(p.getUniqueId());
            mgr.setTempWatching(p.getUniqueId(), watching);
            String color = (watching) ? "&2" : "&4";
            String text = (watching) ? "有効化" : "無効化";
            pl.sendWithPrefix(s, "&7コマンドログを" + color + "一時的に" + text + "&7しました");
        } else {
            String mode = a.get(0).toLowerCase(Locale.ROOT);

            boolean watching;
            if (mode.equalsIgnoreCase("enable") || mode.equalsIgnoreCase("on")) {
                watching = true;
            } else if (mode.equalsIgnoreCase("disable") || mode.equalsIgnoreCase("off")) {
                watching = false;
            } else {
                pl.sendWithPrefix(s, "&con &7OR &coff &4を指定してください");
                return;
            }

            mgr.setWatching(p.getUniqueId(), watching);
            String text = (watching) ? "&2有効化" : "&4無効化";
            pl.sendWithPrefix(s, "&7コマンドログを" + text + "&7しました");
        }
    }

    private void execAssist(CommandSender s, List<String> a) {
        if (!(s.getSender() instanceof ProxiedPlayer)) {
            pl.sendWithPrefix(s, "&4プレイヤーコマンドです");
            return;
        }
        ProxiedPlayer p = (ProxiedPlayer) s.getSender();

        boolean assist;
        if (a.isEmpty()) {
            assist = !pl.getPlayersConfig().getTabAssists().contains(p.getUniqueId());
        } else {
            String mode = a.get(0);
            if (mode.equalsIgnoreCase("enable") || mode.equalsIgnoreCase("on")) {
                assist = true;
            } else if (mode.equalsIgnoreCase("disable") || mode.equalsIgnoreCase("off")) {
                assist = false;
            } else {
                pl.sendWithPrefix(s, "&con &7OR &coff &4を指定してください");
                return;
            }
        }

        if (assist) {
            pl.getPlayersConfig().getTabAssists().add(p.getUniqueId());
        } else {
            pl.getPlayersConfig().getTabAssists().remove(p.getUniqueId());
        }
        pl.getPlayersConfig().save();

        String text = (assist) ? "&2有効化" : "&4無効化";
        pl.sendWithPrefix(s, "&7タブ補完アシスト機能(β)を" + text + "&7しました");
    }

    @NotNull
    private List<String> completeYesOrNo(CommandSender s, String c, List<String> args) {
        if (args.size() == 1) {
            return generateSuggests(args.get(0), Arrays.asList("disable", "enable", "on", "off"));
        }
        return Collections.emptyList();
    }



}
