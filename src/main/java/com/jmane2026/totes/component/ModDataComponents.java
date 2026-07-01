package com.jmane2026.totes.component;

import com.jmane2026.totes.Totes;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.List;
import java.util.function.Supplier;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Totes.MODID);

    public static final Supplier<DataComponentType<ItemResource>> STORED_ITEM = COMPONENTS.register("stored_item",
            () -> DataComponentType.<ItemResource>builder().persistent(ItemResource.CODEC).networkSynchronized(ItemResource.STREAM_CODEC).build());

    public static final Supplier<DataComponentType<Integer>> COUNT = COMPONENTS.register("count", 
            () -> DataComponentType.<Integer>builder().persistent(net.minecraft.util.ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT).build());

    public static final Supplier<DataComponentType<Boolean>> IS_LOCKED = COMPONENTS.register("is_locked",
            () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());

    public static final Supplier<DataComponentType<List<ItemResource>>> UPGRADES = COMPONENTS.register("upgrades",
            () -> DataComponentType.<List<ItemResource>>builder().persistent(ItemResource.CODEC.listOf())
                    .networkSynchronized(ItemResource.STREAM_CODEC.apply(ByteBufCodecs.list(4))).build());

    public static final Supplier<DataComponentType<List<BlockPos>>> LINKED_TOTES = COMPONENTS.register("linked_totes",
            () -> DataComponentType.<List<BlockPos>>builder().persistent(BlockPos.CODEC.listOf())
                    .networkSynchronized(BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list(1024))).build());

    public static final Supplier<DataComponentType<BlockPos>> LINKER_TARGET = COMPONENTS.register("linker_target",
            () -> DataComponentType.<BlockPos>builder().persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC).build());

    public static final Supplier<DataComponentType<Boolean>> LINKER_MODE = COMPONENTS.register("linker_mode",
            () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());
}