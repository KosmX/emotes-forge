package com.kosmx.emotecraft.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;

import java.util.UUID;

public class StopPacket {
    protected UUID player;

    public StopPacket(){}

    public StopPacket(PacketBuffer buffer){
        this.read(buffer);
    }

    public StopPacket(PlayerEntity playerEntity){
        this.player = playerEntity.getGameProfile().getId();
    }
    public void read(PacketBuffer buf){
        player = buf.readUniqueId();
    }
    public UUID getPlayer(){
        return this.player;
    }
    public void write(PacketBuffer buf){
        buf.writeUniqueId(player);
    }
}
