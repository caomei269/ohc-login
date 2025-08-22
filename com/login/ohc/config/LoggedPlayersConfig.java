package com.login.ohc.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * 已登录玩家配置管理器
 * 负责管理已成功登录玩家的昵称列表
 */
public class LoggedPlayersConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggedPlayersConfig.class);
    private static LoggedPlayersConfig _instance;
    private static final String CONFIG_FILE_NAME = "logged_players.json";
    private static final String CONFIG_DIR = "config";
    
    private JsonObject _config;
    private final Gson _gson;
    private Set<String> _loggedPlayerNames;
    
    private LoggedPlayersConfig() {
        _gson = new GsonBuilder().setPrettyPrinting().create();
        _loggedPlayerNames = new HashSet<>();
        _loadConfig();
    }
    
    /**
     * 获取单例实例
     */
    public static LoggedPlayersConfig getInstance() {
        if (_instance == null) {
            _instance = new LoggedPlayersConfig();
        }
        return _instance;
    }
    
    /**
     * 重新加载配置
     */
    public void reload() {
        _loadConfig();
        LOGGER.info("已登录玩家配置已重新加载");
    }
    
    /**
     * 加载配置文件
     */
    private void _loadConfig() {
        Path configPath = Paths.get(CONFIG_DIR, CONFIG_FILE_NAME);
        File configFile = configPath.toFile();
        
        if (!configFile.exists()) {
            _createDefaultConfig(configFile);
        }
        
        try (FileReader reader = new FileReader(configFile)) {
            _config = _gson.fromJson(reader, JsonObject.class);
            LOGGER.info("已登录玩家配置文件加载成功: {}", configPath);
            
            // 加载玩家昵称列表到内存
            _loadPlayerNamesFromConfig();
            
        } catch (IOException e) {
            LOGGER.error("加载已登录玩家配置文件失败: {}", configPath, e);
            _config = _createDefaultConfigObject();
            _loggedPlayerNames.clear();
        }
    }
    
    /**
     * 从配置对象加载玩家昵称到内存
     */
    private void _loadPlayerNamesFromConfig() {
        _loggedPlayerNames.clear();
        
        if (_config.has("logged_players")) {
            JsonArray playersArray = _config.getAsJsonArray("logged_players");
            for (int i = 0; i < playersArray.size(); i++) {
                String playerName = playersArray.get(i).getAsString();
                _loggedPlayerNames.add(playerName);
            }
        }
        
        LOGGER.info("已加载 {} 个已登录玩家昵称", _loggedPlayerNames.size());
    }
    
    /**
     * 创建默认配置文件
     */
    private void _createDefaultConfig(File configFile) {
        try {
            // 确保配置目录存在
            Files.createDirectories(configFile.getParentFile().toPath());
            
            JsonObject defaultConfig = _createDefaultConfigObject();
            
            try (FileWriter writer = new FileWriter(configFile)) {
                _gson.toJson(defaultConfig, writer);
            }
            
            LOGGER.info("已创建默认已登录玩家配置文件: {}", configFile.getPath());
        } catch (IOException e) {
            LOGGER.error("创建默认已登录玩家配置文件失败", e);
        }
    }
    
    /**
     * 创建默认配置对象
     */
    private JsonObject _createDefaultConfigObject() {
        JsonObject config = new JsonObject();
        
        // 配置文件版本号
        config.addProperty("config_version", "2.5.0");
        
        // 已登录玩家昵称列表
        JsonArray loggedPlayers = new JsonArray();
        config.add("logged_players", loggedPlayers);
        
        return config;
    }
    
    /**
     * 添加已登录玩家昵称
     */
    public void addLoggedPlayer(String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) {
            return;
        }
        
        String trimmedName = playerName.trim();
        
        // 如果已经存在，不重复添加
        if (_loggedPlayerNames.contains(trimmedName)) {
            return;
        }
        
        _loggedPlayerNames.add(trimmedName);
        _saveConfig();
        
        LOGGER.info("已添加登录玩家昵称: {}", trimmedName);
    }
    
    /**
     * 移除已登录玩家昵称
     */
    public void removeLoggedPlayer(String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) {
            return;
        }
        
        String trimmedName = playerName.trim();
        
        if (_loggedPlayerNames.remove(trimmedName)) {
            _saveConfig();
            LOGGER.info("已移除登录玩家昵称: {}", trimmedName);
        }
    }
    
    /**
     * 检查玩家昵称是否已登录过
     */
    public boolean isPlayerLogged(String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) {
            return false;
        }
        
        return _loggedPlayerNames.contains(playerName.trim());
    }
    
    /**
     * 获取所有已登录玩家昵称
     */
    public Set<String> getAllLoggedPlayers() {
        return new HashSet<>(_loggedPlayerNames);
    }
    
    /**
     * 清空所有已登录玩家昵称
     */
    public void clearAllLoggedPlayers() {
        _loggedPlayerNames.clear();
        _saveConfig();
        LOGGER.info("已清空所有登录玩家昵称");
    }
    
    /**
     * 保存配置到文件
     */
    private void _saveConfig() {
        // 更新配置对象中的玩家列表
        JsonArray playersArray = new JsonArray();
        for (String playerName : _loggedPlayerNames) {
            playersArray.add(playerName);
        }
        _config.add("logged_players", playersArray);
        
        // 保存到文件
        Path configPath = Paths.get(CONFIG_DIR, CONFIG_FILE_NAME);
        File configFile = configPath.toFile();
        
        try (FileWriter writer = new FileWriter(configFile)) {
            _gson.toJson(_config, writer);
        } catch (IOException e) {
            LOGGER.error("保存已登录玩家配置文件失败", e);
        }
    }
    
    /**
     * 获取已登录玩家数量
     */
    public int getLoggedPlayersCount() {
        return _loggedPlayerNames.size();
    }
}