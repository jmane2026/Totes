package com.jmane2026.totes.block.entity.renderer;

import com.jmane2026.totes.Totes;
import com.jmane2026.totes.block.entity.ToteControllerBlockEntity;
import com.jmane2026.totes.component.ModDataComponents;
import com.jmane2026.totes.item.ModItems;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class ToteControllerBlockEntityRenderer implements BlockEntityRenderer<ToteControllerBlockEntity, ToteControllerBlockEntityRenderer.ControllerRenderState> {

    private static final RenderType TARGET_HIGHLIGHT = RenderType.create("tote_controller_highlight",
            RenderSetup.builder(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                            .withLocation(Identifier.fromNamespaceAndPath(Totes.MODID, "pipeline/controller_highlight"))
                            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
                            .withCull(false)
                            .build())
                    .createRenderSetup());

    public ToteControllerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public ControllerRenderState createRenderState() {
        return new ControllerRenderState();
    }

    @Override
    public void extractRenderState(ToteControllerBlockEntity blockEntity, ControllerRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        Player player = Minecraft.getInstance().player;
        state.isTargeted = false;

        if (player != null) {
            ItemStack held = player.getMainHandItem().is(ModItems.TOTE_LINKER.get()) ? player.getMainHandItem() :
                    (player.getOffhandItem().is(ModItems.TOTE_LINKER.get()) ? player.getOffhandItem() : ItemStack.EMPTY);

            if (!held.isEmpty()) {
                BlockPos targetPos = held.get(ModDataComponents.LINKER_TARGET.get());
                if (blockEntity.getBlockPos().equals(targetPos)) {
                    state.isTargeted = true;
                }
            }
        }
    }

    @Override
    public void submit(ControllerRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (!state.isTargeted) return;

        var bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer builder = bufferSource.getBuffer(TARGET_HIGHLIGHT);
        float size = 0.495f;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        for (Direction dir : Direction.values()) {
            poseStack.pushPose();
            if (dir == Direction.UP) {
                poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            } else if (dir == Direction.DOWN) {
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            } else {
                poseStack.mulPose(Axis.YP.rotationDegrees(-dir.toYRot()));
            }

            poseStack.translate(0, 0, 0.5005);
            Matrix4f matrix = poseStack.last().pose();

            builder.addVertex(matrix, -size, -size, 0).setColor(0, 100, 255, 130);
            builder.addVertex(matrix, -size,  size, 0).setColor(0, 100, 255, 130);
            builder.addVertex(matrix,  size,  size, 0).setColor(0, 100, 255, 130);
            builder.addVertex(matrix,  size, -size, 0).setColor(0, 100, 255, 130);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    public static class ControllerRenderState extends BlockEntityRenderState {
        public boolean isTargeted = false;
    }
}