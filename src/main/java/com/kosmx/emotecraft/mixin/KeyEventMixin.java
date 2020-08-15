package com.kosmx.emotecraft.mixin;

import com.kosmx.emotecraft.config.EmoteHolder;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyBinding.class)
public class KeyEventMixin {
    @Inject(method = "onTick", at = @At(value = "HEAD"))
    private static void keyCallback(InputMappings.Input key, CallbackInfo ci){
        EmoteHolder.playEmote(key);     //Everything registered to KeyPressCallback event if Fabric should be called here.
    }
}
