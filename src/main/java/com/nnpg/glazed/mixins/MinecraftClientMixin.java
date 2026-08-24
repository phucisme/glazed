package com.nnpg.glazed.mixins;

import com.nnpg.glazed.utils.GlazedSell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ChestMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void glazed$keepCurrentScreen(Screen screen, CallbackInfo info) {
        if (GlazedSell.isBackgroundMode()
            && mcScreenIsOpen()
            && screen instanceof AbstractContainerScreen<?> containerScreen
            && containerScreen.getMenu() instanceof ChestMenu) {
            info.cancel();
        }
    }

    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen glazed$hideSellScreen(Screen screen) {
        if (GlazedSell.isBackgroundMode()
            && screen instanceof AbstractContainerScreen<?> containerScreen
            && containerScreen.getMenu() instanceof ChestMenu) {
            return null;
        }

        return screen;
    }

    private boolean mcScreenIsOpen() {
        return ((Minecraft) (Object) this).screen != null;
    }
}