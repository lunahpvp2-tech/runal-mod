package com.runal.client.mixin;

import com.runal.client.HidePlayersState;
import com.runal.client.RealPlayerTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
abstract class HidePlayersMixin {

    @Inject(method = "shouldRender", at = @At("RETURN"), cancellable = true)
    private void runal$hidePlayers(Entity entity, Frustum frustum, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) return;
        if (!HidePlayersState.INSTANCE.isEnabled()) return;
        if (!(entity instanceof Player)) return;

        Minecraft mc = Minecraft.getInstance();
        if (entity == mc.player) return;
        if (mc.getConnection() == null) return;

        if (!RealPlayerTracker.isVerifiedRealPlayer(entity.getUUID())) return;

        cir.setReturnValue(false);
    }
}
