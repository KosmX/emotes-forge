package com.kosmx.emotecraft;

import com.google.gson.JsonParseException;
import com.kosmx.emotecraft.config.SerializableConfig;
import com.kosmx.emotecraft.config.Serializer;
import com.kosmx.emotecraft.network.ForgeNetwork;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class Main {

    public static Logger LOGGER = LogManager.getLogger();

    //init and config variables

    public static final String MOD_ID = "emotecraft";
    public static final String MOD_NAME = "Emotecraft";
    public static final Path CONFIGPATH = FMLPaths.CONFIGDIR.get().resolve("emotecraft.json");

    public static SerializableConfig config;

    public static final ResourceLocation EMOTE_PLAY_NETWORK_PACKET_IDS2C = new ResourceLocation(MOD_ID, "playemotes2c");
    public static final ResourceLocation EMOTE_STOP_NETWORK_PACKET_IDS2C = new ResourceLocation(MOD_ID, "stopemotes2c");
    public static final ResourceLocation EMOTE_PLAY_NETWORK_PACKET_IDC2S = new ResourceLocation(MOD_ID, "playemotec2s");
    public static final ResourceLocation EMOTE_STOP_NETWORK_PACKET_IDC2S = new ResourceLocation(MOD_ID, "stopemotec2s");

    /**
     * This initializer runs on the server and on the client.
     * Load config, init networking
     * And Main has the static variables of the mod.
     */
    public static void onInitialize() {

        Serializer.initializeSerializer();/*
        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT){ //I can't do it in the client initializer because I need it to serialize the config
            Client.initEmotes();
        }
        */

        loadConfig();

        log(Level.INFO, "Initializing");

        //initServerNetwork(); //Network handler both dedicated server and client internal server
        ForgeNetwork.initNetwork();
    }

    public static void log(Level level, String message){
        log(level, message, false);
    }

    public static void log(Level level, String message, boolean force){
        if (force || (config != null && config.showDebug)) LOGGER.log(level, "["+MOD_NAME+"] " + message);
    }

    /*private static void initServerNetwork(){
        ServerSidePacketRegistry.INSTANCE.register(EMOTE_PLAY_NETWORK_PACKET_ID, ((packetContext, packetByteBuf) -> {EmotePacket packet = new EmotePacket();
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            if(!packet.read(packetByteBuf, config.validateEmote)) {
                //Todo kick player
                Main.log(Level.INFO, packetContext.getPlayer().getEntityName() + " is trying to play invalid emote", true);
                return;
            }
                packet.write(buf);
            Stream<PlayerEntity> players = PlayerStream.watching(packetContext.getPlayer());
            players.forEach(playerEntity -> {                                   //TODO check correct emote and kick if not
                if (playerEntity == packetContext.getPlayer()) return;
                ServerSidePacketRegistry.INSTANCE.sendToPlayer(playerEntity, EMOTE_PLAY_NETWORK_PACKET_ID, buf);
            });
        }));

        ServerSidePacketRegistry.INSTANCE.register(EMOTE_STOP_NETWORK_PACKET_ID, ((packetContex, packetByteBuf) -> {
            StopPacket packet = new StopPacket();
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            packet.read(packetByteBuf);
            packet.write(buf);

            Stream<PlayerEntity> players = PlayerStream.watching(packetContex.getPlayer());
            players.forEach(player -> {
                if(player == packetContex.getPlayer())return;
                ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, EMOTE_STOP_NETWORK_PACKET_ID, buf);
            });
        }));
    }

     */

    private static void loadConfig(){
        if(CONFIGPATH.toFile().isFile()){
            try {
                BufferedReader reader = Files.newBufferedReader(CONFIGPATH);
                config = Serializer.serializer.fromJson(reader, SerializableConfig.class);
                reader.close();
                //config = Serializer.serializer.fromJson(FileUtils.readFileToString(CONFIGPATH, "UTF-8"), SerializableConfig.class);
            }
            catch (Throwable e){
                config = new SerializableConfig();
                if(e instanceof IOException){
                    Main.log(Level.ERROR, "Can't access to config file: " + e.getLocalizedMessage(), true);
                }
                else if(e instanceof JsonParseException){
                    Main.log(Level.ERROR, "Config is invalid Json file: " + e.getLocalizedMessage(), true);
                }
                else {
                    e.printStackTrace();
                }
            }
        }
        else {
            config = new SerializableConfig();
        }

    }


}