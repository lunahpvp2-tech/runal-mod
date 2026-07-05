package com.runal.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.runal.client.HideArmorState;
//? if 1.21.4 {
/*import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
*///?} else {
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
//?}
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
abstract class HumanoidArmorLayerMixin {

    //? if 1.21.4 {
    /*@Inject(method = "renderArmorPiece", at = @At("HEAD"), cancellable = true)
    private void runal$hideArmor(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            ItemStack itemStack,
            EquipmentSlot equipmentSlot,
            int light,
            HumanoidModel<?> armorModel,
            CallbackInfo ci
    ) {
        if (HideArmorState.INSTANCE.shouldHide(equipmentSlot)) {
            ci.cancel();
        }
    }
    *///?} else {
    @Inject(method = "renderArmorPiece", at = @At("HEAD"), cancellable = true)
    private <S extends HumanoidRenderState> void runal$hideArmor(
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            ItemStack itemStack,
            EquipmentSlot equipmentSlot,
            int light,
            S humanoidRenderState,
            CallbackInfo ci
    ) {
        if (HideArmorState.INSTANCE.shouldHide(equipmentSlot)) {
            ci.cancel();
        }
    }
    //?}
}
