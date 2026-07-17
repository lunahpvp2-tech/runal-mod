package com.runal.client.mixin;

import com.runal.client.FullbrightState;
//? if 1.21.4 {
/*import net.minecraft.client.renderer.DimensionSpecialEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
*///?}
//? if 1.21.11 {
/*import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
*///?}
//? if 26.1.2 || 26.2 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
//?}

//? if 1.21.4 {
/*@Mixin(DimensionSpecialEffects.class)
abstract class LightmapRenderStateExtractorMixin {

    @Inject(method = "forceBrightLightmap", at = @At("RETURN"), cancellable = true)
    private void runal$fullbright(CallbackInfoReturnable<Boolean> cir) {
        if (FullbrightState.INSTANCE.isEnabled()) {
            cir.setReturnValue(true);
        }
    }
}
*///?}
//? if 1.21.11 {
/*@Mixin(LightTexture.class)
abstract class LightmapRenderStateExtractorMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void runal$fullbright(CallbackInfo ci) {
    }
}
*///?}
//? if 26.1.2 || 26.2 {
@Mixin(LightmapRenderStateExtractor.class)
abstract class LightmapRenderStateExtractorMixin {

    @ModifyExpressionValue(
            method = "extract",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/ARGB;vector3fFromRGB24(I)Lorg/joml/Vector3f;",
                    ordinal = 2
            )
    )
    private Vector3f runal$fullbright(Vector3f original) {
        if (FullbrightState.INSTANCE.isEnabled()) {
            return new Vector3f(1.0f, 1.0f, 1.0f);
        }
        return original;
    }
}
//?}
