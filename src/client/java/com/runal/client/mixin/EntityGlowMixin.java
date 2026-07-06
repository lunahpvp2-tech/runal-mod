package com.runal.client.mixin;

import com.runal.client.TeamTrackerState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityGlowMixin {

    @Inject(method = "isCurrentlyGlowing", at = @At("RETURN"), cancellable = true)
    private void runal$teamGlow(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) return;
        if (!TeamTrackerState.INSTANCE.isEnabled() || !TeamTrackerState.INSTANCE.glowEnabled) return;

        if ((Object) this instanceof Player player && TeamTrackerState.INSTANCE.isTeammate(player.getUUID())) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getTeamColor", at = @At("RETURN"), cancellable = true)
    private void runal$teamColor(CallbackInfoReturnable<Integer> cir) {
        if (!TeamTrackerState.INSTANCE.isEnabled()) return;

        if ((Object) this instanceof Player player && TeamTrackerState.INSTANCE.isTeammate(player.getUUID())) {
            cir.setReturnValue(TeamTrackerState.INSTANCE.getTeammateColor(player.getUUID()) & 0xFFFFFF);
        }
    }
}
