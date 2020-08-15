package com.kosmx.emotecraft.mixin;

import com.kosmx.emotecraft.Emote;
import com.kosmx.emotecraft.Main;
import com.kosmx.emotecraft.config.EmoteHolder;
import com.kosmx.emotecraft.network.EmotePacket;
import com.kosmx.emotecraft.network.StopPacket;
import com.kosmx.emotecraft.playerInterface.EmotePlayerInterface;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class EmotePlayerMixin extends PlayerEntity implements EmotePlayerInterface {

    @Nullable
    private Emote emote;

    private int lastUpdated;

    public EmotePlayerMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Override
    public void playEmote(Emote emote) {
        this.emote = emote;
    }

    @Override
    @Nullable
    public Emote getEmote(){
        return this.emote;
    }

    @Override
    public void resetLastUpdated() {
        this.lastUpdated = 0;
    }

    @Override
    public void tick() {
        super.tick();
        if(Emote.isRunningEmote(this.emote)){
            this.rotationYaw = (this.rotationYaw * 3 + this.cameraYaw)/4; //to set the body to the correct direction smooth.
            if(this != Minecraft.getInstance().getRenderViewEntity() && Minecraft.getInstance().getRenderViewEntity() instanceof ClientPlayerEntity || EmoteHolder.canRunEmote(this)) {
                this.emote.tick();
                this.lastUpdated++;
                if(this == Minecraft.getInstance().getRenderViewEntity() && Minecraft.getInstance().getRenderViewEntity() instanceof ClientPlayerEntity && lastUpdated >= 100){
                    if(emote.getStopTick() - emote.getCurrentTick() < 50 && !emote.isInfinite())return;
                    PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
                    EmotePacket emotePacket = new EmotePacket(emote, this);
                    emotePacket.isRepeat = true;
                    emotePacket.write(buf);
                    ClientSidePacketRegistry.INSTANCE.sendToServer(Main.EMOTE_PLAY_NETWORK_PACKET_ID, buf);
                    lastUpdated = 0;
                }
                else if((this != Minecraft.getInstance().getRenderViewEntity() || Minecraft.getInstance().getRenderViewEntity() instanceof RemoteClientPlayerEntity) && lastUpdated > 300){
                    this.emote.stop();
                }
            }
            else {
                emote.stop();
                PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
                StopPacket packet = new StopPacket(this);
                packet.write(buf);
                ClientSidePacketRegistry.INSTANCE.sendToServer(Main.EMOTE_STOP_NETWORK_PACKET_ID, buf);
            }
        }
    }
}
