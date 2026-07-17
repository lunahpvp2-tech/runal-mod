package com.runal.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    //? if 1.21.4 {
    /*@ModifyExpressionValue(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/KeyMapping;isDown()Z"
            )
    )
    private boolean runal$autoSprint(boolean original) {
        return original || com.runal.AutoSprintState.INSTANCE.isEnabled();
    }
    *///?}
    //? if 1.21.11 || 26.1.2 || 26.2 {
    @ModifyExpressionValue(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Input;sprint()Z"
            )
    )
    private boolean runal$autoSprint(boolean original) {
        return original || com.runal.AutoSprintState.INSTANCE.isEnabled();
    }
    //?}
}
