package com.login.ohc.restrictions;

import com.login.ohc.Ohc_Login;
import com.login.ohc.config.MessageConfig;
import com.login.ohc.data.PlayerData;
import com.login.ohc.data.PlayerDataManager;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.class_1269;
import net.minecraft.class_1657;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_3222;

/**
 * 玩家行为限制管理器
 * 在未登录状态下限制玩家的各种行为
 */
public class PlayerRestrictionManager {
    
    private static class_2561 getRestrictionMessage() {
        return class_2561.method_43470(MessageConfig.getInstance().getRestrictionMessage("movement_restricted"));
    }
    
    /**
     * 注册所有行为限制事件
     */
    public static void registerRestrictions() {
        // 注册方块使用限制
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (_isPlayerRestricted(player)) {
                // 检查是否是传送门方块
                class_2338 pos = hitResult.method_17777();
                if (world.method_8320(pos).method_26204() == class_2246.field_10316 || 
                    world.method_8320(pos).method_26204() == class_2246.field_10027) {
                    if (player instanceof class_3222 serverPlayer) {
                        class_2561 restrictionMessage = class_2561.method_43470(MessageConfig.getInstance().getRestrictionMessage("portal_restricted"));
                        serverPlayer.method_7353(restrictionMessage, true);
                    }
                } else {
                    _sendRestrictionMessage(player);
                }
                return class_1269.field_5814;
            }
            return class_1269.field_5811;
        });
        
        // 限制实体交互
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (_isPlayerRestricted(player)) {
                _sendRestrictionMessage(player);
                return class_1269.field_5814;
            }
            return class_1269.field_5811;
        });
        
        // 限制物品使用
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (_isPlayerRestricted(player)) {
                _sendRestrictionMessage(player);
                return class_1269.field_5814;
            }
            return class_1269.field_5811;
        });
        
        // 限制方块攻击
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (_isPlayerRestricted(player)) {
                _sendRestrictionMessage(player);
                return class_1269.field_5814;
            }
            return class_1269.field_5811;
        });
        
        // 限制实体攻击
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (_isPlayerRestricted(player)) {
                _sendRestrictionMessage(player);
                return class_1269.field_5814;
            }
            return class_1269.field_5811;
        });
        
        Ohc_Login.LOGGER.info("Player restrictions registered successfully");
    }
    
    /**
     * 检查玩家是否被限制
     */
    private static boolean _isPlayerRestricted(class_1657 player) {
        if (!(player instanceof class_3222 serverPlayer)) {
            return false;
        }
        
        String playerName = serverPlayer.method_7334().getName();
        PlayerDataManager dataManager = PlayerDataManager.getInstance();
        PlayerData playerData = dataManager.getPlayerData(playerName);
        
        // 如果玩家未登录，则限制行为
        return !playerData.isLoggedIn();
    }
    
    /**
     * 发送限制消息给玩家
     */
    private static void _sendRestrictionMessage(class_1657 player) {
        if (player instanceof class_3222 serverPlayer) {
            serverPlayer.method_7353(getRestrictionMessage(), true); // 显示在ActionBar
        }
    }
    
    /**
     * 检查玩家是否可以移动
     */
    public static boolean canPlayerMove(class_3222 player) {
        String playerName = player.method_7334().getName();
        PlayerDataManager dataManager = PlayerDataManager.getInstance();
        PlayerData playerData = dataManager.getPlayerData(playerName);
        
        return playerData.isLoggedIn();
    }
    
    /**
     * 限制玩家移动（通过传送回原位置）
     */
    public static void restrictPlayerMovement(class_3222 player, double oldX, double oldY, double oldZ) {
        if (!canPlayerMove(player)) {
            // 将玩家传送回原位置
            player.method_48105(player.method_51469(), oldX, oldY, oldZ, java.util.Set.of(), player.method_36454(), player.method_36455(), false);
            _sendRestrictionMessage(player);
        }
    }
    
    /**
     * 检查玩家是否可以打开聊天
     */
    public static boolean canPlayerChat(class_3222 player) {
        // 检查是否启用聊天限制
        if (!MessageConfig.getInstance().isChatRestrictionEnabled()) {
            return true;
        }
        
        String playerName = player.method_7334().getName();
        PlayerDataManager dataManager = PlayerDataManager.getInstance();
        PlayerData playerData = dataManager.getPlayerData(playerName);
        
        return playerData.isLoggedIn();
    }
    
    /**
     * 检查玩家是否可以使用命令
     */
    public static boolean canPlayerUseCommand(class_3222 player, String command) {
        String playerName = player.method_7334().getName();
        PlayerDataManager dataManager = PlayerDataManager.getInstance();
        PlayerData playerData = dataManager.getPlayerData(playerName);
        
        // 允许登录相关的命令
        if (command.startsWith("/l") || command.startsWith("/sp") || command.startsWith("/rp")) {
            return true;
        }
        
        return playerData.isLoggedIn();
    }
    
    /**
     * 检查玩家是否可以丢弃物品
     */
    public static boolean canPlayerDropItem(class_3222 player) {
        String playerName = player.method_7334().getName();
        PlayerDataManager dataManager = PlayerDataManager.getInstance();
        PlayerData playerData = dataManager.getPlayerData(playerName);
        
        return playerData.isLoggedIn();
    }
    
    /**
     * 检查玩家是否可以拾取物品
     */
    public static boolean canPlayerPickupItem(class_3222 player) {
        String playerName = player.method_7334().getName();
        PlayerDataManager dataManager = PlayerDataManager.getInstance();
        PlayerData playerData = dataManager.getPlayerData(playerName);
        
        return playerData.isLoggedIn();
    }
    
    /**
     * 检查玩家是否可以打开容器
     */
    public static boolean canPlayerOpenContainer(class_3222 player) {
        String playerName = player.method_7334().getName();
        PlayerDataManager dataManager = PlayerDataManager.getInstance();
        PlayerData playerData = dataManager.getPlayerData(playerName);
        
        return playerData.isLoggedIn();
    }
    
    /**
     * 检查玩家是否可以使用传送门
     */
    public static boolean canPlayerUsePortal(class_3222 player) {
        String playerName = player.method_7334().getName();
        PlayerDataManager dataManager = PlayerDataManager.getInstance();
        PlayerData playerData = dataManager.getPlayerData(playerName);
        
        return playerData.isLoggedIn();
    }
}