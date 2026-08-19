package com.nnpg.glazed.mixins;

import com.nnpg.glazed.modules.main.AutoLogin;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.gui.screens.dialog.DialogScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DialogScreen.class)
public class AuthMeDialogMixin {
    @Inject(method = "init", at = @At("TAIL"))
    private void glazed$autoLoginAfterInit(CallbackInfo ci) {
        AutoLogin autoLogin = Modules.get().get(AutoLogin.class);
        if (autoLogin != null && autoLogin.isActive()) {
            autoLogin.handleScreen((DialogScreen<?>) (Object) this);
        }
    }
}