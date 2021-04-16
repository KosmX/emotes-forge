package io.github.kosmx.emotes.forge.mixin;

import com.mojang.authlib.GameProfile;
import io.github.kosmx.emotes.common.emote.EmoteData;
import io.github.kosmx.emotes.common.opennbs.format.Layer;
import io.github.kosmx.emotes.common.tools.Vec3d;
import io.github.kosmx.emotes.arch.emote.EmotePlayImpl;
import io.github.kosmx.emotes.main.emotePlay.EmotePlayer;
import io.github.kosmx.emotes.main.mixinFunctions.IPlayerEntity;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.UUID;

//Mixin it into the player is way easier than storing it somewhere else...
@Mixin(AbstractClientPlayer.class)
public abstract class EmotePlayerMixin extends Player implements IPlayerEntity<ModelPart> {
    int emotes_age = 0;

    @Shadow @Final public ClientLevel clientLevel;
    @Nullable EmotePlayer<ModelPart> emote;

    public EmotePlayerMixin(Level world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Override
    public void playEmote(EmoteData emote, int t) {
        this.emote = new EmotePlayImpl(emote, this::noteConsumer, t);
        this.initEmotePerspective(this.emote);
    }

    private void noteConsumer(Layer.Note note){
        this.clientLevel.playLocalSound(this.getX(), this.getY(), this.getZ(), getInstrumentFromCode(note.instrument).getSoundEvent(), SoundSource.PLAYERS, note.getVolume(), note.getPitch(), true);
    }

    private static NoteBlockInstrument getInstrumentFromCode(byte b){

        //That is more efficient than a switch case...
        NoteBlockInstrument[] instruments = {NoteBlockInstrument.HARP, NoteBlockInstrument.BASS, NoteBlockInstrument.BASEDRUM, NoteBlockInstrument.SNARE, NoteBlockInstrument.HAT,
                NoteBlockInstrument.GUITAR, NoteBlockInstrument.FLUTE, NoteBlockInstrument.BELL, NoteBlockInstrument.CHIME, NoteBlockInstrument.XYLOPHONE,NoteBlockInstrument.IRON_XYLOPHONE,
                NoteBlockInstrument.COW_BELL, NoteBlockInstrument.DIDGERIDOO, NoteBlockInstrument.BIT, NoteBlockInstrument.BANJO, NoteBlockInstrument.PLING};

        if(b >= 0 && b < instruments.length){
            return instruments[b];
        }
        return NoteBlockInstrument.HARP; //I don't want to crash here
    }

    @Override
    public int emotes_getAge() {
        return this.emotes_age;
    }

    @Override
    public int emotes_getAndIncreaseAge() {
        return this.emotes_age++;
    }

    @Override
    public void voidEmote() {
        this.emote = null;
    }

    @Nullable
    @Override
    public EmotePlayer<ModelPart> getEmote() {
        return this.emote;
    }

    @Override
    public UUID emotes_getUUID() {
        return this.getUUID();
    }

    @Override
    public boolean isNotStanding() {
        return this.getPose() != Pose.STANDING;
    }

    @Override
    public Vec3d emotesGetPos() {
        return new Vec3d(this.getX(), this.getY(), this.getZ());
    }

    @Override
    public Vec3d getPrevPos() {
        return new Vec3d(xo, yo, zo);
    }

    @Override
    public float getBodyYaw() {
        return this.yBodyRot;
    }

    @Override
    public float getViewYaw() {
        return this.yRot;
    }

    @Override
    public void setBodyYaw(float newYaw) {
        this.yBodyRot = newYaw;
    }

    @Override
    public void tick() {
        super.tick();
        this.emoteTick();
    }
}
