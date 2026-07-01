package com.jmane2026.totes.block.entity;

import com.jmane2026.totes.Totes;
import com.jmane2026.totes.block.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;
import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Totes.MODID);

    public static final Supplier<BlockEntityType<ToteBlockEntity>> TOTE_BE = BLOCK_ENTITIES.register(
            "tote_be",
            () -> new BlockEntityType<>(
                    ToteBlockEntity::new,
                    Set.of(ModBlocks.TOTE.get())
            )
    );

    public static final Supplier<BlockEntityType<ToteControllerBlockEntity>> TOTE_CONTROLLER_BE = BLOCK_ENTITIES.register(
            "tote_controller_be",
            () -> new BlockEntityType<>(
                    ToteControllerBlockEntity::new,
                    Set.of(ModBlocks.TOTE_CONTROLLER.get())
            )
    );
}