package com.kosmx.emotecraft.network;

import com.kosmx.emotecraft.Main;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

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
        emotePacket.registerMessage(id++, EmotePacket.class, EmotePacket::write, EmotePacket::new, new BiConsumer<EmotePacket, Supplier<NetworkEvent.Context>>() {
            @Override
            public void accept(EmotePacket emotePacket, Supplier<NetworkEvent.Context> contextSupplier) {
                //TODO
            }
        }, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

}
