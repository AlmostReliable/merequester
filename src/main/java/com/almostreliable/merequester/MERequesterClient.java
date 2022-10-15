package com.almostreliable.merequester;

import appeng.client.render.model.AutoRotatingBakedModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.List;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class MERequesterClient {

    public void onInitialize() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(MERequesterClient::onModelBake);
    }

    @OnlyIn(Dist.CLIENT)
    private static void onModelBake(ModelEvent.BakingCompleted event) {
        Map<ResourceLocation, BakedModel> modelRegistry = event.getModels();
        List<ModelResourceLocation> locations = List.of(
            new ModelResourceLocation(BuildConfig.MOD_ID, MERequester.REQUESTER_ID, "active=true"),
            new ModelResourceLocation(BuildConfig.MOD_ID, MERequester.REQUESTER_ID, "active=false")
        );
        locations.forEach(l -> {
            var model = modelRegistry.get(l);
            if (model == null || model.equals(modelRegistry.get(ModelBakery.MISSING_MODEL_LOCATION))) return;
            BakedModel newModel = new AutoRotatingBakedModel(model);
            modelRegistry.put(l, newModel);
        });
    }
}
