package com.login.ohc.network;

import com.login.ohc.Ohc_Login;
import com.login.ohc.config.MessageConfig;
import com.login.ohc.data.PlayerData;
import com.login.ohc.data.PlayerDataManager;
import com.login.ohc.events.ServerEventHandler;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_8710;
import net.minecraft.class_9129;
import net.minecraft.class_9135;
import net.minecraft.class_9139;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

/**
 * 登录网络数据包处理器
 * 处理客户端与服务端之间的登录相关通信
 */
public class LoginPacketHandler {
    
    // 网络包标识符
    public static final class_2960 SET_PASSWORD_REQUEST = class_2960.method_60655(Ohc_Login.MOD_ID, "set_password_request");
    public static final class_2960 LOGIN_REQUEST = class_2960.method_60655(Ohc_Login.MOD_ID, "login_request");
    public static final class_2960 PASSWORD_RESPONSE = class_2960.method_60655(Ohc_Login.MOD_ID, "password_response");
    public static final class_2960 LOGIN_RESPONSE = class_2960.method_60655(Ohc_Login.MOD_ID, "login_response");
    
    // CustomPayload 记录类
    public record PasswordResponsePayload(String message) implements class_8710 {
        public static final class_8710.class_9154<PasswordResponsePayload> ID = new class_8710.class_9154<>(PASSWORD_RESPONSE);
        public static final class_9139<class_9129, PasswordResponsePayload> CODEC = 
            class_9139.method_56434(class_9135.field_48554, PasswordResponsePayload::message, PasswordResponsePayload::new);
        
        @Override
        public class_8710.class_9154<? extends class_8710> method_56479() {
            return ID;
        }
    }
    
    // CustomPayload 记录类 - 客户端到服务端的密码响应
    public record ClientPasswordResponsePayload(String password, boolean isSettingPassword) implements class_8710 {
        public static final class_8710.class_9154<ClientPasswordResponsePayload> ID = new class_8710.class_9154<>(class_2960.method_60655(Ohc_Login.MOD_ID, "client_password_response"));
        public static final class_9139<class_9129, ClientPasswordResponsePayload> CODEC = 
            class_9139.method_56435(
                class_9135.field_48554, ClientPasswordResponsePayload::password,
                class_9135.field_48547, ClientPasswordResponsePayload::isSettingPassword,
                ClientPasswordResponsePayload::new
            );
        
        @Override
        public class_8710.class_9154<? extends class_8710> method_56479() {
            return ID;
        }
    }
    
    public record LoginResponsePayload(String message) implements class_8710 {
         public static final class_8710.class_9154<LoginResponsePayload> ID = new class_8710.class_9154<>(LOGIN_RESPONSE);
         public static final class_9139<class_9129, LoginResponsePayload> CODEC = 
             class_9139.method_56434(class_9135.field_48554, LoginResponsePayload::message, LoginResponsePayload::new);
         
         @Override
         public class_8710.class_9154<? extends class_8710> method_56479() {
             return ID;
         }
     }
     
     public record SetPasswordRequestPayload(String message) implements class_8710 {
         public static final class_8710.class_9154<SetPasswordRequestPayload> ID = new class_8710.class_9154<>(SET_PASSWORD_REQUEST);
         public static final class_9139<class_9129, SetPasswordRequestPayload> CODEC = 
             class_9139.method_56434(class_9135.field_48554, SetPasswordRequestPayload::message, SetPasswordRequestPayload::new);
         
         @Override
         public class_8710.class_9154<? extends class_8710> method_56479() {
             return ID;
         }
     }
     
     public record LoginRequestPayload(String message) implements class_8710 {
         public static final class_8710.class_9154<LoginRequestPayload> ID = new class_8710.class_9154<>(LOGIN_REQUEST);
         public static final class_9139<class_9129, LoginRequestPayload> CODEC = 
             class_9139.method_56434(class_9135.field_48554, LoginRequestPayload::message, LoginRequestPayload::new);
         
         @Override
         public class_8710.class_9154<? extends class_8710> method_56479() {
             return ID;
         }
     }
    
