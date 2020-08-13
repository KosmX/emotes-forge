package com.kosmx.emotecraft.config;

import com.google.gson.JsonParseException;
import com.kosmx.emotecraft.Client;
import com.kosmx.emotecraft.Emote;
import com.kosmx.emotecraft.Main;
import com.kosmx.emotecraft.network.EmotePacket;
import com.kosmx.emotecraft.playerInterface.EmotePlayerInterface;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class EmoteHolder {
    public final Emote emote;
    public final IFormattableTextComponent name;
    public final IFormattableTextComponent description;
    public final IFormattableTextComponent author;
    public final int hash;
    public static List<EmoteHolder> list = new ArrayList<>();
    public InputMappings.Input keyBinding = InputMappings.INPUT_INVALID;
    @Nullable
    public DynamicTexture nativeIcon = null;
    @Nullable
    private ResourceLocation icon = null;
    @Nullable
    public Object path = null;

    /**
     * @param emote       {@link com.kosmx.emotecraft.Emote}
     * @param name        Emote name
     * @param description Emote decription
     * @param author      Name of the Author
     */
    public EmoteHolder(Emote emote, IFormattableTextComponent name, IFormattableTextComponent description, IFormattableTextComponent author, int hash) {
        this.emote = emote;
        this.name = name;
        this.author = author;
        this.description = description;
        this.hash = hash;
    }

    public static void bindKeys(SerializableConfig config) {
        config.emotesWithKey = new ArrayList<>();
        config.emotesWithHash = new ArrayList<>();
        for (EmoteHolder emote : list) {
            if (!emote.keyBinding.equals(InputMappings.INPUT_INVALID)) {
                config.emotesWithKey.add(emote);
                config.emotesWithHash.add(new Tuple<>(emote.hash, emote.keyBinding.getTranslationKey()));
            }
        }
        config.fastMenuHash = new int[8];
        for (int i = 0; i != 8; i++) {
            if (Main.config.fastMenuEmotes[i] != null) {
                Main.config.fastMenuHash[i] = Main.config.fastMenuEmotes[i].hash;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static ActionResultType playEmote(InputMappings.Input key) {
        if (Minecraft.getInstance() != null && Minecraft.getInstance().getRenderViewEntity() != null && Minecraft.getInstance().getRenderViewEntity() instanceof ClientPlayerEntity) {
            for (EmoteHolder emote : Main.config.emotesWithKey) {
                if (emote.keyBinding.equals(key)) {
                    emote.playEmote((PlayerEntity) Minecraft.getInstance().getRenderViewEntity());
                    return ActionResultType.SUCCESS;
                }
            }
        }
        return ActionResultType.PASS;
    }

    public static void clearEmotes() {
        for (EmoteHolder emoteHolder : list) {
            if (emoteHolder.icon != null) {
                Minecraft.getInstance().getTextureManager().deleteTexture(emoteHolder.icon);
                assert emoteHolder.nativeIcon != null;
                emoteHolder.nativeIcon.close();
            }
        }
        list = new ArrayList<>();
    }

    public void bindIcon(Object path){
        if(path instanceof String || path instanceof File)this.path = path;
        else Main.log(Level.FATAL, "Can't use " + path.getClass() + " as file" );
    }

    public void assignIcon(File file) {
        if(file.isFile()) {
            try {
                assignIcon(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void assignIcon(String str){
        assignIcon(Client.class.getResourceAsStream(str));
    }

    public ResourceLocation getIcon(){
        if(icon == null && this.path != null){
            if(this.path instanceof String)assignIcon((String) this.path);
            else if(this.path instanceof File)assignIcon((File) this.path);
        }
        return icon;
    }

    public void assignIcon(InputStream inputStream) {
        try {
            Throwable throwable = null;

            try {
                NativeImage image = NativeImage.read(inputStream);
                DynamicTexture nativeImageBackedTexture = new DynamicTexture(image);
                this.icon = new ResourceLocation(Main.MOD_ID, "icon" + this.hash);
                Minecraft.getInstance().getTextureManager().loadTexture(this.icon, nativeImageBackedTexture);
                this.nativeIcon = nativeImageBackedTexture;
            } catch (IOException e) {
                throwable = e;
                throw e;
            } finally {
                try {
                    inputStream.close();
                } catch (Throwable throwable1) {
                    if (throwable != null) throwable.addSuppressed(throwable1);
                }
            }
        } catch (Throwable var) {
            Main.log(Level.ERROR, "Can't open emote icon: " + var);
            this.icon = null;
            this.nativeIcon = null;
        }
    }


    //public void setKeyBinding(InputUtil.Key key, )

    public Emote getEmote(){
        return emote;
    }

    public static EmoteHolder getEmoteFromHash(int hash){
        for(EmoteHolder emote:list){
            if (emote.hash == hash){
                return emote;
            }
        }
        return null;
    }

    public static EmoteHolder deserializeJson(BufferedReader json) throws JsonParseException {     //throws BowlingBall XD
        return Serializer.serializer.fromJson(json, EmoteHolder.class);
    }
    public static void addEmoteToList(BufferedReader json) throws JsonParseException{
        list.add(deserializeJson(json));
    }
    public static void addEmoteToList(EmoteHolder hold){
        list.add(hold);
    }

    public static boolean playEmote(Emote emote, PlayerEntity player){
        if(canPlayEmote(player)) {
            try {
                PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
                EmotePacket emotePacket = new EmotePacket(emote, player);
                emotePacket.write(buf);
                ClientSidePacketRegistry.INSTANCE.sendToServer(Main.EMOTE_PLAY_NETWORK_PACKET_ID, buf);//TODO
                EmotePlayerInterface target = (EmotePlayerInterface) player;
                target.playEmote(emote);
                emote.start();
            } catch (Exception e) {
                Main.log(Level.ERROR, "cannot play emote reason: " + e.getMessage());
                if (Main.config.showDebug) e.printStackTrace();
            }
            return true;
        }
        else {
            return false;
        }
    }

    private static boolean canPlayEmote(PlayerEntity entity){
        if(!canRunEmote(entity))return false;
        if(entity != Minecraft.getInstance().getRenderViewEntity())return false;
        EmotePlayerInterface target = (EmotePlayerInterface)entity;
        return !(Emote.isRunningEmote(target.getEmote()) && !target.getEmote().isInfStarted());
    }

    public static boolean canRunEmote(Entity entity){
        if(!(entity instanceof AbstractClientPlayerEntity))return false;
        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) entity;
        if(player.getPose() != Pose.STANDING)return false;
        //System.out.println(player.getPos().distanceTo(new Vec3d(player.prevX, player.prevY, player.prevZ)));
        return !(player.getPositionVec().distanceTo(new Vector3d(player.prevPosX, player.prevPosY, player.prevPosZ)) > 0.04f);
    }

    public boolean playEmote(PlayerEntity playerEntity){
        return playEmote(this.getEmote(), playerEntity);
    }
}

