package com.jmane2026.totes;

import com.jmane2026.totes.block.ModBlocks;
import com.jmane2026.totes.block.entity.ModBlockEntities;
import com.jmane2026.totes.component.ModDataComponents;
import com.jmane2026.totes.item.ModItems;
import com.jmane2026.totes.menu.ModMenus;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@Mod(Totes.MODID)
public class Totes {
    public static final String MODID = "totes";

    public Totes(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModDataComponents.COMPONENTS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::registerCapabilities);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.TOTE_BE.get(), (be, side) -> be.getItemHandler());
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.TOTE_CONTROLLER_BE.get(), (be, side) -> be.getItemHandler());
    }
}