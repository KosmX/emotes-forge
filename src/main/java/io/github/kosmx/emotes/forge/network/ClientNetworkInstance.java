package io.github.kosmx.emotes.forge.network;

import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class ClientNetworkInstance extends AbstractNetworkInstance {

    boolean isRemotePresent = false;

    public static ClientNetworkInstance networkInstance = new ClientNetworkInstance();

    public void init(){
        //ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ClientPlayNetworking.registerReceiver(ServerNetwork.channelID, this::receiveMessage));
        ServerNetwork.channel.addListener(this::receiveJunk);
        ServerNetwork.channel.addListener(this::registerServerSide);
        MinecraftForge.EVENT_BUS.addListener(this::connectServerCallback);
    }

    private void connectServerCallback(ClientPlayerNetworkEvent.LoggedInEvent event){
        this.isRemotePresent = false;
    }

    private void receiveJunk(NetworkEvent.ServerCustomPayloadEvent event){
        receiveMessage(event.getPayload());
        event.getSource().get().setPacketHandled(true);
    }

    private void registerServerSide(NetworkEvent.ChannelRegistrationChangeEvent event){
        this.isRemotePresent = event.getRegistrationChangeType() == NetworkEvent.RegistrationChangeType.REGISTER;
    }

    void receiveMessage(FriendlyByteBuf buf){
        if(buf.isDirect()){ //If the received ByteBuf is direct i have to copy that onto the heap
            byte[] bytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), bytes);
            receiveMessage(bytes);
        }
        else {
            receiveMessage(buf.array()); //if heap, I can just use it's byte-array
        }
    }

    private boolean disableNBS = false;
    @Override
    public HashMap<Byte, Byte> getVersions() {
        if(disableNBS){
            HashMap<Byte, Byte> map = new HashMap<>();
            map.put((byte)3, (byte) 0);
            return map;
        }
        return null;
    }

    @Override
    public void setVersions(HashMap<Byte, Byte> map) {
        if(map.containsKey((byte)3)){
            disableNBS = map.get((byte)3) == 0;
        }
    }

    @Override
    public boolean sendPlayerID() {
        return false;
    }

    @Override
    public boolean isActive() {
        return Minecraft.getInstance().getConnection() != null
                && ServerNetwork.channel.isRemotePresent(Minecraft.getInstance().getConnection().getConnection())
                || this.isRemotePresent;
    }

    public static ServerboundCustomPayloadPacket newC2SEmotePacket(NetData data) throws IOException {
        ServerboundCustomPayloadPacket packet = new ServerboundCustomPayloadPacket();
        packet.setName(ServerNetwork.channelID);
        packet.setData(new FriendlyByteBuf(Unpooled.wrappedBuffer(new EmotePacket.Builder(data).build().write().array())));
        return packet;
    }

    @Override
    public void sendMessage(EmotePacket.Builder builder, @Nullable UUID target) throws IOException {
        if(target != null){
            builder.configureTarget(target);
        }
        Minecraft.getInstance().getConnection().send(newC2SEmotePacket(builder.copyAndGetData()));
    }
}
