package com.runal.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
//? if 1.21.4 {
/*import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.renderer.RenderTypes;
*///?}
//? if 1.21.11 {
/*import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.renderer.rendertype.RenderTypes;
*///?}
//? if 26.1.2 {
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.renderer.rendertype.RenderTypes;
//?}
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

import com.mojang.blaze3d.vertex.PoseStack;

public class HitboxRenderer {

    public static void register() {
        //? if 1.21.4 {
        /*WorldRenderEvents.AFTER_TRANSLUCENT.register(HitboxRenderer::render);
        *///?}
        //? if 1.21.11 {
        /*WorldRenderEvents.END_MAIN.register(HitboxRenderer::render);
        *///?}
        //? if 26.1.2 {
        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(HitboxRenderer::render);
        //?}
    }

    //? if 1.21.4 || 1.21.11 {
    /*private static void render(WorldRenderContext context) {
    *///?}
    //? if 26.1.2 {
    private static void render(LevelRenderContext context) {
    //?}
        if (!HitboxesState.INSTANCE.isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        Vec3 cameraPos = context.gameRenderer().getMainCamera().position();
        //? if 1.21.4 {
        /*PoseStack poseStack = context.matrixStack();
        MultiBufferSource.BufferSource bufferSource = (MultiBufferSource.BufferSource) context.consumers();
        VertexConsumer consumer = bufferSource.getBuffer(RenderTypes.lines());
        *///?}
        //? if 1.21.11 {
        /*PoseStack poseStack = context.matrices();
        MultiBufferSource.BufferSource bufferSource = (MultiBufferSource.BufferSource) context.consumers();
        VertexConsumer consumer = bufferSource.getBuffer(RenderTypes.lines());
        *///?}
        //? if 26.1.2 {
        PoseStack poseStack = context.poseStack();
        MultiBufferSource.BufferSource bufferSource = context.bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderTypes.lines());
        //?}

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity) || entity.isRemoved()) continue;
            if (entity == mc.player && mc.options.getCameraType().isFirstPerson()) continue;

            AABB box = entity.getBoundingBox();
            int color = entity instanceof Player ? HitboxesState.INSTANCE.playerColor : HitboxesState.INSTANCE.entityColor;
            //? if 1.21.4 {
            /*ShapeRenderer.renderShape(poseStack, consumer, Shapes.create(box), 0, 0, 0, color);
            *///?}
            //? if 1.21.11 || 26.1.2 {
            ShapeRenderer.renderShape(poseStack, consumer, Shapes.create(box), 0, 0, 0, color, HitboxesState.INSTANCE.lineWidth);
            //?}
        }

        poseStack.popPose();
        //? if 1.21.4 {
        /*bufferSource.endBatch(RenderTypes.lines());
        *///?}
        //? if 1.21.11 || 26.1.2 {
        bufferSource.endBatch(RenderTypes.lines());
        //?}
    }
}
