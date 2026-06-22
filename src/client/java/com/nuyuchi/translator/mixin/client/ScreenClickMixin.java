package com.nuyuchi.translator.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.nuyuchi.translator.TranslatorModClient;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;

@Mixin(Screen.class)
public class ScreenClickMixin {
    @Inject(method = "clickCommandAction", at = @At("HEAD"), cancellable = true)
    private static void translator$interceptCommandClick(LocalPlayer player, String command, Screen screen, CallbackInfo ci) {
        if (TranslatorModClient.handleTranslateMarker(command)) {
            ci.cancel();
        }
    }
}