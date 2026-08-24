package com.nnpg.glazed.mixins;

import com.nnpg.glazed.utils.GlazedSell;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class BackgroundContainerScreenMixin {
    @Inject(method = "removed", at = @At("HEAD"), cancellable = true)
    private void glazed$keepContainerOpen(CallbackInfo info) {
        if (GlazedSell.isBackgroundMode()) info.cancel();
    }
}