package com.almostreliable.merequester;

import appeng.client.render.model.AutoRotatingBakedModel;
import appeng.hooks.ModelsReloadCallback;
import com.almostreliable.merequester.network.PacketHandler;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class MERequesterClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        PacketHandler.initS2C();
        ModelsReloadCallback.EVENT.register(MERequesterClient::onModelBake);
    }

    private static void onModelBake(Map<ResourceLocation, BakedModel> modelRegistry) {
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
