package com.login.ohc.mixin;

import com.login.ohc.config.MessageConfig;
import com.login.ohc.restrictions.PlayerRestrictionManager;
import net.minecraft.class_2561;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 玩家移动限制Mixin
 * 拦截玩家移动并在未登录时阻止移动
 */
@Mixin(class_3222.class)
public class PlayerMovementMixin {
    
    private double lastX = 0;
    private double lastY = 0;
    private double lastZ = 0;
    private boolean positionInitialized = false;
    private class_3218 lastWorld = null;
    
    /**
     * 拦截玩家移动
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void onPlayerTick(CallbackInfo ci) {
        class_3222 player = (class_3222) (Object) this;
        
        // 初始化位置和世界
        if (!positionInitialized) {
            lastX = player.method_23317();
            lastY = player.method_23318();
            lastZ = player.method_23321();
            lastWorld = (class_3218) player.method_51469();
            positionInitialized = true;
            return;
        }
        
        // 检查世界变化（传送门使用）
         class_3218 currentWorld = (class_3218) player.method_51469();
         if (lastWorld != null && !lastWorld.equals(currentWorld)) {
             if (!PlayerRestrictionManager.canPlayerUsePortal(player)) {
                 class_2561 restrictionMessage = class_2561.method_43470(MessageConfig.getInstance().getRestrictionMessage("portal_restricted"));
                 player.method_7353(restrictionMessage, true);
                 // 将玩家传送回原世界的相同位置
                 player.method_48105(lastWorld, lastX, lastY, lastZ, java.util.Set.of(), player.method_36454(), player.method_36455(), false);
                 return;
             }
         }
        
        // 检查是否可以移动
        if (!PlayerRestrictionManager.canPlayerMove(player)) {
            // 检查位置是否发生变化
            double currentX = player.method_23317();
            double currentY = player.method_23318();
            double currentZ = player.method_23321();
            
            double deltaX = Math.abs(currentX - lastX);
            double deltaY = Math.abs(currentY - lastY);
            double deltaZ = Math.abs(currentZ - lastZ);
            
            // 如果移动距离超过阈值，则限制移动
            if (deltaX > 0.1 || deltaY > 0.1 || deltaZ > 0.1) {
                PlayerRestrictionManager.restrictPlayerMovement(player, lastX, lastY, lastZ);
                return;
            }
        }
        
        // 更新最后位置和世界
        lastX = player.method_23317();
        lastY = player.method_23318();
        lastZ = player.method_23321();
        lastWorld = (class_3218) player.method_51469();
    }
}