package com.mlc.mlcgames.utils.file;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final JavaPlugin plugin;
    private final Map<String, FileConfiguration> configs = new HashMap<>();

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public FileConfiguration loadConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        configs.put(fileName, config);
        return config;
    }

    public FileConfiguration getConfig(String fileName) {
        return configs.get(fileName);
    }

    public void saveConfig(String fileName) {
        try {
            configs.get(fileName).save(new File(plugin.getDataFolder(), fileName));
        } catch (IOException e) {
            e.fillInStackTrace();
        }
    }

    //重载指定配置文件
    public void reloadConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.getLogger().warning("尝试重载不存在的配置文件: " + fileName);
            return;
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        configs.put(fileName, config);
        plugin.getLogger().info("配置文件 " + fileName + " 已重载。");
    }

    //重载所有已加载过的配置文件
    public void reloadAllConfigs() {
        if (configs.isEmpty()) {
            plugin.getLogger().info("没有已加载的配置文件需要重载。");
            return;
        }
        for (String fileName : configs.keySet()) {
            reloadConfig(fileName); // 内部已经会处理不存在的文件
        }
        plugin.getLogger().info("所有配置文件重载完成。");
    }
}