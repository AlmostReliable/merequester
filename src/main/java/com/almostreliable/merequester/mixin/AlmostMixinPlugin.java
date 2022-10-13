package com.almostreliable.merequester.mixin;

import com.google.common.collect.ImmutableMap;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

@SuppressWarnings("ALL")
public class AlmostMixinPlugin implements IMixinConfigPlugin {

    private static final BooleanSupplier TRUE = () -> true;
    private static final Map<String, BooleanSupplier> CONDITIONS = ImmutableMap.of(
        "com.almostreliable.merequester.mixin.compat.JEIItemSlotTargetMixin", modLoaded("jei"),
        "com.almostreliable.merequester.mixin.compat.REIItemSlotTargetMixin", modLoaded("roughlyenoughitems")
    );

    private static BooleanSupplier modLoaded(String id) {
        return () -> {
            if (ModList.get() == null) {
                return LoadingModList.get().getMods().stream().map(ModInfo::getModId).anyMatch(id::equals);
            }
            return ModList.get().isLoaded(id);
        };
    }

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return CONDITIONS.getOrDefault(mixinClassName, TRUE).getAsBoolean();
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
