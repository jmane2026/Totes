package com.jmane2026.totes.item;

import com.jmane2026.totes.block.ToteBlock;
import com.jmane2026.totes.block.ToteControllerBlock;
import com.jmane2026.totes.block.entity.ToteControllerBlockEntity;
import com.jmane2026.totes.component.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;

public class ToteLinkerItem extends Item {
    public ToteLinkerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (player == null) return InteractionResult.PASS;

        if (state.getBlock() instanceof ToteControllerBlock) {
            if (!level.isClientSide()) {
                stack.set(ModDataComponents.LINKER_TARGET.get(), pos);
                player.sendSystemMessage(Component.literal("Linker Target Set: ")
                        .withStyle(ChatFormatting.AQUA)
                        .append(Component.literal(pos.toShortString()).withStyle(ChatFormatting.WHITE)));
            }
            return InteractionResult.SUCCESS;
        }

        if (state.getBlock() instanceof ToteBlock) {
            BlockPos controllerPos = stack.get(ModDataComponents.LINKER_TARGET.get());
            if (controllerPos == null) {
                if (!level.isClientSide()) player.sendSystemMessage(Component.literal("Set a Controller target first!")
                        .withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }

            if (level.getBlockEntity(controllerPos) instanceof ToteControllerBlockEntity controller) {
                if (!level.isClientSide()) {
                    boolean isRemoveMode = stack.getOrDefault(ModDataComponents.LINKER_MODE.get(), false);
                    if (isRemoveMode) {
                        if (controller.isLinked(pos)) {
                            controller.removeLink(pos);
                            player.sendSystemMessage(Component.literal("Tote Unlinked").withStyle(ChatFormatting.YELLOW));
                        }
                    } else {
                        if (!controller.isLinked(pos)) {
                            controller.addLink(pos);
                            player.sendSystemMessage(Component.literal("Tote Linked").withStyle(ChatFormatting.GREEN));
                        }
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            if (player.isShiftKeyDown()) {
                stack.remove(ModDataComponents.LINKER_TARGET.get());
                player.sendSystemMessage(Component.literal("Linker Target Cleared").withStyle(ChatFormatting.YELLOW));
            } else {
                boolean currentMode = stack.getOrDefault(ModDataComponents.LINKER_MODE.get(), false);
                stack.set(ModDataComponents.LINKER_MODE.get(), !currentMode);
                
                Component modeText = currentMode ? Component.literal("REMOVE").withStyle(ChatFormatting.RED) 
                                                 : Component.literal("ADD").withStyle(ChatFormatting.GREEN);
                
                player.sendSystemMessage(Component.literal("Linker Mode: ").withStyle(ChatFormatting.GRAY).append(modeText));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(ModDataComponents.LINKER_TARGET.get());
    }
}