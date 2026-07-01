package com.jmane2026.totes;

import com.jmane2026.totes.block.entity.ModBlockEntities;
import com.jmane2026.totes.block.entity.renderer.ToteBlockEntityRenderer;
import com.jmane2026.totes.block.entity.renderer.ToteControllerBlockEntityRenderer;
import com.jmane2026.totes.client.ToteHUDHandler;
import com.jmane2026.totes.client.ToteScreen;
import com.jmane2026.totes.menu.ModMenus;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@Mod(value = Totes.MODID, dist = Dist.CLIENT)
public class TotesClient {
    public TotesClient(ModContainer container, IEventBus modEventBus) {
        modEventBus.addListener(this::registerRenderers);
        modEventBus.addListener(this::registerGuiLayers);
        modEventBus.addListener(this::registerScreens);
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.TOTE_BE.get(), ToteBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TOTE_CONTROLLER_BE.get(), ToteControllerBlockEntityRenderer::new);
    }

    private void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, Identifier.fromNamespaceAndPath(Totes.MODID, "tote_tooltip"),
                (guiGraphics, deltaTracker) -> {
                    ToteHUDHandler.renderToteTooltip(guiGraphics, deltaTracker.getGameTimeDeltaTicks());
                });
    }

    private void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.TOTE_MENU.get(), ToteScreen::new);
    }
}
