package com.jmane2026.totes.block.entity.renderer;

import com.jmane2026.totes.Totes;
import com.jmane2026.totes.block.entity.ToteBlockEntity;
import com.jmane2026.totes.block.entity.ToteControllerBlockEntity;
import com.jmane2026.totes.component.ModDataComponents;
import com.jmane2026.totes.item.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class ToteBlockEntityRenderer implements BlockEntityRenderer<ToteBlockEntity, ToteBlockEntityRenderer.ToteRenderState> {
    private static final Identifier PADLOCK_TEXTURE = Identifier.fromNamespaceAndPath(Totes.MODID, "textures/item/padlock.png");
    
    private static final Identifier OVERLAY_TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/block/white_stained_glass.png");
    private static final Identifier LIGHTMAP_TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/block/white_stained_glass.png");

    private static final RenderType PADLOCK_TYPE = RenderType.create("tote_padlock",
            RenderSetup.builder(RenderPipelines.ENTITY_CUTOUT).withTexture("Sampler0", PADLOCK_TEXTURE)
                    .withTexture("Sampler1", OVERLAY_TEXTURE).withTexture("Sampler2", LIGHTMAP_TEXTURE).createRenderSetup());

    private static final RenderType LINKER_HIGHLIGHT = RenderType.create("tote_linker_highlight",
            RenderSetup.builder(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath(Totes.MODID, "pipeline/linker_highlight"))
                    .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
                    .withCull(false)
                    .build())
                .createRenderSetup());

    private final ItemModelResolver itemModelResolver;
    private final Font font;

    public ToteBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
        this.font = context.font();
    }

    @Override
    public ToteRenderState createRenderState() {
        return new ToteRenderState();
    }

    @Override
    public void extractRenderState(ToteBlockEntity blockEntity, ToteRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        state.facing = blockEntity.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        state.count = blockEntity.getCount();
        state.isLocked = blockEntity.isLocked();

        this.itemModelResolver.updateForTopItem(state.itemRenderState, blockEntity.getStoredItem(), ItemDisplayContext.FIXED, blockEntity.getLevel(), null, 0);

        for (int i = 0; i < 4; i++) {
            this.itemModelResolver.updateForTopItem(state.upgradeRenderStates[i], blockEntity.getUpgrades().get(i), ItemDisplayContext.FIXED, blockEntity.getLevel(), null, 0);
        }

        Player player = Minecraft.getInstance().player;
        state.isLinkedHighlight = false;
        if (player != null) {
            ItemStack held = player.getMainHandItem().is(ModItems.TOTE_LINKER.get()) ? player.getMainHandItem() :
                    (player.getOffhandItem().is(ModItems.TOTE_LINKER.get()) ? player.getOffhandItem() : ItemStack.EMPTY);

            if (!held.isEmpty()) {
                BlockPos targetController = held.get(ModDataComponents.LINKER_TARGET.get());
                if (targetController != null) {
                    if (blockEntity.getLevel().getBlockEntity(targetController) instanceof ToteControllerBlockEntity controller) {
                        state.isLinkedHighlight = controller.isLinked(blockEntity.getBlockPos());
                        if (state.isLinkedHighlight) {
                        }
                    }
                }
            }
        }
    }

    @Override
    public void submit(ToteRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (state.count <= 0 && !state.isLocked) return;

        Direction facing = state.facing;
        MultiBufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        if (state.isLinkedHighlight) {
            VertexConsumer builder = bufferSource.getBuffer(LINKER_HIGHLIGHT);
            float size = 0.49f;

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
                builder.addVertex(matrix, -size, -size, 0).setColor(0, 255, 0, 120);
                builder.addVertex(matrix, -size,  size, 0).setColor(0, 255, 0, 120);
                builder.addVertex(matrix,  size,  size, 0).setColor(0, 255, 0, 120);
                builder.addVertex(matrix,  size, -size, 0).setColor(0, 255, 0, 120);
                poseStack.popPose();
            }
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        poseStack.translate(0, 0, 0.501);

        poseStack.pushPose();
        poseStack.translate(0, 0.02, 0);
        poseStack.scale(0.45f, 0.45f, 0.001f);
        state.itemRenderState.submit(poseStack, submitNodeCollector, 15728880, 655360, 0);
        poseStack.popPose();

        if (state.isLocked) {
            poseStack.pushPose();
            poseStack.translate(0, 0.3575, 0.002);

            PoseStack.Pose pose = poseStack.last();
            Matrix4f matrix = pose.pose();
            VertexConsumer builder = bufferSource.getBuffer(PADLOCK_TYPE);
            float s = 0.05f;

            builder.addVertex(matrix, -s, -s, 0).setColor(255, 255, 255, 255).setUv(0, 1).setUv1(0, 10).setUv2(240, 240).setNormal(pose, 0, 0, 1);
            builder.addVertex(matrix, -s,  s, 0).setColor(255, 255, 255, 255).setUv(0, 0).setUv1(0, 10).setUv2(240, 240).setNormal(pose, 0, 0, 1);
            builder.addVertex(matrix,  s,  s, 0).setColor(255, 255, 255, 255).setUv(1, 0).setUv1(0, 10).setUv2(240, 240).setNormal(pose, 0, 0, 1);
            builder.addVertex(matrix,  s, -s, 0).setColor(255, 255, 255, 255).setUv(1, 1).setUv1(0, 10).setUv2(240, 240).setNormal(pose, 0, 0, 1);

            poseStack.popPose();
        }

        if (state.count > 0 || state.isLocked) {
            renderText(formatCompact(state.count), -0.36f, 1.0f, poseStack, bufferSource, 15728880);
        }

        for (int i = 0; i < 4; i++) {
            if (!state.upgradeRenderStates[i].isEmpty()) {
                poseStack.pushPose();
                float spacing = 0.07f;
                float offsetX = 0.1575f + (i * spacing);
                float offsetY = 0.40f;
                poseStack.translate(offsetX, offsetY, 0.005);
                poseStack.scale(0.05f, 0.05f, 0.05f);
                state.upgradeRenderStates[i].submit(poseStack, submitNodeCollector, 15728880, 655360, 0);
                poseStack.popPose();
            }
        }

        poseStack.popPose();
    }

    private String formatCompact(long count) {
        if (count < 100000) return String.valueOf(count);
        if (count < 1000000) {
            double value = count / 1000.0;
            return (count % 1000 == 0) ? String.format("%.0fk", value) : String.format("%.1fk", value);
        }
        double value = count / 1000000.0;
        if (count % 1000000 == 0) return String.format("%.0fM", value);
        if (count % 100000 == 0) return String.format("%.1fM", value);
        return String.format("%.2fM", value);
    }

    private void renderText(String text, float yOffset, float scaleMult, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();
        poseStack.translate(0, yOffset, 0.001);
        poseStack.scale(0.012f * scaleMult, -0.012f * scaleMult, 0.012f * scaleMult);
        float width = -font.width(text) / 2f;
        font.drawInBatch(text, width, 0, 0xFFFFFFFF, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, light);
        poseStack.popPose();
    }

    public static class ToteRenderState extends BlockEntityRenderState {
        public Direction facing = Direction.NORTH;
        public final ItemStackRenderState itemRenderState = new ItemStackRenderState();
        public final ItemStackRenderState padlockRenderState = new ItemStackRenderState();
        public final ItemStackRenderState[] upgradeRenderStates = new ItemStackRenderState[]{
                new ItemStackRenderState(),
                new ItemStackRenderState(),
                new ItemStackRenderState(),
                new ItemStackRenderState()
        };
        public int count = 0;
        public boolean isLocked = false;
        public boolean isLinkedHighlight = false;
    }


}