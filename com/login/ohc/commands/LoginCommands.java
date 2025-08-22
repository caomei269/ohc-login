package com.login.ohc.commands;

import com.login.ohc.config.MessageConfig;
import com.login.ohc.data.PlayerData;
import com.login.ohc.data.PlayerDataManager;
import com.login.ohc.events.ServerEventHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.class_124;
import net.minecraft.class_2168;
import net.minecraft.class_2170;
import net.minecraft.class_2561;
import net.minecraft.class_3222;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 登录命令处理器
 * 处理玩家登录、设置密码和修改密码相关命令
 */
public class LoginCommands {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginCommands.class);
    
    /**
     * 注册所有登录相关命令
     */
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            _registerLoginCommand(dispatcher);
            _registerSetPasswordCommand(dispatcher);
            _registerChangePasswordCommand(dispatcher);
            _registerDeletePasswordCommand(dispatcher);
            _registerResetPasswordCommand(dispatcher);
        });
        
        LOGGER.info("登录命令已注册");
    }
    
    /**
     * 注册登录命令 /l <密码>
     */
    private static void _registerLoginCommand(CommandDispatcher<class_2168> dispatcher) {
        dispatcher.register(
            class_2170.method_9247("l")
                .then(class_2170.method_9244("password", StringArgumentType.greedyString())
                    .executes(LoginCommands::_executeLogin)
                )
                .executes(LoginCommands::_showLoginUsage)
        );
    }
    
    /**
     * 注册设置密码命令 /sp <密码>
     */
    private static void _registerSetPasswordCommand(CommandDispatcher<class_2168> dispatcher) {
        dispatcher.register(
            class_2170.method_9247("sp")
                .then(class_2170.method_9244("password", StringArgumentType.greedyString())
                    .executes(LoginCommands::_executeSetPassword)
                )
                .executes(LoginCommands::_showSetPasswordUsage)
        );
    }
    
    /**
     * 注册修改密码命令 /rp <旧密码> <新密码>
     */
    private static void _registerChangePasswordCommand(CommandDispatcher<class_2168> dispatcher) {
        dispatcher.register(
            class_2170.method_9247("rp")
                .then(class_2170.method_9244("old_password", StringArgumentType.string())
                    .then(class_2170.method_9244("new_password", StringArgumentType.greedyString())
                        .executes(LoginCommands::_executeChangePassword)
                    )
                )
                .executes(LoginCommands::_showChangePasswordUsage)
        );
    }
    
    /**
     * 执行登录命令
     */
    private static int _executeLogin(CommandContext<class_2168> context) {
        try {
            class_3222 player = context.getSource().method_9207();
        String password = StringArgumentType.getString(context, "password").trim();
        String playerName = player.method_7334().getName();
        
        PlayerDataManager dataManager = PlayerDataManager.getInstance();
        PlayerData playerData = dataManager.getPlayerData(playerName);
        MessageConfig config = MessageConfig.getInstance();
        
        // 检查是否已登录
        if (playerData.isLoggedIn()) {
            player.method_64398(class_2561.method_43470(config.getCommandMessage("already_logged_in")).method_27692(class_124.field_1054));
            return 1;
        }
        
        // 检查是否已设置密码
        if (!playerData.hasPassword()) {
            player.method_64398(class_2561.method_43470(config.getErrorMessage("password_not_set")).method_27692(class_124.field_1061));
            return 0;
        }
        
        // 检查冷却状态
        if (playerData.isInCooldown()) {
            long remainingSeconds = playerData.getRemainingCooldownSeconds();
            String cooldownMessage = config.getErrorMessage("login_cooldown").replace("{time}", String.valueOf(remainingSeconds));
            player.method_64398(class_2561.method_43470(cooldownMessage).method_27692(class_124.field_1061));
            return 0;
        }
        
        // 检查封停状态
        if (playerData.isBanned()) {
            long remainingHours = playerData.getRemainingBanHours();
            String banMessage = config.getErrorMessage("player_banned").replace("{time}", String.valueOf(remainingHours));
            player.method_64398(class_2561.method_43470(banMessage).method_27692(class_124.field_1061));
            return 0;
        }
        
        // 验证密码
        if (playerData.verifyPassword(password)) {
            ServerEventHandler.handleLoginSuccess(player);
        } else {
            ServerEventHandler.handleLoginFailure(player);
        }
        } catch (Exception e) {
            LOGGER.error("Error executing login command", e);
        }
        
        return 1;
    }
    
    /**
     * 执行设置密码命令
     */
    private static int _executeSetPassword(CommandContext<class_2168> context) {
        try {
            class_3222 player = context.getSource().method_9207();
        String password = StringArgumentType.getString(context, "password").trim();
        String playerName = player.method_7334().getName();
        
        PlayerDataManager dataManager = PlayerDataManager.getInstance();
        PlayerData playerData = dataManager.getPlayerData(playerName);
        MessageConfig config = MessageConfig.getInstance();
        
        // 检查是否已设置密码
        if (playerData.hasPassword()) {
            player.method_64398(class_2561.method_43470(config.getErrorMessage("password_already_set")).method_27692(class_124.field_1061));
            return 0;
        }
        
        // 验证密码格式
        if (!_isValidPassword(password)) {
            _sendPasswordFormatError(player, password);
            return 0;
        }
        
        // 设置密码
        playerData.setPassword(password);
        dataManager.savePlayerData(playerData);
        
        // 自动登录
        ServerEventHandler.handleLoginSuccess(player);
        
        LOGGER.info("玩家 {} 设置了密码", playerName);
        } catch (Exception e) {
            LOGGER.error("Error executing set password command", e);
        }
        return 1;
    }
    
    /**
     * 执行修改密码命令
     */
    private static int _executeChangePassword(CommandContext<class_2168> context) {
        try {
            class_3222 player = context.getSource().method_9207();
        String oldPassword = StringArgumentType.getString(context, "old_password").trim();
        String newPassword = StringArgumentType.getString(context, "new_password").trim();
        String playerName = player.method_7334().getName();
        
        PlayerDataManager dataManager = PlayerDataManager.getInstance();
        PlayerData playerData = dataManager.getPlayerData(playerName);
        MessageConfig config = MessageConfig.getInstance();
        
        // 检查是否已设置密码
        if (!playerData.hasPassword()) {
            player.method_64398(class_2561.method_43470(config.getErrorMessage("password_not_set")).method_27692(class_124.field_1061));
            return 0;
        }
        
        // 检查是否已登录
        if (!playerData.isLoggedIn()) {
            player.method_64398(class_2561.method_43470(config.getCommandMessage("must_login_first")).method_27692(class_124.field_1061));
            return 0;
        }
        
        // 验证旧密码
        if (!playerData.verifyPassword(oldPassword)) {
            player.method_64398(class_2561.method_43470(config.getCommandMessage("password_change_failed")).method_27692(class_124.field_1061));
            LOGGER.warn("玩家 {} 尝试修改密码但旧密码错误", playerName);
            return 0;
        }
        
        // 验证新密码格式
        if (!_isValidPassword(newPassword)) {
            _sendPasswordFormatError(player, newPassword);
            return 0;
        }
        
        // 检查新旧密码是否相同
        if (oldPassword.equals(newPassword)) {
            player.method_64398(class_2561.method_43470("§c新密码不能与旧密码相同！").method_27692(class_124.field_1061));
            return 0;
        }
        
        // 修改密码
        playerData.setPassword(newPassword);
        dataManager.savePlayerData(playerData);
        
        player.method_64398(class_2561.method_43470(config.getCommandMessage("password_change_success")).method_27692(class_124.field_1060));
        LOGGER.info("玩家 {} 修改了密码", playerName);
        } catch (Exception e) {
            LOGGER.error("Error executing change password command", e);
        }
        return 1;
    }
    
    /**
     * 显示登录命令用法
     */
    private static int _showLoginUsage(CommandContext<class_2168> context) {
        try {
            class_3222 player = context.getSource().method_9207();
            MessageConfig config = MessageConfig.getInstance();
            player.method_64398(class_2561.method_43470(config.getErrorMessage("command_usage_login")).method_27692(class_124.field_1054));
        } catch (Exception e) {
            LOGGER.error("Error showing login usage", e);
        }
        return 1;
    }
    
    /**
     * 显示设置密码命令用法
     */
    private static int _showSetPasswordUsage(CommandContext<class_2168> context) {
        try {
            class_3222 player = context.getSource().method_9207();
            MessageConfig config = MessageConfig.getInstance();
            player.method_64398(class_2561.method_43470(config.getErrorMessage("command_usage_setpassword")).method_27692(class_124.field_1054));
        } catch (Exception e) {
            LOGGER.error("Error showing set password usage", e);
        }
        return 1;
    }
    
    /**
     * 显示修改密码命令用法
     */
    private static int _showChangePasswordUsage(CommandContext<class_2168> context) {
        try {
            class_3222 player = context.getSource().method_9207();
            player.method_64398(class_2561.method_43470("§e用法: /rp <旧密码> <新密码>").method_27692(class_124.field_1054));
        } catch (Exception e) {
            LOGGER.error("Error showing change password usage", e);
        }
        return 1;
    }
    
    /**
     * 验证密码格式
     */
    private static boolean _isValidPassword(String password) {
        MessageConfig config = MessageConfig.getInstance();
        int minLength = config.getPasswordMinLength();
        int maxLength = config.getPasswordMaxLength();
        
        // 检查密码长度
        if (password.length() < minLength || password.length() > maxLength) {
            return false;
        }
        
        // 检查是否包含禁用字符 / 和 \\
        if (password.contains("/") || password.contains("\\")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 发送密码格式错误消息
     */
    private static void _sendPasswordFormatError(class_3222 player, String password) {
        MessageConfig config = MessageConfig.getInstance();
        int minLength = config.getPasswordMinLength();
        int maxLength = config.getPasswordMaxLength();
        
        if (password.length() < minLength) {
            String message = config.getErrorMessage("password_too_short").replace("{min}", String.valueOf(minLength));
            player.method_64398(class_2561.method_43470(message).method_27692(class_124.field_1061));
        } else if (password.length() > maxLength) {
            String message = config.getErrorMessage("password_too_long").replace("{max}", String.valueOf(maxLength));
            player.method_64398(class_2561.method_43470(message).method_27692(class_124.field_1061));
        } else if (password.contains("/") || password.contains("\\")) {
            String message = config.getErrorMessage("password_invalid_chars");
            player.method_64398(class_2561.method_43470(message).method_27692(class_124.field_1061));
        }
    }
    
    /**
     * 注册删除密码命令 /dp <玩家名>
     */
    private static void _registerDeletePasswordCommand(CommandDispatcher<class_2168> dispatcher) {
        dispatcher.register(
            class_2170.method_9247("dp")
                .requires(source -> source.method_9259(2)) // 需要管理员权限
                .then(class_2170.method_9244("player", StringArgumentType.string())
                    .executes(LoginCommands::_executeDeletePassword)
                )
                .executes(LoginCommands::_showDeletePasswordUsage)
        );
    }
    
    /**
     * 注册重置密码命令 /drp <玩家名> <新密码>
     */
    private static void _registerResetPasswordCommand(CommandDispatcher<class_2168> dispatcher) {
        dispatcher.register(
            class_2170.method_9247("drp")
                .requires(source -> source.method_9259(2)) // 需要管理员权限
                .then(class_2170.method_9244("player", StringArgumentType.string())
                    .then(class_2170.method_9244("password", StringArgumentType.greedyString())
                        .executes(LoginCommands::_executeResetPassword)
                    )
                )
                .executes(LoginCommands::_showResetPasswordUsage)
        );
    }
    
    /**
     * 执行删除密码命令
     */
    private static int _executeDeletePassword(CommandContext<class_2168> context) {
        class_2168 source = context.getSource();
        String targetPlayerName = StringArgumentType.getString(context, "player");
        MessageConfig config = MessageConfig.getInstance();
        
        try {
            // 检查权限
            if (!source.method_9259(2)) {
                String message = config.getAdminMessage("admin_no_permission");
                source.method_45068(class_2561.method_43470(message));
                return 0;
            }
            
            PlayerDataManager dataManager = PlayerDataManager.getInstance();
            PlayerData playerData = dataManager.getPlayerData(targetPlayerName);
            
            if (playerData == null || !playerData.hasPassword()) {
                String message = config.getAdminMessage("delete_password_not_found")
                    .replace("{player}", targetPlayerName);
                source.method_45068(class_2561.method_43470(message));
                return 0;
            }
            
            // 删除密码
            playerData.clearPassword();
            dataManager.savePlayerData(playerData);
            
            String message = config.getAdminMessage("delete_password_success")
                .replace("{player}", targetPlayerName);
            source.method_45068(class_2561.method_43470(message));
            
            LOGGER.info("管理员 {} 删除了玩家 {} 的密码", source.method_9214(), targetPlayerName);
            return 1;
            
        } catch (Exception e) {
            String message = config.getAdminMessage("delete_password_failed")
                .replace("{player}", targetPlayerName)
                .replace("{reason}", e.getMessage());
            source.method_45068(class_2561.method_43470(message));
            LOGGER.error("删除玩家密码时发生错误", e);
            return 0;
        }
    }
    
    /**
     * 执行重置密码命令
     */
    private static int _executeResetPassword(CommandContext<class_2168> context) {
        class_2168 source = context.getSource();
        String targetPlayerName = StringArgumentType.getString(context, "player");
        String newPassword = StringArgumentType.getString(context, "password");
        MessageConfig config = MessageConfig.getInstance();
        
        try {
            // 检查权限
            if (!source.method_9259(2)) {
                String message = config.getAdminMessage("admin_no_permission");
                source.method_45068(class_2561.method_43470(message));
                return 0;
            }
            
            // 验证密码格式
            if (!_isValidPassword(newPassword)) {
                String message = config.getAdminMessage("admin_invalid_password");
                source.method_45068(class_2561.method_43470(message));
                return 0;
            }
            
            PlayerDataManager dataManager = PlayerDataManager.getInstance();
            PlayerData playerData = dataManager.getPlayerData(targetPlayerName);
            
            if (playerData == null) {
                playerData = new PlayerData(targetPlayerName);
            }
            
            // 设置新密码
            playerData.setPassword(newPassword);
            playerData.setLoggedIn(false); // 重置登录状态
            dataManager.savePlayerData(playerData);
            
            String message = config.getAdminMessage("reset_password_success")
                .replace("{player}", targetPlayerName);
            source.method_45068(class_2561.method_43470(message));
            
            LOGGER.info("管理员 {} 为玩家 {} 重置了密码", source.method_9214(), targetPlayerName);
            return 1;
            
        } catch (Exception e) {
            String message = config.getAdminMessage("reset_password_failed")
                .replace("{player}", targetPlayerName)
                .replace("{reason}", e.getMessage());
            source.method_45068(class_2561.method_43470(message));
            LOGGER.error("重置玩家密码时发生错误", e);
            return 0;
        }
    }
    
    /**
     * 显示删除密码命令用法
     */
    private static int _showDeletePasswordUsage(CommandContext<class_2168> context) {
        class_2168 source = context.getSource();
        source.method_45068(class_2561.method_43470("§c用法: /dp <玩家名>"));
        source.method_45068(class_2561.method_43470("§7删除指定玩家的密码"));
        return 1;
    }
    
    /**
     * 显示重置密码命令用法
     */
    private static int _showResetPasswordUsage(CommandContext<class_2168> context) {
        class_2168 source = context.getSource();
        source.method_45068(class_2561.method_43470("§c用法: /drp <玩家名> <新密码>"));
        source.method_45068(class_2561.method_43470("§7为指定玩家重置密码"));
        return 1;
    }
}