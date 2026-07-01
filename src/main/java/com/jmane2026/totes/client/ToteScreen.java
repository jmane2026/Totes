package com.jmane2026.totes.client;

import com.jmane2026.totes.block.entity.ToteBlockEntity;
import com.jmane2026.totes.menu.ToteMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class ToteScreen extends AbstractContainerScreen<ToteMenu> {

    public ToteScreen(ToteMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }
    
    @Override
    protected void init() {
        super.init();
        this.topPos = this.height - 230;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int bw = 90;
        int bh = 110;
        int x = this.leftPos + 43;
        int y = this.topPos + 10; 

        graphics.fill(x, y, x + bw, y + bh, 0xFFC6C6C6);

        graphics.fill(x, y, x + bw, y + 1, 0xFFFFFFFF);
        graphics.fill(x, y, x + 1, y + bh, 0xFFFFFFFF);
        graphics.fill(x + bw - 1, y, x + bw, y + bh, 0xFF555555);
        graphics.fill(x, y + bh - 1, x + bw, y + bh, 0xFF555555);

        for (int i = 0; i < 4; i++) {
            drawSlotBg(graphics, this.leftPos + 51 + (i * 18), this.topPos + 87);
        }
    }

    private void drawSlotBg(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, 0xFF8B8B8B);
        graphics.fill(x, y, x + 18, y + 1, 0xFF373737);
        graphics.fill(x, y, x + 1, y + 18, 0xFF373737);
        graphics.fill(x + 17, y, x + 18, y + 18, 0xFFFFFFFF);
        graphics.fill(x, y + 17, x + 18, y + 18, 0xFFFFFFFF);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }

    @Override
    protected void extractSlot(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY) {
        if (slot.index >= 4) return;
        super.extractSlot(graphics, slot, mouseX, mouseY);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        ToteBlockEntity tote = menu.getBlockEntity();
        if (tote == null) return;

        int centerX = this.leftPos + 88;
        int boxTop = this.topPos + 10;

        Component name = tote.getStoredItem().isEmpty() ? Component.literal("Empty Tote") : tote.getStoredItem().getHoverName();
        graphics.text(this.font, name, centerX - (this.font.width(name) / 2), boxTop + 8, 0xFF404040, false);

        String readout = formatCompact(tote.getCount()) + " / " + formatCompact(tote.getMaxCapacity());
        graphics.text(this.font, readout, centerX - (this.font.width(readout) / 2), boxTop + 50, 0xFF404040, false);

        graphics.text(this.font, "Upgrades", centerX - (this.font.width("Upgrades") / 2), boxTop + 65, 0xFF404040, false);

        graphics.item(tote.getStoredItem(), centerX - 8, boxTop + 20);
    }

    private String formatCompact(long count) {
        if (count < 100000) return String.format("%,d", count);
        if (count < 1000000) {
            double value = count / 1000.0;
            return (count % 1000 == 0) ? String.format("%.0fk", value) : String.format("%.1fk", value);
        }
        double value = count / 1000000.0;
        if (count % 1000000 == 0) return String.format("%.0fM", value);
        if (count % 100000 == 0) return String.format("%.1fM", value);
        return String.format("%.2fM", value);
    }
}