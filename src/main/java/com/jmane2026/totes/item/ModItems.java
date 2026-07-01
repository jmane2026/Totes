package com.jmane2026.totes.item;

import com.jmane2026.totes.Totes;
import com.jmane2026.totes.block.ModBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Totes.MODID);

    public static final DeferredItem<BlockItem> TOTE = ITEMS.registerSimpleBlockItem("tote", ModBlocks.TOTE);

    public static final DeferredItem<BlockItem> TOTE_CONTROLLER = ITEMS.registerSimpleBlockItem("tote_controller", ModBlocks.TOTE_CONTROLLER);

    public static final DeferredItem<Item> TOTE_KEY = ITEMS.registerItem("tote_key", Item::new, p -> p.stacksTo(1));

    public static final DeferredItem<ToteLinkerItem> TOTE_LINKER = ITEMS.registerItem("tote_linker", ToteLinkerItem::new, p -> p.stacksTo(1));

    public static final DeferredItem<Item> PLASTIC = ITEMS.registerItem("plastic", Item::new, p -> p);

    public static final DeferredItem<Item> TOTE_UPGRADE_COPPER = ITEMS.registerItem("copper_upgrade", Item::new, p -> p.stacksTo(64));
    public static final DeferredItem<Item> TOTE_UPGRADE_IRON = ITEMS.registerItem("iron_upgrade", Item::new, p -> p.stacksTo(64));
    public static final DeferredItem<Item> TOTE_UPGRADE_GOLD = ITEMS.registerItem("gold_upgrade", Item::new, p -> p.stacksTo(64));
    public static final DeferredItem<Item> TOTE_UPGRADE_DIAMOND = ITEMS.registerItem("diamond_upgrade", Item::new, p -> p.stacksTo(64));
    public static final DeferredItem<Item> TOTE_UPGRADE_NETHERITE = ITEMS.registerItem("netherite_upgrade", Item::new, p -> p.stacksTo(64));
    public static final DeferredItem<Item> TOTE_UPGRADE_VOID = ITEMS.registerItem("void_upgrade", Item::new, p -> p.stacksTo(64));


    public static boolean isUpgrade(ItemStack stack) {
        Item item = stack.getItem();
        return item == TOTE_UPGRADE_COPPER.get() ||
                item == TOTE_UPGRADE_IRON.get() ||
                item == TOTE_UPGRADE_GOLD.get() ||
                item == TOTE_UPGRADE_DIAMOND.get() ||
                item == TOTE_UPGRADE_NETHERITE.get() ||
                item == TOTE_UPGRADE_VOID.get();
    }
}