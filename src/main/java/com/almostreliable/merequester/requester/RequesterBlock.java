package com.almostreliable.merequester.requester;

import appeng.block.AEBaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;

public class RequesterBlock extends AEBaseEntityBlock<RequesterBlockEntity> {

    private static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public RequesterBlock() {
        super(defaultProps(Material.METAL));
        registerDefaultState(defaultBlockState().setValue(ACTIVE, false));
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
