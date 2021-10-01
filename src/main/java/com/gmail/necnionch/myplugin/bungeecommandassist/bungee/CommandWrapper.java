package com.gmail.necnionch.myplugin.bungeecommandassist.bungee;

import com.google.common.collect.Lists;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;


public class CommandWrapper extends Command implements TabExecutor {
    private final Executor executor;
    private final Completer completer;

    public CommandWrapper(String name, String permission, Executor executor, Completer completer) {
        super(name, permission);
        this.executor = executor;
        this.completer = completer;
    }


    @Override
    public void execute(CommandSender sender, String[] args) {
        executor.execute(sender, args);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return completer != null ? completer.onTabComplete(sender, args) : Collections.emptyList();
    }



    public interface Executor {
        void execute(CommandSender sender, String[] args);
    }

    public interface Completer {
        Iterable<String> onTabComplete(CommandSender sender, String[] args);


        static CompleteBuilder complete(CompleteBuilder.CompleteGenerator generator) {
            return new CompleteBuilder(generator);
        }

        static Iterable<String> generateSuggests(String arg, List<String> arguments) {
            String lowerArg = arg.toLowerCase(Locale.ROOT);
            return arguments.stream()
                    .filter(a -> a.toLowerCase(Locale.ROOT).startsWith(lowerArg))
                    .collect(Collectors.toList());
        }

    }


    public static class CompleteBuilder implements Completer {
        private final List<CompleteGenerator> generators = Lists.newArrayList();

        public CompleteBuilder(CompleteGenerator firstGenerator) {
            generators.add(firstGenerator);
        }

        public CompleteGenerator get(int index) {
            if (0 <= index  && index < generators.size())
                return generators.get(index);
            return null;
        }

        public CompleteBuilder complete(CompleteGenerator generator) {
            generators.add(generator);
            return this;
        }


        @Override
        public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
            CompleteGenerator generator = get(args.length - 1);
            if (generator == null)
                return Collections.emptyList();

            return Completer.generateSuggests(args[args.length - 1], generator.onGenerate());
        }


        public interface CompleteGenerator {
            List<String> onGenerate();
        }
    }


    public static CommandWrapper create(Plugin owner, String name, String permission, CommandWrapper.Executor executor, CommandWrapper.Completer completer) {
        CommandWrapper command = new CommandWrapper(name, permission, executor, completer);
        ProxyServer.getInstance().getPluginManager().registerCommand(owner, command);
        return command;
    }


}
