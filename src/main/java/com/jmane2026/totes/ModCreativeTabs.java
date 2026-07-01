package com.jmane2026.totes;

import com.jmane2026.totes.block.ModBlocks;
import com.jmane2026.totes.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Totes.MODID);

    public static final Supplier<CreativeModeTab> TOTE_TAB = CREATIVE_MODE_TABS.register("tote_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.totes.tote_tab"))
                    .icon(() -> new ItemStack(ModBlocks.TOTE_CONTROLLER.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.TOTE.get());
                        output.accept(ModItems.TOTE_CONTROLLER.get());
                        output.accept(ModItems.TOTE_KEY.get());
                        output.accept(ModItems.TOTE_LINKER.get());
                        output.accept(ModItems.TOTE_UPGRADE_COPPER.get());
                        output.accept(ModItems.TOTE_UPGRADE_IRON.get());
                        output.accept(ModItems.TOTE_UPGRADE_GOLD.get());
                        output.accept(ModItems.TOTE_UPGRADE_DIAMOND.get());
                        output.accept(ModItems.TOTE_UPGRADE_NETHERITE.get());
                        output.accept(ModItems.TOTE_UPGRADE_VOID.get());
                        output.accept(ModItems.PLASTIC.get());
                    }).build());
}