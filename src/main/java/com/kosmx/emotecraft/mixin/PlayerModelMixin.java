package com.kosmx.emotecraft.mixin;


import com.kosmx.emotecraft.Emote;
import com.kosmx.emotecraft.playerInterface.EmotePlayerInterface;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerModel.class)
public class PlayerModelMixin<T extends LivingEntity> extends BipedModel<T> {


    public PlayerModelMixin(float scale) {
        super(scale);
    }

    private void setDefaultPivot(){
        this.bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
        this.bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);
        this.bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.bipedRightArm.rotationPointZ = 0.0F;
        this.bipedRightArm.rotationPointX = -5.0F;
        this.bipedLeftArm.rotationPointZ = 0.0F;
        this.bipedLeftArm.rotationPointX = 5.0F;
        this.bipedBody.rotateAngleX = 0.0F;
        this.bipedRightLeg.rotationPointZ = 0.1F;
        this.bipedLeftLeg.rotationPointZ = 0.1F;
        this.bipedRightLeg.rotationPointY = 12.0F;
        this.bipedLeftLeg.rotationPointY = 12.0F;
        this.bipedHead.rotationPointY = 0.0F;
        this.bipedHead.rotateAngleZ = 0f;
        this.bipedBody.rotationPointY = 0.0F;
    }

    @Redirect(method = "setRotationAngles", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/model/BipedModel;setRotationAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V"))
    private void setEmote(BipedModel<?> idk,T livingEntity, float f, float g, float h, float i, float j){
        setDefaultPivot();  //to not make everything wrong
        super.setRotationAngles(livingEntity, f, g, h, i, j);
        if(livingEntity instanceof AbstractClientPlayerEntity && Emote.isRunningEmote(((EmotePlayerInterface)livingEntity).getEmote())){
            Emote emote = ((EmotePlayerInterface) livingEntity).getEmote();
            emote.head.setBodyPart(this.bipedHead);
            this.bipedHeadwear.copyModelAngles(this.bipedHead);
            emote.leftArm.setBodyPart(this.bipedLeftArm);
            emote.rightArm.setBodyPart(this.bipedRightArm);
            emote.leftLeg.setBodyPart(this.bipedLeftLeg);
            emote.rightLeg.setBodyPart(this.bipedRightLeg);
        }
    }
}