    /**
     * 注册网络包处理器
     */
    public static void registerPacketHandlers() {
        // 注册 CustomPayload 类型
         PayloadTypeRegistry.playS2C().register(PasswordResponsePayload.ID, PasswordResponsePayload.CODEC);
         PayloadTypeRegistry.playS2C().register(LoginResponsePayload.ID, LoginResponsePayload.CODEC);
         PayloadTypeRegistry.playS2C().register(SetPasswordRequestPayload.ID, SetPasswordRequestPayload.CODEC);
         PayloadTypeRegistry.playS2C().register(LoginRequestPayload.ID, LoginRequestPayload.CODEC);
        
        // 注册客户端到服务端的 CustomPayload 类型
        PayloadTypeRegistry.playC2S().register(ClientPasswordResponsePayload.ID, ClientPasswordResponsePayload.CODEC);
        
        // 注册客户端到服务端的密码响应处理器
        ServerPlayNetworking.registerGlobalReceiver(ClientPasswordResponsePayload.ID, (payload, context) -> {
            class_3222 player = context.player();
            String password = payload.password();
            boolean isSettingPassword = payload.isSettingPassword();
            
            context.server().execute(() -> {
                if (isSettingPassword) {
                    _handlePasswordSet(player, password);
                } else {
                    _handleLoginAttempt(player, password);
                }
            });
        });
        
        Ohc_Login.LOGGER.info("Network packet handlers registered successfully");
    }
    
    /**
     * 发送设置密码请求到客户端
     */
    public static void sendSetPasswordRequest(class_3222 player) {
        ServerPlayNetworking.send(player, new SetPasswordRequestPayload("请设置您的登录密码"));
        Ohc_Login.LOGGER.debug("Sent set password request to player {}", player.method_7334().getName());
    }
    
    /**
     * 发送登录请求到客户端
     */
    public static void sendLoginRequest(class_3222 player) {
        ServerPlayNetworking.send(player, new LoginRequestPayload("请输入您的密码"));
        Ohc_Login.LOGGER.debug("Sent login request to player {}", player.method_7334().getName());
    }
    
    /**
     * 处理密码设置
     */
    private static void _handlePasswordSet(class_3222 player, String password) {
        String playerName = player.method_7334().getName();
        PlayerDataManager dataManager = PlayerDataManager.getInstance();
        PlayerData playerData = dataManager.getPlayerData(playerName);
        
        // 验证密码格式
        if (!_isValidPassword(password)) {
            _sendPasswordError(player, "密码格式无效！密码长度应为4-20个字符。");
            sendSetPasswordRequest(player); // 重新发送设置请求
            return;
        }
        
        // 设置密码
        playerData.setPassword(password);
        dataManager.savePlayerData(playerData);
        
        // 通知事件处理器
        ServerEventHandler.handlePasswordSetSuccess(player);
        
        Ohc_Login.LOGGER.info("Player {} set password successfully", playerName);
    }
    
    /**
     * 处理登录尝试
     */
    private static void _handleLoginAttempt(class_3222 player, String password) {
        String playerName = player.method_7334().getName();
        PlayerDataManager dataManager = PlayerDataManager.getInstance();
        PlayerData playerData = dataManager.getPlayerData(playerName);
        
        // 检查冷却状态
        if (playerData.isInCooldown()) {
            long remainingSeconds = playerData.getRemainingCooldownSeconds();
            _sendPasswordError(player, "您在冷却期内，请等待 " + remainingSeconds + " 秒后再试。");
            return;
        }
        
        // 检查封停状态
        if (playerData.isBanned()) {
            long remainingHours = playerData.getRemainingBanHours();
            _sendPasswordError(player, "您的账号已被封停，剩余时间: " + remainingHours + " 小时。");
            return;
        }
        
        // 验证密码
        if (playerData.verifyPassword(password)) {
            // 登录成功
            ServerEventHandler.handleLoginSuccess(player);
        } else {
            // 登录失败
            ServerEventHandler.handleLoginFailure(player);
        }
    }
    
    /**
     * 发送密码错误消息
     */
    private static void _sendPasswordError(class_3222 player, String errorMessage) {
        ServerPlayNetworking.send(player, new LoginResponsePayload(errorMessage));
    }
    
    /**
     * 验证密码格式
     */
    private static boolean _isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        
        MessageConfig config = MessageConfig.getInstance();
        int minLength = config.getPasswordMinLength();
        int maxLength = config.getPasswordMaxLength();
        
        String trimmedPassword = password.trim();
        return trimmedPassword.length() >= minLength && trimmedPassword.length() <= maxLength;
    }
    
    /**
     * 发送登录成功响应
     */
    public static void sendLoginSuccess(class_3222 player) {
        ServerPlayNetworking.send(player, new LoginResponsePayload("登录成功！"));
    }
    
    /**
     * 发送登录失败响应
     */
    public static void sendLoginFailure(class_3222 player, String reason) {
        ServerPlayNetworking.send(player, new LoginResponsePayload(reason));
    }
}