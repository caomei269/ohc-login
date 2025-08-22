package com.login.ohc.commands;

import com.login.ohc.config.MessageConfig;
import com.login.ohc.effects.BlindnessEffectManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.class_124;
import net.minecraft.class_2168;
import net.minecraft.class_2170;
import net.minecraft.class_2561;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 配置管理命令
 * 提供重载配置文件等管理功能
 */
public class ConfigCommands {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigCommands.class);
    
    /**
     * 注册配置管理命令
     */
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            _registerReloadCommand(dispatcher);
        });
        
        LOGGER.info("配置管理命令已注册");
    }
    
    /**
     * 注册重载配置命令
     */
    private static void _registerReloadCommand(CommandDispatcher<class_2168> dispatcher) {
        dispatcher.register(
            class_2170.method_9247("ohc-reload")
                .requires(source -> source.method_9259(3)) // 需要管理员权限
                .executes(ConfigCommands::_executeReload)
        );
    }
    
    /**
     * 执行重载配置命令
     */
    private static int _executeReload(CommandContext<class_2168> context) {
        class_2168 source = context.getSource();
        
        try {
            // 重新加载消息配置
            MessageConfig.getInstance().reload();
            
            // 清理失明效果记录（配置可能已更改）
            BlindnessEffectManager.getInstance().clearAllRecords();
            
            class_2561 successMessage = class_2561.method_43470("§a配置文件重载成功！")
                    .method_10852(class_2561.method_43470("\n§7所有消息配置已更新").method_27692(class_124.field_1080));
            source.method_9226(() -> successMessage, true);
            
            LOGGER.info("管理员 {} 重新加载了配置文件", source.method_9214());
            return 1;
            
        } catch (Exception e) {
            class_2561 errorMessage = class_2561.method_43470("§c配置文件重载失败: " + e.getMessage());
            source.method_9213(errorMessage);
            
            LOGGER.error("配置文件重载失败", e);
            return 0;
        }
    }
}