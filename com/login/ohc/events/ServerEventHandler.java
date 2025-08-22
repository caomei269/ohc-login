package com.login.ohc.events;

import com.login.ohc.Ohc_Login;
import com.login.ohc.config.MessageConfig;
import com.login.ohc.config.LoggedPlayersConfig;
import com.login.ohc.effects.BlindnessEffectManager;
import com.login.ohc.data.PlayerData;
import com.login.ohc.data.PlayerDataManager;
import com.login.ohc.network.LoginPacketHandler;
import com.login.ohc.restrictions.PlayerRestrictionManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_3222;

/**
 * 服务端事件处理器
 * 处理玩家加入、离开等关键事件
 */
public class ServerEventHandler {
    
    /**
     * 注册所有服务端事件
     */
    public static void registerEvents() {
        // 注册服务器启动事件
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            PlayerDataManager.getInstance().initialize(server);
            Ohc_Login.LOGGER.info("PlayerDataManager initialized");
        });
        
        // 注册玩家加入事件
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            class_3222 player = handler.method_32311();
            _onPlayerJoin(player);
        });
        
        // 玩家离开服务器事件
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            class_3222 player = handler.method_32311();
            _onPlayerLeave(player);
        });
        
        // 注册聊天消息事件
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            if (!PlayerRestrictionManager.canPlayerChat(sender)) {
                class_2561 restrictionMessage = class_2561.method_43470(MessageConfig.getInstance().getRestrictionMessage("chat_restricted"));
                sender.method_7353(restrictionMessage, false);
                return;
            }
        });
        
        Ohc_Login.LOGGER.info("Server events registered successfully");
    }
    
    /**
     * 处理玩家加入事件
     */
    private static void _onPlayerJoin(class_3222 player) {
        String playerName = player.method_7334().getName();
        PlayerDataManager dataManager = PlayerDataManager.getInstance();
        PlayerData playerData = dataManager.getPlayerData(playerName);
        
        Ohc_Login.LOGGER.info("Player {} joined the server", playerName);
        
        // 检查玩家是否被封停
        if (playerData.isBanned()) {
            long remainingHours = playerData.getRemainingBanHours();
            MessageConfig config = MessageConfig.getInstance();
            int banDurationMinutes = config.getBanDurationMinutes();
            int banDurationHours = banDurationMinutes / 60;
            
            // 发送封停消息
            class_2561 banMessage = class_2561.method_43470("你密码输入错误次数太多被封停" + banDurationHours + "小时")
                    .method_27692(class_124.field_1061)
                    .method_10852(class_2561.method_43470("\n剩余时间: " + remainingHours + " 小时")
                            .method_27692(class_124.field_1054));
            
            player.method_7353(banMessage, false);
            
            // 延迟后踢出玩家
            int kickDelaySeconds = config.getKickDelaySeconds();
            new Thread(() -> {
                try {
                    Thread.sleep(kickDelaySeconds * 1000);
                    player.method_5682().execute(() -> {
                        if (player.field_13987 != null && !player.method_14239()) {
                            player.field_13987.method_52396(class_2561.method_43470("账号被封停"));
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
            return;
        }
        
        // 检查玩家是否在冷却期
        if (playerData.isInCooldown()) {
            long remainingSeconds = playerData.getRemainingCooldownSeconds();
            class_2561 cooldownMessage = class_2561.method_43470("密码错误次数过多，请等待 " + remainingSeconds + " 秒后再试")
                    .method_27692(class_124.field_1054);
            player.method_7353(cooldownMessage, false);
            return;
        }
        
        // 检查是否需要设置密码或登录
        if (!playerData.hasPassword()) {
            // 首次加入，需要设置密码
            _sendSetPasswordRequest(player);
        } else {
            // 需要登录
            _sendLoginRequest(player);
        }
        
        // 应用失明效果（如果启用）
        BlindnessEffectManager.getInstance().applyBlindnessEffect(player);
    }
    
    /**
     * 处理玩家离开事件
     */
    private static void _onPlayerLeave(class_3222 player) {
        String playerName = player.method_7334().getName();
        PlayerDataManager dataManager = PlayerDataManager.getInstance();
        
        // 将玩家标记为离线状态
        dataManager.removePlayerData(playerName);
        
        // 清理失明效果记录
        BlindnessEffectManager.getInstance().cleanupPlayer(player.method_5667());
        
        Ohc_Login.LOGGER.info("Player {} left the server", playerName);
    }
    
    /**
     * 发送设置密码请求
     */
    private static void _sendSetPasswordRequest(class_3222 player) {
        class_2561 message = class_2561.method_43470(MessageConfig.getInstance().getWelcomeMessage("first_join_set_password"));
        player.method_7353(message, false);
    }
    
    /**
     * 发送登录请求
     */
    private static void _sendLoginRequest(class_3222 player) {
        class_2561 message = class_2561.method_43470(MessageConfig.getInstance().getWelcomeMessage("returning_player_login"));
        player.method_7353(message, false);
    }
    
    /**
     * 处理登录成功
     */
    public static void handleLoginSuccess(class_3222 player) {
        String playerName = player.method_7334().getName();
        PlayerDataManager dataManager = PlayerDataManager.getInstance();
        PlayerData playerData = dataManager.getPlayerData(playerName);
        
        // 标记为已登录
        playerData.loginSuccess();
        dataManager.savePlayerData(playerData);
        
        // 将玩家昵称添加到已登录玩家配置中
        LoggedPlayersConfig.getInstance().addLoggedPlayer(playerName);
        
        // 移除失明效果
        BlindnessEffectManager.getInstance().removeBlindnessEffect(player);
        
        // 发送成功消息
        class_2561 successMessage = class_2561.method_43470(MessageConfig.getInstance().getCommandMessage("login_success"));
        player.method_7353(successMessage, false);
        
        Ohc_Login.LOGGER.info("Player {} logged in successfully", playerName);
    }
    
    /**
     * 处理登录失败
     */
    public static void handleLoginFailure(class_3222 player) {
        String playerName = player.method_7334().getName();
        PlayerDataManager dataManager = PlayerDataManager.getInstance();
        PlayerData playerData = dataManager.getPlayerData(playerName);
        
        // 记录失败次数
        playerData.loginFailed();
        dataManager.savePlayerData(playerData);
        
        // 检查是否触发冷却或封停
        if (playerData.isBanned()) {
            MessageConfig config = MessageConfig.getInstance();
            int banDurationMinutes = config.getBanDurationMinutes();
            int banDurationHours = banDurationMinutes / 60;
            class_2561 banMessage = class_2561.method_43470("密码错误次数过多，账号已被封停" + banDurationHours + "小时！")
                    .method_27692(class_124.field_1061);
            player.method_7353(banMessage, false);
            
            // 延迟后踢出
            int kickDelaySeconds = config.getKickDelaySeconds();
            new Thread(() -> {
                try {
                    Thread.sleep(kickDelaySeconds * 1000);
                    player.method_5682().execute(() -> {
                        if (player.field_13987 != null && !player.method_14239()) {
                            player.field_13987.method_52396(class_2561.method_43470("账号被封停"));
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        } else if (playerData.isInCooldown()) {
            long remainingSeconds = playerData.getRemainingCooldownSeconds();
            class_2561 cooldownMessage = class_2561.method_43470("密码错误！冷却时间: " + remainingSeconds + " 秒")
                    .method_27692(class_124.field_1061);
            player.method_7353(cooldownMessage, false);
        } else {
            MessageConfig config = MessageConfig.getInstance();
            int maxAttempts = config.getMaxLoginAttempts();
            class_2561 failMessage = class_2561.method_43470("密码错误！剩余尝试次数: " + (maxAttempts - playerData.getFailedAttempts()))
                    .method_27692(class_124.field_1061);
            player.method_7353(failMessage, false);
            
            // 重新发送登录请求
            _sendLoginRequest(player);
        }
        
        Ohc_Login.LOGGER.warn("Player {} failed login attempt ({})", playerName, playerData.getFailedAttempts());
    }
    
    /**
     * 处理密码设置成功
     */
    public static void handlePasswordSetSuccess(class_3222 player) {
        String playerName = player.method_7334().getName();
        
        class_2561 successMessage = class_2561.method_43470("密码设置成功！您现在可以正常游戏了。")
                .method_27692(class_124.field_1060);
        player.method_7353(successMessage, false);
        
        // 自动登录
        handleLoginSuccess(player);
        
        Ohc_Login.LOGGER.info("Player {} set password successfully", playerName);
    }
}