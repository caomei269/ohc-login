package com.login.ohc.mixin;

import com.login.ohc.Ohc_Login;
import com.login.ohc.restrictions.PlayerRestrictionManager;
import net.minecraft.class_124;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_3222;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * PlayerItemMixin - 拦截玩家物品丢弃行为
 * 
 * 这个Mixin拦截PlayerEntity中的物品丢弃方法，确保未登录的玩家无法丢弃物品。
 * 通过拦截核心的dropItem方法，我们可以从根本上阻止所有形式的物品丢弃。
 */
@Mixin(class_1657.class)
public class PlayerItemMixin {
    

    
    /**
     * 拦截dropItem(ItemStack, boolean)方法
     * 这是PlayerEntity中处理物品丢弃的核心方法之一
     */
    @Inject(method = "dropItem(Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/entity/ItemEntity;", at = @At("HEAD"), cancellable = true)
    private void onDropItem(class_1799 stack, boolean retainOwnership, CallbackInfoReturnable<class_1542> cir) {
        class_1657 player = (class_1657) (Object) this;
        
        if (player instanceof class_3222 serverPlayer) {
            if (!PlayerRestrictionManager.canPlayerDropItem(serverPlayer)) {
                String playerName = serverPlayer.method_7334().getName();
                String itemName = stack.method_7909().toString();
                int itemCount = stack.method_7947();
                
                Ohc_Login.LOGGER.info("阻止未登录玩家 {} 丢弃物品: {} x{}", 
                        playerName, itemName, itemCount);
                
                class_2561 restrictionMessage = class_2561.method_43470("请先登录后再丢弃物品！")
                        .method_27692(class_124.field_1061);
                serverPlayer.method_7353(restrictionMessage, true);
                cir.setReturnValue(null);
            }
        }
    }
    

    
    // 注意：dropSelectedItem方法在服务端PlayerEntity中不存在，只存在于客户端
    // dropItem(ItemStack, boolean)方法应该是服务端处理所有物品丢弃的核心方法
    // 如果拦截仍然无效，可能需要检查网络包处理或其他途径
}