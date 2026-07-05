package com.runal.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.runal.client.PlayerScaleState;
import com.runal.client.RealPlayerTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
abstract class AvatarRendererMixin {

    @Inject(
            method = "scale(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V",
            at = @At("HEAD")
    )
    private void runal$scale(AvatarRenderState avatarRenderState, PoseStack poseStack, CallbackInfo ci) {
        if (!PlayerScaleState.INSTANCE.isScaled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Entity entity = mc.level.getEntity(avatarRenderState.id);
        if (entity == null) return;

        boolean isSelf = entity == mc.player;
        if ("Self".equals(PlayerScaleState.INSTANCE.target)) {
            // "Self" should only ever affect the local client's own model.
            if (!isSelf) return;
        } else if (!isSelf && !RealPlayerTracker.isVerifiedRealPlayer(entity.getUUID())) {
            // "Everyone" means every genuine player - never an NPC impersonating one.
            return;
        }

        poseStack.scale(PlayerScaleState.INSTANCE.getXScale(), PlayerScaleState.INSTANCE.getYScale(), PlayerScaleState.INSTANCE.getZScale());
    }
}
