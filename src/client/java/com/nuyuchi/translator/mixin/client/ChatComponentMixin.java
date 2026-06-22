package com.nuyuchi.translator.mixin.client;

import com.nuyuchi.translator.TranslatorModClient;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {

    // 26.2で分裂した3つのメソッドすべてを対象に指定します
    @ModifyVariable(
        method = {"addClientSystemMessage", "addServerSystemMessage", "addPlayerMessage"}, 
        at = @At("HEAD"), 
        argsOnly = true, 
        ordinal = 0
    )
    private Component translator$prependPrefixSimple(Component component) {
        return TranslatorModClient.withTranslatePrefix(component, null);
    }
}