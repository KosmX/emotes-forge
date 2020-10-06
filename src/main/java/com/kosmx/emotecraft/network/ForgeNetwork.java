package com.kosmx.emotecraft.network;

import com.kosmx.emotecraft.Emote;
import com.kosmx.emotecraft.Main;
import com.kosmx.emotecraft.playerInterface.EmotePlayerInterface;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.Level;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ForgeNetwork {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel emotePacketc2s = NetworkRegistry.newSimpleChannel(
            Main.EMOTE_PLAY_NETWORK_PACKET_IDC2S,
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    public static final SimpleChannel stopPacketc2s = NetworkRegistry.newSimpleChannel(
            Main.EMOTE_STOP_NETWORK_PACKET_IDC2S,
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    public static final SimpleChannel emotePackets2c = NetworkRegistry.newSimpleChannel(
            Main.EMOTE_PLAY_NETWORK_PACKET_IDS2C,
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    public static final SimpleChannel stopPackets2c = NetworkRegistry.newSimpleChannel(
            Main.EMOTE_STOP_NETWORK_PACKET_IDS2C,
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void initNetwork(){
        int id = 0;
        emotePacketc2s.registerMessage(id++, EmotePacket.class, EmotePacket::write, EmotePacket::new, (emotePacket, contextSupplier) -> {
            if(!emotePacket.correct){
                Main.log(Level.INFO, contextSupplier.get().getSender() + " is trying to play invalid emote", true);
                return;
            }
            //contextSupplier.get().enqueueWork(()-> {
                ForgeNetwork.emotePackets2c.send(PacketDistributor.NEAR.with(getTargetPointSupplier(contextSupplier.get().getSender())), emotePacket);
            //});
        }, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        stopPacketc2s.registerMessage(id++, StopPacket.class, StopPacket::write, StopPacket::new, (emotePacket, contexSupplier) -> {
            //contexSupplier.get().enqueueWork(()->{
                ForgeNetwork.stopPackets2c.send(PacketDistributor.NEAR.with(getTargetPointSupplier(contexSupplier.get().getSender())), emotePacket);
            //});
        });
        if(FMLEnvironment.dist == Dist.DEDICATED_SERVER){
            //To register the ids on the server-side if no client-side
            emotePackets2c.registerMessage(8, EmotePacket.class, EmotePacket::write, EmotePacket::new, null);
            stopPackets2c.registerMessage(9, StopPacket.class, StopPacket::read, StopPacket::new, null);
        }
    }

    private static Supplier<PacketDistributor.TargetPoint> getTargetPointSupplier(ServerPlayerEntity player){
        return () -> {
            Vector3d pos = player.getPositionVec();
            return new PacketDistributor.TargetPoint(player, pos.x, pos.y, pos.z, 2048, player.getEntityWorld().func_234923_W_());
        };
    }

    public static void initClientNet(){
        int id = 8;
        emotePackets2c.registerMessage(id++, EmotePacket.class, EmotePacket::write, EmotePacket::new, (emotePacket, ctx) -> {
            if(!emotePacket.correct)return;
            Emote emote = emotePacket.getEmote();
            ctx.get().enqueueWork(()->{
                PlayerEntity playerEntity = Minecraft.getInstance().world.getPlayerByUuid(emotePacket.getPlayer());
                if(playerEntity != null){
                    if(!emotePacket.isRepeat || !Emote.isRunningEmote(((EmotePlayerInterface) playerEntity).getEmote())) {
                        ((EmotePlayerInterface) playerEntity).playEmote(emote);
                        ((EmotePlayerInterface) playerEntity).getEmote().start();
                    }
                    else {
                        ((EmotePlayerInterface)playerEntity).resetLastUpdated();
                    }
                }
            });
        }, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        stopPackets2c.registerMessage(id++, StopPacket.class, StopPacket::write, StopPacket::new, (stopPacket, ctx) -> {
            ctx.get().enqueueWork(()->{
                EmotePlayerInterface player = (EmotePlayerInterface) Minecraft.getInstance().world.getPlayerByUuid(stopPacket.getPlayer());
                if(player != null && Emote.isRunningEmote(player.getEmote()))player.getEmote().stop();
            });
        }, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

}
