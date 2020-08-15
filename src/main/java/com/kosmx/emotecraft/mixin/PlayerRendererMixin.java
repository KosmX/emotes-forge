package com.kosmx.emotecraft.mixin;

import com.kosmx.emotecraft.Emote;
import com.kosmx.emotecraft.playerInterface.EmotePlayerInterface;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {


    @Inject(method = "applyRotations", at = @At("RETURN"))
    private void setRotation(AbstractClientPlayerEntity abstractClientPlayerEntity, MatrixStack matrixStack, float a, float bodyYaw, float tickDelta, CallbackInfo info){
        if( Emote.isRunningEmote(((EmotePlayerInterface)abstractClientPlayerEntity).getEmote()))
        {
            Emote emote = ((EmotePlayerInterface)abstractClientPlayerEntity).getEmote();
            emote.setTickDelta(tickDelta);

            Vector3d vec3d = emote.torso.getBodyOffshet();
            matrixStack.translate(vec3d.getX(), vec3d.getY() + 0.7, vec3d.getZ());
            Vector3f vec3f = emote.torso.getBodyRotation();
            matrixStack.rotate(Vector3f.ZP.rotation(vec3f.getZ()));    //roll
            matrixStack.rotate(Vector3f.YP.rotation(vec3f.getY()));    //pitch
            matrixStack.rotate(Vector3f.XP.rotation(vec3f.getX()));    //yaw
            matrixStack.translate(0, -0.7d, 0);
        }
    }
}
