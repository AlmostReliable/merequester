package com.almostreliable.merequester.requester;

import appeng.block.AEBaseEntityBlock;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.util.InteractionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class RequesterBlock extends AEBaseEntityBlock<RequesterBlockEntity> {

    private static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public RequesterBlock() {
        super(defaultProps(Material.METAL));
        registerDefaultState(defaultBlockState().setValue(ACTIVE, false));
    }

    @Override
    public InteractionResult onActivated(
        Level level, BlockPos pos, Player player, InteractionHand hand, @Nullable ItemStack stack, BlockHitResult hit
    ) {
        var entity = getBlockEntity(level, pos);
        if (entity == null || InteractionUtil.isInAlternateUseMode(player)) return InteractionResult.PASS;

        if (!level.isClientSide()) {
            MenuOpener.open(RequesterMenu.TYPE, player, MenuLocators.forBlockEntity(entity));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected BlockState updateBlockStateFromBlockEntity(BlockState currentState, RequesterBlockEntity be) {
        return currentState.setValue(ACTIVE, be.isActive());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE);
    }
}
