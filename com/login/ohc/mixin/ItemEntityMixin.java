package com.login.ohc.mixin;

import com.login.ohc.restrictions.PlayerRestrictionManager;
import net.minecraft.class_124;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import net.minecraft.class_3222;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 物品实体Mixin
 * 拦截玩家拾取物品的行为
 */
@Mixin(class_1542.class)
public class ItemEntityMixin {
    
    /**
     * 拦截物品拾取
     */
    @Inject(method = "onPlayerCollision", at = @At("HEAD"), cancellable = true)
    private void onPlayerCollision(class_1657 player, CallbackInfo ci) {
        if (player instanceof class_3222 serverPlayer) {
            if (!PlayerRestrictionManager.canPlayerPickupItem(serverPlayer)) {
                class_2561 restrictionMessage = class_2561.method_43470("请先登录后再拾取物品！")
                        .method_27692(class_124.field_1061);
                serverPlayer.method_7353(restrictionMessage, true);
                ci.cancel();
            }
        }
    }
}