package com.nuyuchi.translator.mixin.client;

import com.nuyuchi.translator.TranslatorModClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenClickMixin {

    @Inject(method = "clickCommandAction", at = @At("HEAD"), cancellable = true)
    private static void translator$interceptTranslateCommand(LocalPlayer player, String command, Screen screen, CallbackInfo ci) {
        if (TranslatorModClient.handleTranslateMarker(command)) {
            ci.cancel();
        }
    }

    @Inject(method = "defaultHandleClickEvent", at = @At("HEAD"), cancellable = true)
    private static void translator$interceptTranslateClick(ClickEvent clickEvent, Minecraft minecraft, Screen screen, CallbackInfo ci) {
        if (!(clickEvent instanceof ClickEvent.RunCommand runCommand)) {
            return;
        }

        if (TranslatorModClient.handleTranslateMarker(runCommand.command())) {
            ci.cancel();
        }
    }

    @Inject(method = "defaultHandleGameClickEvent", at = @At("HEAD"), cancellable = true)
    private static void translator$interceptTranslateGameClick(ClickEvent clickEvent, Minecraft minecraft, Screen screen, CallbackInfo ci) {
        if (!(clickEvent instanceof ClickEvent.RunCommand runCommand)) {
            return;
        }

        if (TranslatorModClient.handleTranslateMarker(runCommand.command())) {
            ci.cancel();
        }
    }
}
