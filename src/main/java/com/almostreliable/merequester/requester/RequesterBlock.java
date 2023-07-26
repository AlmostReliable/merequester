package com.almostreliable.merequester.requester;

import appeng.api.orientation.IOrientationStrategy;
import appeng.block.AEBaseEntityBlock;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.util.InteractionUtil;
import com.almostreliable.merequester.MERequester;
import com.almostreliable.merequester.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.List;

import static com.almostreliable.merequester.Utils.f;

public class RequesterBlock extends AEBaseEntityBlock<RequesterBlockEntity> {

    private static final IOrientationStrategy ORIENTATION_STRATEGY = new FacingWithVerticalSpin();
    private static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public RequesterBlock() {
        super(metalProps());
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

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return ORIENTATION_STRATEGY;
    }

    @Override
    public void appendHoverText(
        ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag
    ) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.literal(" "));
            tooltip.add(Utils.translate("tooltip", f("{}_desc", MERequester.REQUESTER_ID))
                .withStyle(ChatFormatting.AQUA));
        } else {
            Utils.addShiftInfoTooltip(tooltip);
        }
    }
}
