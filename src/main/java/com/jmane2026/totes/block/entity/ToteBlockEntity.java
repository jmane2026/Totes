package com.jmane2026.totes.block.entity;

import com.jmane2026.totes.menu.ToteMenu;
import com.jmane2026.totes.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import com.jmane2026.totes.component.ModDataComponents;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class ToteBlockEntity extends BlockEntity implements MenuProvider {

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.totes.tote");
    }

    @Override
    public @org.jspecify.annotations.Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ToteMenu(containerId, inventory, this);
    }

    private record ToteSnapshot(ItemStack item, int count, List<ItemStack> upgrades) {}

    private final SnapshotJournal<ToteSnapshot> journal = new SnapshotJournal<>() {
        @Override
        protected ToteSnapshot createSnapshot() {
            return new ToteSnapshot(storedItem.copy(), count, List.copyOf(upgrades));
        }

        @Override
        protected void revertToSnapshot(ToteSnapshot snapshot) {
            storedItem = snapshot.item();
            count = snapshot.count();
            upgrades.clear();
            upgrades.addAll(snapshot.upgrades());
        }

        @Override
        protected void onRootCommit(ToteSnapshot originalState) {
            markUpdated();
        }
    };

    private final ResourceHandler<ItemResource> itemHandler = new ResourceHandler<>() {
        @Override
        public int size() { return 1; }

        @Override
        public ItemResource getResource(int slot) {
            return ItemResource.of(storedItem);
        }


        @Override
        public long getAmountAsLong(int slot) {
            return count;
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (resource.isEmpty() || amount <= 0) return 0;

            boolean canInsert = false;
            if (count > 0) {
                canInsert = ItemResource.of(storedItem).equals(resource);
            } else if (isLocked) {
                canInsert = ItemResource.of(storedItem.getItem()).equals(resource);
            } else {
                canInsert = true;
            }

            if (!canInsert) return 0;

            long space = Math.max(0, getMaxCapacity() - count);
            int toTake = hasVoidUpgrade() ? amount : (int)Math.min(amount, space);

            if (toTake > 0) {
                journal.updateSnapshots(transaction);

                if (count <= 0) storedItem = resource.toStack(1);
                count += (int)Math.min(toTake, space);
            }
            return toTake;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (storedItem.isEmpty() || count <= 0 || amount <= 0 || !ItemResource.of(storedItem).equals(resource)) return 0;

            long toExtract = Math.min(amount, count);
            if (toExtract > 0) {
                journal.updateSnapshots(transaction);

                count -= (int) toExtract;
                if (count <= 0 && !isLocked) storedItem = ItemStack.EMPTY;
            }
            return (int) toExtract;
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            return getMaxCapacity();
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            if (resource.isEmpty()) return false;
            return count > 0 ? ItemResource.of(storedItem).equals(resource) : !isLocked || ItemResource.of(storedItem.getItem()).equals(resource);
        }
    };

    public ResourceHandler<ItemResource> getItemHandler() { return itemHandler; }

    private ItemStack storedItem = ItemStack.EMPTY;
    private int count = 0;
    private boolean isLocked = false;
    private final NonNullList<ItemStack> upgrades = NonNullList.withSize(4, ItemStack.EMPTY);

    public NonNullList<ItemStack> getUpgrades() { return upgrades; }

    public boolean hasVoidUpgrade() {
        return upgrades.stream().anyMatch(stack -> stack.is(ModItems.TOTE_UPGRADE_VOID.get()));
    }

    public long getMaxCapacity() {
        return getMaxCapacityWithout(-1);
    }

    public long getMaxCapacityWithout(int excludedIndex) {
        long cap = baseCapacity;
        for (int i = 0; i < upgrades.size(); i++) {
            if (i == excludedIndex) continue;
            ItemStack stack = upgrades.get(i);
            if (!stack.isEmpty()) {
                cap *= getMultiplier(stack.getItem());
            }
        }
        if (cap < baseCapacity) return baseCapacity;
        return cap;
    }

    private int getMultiplier(Item item) {
        if (item == ModItems.TOTE_UPGRADE_COPPER.get()) return 2;
        if (item == ModItems.TOTE_UPGRADE_IRON.get()) return 4;
        if (item == ModItems.TOTE_UPGRADE_GOLD.get()) return 6;
        if (item == ModItems.TOTE_UPGRADE_DIAMOND.get()) return 8;
        if (item == ModItems.TOTE_UPGRADE_NETHERITE.get()) return 10;
        if (item == ModItems.TOTE_UPGRADE_VOID.get()) return 1;
        return 1;
    }

    private final int baseCapacity = 2048;
    private long lastInteractionTime = 0;
    private UUID lastInteractingPlayer = null;

    public ToteBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TOTE_BE.get(), pos, state);
    }

    public ItemStack getStoredItem() { return storedItem; }
    public int getCount() { return count; }
    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) {
        this.isLocked = locked;
        if (!this.isLocked && this.count <= 0) {
            this.storedItem = ItemStack.EMPTY;
        }
        markUpdated();
    }

    public void depositItems(Player player, InteractionHand hand, int amount) {
        ItemStack playerStack = player.getItemInHand(hand);
        if (playerStack.isEmpty() && storedItem.isEmpty()) return;

        long currentTime = level.getGameTime();
        boolean isHolding = player.getUUID().equals(lastInteractingPlayer) && (currentTime - lastInteractionTime) < 5;
        
        lastInteractionTime = currentTime;
        lastInteractingPlayer = player.getUUID();

        if (isHolding) {
            boolean voiding = hasVoidUpgrade();
            
            ItemStack template = !storedItem.isEmpty() ? storedItem : playerStack;
            if (template.isEmpty()) return;

            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack invStack = player.getInventory().getItem(i);
                
                if (!invStack.isEmpty() && ItemStack.isSameItemSameComponents(template, invStack)) {
                    long space = getMaxCapacity() - count;
                    if (space <= 0 && !voiding) break;

                    int toTake = voiding ? invStack.getCount() : (int) Math.min(invStack.getCount(), space);
                    
                    if (storedItem.isEmpty()) {
                        storedItem = invStack.copyWithCount(1);
                    }
                    int added = (int) Math.min(toTake, space);
                    count += Math.max(0, added);
                    invStack.shrink(toTake);
                }
            }
        } else if (!playerStack.isEmpty()) {
            if (storedItem.isEmpty()) {
                storedItem = playerStack.copyWithCount(1);
                long space = getMaxCapacity();
                int toTake = hasVoidUpgrade() ? playerStack.getCount() : (int) Math.min(playerStack.getCount(), space);
                
                count = (int) Math.min(toTake, space);
                playerStack.shrink(toTake);
            } else if (ItemStack.isSameItemSameComponents(storedItem, playerStack)) {
                long space = getMaxCapacity() - count;
                int toTake = hasVoidUpgrade() ? playerStack.getCount() : (int) Math.min(playerStack.getCount(), (int)space);
                
                int added = (int) Math.min(toTake, space);
                count += Math.max(0, added);
                playerStack.shrink(toTake);
            }
        }
        markUpdated();
    }

    public boolean addUpgrade(ItemStack stack) {
        if (stack.is(ModItems.TOTE_UPGRADE_VOID.get()) && hasVoidUpgrade()) return false;

        for (int i = 0; i < upgrades.size(); i++) {
            if (upgrades.get(i).isEmpty()) {
                upgrades.set(i, stack.copyWithCount(1));
                this.setChanged();
                markUpdated();
                return true;
            }
        }
        return false;
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("stored_item", ItemStack.OPTIONAL_CODEC, this.storedItem);
        output.putInt("count", this.count);
        output.putBoolean("is_locked", this.isLocked);
        output.store("upgrades", ItemStack.OPTIONAL_CODEC.listOf(), List.copyOf(this.upgrades));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.storedItem = input.read("stored_item", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        this.count = input.getIntOr("count", 0);
        this.isLocked = input.getBooleanOr("is_locked", false);
        List<ItemStack> loadedUpgrades = input.read("upgrades", ItemStack.OPTIONAL_CODEC.listOf()).orElse(List.of());
        for (int i = 0; i < 4; i++) {
            this.upgrades.set(i, i < loadedUpgrades.size() ? loadedUpgrades.get(i) : ItemStack.EMPTY);
        }
    }

    public void extractItems(Player player, int amount) {
        if (storedItem.isEmpty() || count <= 0) return;

        int toExtract = Math.min(count, Math.min(amount, storedItem.getMaxStackSize()));
        ItemStack drop = storedItem.copyWithCount(toExtract);
        
        if (!player.getInventory().add(drop)) {
            level.addFreshEntity(new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5, drop));
        }
        
        count -= toExtract;
        if (count <= 0) {
            if (!isLocked) {
                storedItem = ItemStack.EMPTY;
            }
            count = 0;
        }
        markUpdated();
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
        ItemStack.OPTIONAL_CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), this.storedItem)
                .ifSuccess(result -> tag.put("stored_item", result));
        
        tag.putInt("count", this.count);
        tag.putBoolean("is_locked", this.isLocked);
        
        ItemStack.OPTIONAL_CODEC.listOf().encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), List.copyOf(this.upgrades))
                .ifSuccess(result -> tag.put("upgrades", result));

        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        ItemResource storedRes = components.getOrDefault(ModDataComponents.STORED_ITEM.get(), ItemResource.of(this.storedItem));
        this.storedItem = storedRes.toStack(1);
        
        this.count = components.getOrDefault(ModDataComponents.COUNT.get(), this.count);
        this.isLocked = components.getOrDefault(ModDataComponents.IS_LOCKED.get(), this.isLocked);

        List<ItemResource> loadedUpgrades = components.get(ModDataComponents.UPGRADES.get());
        if (loadedUpgrades != null) {
            for (int i = 0; i < 4; i++) {
                ItemResource res = i < loadedUpgrades.size() ? loadedUpgrades.get(i) : ItemResource.of(ItemStack.EMPTY);
                this.upgrades.set(i, res.toStack(1));
            }
        }
    }
}