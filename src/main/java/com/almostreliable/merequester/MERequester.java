package com.almostreliable.merequester;

import appeng.client.render.model.AutoRotatingBakedModel;
import appeng.core.definitions.BlockDefinition;
import appeng.core.definitions.ItemDefinition;
import appeng.items.parts.PartItem;
import com.almostreliable.merequester.network.PacketHandler;
import com.almostreliable.merequester.requester.RequesterBlock;
import com.almostreliable.merequester.terminal.RequesterTerminalPart;
import com.mojang.logging.LogUtils;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

@SuppressWarnings("UtilityClassWithPublicConstructor")
@Mod(BuildConfig.MOD_ID)
public final class MERequester {

    /*
        TODO:
        - add serialization for requesters
        - fix requester not detecting changes to the network
        - add drops
        - add recipes
        - add screen to the requester block
        - test if the REI drag and drop works when the AE2 commit is released and the REI plugin is loaded
        https://github.com/AppliedEnergistics/Applied-Energistics-2/commit/6c825cc00e228b54d5bc318b24de1a841a2b59b0
     */

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final String TERMINAL_ID = "requester_terminal";
    public static final String REQUESTER_ID = "requester";

    public static final ItemDefinition<PartItem<RequesterTerminalPart>> TERMINAL = Registration.setupTerminal();
    public static final BlockDefinition<RequesterBlock> REQUESTER = Registration.setupRequester();

    public MERequester() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(MERequester::onCommonSetup);
        modEventBus.addListener(MERequester::onModelBake);
    }

    @OnlyIn(Dist.CLIENT)
    private static void onModelBake(ModelEvent.BakingCompleted event) {
        Map<ResourceLocation, BakedModel> modelRegistry = event.getModels();
        List<ModelResourceLocation> locations = List.of(
            new ModelResourceLocation(BuildConfig.MOD_ID, REQUESTER_ID, "active=true"),
            new ModelResourceLocation(BuildConfig.MOD_ID, REQUESTER_ID, "active=false")
        );
        locations.forEach(l -> {
            var model = modelRegistry.get(l);
            if (model == null || model.equals(modelRegistry.get(ModelBakery.MISSING_MODEL_LOCATION))) return;
            BakedModel newModel = new AutoRotatingBakedModel(model);
            modelRegistry.put(l, newModel);
        });
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        PacketHandler.init();
    }
}
