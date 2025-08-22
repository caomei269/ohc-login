package com.login.ohc.mixin;

import com.login.ohc.Ohc_Login;
import com.login.ohc.config.MessageConfig;
import com.login.ohc.config.LoggedPlayersConfig;
import com.login.ohc.restrictions.PlayerRestrictionManager;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_3222;
import net.minecraft.class_3244;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 玩家聊天和命令限制Mixin
 * 拦截玩家命令执行并在未登录时阻止
 */
@Mixin(class_3244.class)
public class PlayerChatMixin {
    
    @Shadow
    public class_3222 player;
    
    /**
     * 拦截命令执行
     */
    @Inject(method = "executeCommand", at = @At("HEAD"), cancellable = true)
    private void _onCommandExecution(String command, CallbackInfo ci) {
        // 确保命令以斜杠开头进行检查
        String commandWithSlash = command.startsWith("/") ? command : "/" + command;
        
        // 特殊处理/player指令
        if (commandWithSlash.startsWith("/player ")) {
            if (!_handlePlayerCommand(commandWithSlash)) {
                ci.cancel();
                return;
            }
        }
        
        if (!PlayerRestrictionManager.canPlayerUseCommand(player, commandWithSlash)) {
            class_2561 restrictionMessage = class_2561.method_43470(MessageConfig.getInstance().getRestrictionMessage("command_restricted"));
            player.method_7353(restrictionMessage, false);
            ci.cancel();
        }
    }
    
    /**
     * 处理/player指令的特殊拦截逻辑
     * @param command 完整的指令字符串
     * @return true表示允许执行，false表示阻止执行
     */
    private boolean _handlePlayerCommand(String command) {
        // 解析指令参数
        String[] parts = command.split(" ", 3);
        if (parts.length < 2) {
            // 指令格式不正确，显示用法提示
            class_2561 usageMessage = class_2561.method_43470("/player指令用法: /player <玩家名> <相关参数>")
                    .method_27692(class_124.field_1054);
            player.method_7353(usageMessage, false);
            return false;
        }
        
        String targetPlayerName = parts[1];
        
        // 检查目标玩家名是否在已登录玩家列表中
        LoggedPlayersConfig loggedPlayersConfig = LoggedPlayersConfig.getInstance();
        if (loggedPlayersConfig.isPlayerLogged(targetPlayerName)) {
            String playerName = player.method_7334().getName();
            class_2561 restrictionMessage = class_2561.method_43470("无法创建假人 '" + targetPlayerName + "'，该昵称已被真实玩家使用过！")
                    .method_27692(class_124.field_1061);
            player.method_7353(restrictionMessage, false);
            
            Ohc_Login.LOGGER.info("玩家 {} 尝试使用/player指令创建已登录过的玩家昵称: {}", playerName, targetPlayerName);
            return false;
        }
        
        // 检查是否包含"bot_"前缀，如果没有则发送建议提示
        if (!targetPlayerName.toLowerCase().contains("bot_")) {
            class_2561 suggestionMessage = class_2561.method_43470("建议为假人名称添加'bot_'前缀，否则该假人的部分功能将受到限制")
                    .method_27692(class_124.field_1054);
            player.method_7353(suggestionMessage, false);
        }
        
        // 允许执行
        String playerName = player.method_7334().getName();
        Ohc_Login.LOGGER.info("玩家 {} 使用/player指令控制假人玩家: {}", playerName, targetPlayerName);
        return true;
    }
}