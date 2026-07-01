package com.jmane2026.totes.menu;

import com.jmane2026.totes.block.entity.ToteBlockEntity;
import com.jmane2026.totes.item.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ToteMenu extends AbstractContainerMenu {
    private final ToteBlockEntity blockEntity;
    private final Container upgradeContainer;

    public ToteMenu(int containerId, Inventory inv) {
        this(containerId, inv, (ToteBlockEntity) null);
    }

    public ToteMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (ToteBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public ToteMenu(int containerId, Inventory playerInventory, ToteBlockEntity entity) {
        super(ModMenus.TOTE_MENU.get(), containerId);
        this.blockEntity = entity;
        
        this.upgradeContainer = new Container() {
            @Override public int getContainerSize() { return 4; }
            @Override public boolean isEmpty() { 
                for(ItemStack s : blockEntity.getUpgrades()) if(!s.isEmpty()) return false;
                return true;
            }
            @Override public ItemStack getItem(int slot) { return blockEntity.getUpgrades().get(slot); }
            @Override public ItemStack removeItem(int slot, int amount) {
                ItemStack stack = blockEntity.getUpgrades().get(slot).split(amount);
                if (stack.getCount() > 0) setChanged();
                return stack;
            }
            @Override public ItemStack removeItemNoUpdate(int slot) {
                ItemStack stack = blockEntity.getUpgrades().set(slot, ItemStack.EMPTY);
                return stack;
            }
            @Override public void setItem(int slot, ItemStack stack) {
                blockEntity.getUpgrades().set(slot, stack);
                if (!stack.isEmpty() && stack.getCount() > 1) {
                    stack.setCount(1);
                }
                setChanged();
            }
            @Override public void setChanged() { blockEntity.setChanged(); }
            @Override public boolean stillValid(Player player) { return true; }
            @Override public void clearContent() { blockEntity.getUpgrades().clear(); setChanged(); }
            @Override public int getMaxStackSize() { return 1; }
        };

        for (int i = 0; i < 4; i++) {
            final int slotIdx = i;
            this.addSlot(new Slot(upgradeContainer, i, 52 + (i * 18), 88) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    if (!ModItems.isUpgrade(stack)) return false;
                    
                    if (stack.is(ModItems.TOTE_UPGRADE_VOID.get())) {
                        for (int j = 0; j < 4; j++) {
                            if (j != slotIdx && blockEntity.getUpgrades().get(j).is(ModItems.TOTE_UPGRADE_VOID.get())) return false;
                        }
                    }
                    return true;
                }

                @Override
                public boolean mayPickup(Player player) {
                    long potentialMax = blockEntity.getMaxCapacityWithout(slotIdx);
                    return blockEntity.getCount() <= potentialMax;
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }

                @Override
                public int getMaxStackSize(ItemStack stack) {
                    return 1;
                }
            });
        }

        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, i * 20, 211));
        }
    }

    public ToteBlockEntity getBlockEntity() { return blockEntity; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < 4) {
                long potentialMax = blockEntity.getMaxCapacityWithout(index);
                if (blockEntity.getCount() > potentialMax) {
                    return ItemStack.EMPTY;
                }
            }

            if (index < 4) {
                if (!this.moveItemStackTo(itemstack1, 4, 13, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(itemstack1, 0, 4, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) { return true; }
}