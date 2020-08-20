package com.kosmx.emotecraft.network;

import com.kosmx.emotecraft.Emote;
import com.kosmx.emotecraft.Main;
import com.kosmx.emotecraft.playerInterface.EmotePlayerInterface;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.Level;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ForgeNetwork {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel emotePacket = NetworkRegistry.newSimpleChannel(
            Main.EMOTE_PLAY_NETWORK_PACKET_ID,
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    public static final SimpleChannel stopPacket = NetworkRegistry.newSimpleChannel(
            Main.EMOTE_STOP_NETWORK_PACKET_ID,
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void initNetwork(){
        int id = 0;
        emotePacket.registerMessage(id++, EmotePacket.class, EmotePacket::write, EmotePacket::new, (emotePacket, contextSupplier) -> {
            if(!emotePacket.correct){
                Main.log(Level.INFO, contextSupplier.get().getSender() + " is trying to play invalid emote", true);
                return;
            }
            contextSupplier.get().enqueueWork(()-> {
                ForgeNetwork.emotePacket.send(PacketDistributor.NEAR.noArg(), emotePacket);
            });
        }, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        emotePacket.registerMessage(id++, StopPacket.class, StopPacket::write, StopPacket::new, (emotePacket, contexSupplier) -> {
            contexSupplier.get().enqueueWork(()->{
                ForgeNetwork.emotePacket.send(PacketDistributor.NEAR.noArg(), emotePacket);
            });
        });
    }

    public static void initClientNet(){
        int id = 8;
        emotePacket.registerMessage(id++, EmotePacket.class, EmotePacket::write, EmotePacket::new, (emotePacket, ctx) -> {
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
        });
        emotePacket.registerMessage(id++, StopPacket.class, StopPacket::write, StopPacket::new, (stopPacket, ctx) -> {
            ctx.get().enqueueWork(()->{
                EmotePlayerInterface player = (EmotePlayerInterface) Minecraft.getInstance().world.getPlayerByUuid(stopPacket.getPlayer());
                if(player != null && Emote.isRunningEmote(player.getEmote()))player.getEmote().stop();
            });
        });
    }

}
