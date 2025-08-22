package com.login.ohc.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

/**
 * 消息配置管理器
 * 负责加载和管理登录系统的所有消息文本配置
 */
public class MessageConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageConfig.class);
    private static MessageConfig _instance;
    private static final String CONFIG_FILE_NAME = "login_messages.json";
    private static final String CONFIG_DIR = "config";
    
    private JsonObject _config;
    private final Gson _gson;
    
    private MessageConfig() {
        _gson = new GsonBuilder().setPrettyPrinting().create();
        _loadConfig();
    }
    
    /**
     * 获取单例实例
     */
    public static MessageConfig getInstance() {
        if (_instance == null) {
            _instance = new MessageConfig();
        }
        return _instance;
    }
    
    /**
     * 重新加载配置
     */
    public void reload() {
        _loadConfig();
        LOGGER.info("消息配置已重新加载");
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
            LOGGER.info("消息配置文件加载成功: {}", configPath);
            
            // 检查配置文件版本并补全缺失项
            _checkAndUpdateConfigVersion(configFile);
            
        } catch (IOException e) {
            LOGGER.error("加载消息配置文件失败: {}", configPath, e);
            _config = _createDefaultConfigObject();
        }
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
            
            LOGGER.info("已创建默认消息配置文件: {}", configFile.getPath());
        } catch (IOException e) {
            LOGGER.error("创建默认消息配置文件失败", e);
        }
    }
    
    /**
     * 创建默认配置对象
     */
    private JsonObject _createDefaultConfigObject() {
        JsonObject config = new JsonObject();
        
        // 配置文件版本号
        config.addProperty("config_version", "2.0.0");
        
        // 欢迎消息
        JsonObject welcomeMessages = new JsonObject();
        welcomeMessages.addProperty("first_join_set_password", "§e欢迎来到服务器！请设置您的登录密码: /sp <密码>");
        welcomeMessages.addProperty("returning_player_login", "§e欢迎回来！请输入您的密码登录: /l <密码>");
        config.add("welcome_messages", welcomeMessages);
        
        // 命令消息
        JsonObject commandMessages = new JsonObject();
        commandMessages.addProperty("password_set_success", "§a密码设置成功！您已自动登录。");
        commandMessages.addProperty("password_set_failed", "§c密码设置失败，请重试。");
        commandMessages.addProperty("password_change_success", "§a密码修改成功！");
        commandMessages.addProperty("password_change_failed", "§c密码修改失败，旧密码错误。");
        commandMessages.addProperty("login_success", "§a登录成功！欢迎回来。");
        commandMessages.addProperty("login_failed", "§c登录失败，密码错误。");
        commandMessages.addProperty("already_logged_in", "§e您已经登录了。");
        commandMessages.addProperty("must_login_first", "§c请先登录后再执行此操作。");
        config.add("command_messages", commandMessages);
        
        // 限制消息
        JsonObject restrictionMessages = new JsonObject();
        restrictionMessages.addProperty("movement_restricted", "§c请先登录后再移动。");
        restrictionMessages.addProperty("interaction_restricted", "§c请先登录后再进行交互。");
        restrictionMessages.addProperty("chat_restricted", "§c请先登录后再聊天。");
        restrictionMessages.addProperty("command_restricted", "§c请先登录后再使用命令。");
        restrictionMessages.addProperty("item_restricted", "§c请先登录后再使用物品。");
        restrictionMessages.addProperty("container_restricted", "§c请先登录后再打开容器。");
        restrictionMessages.addProperty("portal_restricted", "§c请先登录后再使用传送门！");
        config.add("restriction_messages", restrictionMessages);
        
        // 错误消息
        JsonObject errorMessages = new JsonObject();
        errorMessages.addProperty("password_too_short", "§c密码太短！最少需要 {min} 个字符。");
        errorMessages.addProperty("password_too_long", "§c密码太长！最多允许 {max} 个字符。");
        errorMessages.addProperty("password_invalid_chars", "§c密码不能包含 / 或 \\ 字符。");
        errorMessages.addProperty("password_already_set", "§c您已经设置过密码了。如需修改请使用 /rp <旧密码> <新密码>");
        errorMessages.addProperty("password_not_set", "§c您还没有设置密码，请使用 /sp <密码> 设置密码。");
        errorMessages.addProperty("login_cooldown", "§c您在冷却期内，请等待 {time} 秒后再试。");
        errorMessages.addProperty("player_banned", "§c您的账号已被封停，剩余时间: {time} 小时。");
        errorMessages.addProperty("command_usage_ord", "§e用法: /sp <密码>");
        errorMessages.addProperty("command_usage_login", "§e用法: /l <密码>");
        errorMessages.addProperty("command_usage_changepassword", "§e用法: /rp <旧密码> <新密码>");
        config.add("error_messages", errorMessages);
        
        // ActionBar消息
        JsonObject actionbarMessages = new JsonObject();
        actionbarMessages.addProperty("login_required", "§c请输入密码登录: /l <密码>");
        actionbarMessages.addProperty("set_password_required", "§e请设置登录密码: /sp <密码>");
        actionbarMessages.addProperty("login_attempts_warning", "§c警告: 登录失败次数过多可能导致封停");
        config.add("actionbar_messages", actionbarMessages);
        
        // 管理员消息
        JsonObject adminMessages = new JsonObject();
        adminMessages.addProperty("player_login_success", "玩家 {player} 登录成功");
        adminMessages.addProperty("player_login_failed", "玩家 {player} 登录失败");
        adminMessages.addProperty("player_banned", "玩家 {player} 因多次登录失败被封停");
        adminMessages.addProperty("delete_password_success", "§a成功删除玩家 {player} 的密码");
        adminMessages.addProperty("delete_password_failed", "§c删除玩家 {player} 的密码失败: {reason}");
        adminMessages.addProperty("delete_password_not_found", "§c玩家 {player} 不存在或未设置密码");
        adminMessages.addProperty("reset_password_success", "§a成功为玩家 {player} 重置密码");
        adminMessages.addProperty("reset_password_failed", "§c为玩家 {player} 重置密码失败: {reason}");
        adminMessages.addProperty("admin_no_permission", "§c你没有权限执行此命令");
        adminMessages.addProperty("admin_player_not_found", "§c玩家 {player} 不存在");
        adminMessages.addProperty("admin_invalid_password", "§c密码格式无效");
        config.add("admin_messages", adminMessages);
        
        // 系统设置
        JsonObject settings = new JsonObject();
        settings.addProperty("max_login_attempts", 5);
        settings.addProperty("login_cooldown_seconds", 60);
        settings.addProperty("ban_duration_minutes", 60);
        settings.addProperty("password_min_length", 4);
        settings.addProperty("password_max_length", 20);
        settings.addProperty("enable_actionbar_hints", true);
        settings.addProperty("enable_admin_notifications", true);
        settings.addProperty("enable_blindness_effect", true);
        settings.addProperty("blindness_effect_level", 2);
        settings.addProperty("blindness_effect_duration", 999999);
        settings.addProperty("kick_delay_seconds", 3);
        settings.addProperty("enable_chat_restriction", true);
        config.add("settings", settings);
        
        return config;
    }
    
    /**
     * 检查配置文件版本并更新缺失的配置项
     */
    private void _checkAndUpdateConfigVersion(File configFile) {
        String currentVersion = "2.5.0";
        String configVersion = _getConfigVersion();
        
        if (!currentVersion.equals(configVersion)) {
            LOGGER.info("检测到配置文件版本不匹配 - 当前: {}, 配置文件: {}", currentVersion, configVersion);
            
            // 如果检测不到版本号（返回默认值1.0.0），直接全部替换配置
            if ("1.0.0".equals(configVersion)) {
                LOGGER.info("检测不到配置文件版本号，执行全部替换");
                _config = _createDefaultConfigObject();
                _saveUpdatedConfig(configFile);
                LOGGER.info("配置文件已完全替换为版本: {}", currentVersion);
            } else {
                // 版本号存在但不匹配，执行合并更新
                JsonObject defaultConfig = _createDefaultConfigObject();
                boolean hasUpdates = _mergeConfigUpdates(defaultConfig);
                
                if (hasUpdates) {
                    _saveUpdatedConfig(configFile);
                    LOGGER.info("配置文件已更新到版本: {}", currentVersion);
                }
            }
        }
    }
    
    /**
     * 获取配置文件版本号
     */
    private String _getConfigVersion() {
        if (_config != null && _config.has("config_version")) {
            return _config.get("config_version").getAsString();
        }
        return "1.0.0"; // 默认版本号
    }
    
    /**
     * 合并配置更新
     */
    private boolean _mergeConfigUpdates(JsonObject defaultConfig) {
        boolean hasUpdates = false;
        
        // 更新版本号
        if (!_config.has("config_version") || 
            !_config.get("config_version").getAsString().equals(defaultConfig.get("config_version").getAsString())) {
            _config.addProperty("config_version", defaultConfig.get("config_version").getAsString());
            hasUpdates = true;
        }
        
        // 检查并补全各个配置段
        hasUpdates |= _mergeConfigSection("welcome_messages", defaultConfig);
        hasUpdates |= _mergeConfigSection("command_messages", defaultConfig);
        hasUpdates |= _mergeConfigSection("restriction_messages", defaultConfig);
        hasUpdates |= _mergeConfigSection("error_messages", defaultConfig);
        hasUpdates |= _mergeConfigSection("actionbar_messages", defaultConfig);
        hasUpdates |= _mergeConfigSection("admin_messages", defaultConfig);
        hasUpdates |= _mergeConfigSection("settings", defaultConfig);
        
        return hasUpdates;
    }
    
    /**
     * 合并指定配置段
     */
    private boolean _mergeConfigSection(String sectionName, JsonObject defaultConfig) {
        boolean hasUpdates = false;
        
        if (!defaultConfig.has(sectionName)) {
            return false;
        }
        
        JsonObject defaultSection = defaultConfig.getAsJsonObject(sectionName);
        
        // 如果配置文件中没有这个段，直接添加
        if (!_config.has(sectionName)) {
            _config.add(sectionName, defaultSection);
            LOGGER.info("添加缺失的配置段: {}", sectionName);
            return true;
        }
        
        JsonObject currentSection = _config.getAsJsonObject(sectionName);
        
        // 检查并添加缺失的配置项
        for (String key : defaultSection.keySet()) {
            if (!currentSection.has(key)) {
                currentSection.add(key, defaultSection.get(key));
                LOGGER.info("添加缺失的配置项: {}.{}", sectionName, key);
                hasUpdates = true;
            }
        }
        
        return hasUpdates;
    }
    
    /**
     * 保存更新后的配置文件
     */
    private void _saveUpdatedConfig(File configFile) {
        try (FileWriter writer = new FileWriter(configFile)) {
            _gson.toJson(_config, writer);
            LOGGER.info("配置文件已保存: {}", configFile.getPath());
        } catch (IOException e) {
            LOGGER.error("保存配置文件失败", e);
        }
    }
    
    /**
     * 获取欢迎消息
     */
    public String getWelcomeMessage(String key) {
        return _getMessageFromSection("welcome_messages", key, "§e欢迎消息未配置");
    }
    
    /**
     * 获取命令消息
     */
    public String getCommandMessage(String key) {
        return _getMessageFromSection("command_messages", key, "§e命令消息未配置");
    }
    
    /**
     * 获取限制消息
     */
    public String getRestrictionMessage(String key) {
        return _getMessageFromSection("restriction_messages", key, "§c操作受限");
    }
    
    /**
     * 获取错误消息
     */
    public String getErrorMessage(String key) {
        return _getMessageFromSection("error_messages", key, "§c发生错误");
    }
    
    /**
     * 获取ActionBar消息
     */
    public String getActionbarMessage(String key) {
        return _getMessageFromSection("actionbar_messages", key, "§e提示消息");
    }
    
    /**
     * 获取管理员消息
     */
    public String getAdminMessage(String key) {
        return _getMessageFromSection("admin_messages", key, "管理员消息未配置");
    }
    
    /**
     * 从指定节获取消息
     */
    private String _getMessageFromSection(String section, String key, String defaultValue) {
        try {
            if (_config.has(section)) {
                JsonObject sectionObj = _config.getAsJsonObject(section);
                if (sectionObj.has(key)) {
                    return sectionObj.get(key).getAsString();
                }
            }
        } catch (Exception e) {
            LOGGER.warn("获取消息配置失败: {}.{}", section, key, e);
        }
        return defaultValue;
    }
    
    /**
     * 获取密码最小长度
     */
    public int getPasswordMinLength() {
        return _getSettingAsInt("password_min_length", 4);
    }
    
    /**
     * 获取密码最大长度
     */
    public int getPasswordMaxLength() {
        return _getSettingAsInt("password_max_length", 20);
    }
    
    /**
     * 获取最大登录尝试次数
     */
    public int getMaxLoginAttempts() {
        return _getSettingAsInt("max_login_attempts", 5);
    }
    
    /**
     * 获取登录冷却时间（秒）
     */
    public int getLoginCooldownSeconds() {
        return _getSettingAsInt("login_cooldown_seconds", 300);
    }
    
    /**
     * 获取封停持续时间（分钟）
     */
    public int getBanDurationMinutes() {
        return _getSettingAsInt("ban_duration_minutes", 60);
    }
    
    /**
     * 是否启用ActionBar提示
     */
    public boolean isActionbarHintsEnabled() {
        return _getSettingAsBoolean("enable_actionbar_hints", true);
    }
    
    /**
     * 是否启用管理员通知
     */
    public boolean isAdminNotificationsEnabled() {
        return _getSettingAsBoolean("enable_admin_notifications", true);
    }
    
    /**
     * 是否启用失明效果
     */
    public boolean isBlindnessEffectEnabled() {
        return _getSettingAsBoolean("enable_blindness_effect", true);
    }
    
    /**
     * 获取失明效果等级
     */
    public int getBlindnessEffectLevel() {
        return _getSettingAsInt("blindness_effect_level", 2);
    }
    
    /**
     * 获取失明效果持续时间
     */
    public int getBlindnessEffectDuration() {
        return _getSettingAsInt("blindness_effect_duration", 999999);
    }
    
    /**
     * 获取踢出延迟时间（秒）
     */
    public int getKickDelaySeconds() {
        return _getSettingAsInt("kick_delay_seconds", 3);
    }
    
    /**
     * 是否启用聊天限制
     */
    public boolean isChatRestrictionEnabled() {
        return _getSettingAsBoolean("enable_chat_restriction", true);
    }
    
    /**
     * 获取设置项（整数）
     */
    private int _getSettingAsInt(String key, int defaultValue) {
        try {
            if (_config.has("settings")) {
                JsonObject settings = _config.getAsJsonObject("settings");
                if (settings.has(key)) {
                    return settings.get(key).getAsInt();
                }
            }
        } catch (Exception e) {
            LOGGER.warn("获取设置项失败: {}", key, e);
        }
        return defaultValue;
    }
    
    /**
     * 获取设置项（布尔值）
     */
    private boolean _getSettingAsBoolean(String key, boolean defaultValue) {
        try {
            if (_config.has("settings")) {
                JsonObject settings = _config.getAsJsonObject("settings");
                if (settings.has(key)) {
                    return settings.get(key).getAsBoolean();
                }
            }
        } catch (Exception e) {
            LOGGER.warn("获取设置项失败: {}", key, e);
        }
        return defaultValue;
    }
}