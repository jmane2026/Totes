package com.jmane2026.totes.block.entity;

import com.jmane2026.totes.component.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ToteControllerBlockEntity extends BlockEntity {
    private final List<BlockPos> linkedTotes = new ArrayList<>();

    public ToteControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TOTE_CONTROLLER_BE.get(), pos, state);
    }

    public List<BlockPos> getLinkedTotes() { return linkedTotes; }

    public void addLink(BlockPos pos) {
        if (!linkedTotes.contains(pos)) {
            linkedTotes.add(pos);
            markUpdated();
        }
    }

    private final ResourceHandler<ItemResource> itemHandler = new ResourceHandler<>() {
        @Override
        public int size() {
            return 1;
        }

        @Override
        public ItemResource getResource(int slot) {
            return ItemResource.EMPTY;
        }

        @Override
        public long getAmountAsLong(int slot) {
            return 0;
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (resource.isEmpty() || amount <= 0) return 0;

            int totalInserted = 0;
            int remaining = amount;

            for (BlockPos totePos : linkedTotes) {
                ResourceHandler<ItemResource> handler = getHandler(totePos);
                if (handler != null && !handler.getResource(0).isEmpty() && handler.getResource(0).equals(resource)) {
                    int inserted = handler.insert(0, resource, remaining, transaction);
                    totalInserted += inserted;
                    remaining -= inserted;
                    if (remaining <= 0) return totalInserted;
                }
            }

            for (BlockPos totePos : linkedTotes) {
                ResourceHandler<ItemResource> handler = getHandler(totePos);
                if (handler != null && handler.getResource(0).isEmpty()) {
                    int inserted = handler.insert(0, resource, remaining, transaction);
                    totalInserted += inserted;
                    remaining -= inserted;
                    if (remaining <= 0) return totalInserted;
                }
            }

            return totalInserted;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (resource.isEmpty() || amount <= 0) return 0;

            int totalExtracted = 0;
            int remaining = amount;

            for (BlockPos totePos : linkedTotes) {
                ResourceHandler<ItemResource> handler = getHandler(totePos);
                if (handler != null && handler.getResource(0).equals(resource)) {
                    int extracted = handler.extract(0, resource, remaining, transaction);
                    totalExtracted += extracted;
                    remaining -= extracted;
                    if (remaining <= 0) break;
                }
            }

            return totalExtracted;
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            long total = 0;
            for (BlockPos pos : linkedTotes) {
                ResourceHandler<ItemResource> handler = getHandler(pos);
                if (handler != null) total += handler.getCapacityAsLong(0, resource);
            }
            return total;
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            for (BlockPos pos : linkedTotes) {
                ResourceHandler<ItemResource> handler = getHandler(pos);
                if (handler != null && handler.isValid(0, resource)) return true;
            }
            return false;
        }

        @Nullable
        private ResourceHandler<ItemResource> getHandler(BlockPos pos) {
            if (level == null || !level.isLoaded(pos)) return null;
            return level.getCapability(Capabilities.Item.BLOCK, pos, null);
        }
    };

    public ResourceHandler<ItemResource> getItemHandler() { return itemHandler; }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("linked_totes", BlockPos.CODEC.listOf(), List.copyOf(linkedTotes));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        linkedTotes.clear();
        linkedTotes.addAll(input.read("linked_totes", BlockPos.CODEC.listOf()).orElse(List.of()));
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        List<BlockPos> loaded = components.get(ModDataComponents.LINKED_TOTES.get());
        if (loaded != null) {
            this.linkedTotes.clear();
            this.linkedTotes.addAll(loaded);
        }
    }

    public void removeLink(BlockPos pos) {
        if (linkedTotes.remove(pos)) {
            markUpdated();
        }
    }

    public boolean isLinked(BlockPos pos) {
        return linkedTotes.contains(pos);
    }

    private void markUpdated() {
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        BlockPos.CODEC.listOf().encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), List.copyOf(this.linkedTotes))
                .ifSuccess(result -> tag.put("linked_totes", result));
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}