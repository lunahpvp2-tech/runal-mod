package com.runal.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    //? if 1.21.4 {
    /*// Real 1.21.4's aiStep() doesn't read Input.sprint() directly (unlike 1.21.11/26.1.2) - it
    // checks the sprint KeyMapping.isDown() twice instead, each immediately followed by
    // setSprinting(), so that's the equivalent "is the sprint key held" signal to override here.
    @ModifyExpressionValue(
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
    //? if 1.21.11 || 26.1.2 {
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
