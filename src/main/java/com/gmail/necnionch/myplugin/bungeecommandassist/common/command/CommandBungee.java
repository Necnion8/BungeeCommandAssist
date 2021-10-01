package com.gmail.necnionch.myplugin.bungeecommandassist.common.command;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;


public class CommandBungee {
    public static net.md_5.bungee.api.plugin.Command build(RootCommand command, String name, String permission, String... aliases) {
        class BungeeWrapper extends net.md_5.bungee.api.plugin.Command implements TabExecutor {
            public BungeeWrapper() {
                super(name, permission, aliases);
            }

            @Override
            public void execute(net.md_5.bungee.api.CommandSender sender, String[] args) {
                command.execute(conv(sender), listFrom(args));
            }

            @Override
            public Iterable<String> onTabComplete(net.md_5.bungee.api.CommandSender sender, String[] args) {
                return command.tabComplete(conv(sender), "", listFrom(args));
            }

            @Override
            public boolean hasPermission(net.md_5.bungee.api.CommandSender sender) {
                if (permission == null)
                    return super.hasPermission(sender);
                return !command.getCommands(conv(sender)).isEmpty();
            }
        }

        return new BungeeWrapper();
    }


    public static Sender conv(net.md_5.bungee.api.CommandSender sender) {
        return new Sender(sender);
    }

    public static ArrayList<String> listFrom(String[] args) {
        return new ArrayList<>(Arrays.asList(args));
    }



    public static class Sender implements CommandSender {
        private final net.md_5.bungee.api.CommandSender sender;

        public Sender(net.md_5.bungee.api.CommandSender sender) {
            this.sender = sender;
        }

        @Override
        public net.md_5.bungee.api.CommandSender getSender() {
            return sender;
        }

        @Override
        public void sendMessage(BaseComponent[] components) {
            sender.sendMessage(components);
        }

        @Override
        public boolean hasPermission(String permission) {
            return sender.hasPermission(permission);
        }

        @Override
        public boolean hasPermission(Command command) {
            return command.getPermission() == null || sender.hasPermission(command.getPermission());
        }

        @Override
        public String getLocale() {
            if (sender instanceof ProxiedPlayer) {
                Locale locale = ((ProxiedPlayer) sender).getLocale();
                if (locale != null)
                    return String.format("%s_%s", locale.getLanguage(), locale.getCountry()).toLowerCase(Locale.ROOT);
            }
            return null;
        }

    }

}
