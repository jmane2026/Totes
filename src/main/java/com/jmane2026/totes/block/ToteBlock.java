package com.jmane2026.totes.block;

import com.jmane2026.totes.block.entity.ToteBlockEntity;
import com.jmane2026.totes.component.ModDataComponents;
import net.neoforged.neoforge.transfer.item.ItemResource;
import com.jmane2026.totes.item.ModItems;
import net.minecraft.ChatFormatting;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ToteBlock extends BaseEntityBlock {
    public static final MapCodec<ToteBlock> CODEC = simpleCodec(ToteBlock::new);

    public ToteBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ToteBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HorizontalDirectionalBlock.FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof ToteBlockEntity tote) {
            if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

            if (stack.is(ModItems.TOTE_LINKER.get())) return InteractionResult.PASS;

            Direction clickedFace = hitResult.getDirection();
            Direction blockFacing = state.getValue(HorizontalDirectionalBlock.FACING);
            boolean isFront = (clickedFace == blockFacing);

            if (player.isShiftKeyDown() && stack.isEmpty()) {
                if (!level.isClientSide()) {
                    player.openMenu(tote, pos);
                }
                return InteractionResult.SUCCESS;
            }

            if (ModItems.isUpgrade(stack)) {
                if (!isFront || tote.getCount() > 0) {
                    if (!level.isClientSide()) {
                        if (tote.addUpgrade(stack)) stack.shrink(1);
                    }
                    return InteractionResult.SUCCESS;
                }
            }

            if (isFront) {
                if (stack.is(ModItems.TOTE_KEY.get())) {
                    if (!level.isClientSide()) {
                        tote.setLocked(!tote.isLocked());
                    }
                    return InteractionResult.SUCCESS;
                }

                if (!level.isClientSide()) {
                    tote.depositItems(player, hand, 1);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof ToteBlockEntity tote) {
            HitResult hitResult = player.pick(player.blockInteractionRange(), 0, false);

            if (hitResult instanceof BlockHitResult blockHit && blockHit.getBlockPos().equals(pos)) {
                if (blockHit.getDirection() == state.getValue(HorizontalDirectionalBlock.FACING)) {
                    if (!level.isClientSide()) {
                        int amount = player.isShiftKeyDown() ? 64 : 1;
                        tote.extractItems(player, amount);
                    }
                }
            }
        }
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        BlockEntity be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof ToteBlockEntity tote) {
            ItemStack stack = new ItemStack(this);
            applyToteDataToStack(tote, stack);
            return List.of(stack);
        }
        return super.getDrops(state, params);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player) {
        ItemStack stack = super.getCloneItemStack(level, pos, state, includeData, player);
        if (level.getBlockEntity(pos) instanceof ToteBlockEntity tote) {
            applyToteDataToStack(tote, stack);
        }
        return stack;
    }

    private void applyToteDataToStack(ToteBlockEntity tote, ItemStack stack) {
        if (!tote.getStoredItem().isEmpty()) {
            stack.set(ModDataComponents.STORED_ITEM.get(), ItemResource.of(tote.getStoredItem()));
        }
        
        if (tote.getCount() > 0) {
            stack.set(ModDataComponents.COUNT.get(), tote.getCount());
        }
        
        if (tote.isLocked()) {
            stack.set(ModDataComponents.IS_LOCKED.get(), true);
        }

        List<ItemResource> activeUpgrades = tote.getUpgrades().stream().filter(s -> !s.isEmpty()).map(ItemResource::of).toList();
        if (!activeUpgrades.isEmpty()) {
            stack.set(ModDataComponents.UPGRADES.get(), activeUpgrades);
        }

        if (!tote.getStoredItem().isEmpty()) {
            String name = "Tote (" + tote.getStoredItem().getHoverName().getString() + " x" + tote.getCount() + ")";
            stack.set(DataComponents.CUSTOM_NAME, Component.literal(name).withStyle(style -> style.withItalic(false)));
        }
    }
}