package com.vypalstudios.BungeeFriends.util;

import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.vypalstudios.BungeeFriends.BungeeFriends;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

// ------BungeeFriends------
// author: vyPal
// -------------------------
public class ConfigHelper {

    private final BungeeFriends plugin;

    private final String name;
    private Configuration config;
    private File file;

    public ConfigHelper(String name, BungeeFriends plugin) throws IOException {
        this.name = name;
        this.plugin = plugin;
        createConfig();
    }

    public ConfigHelper(String name, String preset, BungeeFriends plugin) throws IOException {
        this.name = name;
        this.plugin = plugin;
        createConfig(preset);
    }

    private void createConfig() throws IOException {
        file = new File(plugin.getDataFolder().toString(), name + ".yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
    }

    private void createConfig(String preset) throws IOException {
        file = new File(plugin.getDataFolder().toString(), name + ".yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
            ByteStreams.copy(getClass().getResourceAsStream("/" + preset + ".yml"), new FileOutputStream(file));
        }
        config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
    }

    public void setObject(String path, Object object) throws IOException {
        config.set(path, object);
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, file);
    }

    public Configuration getConfig() {
        return config;
    }
}
