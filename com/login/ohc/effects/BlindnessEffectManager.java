package com.login.ohc.effects;

import com.login.ohc.config.MessageConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_3222;

/**
 * 失明效果管理器
 * 负责为未登录玩家应用失明效果，登录后移除效果
 */
public class BlindnessEffectManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlindnessEffectManager.class);
    private static BlindnessEffectManager _instance;
    
    // 记录当前应用了失明效果的玩家
    private final Set<UUID> _blindedPlayers = new HashSet<>();
    
    private BlindnessEffectManager() {}
    
    /**
     * 获取单例实例
     */
    public static BlindnessEffectManager getInstance() {
        if (_instance == null) {
            _instance = new BlindnessEffectManager();
        }
        return _instance;
    }
    
    /**
     * 为玩家应用失明效果
     */
    public void applyBlindnessEffect(class_3222 player) {
        // 检查失明效果是否启用
        if (!MessageConfig.getInstance().isBlindnessEffectEnabled()) {
            return;
        }
        
        UUID playerId = player.method_5667();
        
        try {
            int level = MessageConfig.getInstance().getBlindnessEffectLevel();
            int duration = MessageConfig.getInstance().getBlindnessEffectDuration();
            
            // 先移除现有的失明效果（如果有的话）
            player.method_6016(class_1294.field_5919);
            
            // 创建失明效果实例
            class_1293 blindnessEffect = new class_1293(
                class_1294.field_5919,
                duration,
                level - 1, // Minecraft效果等级从0开始
                false, // 不显示粒子效果
                false, // 不显示在HUD中
                false  // 不显示图标
            );
            
            // 应用效果
            player.method_6092(blindnessEffect);
            _blindedPlayers.add(playerId);
            
            LOGGER.debug("为玩家 {} 应用失明效果 (等级: {}, 持续时间: {})", 
                player.method_5477().getString(), level, duration);
                
        } catch (Exception e) {
            LOGGER.error("为玩家 {} 应用失明效果时发生错误", player.method_5477().getString(), e);
        }
    }
    
    /**
     * 移除玩家的失明效果
     */
    public void removeBlindnessEffect(class_3222 player) {
        UUID playerId = player.method_5667();
        
        // 如果玩家没有失明效果，无需移除
        if (!_blindedPlayers.contains(playerId)) {
            return;
        }
        
        try {
            // 移除失明状态效果
            player.method_6016(class_1294.field_5919);
            _blindedPlayers.remove(playerId);
            
            LOGGER.debug("移除玩家 {} 的失明效果", player.method_5477().getString());
            
        } catch (Exception e) {
            LOGGER.error("移除玩家 {} 失明效果时发生错误", player.method_5477().getString(), e);
        }
    }
    
    /**
     * 检查玩家是否有失明效果
     */
    public boolean hasBlindnessEffect(class_3222 player) {
        return _blindedPlayers.contains(player.method_5667());
    }
    
    /**
     * 清理断开连接玩家的记录
     */
    public void cleanupPlayer(UUID playerId) {
        _blindedPlayers.remove(playerId);
        LOGGER.debug("清理玩家 {} 的失明效果记录", playerId);
    }
    
    /**
     * 获取当前有失明效果的玩家数量
     */
    public int getBlindedPlayerCount() {
        return _blindedPlayers.size();
    }
    
    /**
     * 清理所有失明效果记录（用于重载配置时）
     */
    public void clearAllRecords() {
        _blindedPlayers.clear();
        LOGGER.info("清理所有失明效果记录");
    }
}