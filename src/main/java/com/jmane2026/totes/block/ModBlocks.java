package com.jmane2026.totes.block;

import com.jmane2026.totes.Totes;
import com.jmane2026.totes.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Totes.MODID);

    public static final DeferredBlock<ToteBlock> TOTE = BLOCKS.registerBlock("tote",
            ToteBlock::new,
            p -> p.strength(1.0f).noOcclusion());

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () -> new BlockItem(toReturn.get(), new Item.Properties()));
        return toReturn;
    }

    public static final DeferredBlock<ToteControllerBlock> TOTE_CONTROLLER = BLOCKS.registerBlock("tote_controller",
            ToteControllerBlock::new,
            p -> p.strength(1.0f).noOcclusion());
}