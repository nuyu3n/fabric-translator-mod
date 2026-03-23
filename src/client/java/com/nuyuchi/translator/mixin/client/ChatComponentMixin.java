package com.nuyuchi.translator.mixin.client;

import com.nuyuchi.translator.TranslatorModClient;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {

    @ModifyVariable(method = "addMessage(Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private Component translator$prependPrefixSimple(Component component) {
        return TranslatorModClient.withTranslatePrefix(component, null, null);
    }

    @ModifyVariable(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private Component translator$prependPrefixTagged(Component component, Component filterMask, MessageSignature signature, GuiMessageTag tag) {
        return TranslatorModClient.withTranslatePrefix(component, signature, tag);
    }
}
