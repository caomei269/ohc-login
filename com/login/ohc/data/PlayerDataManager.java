package com.login.ohc.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.login.ohc.Ohc_Login;
import net.minecraft.class_3222;
import net.minecraft.server.MinecraftServer;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 玩家数据管理器
 * 负责管理所有玩家的登录数据，包括加载、保存和查询
 */
public class PlayerDataManager {
    private static PlayerDataManager instance;
    private final Map<String, PlayerData> playerDataMap;
    private final Gson gson;
    private Path dataFile;
    
    private PlayerDataManager() {
        this.playerDataMap = new HashMap<>();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }
    
    public static PlayerDataManager getInstance() {
        if (instance == null) {
            instance = new PlayerDataManager();
        }
        return instance;
    }
    
    /**
     * 初始化数据管理器
     */
    public void initialize(MinecraftServer server) {
        // 设置数据文件路径
        Path worldDir = server.method_27050(net.minecraft.class_5218.field_24188);
        this.dataFile = worldDir.resolve("ohc_login_data.json");
        
        // 加载现有数据
        _loadData();
        
        Ohc_Login.LOGGER.info("PlayerDataManager initialized with {} players", playerDataMap.size());
    }
    
    /**
     * 获取玩家数据
     */
    public PlayerData getPlayerData(String playerName) {
        return playerDataMap.computeIfAbsent(playerName, PlayerData::new);
    }
    
    /**
     * 获取玩家数据（通过ServerPlayerEntity）
     */
    public PlayerData getPlayerData(class_3222 player) {
        return getPlayerData(player.method_7334().getName());
    }
    
    /**
     * 保存玩家数据
     */
    public void savePlayerData(PlayerData playerData) {
        playerDataMap.put(playerData.getPlayerName(), playerData);
        _saveData();
    }
    
    /**
     * 移除玩家数据（玩家离开时调用）
     */
    public void removePlayerData(String playerName) {
        PlayerData data = playerDataMap.get(playerName);
        if (data != null) {
            data.logout();
            _saveData();
        }
    }
    
    /**
     * 检查玩家是否已登录
     */
    public boolean isPlayerLoggedIn(String playerName) {
        PlayerData data = playerDataMap.get(playerName);
        return data != null && data.isLoggedIn();
    }
    
    /**
     * 检查玩家是否已登录（通过ServerPlayerEntity）
     */
    public boolean isPlayerLoggedIn(class_3222 player) {
        return isPlayerLoggedIn(player.method_7334().getName());
    }
    
    /**
     * 获取所有在线但未登录的玩家数量
     */
    public int getUnloggedPlayerCount() {
        return (int) playerDataMap.values().stream()
                .filter(data -> !data.isLoggedIn())
                .count();
    }
    
    /**
     * 从文件加载数据
     */
    private void _loadData() {
        if (!Files.exists(dataFile)) {
            Ohc_Login.LOGGER.info("No existing player data file found, starting fresh");
            return;
        }
        
        try (Reader reader = Files.newBufferedReader(dataFile)) {
            Type type = new TypeToken<Map<String, PlayerData>>(){}.getType();
            Map<String, PlayerData> loadedData = gson.fromJson(reader, type);
            
            if (loadedData != null) {
                playerDataMap.clear();
                playerDataMap.putAll(loadedData);
                
                // 重置所有玩家的登录状态（服务器重启后需要重新登录）
                playerDataMap.values().forEach(PlayerData::logout);
                
                Ohc_Login.LOGGER.info("Loaded {} player data entries", playerDataMap.size());
            }
        } catch (IOException e) {
            Ohc_Login.LOGGER.error("Failed to load player data", e);
        }
    }
    
    /**
     * 保存数据到文件
     */
    private void _saveData() {
        try {
            // 确保目录存在
            Files.createDirectories(dataFile.getParent());
            
            try (Writer writer = Files.newBufferedWriter(dataFile)) {
                gson.toJson(playerDataMap, writer);
            }
        } catch (IOException e) {
            Ohc_Login.LOGGER.error("Failed to save player data", e);
        }
    }
    
    /**
     * 保存所有数据
     */
    public void saveAllData() {
        _saveData();
        Ohc_Login.LOGGER.info("All player data saved");
    }
    
    /**
     * 服务器关闭时保存所有数据
     */
    public void shutdown() {
        // 登出所有玩家
        playerDataMap.values().forEach(PlayerData::logout);
        _saveData();
        Ohc_Login.LOGGER.info("PlayerDataManager shutdown complete");
    }
    
    /**
     * 清理过期的冷却和封停状态
     */
    public void cleanupExpiredStates() {
        long currentTime = System.currentTimeMillis();
        playerDataMap.values().forEach(data -> {
            // 这里可以添加清理逻辑，但由于我们在检查时已经处理了过期状态，
            // 所以这个方法主要用于定期维护
        });
    }
}