package com.login.ohc.mixin;

import com.login.ohc.Ohc_Login;
import com.login.ohc.restrictions.PlayerRestrictionManager;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_2846;
import net.minecraft.class_3222;
import net.minecraft.class_3244;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ServerPlayNetworkHandlerMixin - 拦截网络包处理中的物品丢弃行为
 * 
 * 这个Mixin拦截ServerPlayNetworkHandler中的网络包处理方法，
 * 确保未登录的玩家无法通过网络包丢弃物品。
 */
@Mixin(class_3244.class)
public class ServerPlayNetworkHandlerMixin {
    
    @Shadow
    public class_3222 player;
    
    /**
     * 拦截玩家动作网络包处理
     * 这包括DROP_ITEM和SWAP_ITEM_WITH_OFFHAND等动作
     */
    @Inject(method = "onPlayerAction", at = @At("HEAD"), cancellable = true)
    private void onPlayerAction(class_2846 packet, CallbackInfo ci) {
        if (player == null) {
            return;
        }
        
        class_2846.class_2847 action = packet.method_12363();
        
        // 检查是否为物品丢弃相关动作
        if (action == class_2846.class_2847.field_12975 || 
            action == class_2846.class_2847.field_12970 ||
            action == class_2846.class_2847.field_12969) {
            
            String playerName = player.method_7334().getName();
            
            if (!PlayerRestrictionManager.canPlayerDropItem(player)) {
                Ohc_Login.LOGGER.info("阻止未登录玩家 {} 执行物品丢弃动作: {}", playerName, action);
                
                class_2561 restrictionMessage = class_2561.method_43470("请先登录后再丢弃物品！")
                        .method_27692(class_124.field_1061);
                player.method_7353(restrictionMessage, true);
                ci.cancel();
            }
        }
    }
}