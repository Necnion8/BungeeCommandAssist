package com.gmail.necnionch.myplugin.bungeecommandassist.bungee.config;

import com.gmail.necnionch.myplugin.bungeecommandassist.bungee.Lang;
import com.gmail.necnionch.myplugin.bungeecommandassist.common.command.CommandSender;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class LanguageConfig {

    private final Plugin owner;
    private final Map<String, BaseComponent[]> customLangMap = Maps.newHashMap();
    private final Map<String, Map<String, BaseComponent[]>> langMessages = Maps.newHashMap();

    public LanguageConfig(Plugin owner) {
        this.owner = owner;
    }

    public void load() {
        unload();

        File message = new File(owner.getDataFolder(), "message.yml");
        if (message.isFile()) {
            Configuration config = loadFile(message);
            if (config != null) {
                customLangMap.putAll(config.getKeys().stream().collect(Collectors.toMap(
                        k -> k, k -> TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', config.getString(k))))
                ));
                owner.getLogger().info("Loaded message.yml");
            }
        }
    }

    public void unload() {
        customLangMap.clear();
        langMessages.clear();
    }

    public BaseComponent[] getMessage(Lang message, @Nullable String locale) {
        if (!customLangMap.isEmpty()) {
            if (customLangMap.containsKey(message.getKey())) {
                return customLangMap.get(message.getKey());
            } else {
                return TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message.getDefaultMessage()));
            }
        }

        if (locale == null)
            locale = "en_us";  // fallback

        if (!langMessages.containsKey(locale)) {
            // load
            loadLang(locale);
        }

        if (langMessages.get(locale).containsKey(message.getKey())) {
            return langMessages.get(locale).get(message.getKey());
        } else {
            return TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message.getDefaultMessage()));
        }
    }

    public void send(CommandSender sender, Lang message) {
        sender.sendMessage(getMessage(message, sender.getLocale()));
    }


    public void loadLang(String lang) {
        Configuration config;
        try (InputStream is = owner.getResourceAsStream("lang/" + lang + ".yml")) {
            if (is == null) {
                langMessages.put(lang, Collections.emptyMap());
                owner.getLogger().warning("Unknown lang: " + lang);
                return;
            }
            try (InputStreamReader reader = new InputStreamReader(is, Charsets.UTF_8)) {
                config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(reader);
            }

        } catch (IOException e) {
            owner.getLogger().warning("Failed to load lang/" + lang + ".yml from resource");
            langMessages.put(lang, Collections.emptyMap());
            return;
        }

        langMessages.put(lang, config.getKeys().stream().collect(Collectors.toMap(
                k -> k, k -> TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', config.getString(k))))
        ));
    }

    private Configuration loadFile(File file) {
        try (InputStream is = new FileInputStream(file);
             InputStreamReader reader = new InputStreamReader(is, Charsets.UTF_8)) {
            return ConfigurationProvider.getProvider(YamlConfiguration.class).load(reader);

        } catch (IOException e) {
            owner.getLogger().warning("Failed to load " + file);
            return null;
        }
    }

}
