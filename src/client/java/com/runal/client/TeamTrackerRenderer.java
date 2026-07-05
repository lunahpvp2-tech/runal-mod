package com.runal.client;

import com.mojang.blaze3d.vertex.PoseStack;
//? if 1.21.4 {
/*import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
*///?}
//? if 1.21.11 {
/*import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
*///?}
//? if 26.1.2 {
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
//?}
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class TeamTrackerRenderer {

    public static void register() {
        //? if 1.21.4 {
        /*WorldRenderEvents.AFTER_TRANSLUCENT.register(TeamTrackerRenderer::render);
        *///?}
        //? if 1.21.11 {
        /*WorldRenderEvents.END_MAIN.register(TeamTrackerRenderer::render);
        *///?}
        //? if 26.1.2 {
        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(TeamTrackerRenderer::render);
        //?}
    }

    //? if 1.21.4 || 1.21.11 {
    /*private static void render(WorldRenderContext context) {
    *///?}
    //? if 26.1.2 {
    private static void render(LevelRenderContext context) {
    //?}
        if (!TeamTrackerState.INSTANCE.isEnabled()) return;
        if (TeamTrackerState.INSTANCE.getTeammates().isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        Vec3 cameraPos = context.gameRenderer().getMainCamera().position();
        //? if 1.21.4 {
        /*PoseStack poseStack = context.matrixStack();
        MultiBufferSource.BufferSource bufferSource = (MultiBufferSource.BufferSource) context.consumers();
        *///?}
        //? if 1.21.11 {
        /*PoseStack poseStack = context.matrices();
        MultiBufferSource.BufferSource bufferSource = (MultiBufferSource.BufferSource) context.consumers();
        *///?}
        //? if 26.1.2 {
        PoseStack poseStack = context.poseStack();
        MultiBufferSource.BufferSource bufferSource = context.bufferSource();
        //?}
        Font font = mc.font;
        boolean rendered = false;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof Player player) || entity.isRemoved()) continue;
            if (player == mc.player) continue;
            if (!TeamTrackerState.INSTANCE.isTeammate(player.getUUID())) continue;

            String name = player.getGameProfile().name();
            double x = entity.getX() - cameraPos.x;
            double y = entity.getY() + entity.getBbHeight() + 0.5 - cameraPos.y;
            double z = entity.getZ() - cameraPos.z;

            poseStack.pushPose();
            poseStack.translate(x, y, z);
            poseStack.mulPose(context.gameRenderer().getMainCamera().rotation());
            poseStack.scale(-0.025f, -0.025f, 0.025f);

            int textWidth = font.width(name);
            font.drawInBatch(name, -textWidth / 2f, 0, TeamTrackerState.INSTANCE.markerColor, false,
                    poseStack.last().pose(), bufferSource, Font.DisplayMode.SEE_THROUGH, 0, 0xF000F0);

            poseStack.popPose();
            rendered = true;
        }

        if (rendered) bufferSource.endBatch();
    }
}
