package com.kosmx.emotecraft;

import com.kosmx.emotecraft.config.EmoteHolder;
import com.kosmx.emotecraft.config.Serializer;
import com.kosmx.emotecraft.network.EmotePacket;
import com.kosmx.emotecraft.network.ForgeNetwork;
import com.kosmx.emotecraft.network.StopPacket;
import com.kosmx.emotecraft.playerInterface.EmotePlayerInterface;
import com.kosmx.emotecraft.gui.ingame.FastMenuScreen;
import com.kosmx.quarktool.QuarkReader;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.Level;
import org.lwjgl.glfw.GLFW;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class Client {

    private static KeyBinding emoteKeyBinding;
    private static KeyBinding debugEmote = null;
    private static KeyBinding stopEmote;
    public static final File externalEmotes = FMLPaths.GAMEDIR.get().resolve("emotes").toFile();
    public static void onInitializeClient() {
        //There is the only client stuff
        //like get emote list, or add emote player key
        //EmoteSerializer.initilaizeDeserializer();
        //Every type of initializing process has it's own method... It's easier to see through that

        initKeyBindings();      //Init keyBinding, including debug key

        //initNetworkClient();        //Init the Client-ide network manager. The Main will have a server-side
        ForgeNetwork.initClientNet();

        initEmotes();       //Import the emotes, including both the default and the external.


    }

    /*private static void initNetworkClient(){
        ClientSidePacketRegistry.INSTANCE.register(Main.EMOTE_PLAY_NETWORK_PACKET_ID, ((packetContext, packetByteBuf) -> {
            EmotePacket emotePacket;
            Emote emote;
            emotePacket = new EmotePacket();
            if(!emotePacket.read(packetByteBuf, false)) return;

            emote = emotePacket.getEmote();
            boolean isRepeat = emotePacket.isRepeat;
            packetContext.getTaskQueue().execute(() ->{
                PlayerEntity playerEntity = Minecraft.getInstance().world.getPlayerByUuid(emotePacket.getPlayer());
                if(playerEntity != null) {
                    if(!isRepeat || !Emote.isRunningEmote(((EmotePlayerInterface) playerEntity).getEmote())) {
                        ((EmotePlayerInterface) playerEntity).playEmote(emote);
                        ((EmotePlayerInterface) playerEntity).getEmote().start();
                    }
                    else {
                        ((EmotePlayerInterface)playerEntity).resetLastUpdated();
                    }
                }
            });
        }));

        ClientSidePacketRegistry.INSTANCE.register(Main.EMOTE_STOP_NETWORK_PACKET_ID, ((packetContex, packetByyeBuf) -> {
            StopPacket packet = new StopPacket();
            packet.read(packetByyeBuf);

            packetContex.getTaskQueue().execute(()-> {
                EmotePlayerInterface player = (EmotePlayerInterface) Minecraft.getInstance().world.getPlayerByUuid(packet.getPlayer());
                if(player != null && Emote.isRunningEmote(player.getEmote()))player.getEmote().stop();
            });
        }));
    }

     */

    public static void initEmotes(){
        //Serialize emotes
        EmoteHolder.clearEmotes();

        serializeInternalEmotes("waving");
        serializeInternalEmotes("clap");
        serializeInternalEmotes("crying");
        serializeInternalEmotes("point");
        serializeInternalEmotes("here");
        serializeInternalEmotes("palm");
        //TODO add internal emotes to the list


        if(!externalEmotes.isDirectory())externalEmotes.mkdirs();
        serializeExternalEmotes();

        Main.config.assignEmotes();
    }

    private static void serializeInternalEmotes(String name){
        InputStream stream = Client.class.getResourceAsStream("/assets/" + Main.MOD_ID + "/emotes/" + name + ".json");
        InputStreamReader streamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        Reader reader = new BufferedReader(streamReader);
        EmoteHolder emoteHolder = Serializer.serializer.fromJson(reader, EmoteHolder.class);
        EmoteHolder.addEmoteToList(emoteHolder);
        emoteHolder.bindIcon((String)("/assets/" + Main.MOD_ID + "/emotes/" + name + ".png"));
    }

    private static void serializeExternalEmotes(){
        for(File file: Objects.requireNonNull(Client.externalEmotes.listFiles((dir, name) -> name.endsWith(".json")))){
            try{
                BufferedReader reader = Files.newBufferedReader(file.toPath());
                EmoteHolder emote = EmoteHolder.deserializeJson(reader);
                EmoteHolder.addEmoteToList(emote);
                reader.close();
                File icon = Client.externalEmotes.toPath().resolve(file.getName().substring(0, file.getName().length()-5) + ".png").toFile();
                if(icon.isFile())emote.bindIcon(icon);
            }
            catch (Exception e){
                Main.log(Level.ERROR, "Error while importing external emote: " + file.getName() + ".", true);
                Main.log(Level.ERROR, e.getMessage());
            }
        }

        if(Main.config.enableQuark){
            Main.log(Level.WARN, "Quark importer is on", true);
            initQuarkEmotes(Client.externalEmotes);
        }
    }

    private static void initQuarkEmotes(File path){
        for(File file: Objects.requireNonNull(path.listFiles((dir, name) -> name.endsWith(".emote")))){
            Main.log(Level.INFO, "[Quarktool]  Importing Quark emote: " + file.getName());
            try {
                BufferedReader reader = Files.newBufferedReader(file.toPath());
                QuarkReader quarkReader = new QuarkReader();
                if(quarkReader.deserialize(reader, file.getName())){
                    EmoteHolder emote = quarkReader.getEmote();
                    EmoteHolder.addEmoteToList(emote);
                    File icon = Client.externalEmotes.toPath().resolve(file.getName().substring(0, file.getName().length()-6) + ".png").toFile();
                    if(icon.isFile())emote.bindIcon(icon);
                }
            }
            catch (Throwable e){ //try to catch everything
                if(Main.config.showDebug){
                    Main.log(Level.ERROR, e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }


    private static void playDebugEmote(){
        Main.log(Level.INFO, "Playing debug emote");
        Path location = FMLPaths.GAMEDIR.get().resolve("emote.json");
        try {
            BufferedReader reader = Files.newBufferedReader(location);
            EmoteHolder emoteHolder = EmoteHolder.deserializeJson(reader);
            reader.close();
            if(Minecraft.getInstance().getRenderViewEntity() instanceof ClientPlayerEntity){
                PlayerEntity entity = (PlayerEntity) Minecraft.getInstance().getRenderViewEntity();
                emoteHolder.playEmote(entity);
            }
        }
        catch (Exception e){
            Main.log(Level.ERROR, "Error while importing debug emote.", true);
            Main.log(Level.ERROR, e.getMessage());
            e.printStackTrace();
        }
    }


    private static void initKeyBindings(){
        emoteKeyBinding = new KeyBinding(
                "key.emotecraft.fastchoose",
                InputMappings.Type.KEYSYM,
                GLFW.GLFW_KEY_B,        //because bedrock edition has the same key
                "category.emotecraft.keybinding"
        );
        if(FMLPaths.GAMEDIR.get().resolve("emote.json").toFile().isFile()) { //Secret feature//
            debugEmote = new KeyBinding(
                    "key.emotecraft.debug",
                    InputMappings.Type.KEYSYM,
                    GLFW.GLFW_KEY_O,       //I don't know why... just
                    "category.emotecraft.keybinding"
            );
            ClientRegistry.registerKeyBinding(debugEmote);
            //ClientTickEvents.END_CLIENT_TICK.register(Minecraft -> {
            //    if (debugEmote.wasPressed()){
            //        playDebugEmote();
            //    }
            //});
        }
        ClientRegistry.registerKeyBinding(emoteKeyBinding);
        /*
        ClientTickEvents.END_CLIENT_TICK.register(Minecraft -> {
            if (emoteKeyBinding.wasPressed()){
                if(Minecraft.getInstance().getCameraEntity() instanceof ClientPlayerEntity){
                    Minecraft.getInstance().openScreen(new FastMenuScreen(new TranslatableText("emotecraft.fastmenu")));
                }
            }
        });

         */

        stopEmote = new KeyBinding("key.emotecraft.stop", InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.emotecraft.keybinding");
        ClientRegistry.registerKeyBinding(stopEmote);
        /*
        ClientTickEvents.END_CLIENT_TICK.register(Minecraft -> {
            if(stopEmote.wasPressed() && Minecraft.getInstance().getCameraEntity() instanceof ClientPlayerEntity && Emote.isRunningEmote(((EmotePlayerInterface)Minecraft.getInstance().getCameraEntity()).getEmote())){
                ((EmotePlayerInterface)Minecraft.getInstance().getCameraEntity()).getEmote().stop();
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                StopPacket packet = new StopPacket((PlayerEntity) Minecraft.getInstance().getCameraEntity());
                packet.write(buf);
                ClientSidePacketRegistry.INSTANCE.sendToServer(Main.EMOTE_STOP_NETWORK_PACKET_ID, buf);
            }
        });

         */

        //KeyPressCallback.EVENT.register((EmoteHolder::playEmote));

        MinecraftForge.EVENT_BUS.register(new KeyInputHandler());
    }
    private static class KeyInputHandler{
        @SubscribeEvent
        public void onKeyInput(InputEvent.KeyInputEvent event){
            if(debugEmote != null && debugEmote.isPressed())playDebugEmote();
            if (emoteKeyBinding.isPressed()){
                if(Minecraft.getInstance().getRenderViewEntity() instanceof ClientPlayerEntity){
                    Minecraft.getInstance().displayGuiScreen(new FastMenuScreen(new TranslationTextComponent("emotecraft.fastmenu")));
                }
            }
            if(stopEmote.isPressed() && Minecraft.getInstance().getRenderViewEntity() instanceof ClientPlayerEntity && Emote.isRunningEmote(((EmotePlayerInterface)Minecraft.getInstance().getRenderViewEntity()).getEmote())){
                ((EmotePlayerInterface)Minecraft.getInstance().getRenderViewEntity()).getEmote().stop();
                ForgeNetwork.stopPacket.sendToServer(new StopPacket(Minecraft.getInstance().player));
            }
        }
    }
}
