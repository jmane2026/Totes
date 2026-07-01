package com.jmane2026.totes.client;

import com.jmane2026.totes.block.entity.ToteBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ToteHUDHandler {
    public static void renderToteTooltip(GuiGraphicsExtractor guiGraphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        HitResult hit = mc.hitResult;
        if (hit instanceof BlockHitResult blockHit) {
            if (mc.level.getBlockEntity(blockHit.getBlockPos()) instanceof ToteBlockEntity tote) {
                ItemStack stored = tote.getStoredItem();
                if (!stored.isEmpty() && mc.screen == null) {
                    String text = stored.getHoverName().getString();

                    int x = guiGraphics.guiWidth() / 2;
                    int y = guiGraphics.guiHeight() - 72;

                    guiGraphics.centeredText(mc.font, text, x, y, 0xFFFFFFFF);
                }
            }
        }
    }
}