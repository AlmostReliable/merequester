package com.almostreliable.merequester.terminal;

import appeng.api.config.Settings;
import appeng.api.config.TypeFilter;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.items.parts.PartModels;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.parts.PartModel;
import appeng.parts.reporting.AbstractDisplayPart;
import appeng.parts.reporting.PatternAccessTerminalPart;
import appeng.util.ConfigManager;
import com.almostreliable.merequester.MERequester;
import com.almostreliable.merequester.utils.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * logic taken from {@link PatternAccessTerminalPart}
 */
public class RequesterTerminalPart extends AbstractDisplayPart implements IConfigurableObject, INBTSerializable<CompoundTag> {

    @PartModels public static final ResourceLocation MODEL_OFF = Utils.getRL("part/" + MERequester.TERMINAL_ID + "_off");
    @PartModels public static final ResourceLocation MODEL_ON = Utils.getRL("part/" + MERequester.TERMINAL_ID + "_on");

    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);

    private final ConfigManager configManager = new ConfigManager(() -> getHost().markForSave());

    public RequesterTerminalPart(IPartItem<?> partItem) {
        super(partItem, true);
        configManager.registerSetting(Settings.TYPE_FILTER, TypeFilter.ALL);
    }

    @Override
    public boolean onPartActivate(Player player, InteractionHand hand, Vec3 pos) {
        if (!super.onPartActivate(player, hand, pos) && !isClientSide()) {
            MenuOpener.open(RequesterTerminalMenu.TYPE, player, MenuLocators.forPart(this));
        }
        return true;
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        writeToNBT(tag);
        configManager.writeToNBT(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        readFromNBT(tag);
        configManager.readFromNBT(tag);
    }

    @Override
    public IPartModel getStaticModels() {
        return selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
    }

    @Override
    public IConfigManager getConfigManager() {
        return configManager;
    }
}
