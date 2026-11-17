package com.almostreliable.merequester.mixin;

import com.almostreliable.merequester.requester.DeferredCraftingProviderUpdates;
import com.almostreliable.merequester.requester.RequesterPlanActivity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.me.service.CraftingService;
import appeng.me.service.helpers.NetworkCraftingProviders;

/**
 * Requester plans read the pattern maps off the server thread. Provider updates and the end-of-tick
 * craftable-key copy wait until every requester plan is paused, then run on the server thread.
 */
@Mixin(value = CraftingService.class, remap = false)
public class CraftingServiceMixin {

    @Shadow
    private NetworkCraftingProviders craftingProviders;

    /**
     * Set when {@code addNode} must keep its remove-and-add pair together.
     * Splitting them across an awake check can unmount a provider that was just mounted.
     */
    @Unique
    private IGridNode merequester$deferredAddNode;

    @Inject(method = "onServerEndTick", at = @At("HEAD"), remap = false)
    private void merequester$flushProviderUpdates(CallbackInfo ci) {
        DeferredCraftingProviderUpdates.flush();
    }

    @Inject(
        method = "onServerEndTick",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/me/service/helpers/NetworkCraftingProviders;getLastModifiedOnTick()J"
        ),
        cancellable = true,
        remap = false
    )
    private void merequester$deferCraftableCopy(CallbackInfo ci) {
        if (RequesterPlanActivity.isAwake()) {
            ci.cancel();
        }
    }

    @Inject(method = "refreshNodeCraftingProvider", at = @At("HEAD"), cancellable = true, remap = false)
    private void merequester$deferNodeRefresh(IGridNode node, CallbackInfo ci) {
        if (!RequesterPlanActivity.isAwake()) return;

        var providers = this.craftingProviders;
        DeferredCraftingProviderUpdates.defer((CraftingService) (Object) this, () -> {
            providers.removeProvider(node);
            providers.addProvider(node);
        });
        ci.cancel();
    }

    @Inject(method = "refreshGlobalCraftingProvider", at = @At("HEAD"), cancellable = true, remap = false)
    private void merequester$deferGlobalRefresh(ICraftingProvider provider, CallbackInfo ci) {
        if (!RequesterPlanActivity.isAwake()) return;

        var providers = this.craftingProviders;
        DeferredCraftingProviderUpdates.defer((CraftingService) (Object) this, () -> {
            providers.removeProvider(provider);
            providers.addProvider(provider);
        });
        ci.cancel();
    }

    @Inject(method = "addGlobalCraftingProvider", at = @At("HEAD"), cancellable = true, remap = false)
    private void merequester$deferGlobalAdd(ICraftingProvider provider, CallbackInfo ci) {
        if (!RequesterPlanActivity.isAwake()) return;

        var providers = this.craftingProviders;
        DeferredCraftingProviderUpdates.defer((CraftingService) (Object) this, () -> providers.addProvider(provider));
        ci.cancel();
    }

    @Inject(method = "removeGlobalCraftingProvider", at = @At("HEAD"), cancellable = true, remap = false)
    private void merequester$deferGlobalRemove(ICraftingProvider provider, CallbackInfo ci) {
        if (!RequesterPlanActivity.isAwake()) return;

        var providers = this.craftingProviders;
        DeferredCraftingProviderUpdates.defer((CraftingService) (Object) this, () -> providers.removeProvider(provider));
        ci.cancel();
    }

    @Redirect(
        method = "addNode",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/me/service/helpers/NetworkCraftingProviders;removeProvider(Lappeng/api/networking/IGridNode;)V"
        ),
        remap = false
    )
    private void merequester$addNodeRemove(NetworkCraftingProviders providers, IGridNode node) {
        if (!RequesterPlanActivity.isAwake()) {
            providers.removeProvider(node);
            return;
        }
        this.merequester$deferredAddNode = node;
    }

    @Redirect(
        method = "addNode",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/me/service/helpers/NetworkCraftingProviders;addProvider(Lappeng/api/networking/IGridNode;)V"
        ),
        remap = false
    )
    private void merequester$addNodeAdd(NetworkCraftingProviders providers, IGridNode node) {
        if (this.merequester$deferredAddNode != node) {
            providers.addProvider(node);
            return;
        }

        this.merequester$deferredAddNode = null;
        DeferredCraftingProviderUpdates.defer((CraftingService) (Object) this, () -> {
            providers.removeProvider(node);
            providers.addProvider(node);
        });
    }

    @Redirect(
        method = "removeNode",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/me/service/helpers/NetworkCraftingProviders;removeProvider(Lappeng/api/networking/IGridNode;)V"
        ),
        remap = false
    )
    private void merequester$removeNodeProvider(NetworkCraftingProviders providers, IGridNode node) {
        if (!RequesterPlanActivity.isAwake()) {
            providers.removeProvider(node);
            return;
        }
        DeferredCraftingProviderUpdates.defer((CraftingService) (Object) this, () -> providers.removeProvider(node));
    }
}
