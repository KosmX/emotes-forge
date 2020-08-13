package com.kosmx.emotecraft.config;

import com.google.gson.*;
import com.kosmx.emotecraft.Main;
import net.minecraft.util.Tuple;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.lang.reflect.Type;

public class ConfigSerializer implements JsonDeserializer<SerializableConfig>, JsonSerializer<SerializableConfig> {

    @Override
    public SerializableConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject node = json.getAsJsonObject();
        SerializableConfig config = new SerializableConfig();
        if(node.has("showDebug"))config.showDebug = node.get("showDebug").getAsBoolean();
        if(node.has("validate"))config.validateEmote = node.get("validate").getAsBoolean();
        if(node.has("enablequark"))config.enableQuark = node.get("enablequark").getAsBoolean();
        if(FMLEnvironment.dist == Dist.CLIENT)clientDeserialize(node, config);
        return config;
    }

    @OnlyIn(Dist.CLIENT)
    private void clientDeserialize(JsonObject node, SerializableConfig config){
        if(node.has("dark"))config.dark = node.get("dark").getAsBoolean();
        if(node.has("showIcon"))config.showIcons = node.get("showIcon").getAsBoolean();
        if(node.has("fastmenu"))fastMenuDeserializer(node.get("fastmenu").getAsJsonObject(), config);
        if(node.has("keys"))keyBindsDeserializer(node.get("keys").getAsJsonArray(), config);
    }
    @OnlyIn(Dist.CLIENT)
    private void fastMenuDeserializer(JsonObject node, SerializableConfig config){
        for(int i = 0;i != 8; i++){
            if(node.has(Integer.toString(i))){
                config.fastMenuHash[i] = node.get(Integer.toString(i)).getAsInt();
                /*
                EmoteHolder emote = EmoteHolder.getEmoteFromHash(node.get(Integer.toString(i)).getAsInt());
                config.fastMenuEmotes[i] = emote;
                if(emote == null){
                    Main.log(Level.ERROR, "Can't find emote from hash: " + node.get(Integer.toString(i)).getAsInt());
                }

                 *///TODO remove comment if stable
            }
        }
    }
    @OnlyIn(Dist.CLIENT)
    private void keyBindsDeserializer(JsonArray node, SerializableConfig config){
        for(JsonElement object:node){
            JsonObject n = object.getAsJsonObject();
            config.emotesWithHash.add(new Tuple<>(n.get("id").getAsInt(), n.get("key").getAsString()));
            //keyBindDeserializer(object.getAsJsonObject());
        }
    }
    /*
    @Environment(Dist.CLIENT)
    private void keyBindDeserializer(JsonObject node){

        EmoteHolder emote = EmoteHolder.getEmoteFromHash(node.get("id").getAsInt());
        if(emote != null){
            emote.keyBinding = InputUtil.fromTranslationKey(node.get("key").getAsString());
        }
        else {
            Main.log(Level.ERROR, "Can't find emote from hash: " + node.get("id").getAsInt());
        }
    }

     */

    @Override
    public JsonElement serialize(SerializableConfig config, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject node = new JsonObject();
        node.addProperty("showDebug", config.showDebug);
        node.addProperty("validate", config.validateEmote);
        if(FMLEnvironment.dist == Dist.CLIENT) clientSerialize(config, node);
        return node;
    }

    @OnlyIn(Dist.CLIENT)
    private void clientSerialize(SerializableConfig config, JsonObject node){
        node.addProperty("dark", config.dark);
        if(Main.config.enableQuark)node.addProperty("enablequark", true);
        node.addProperty("showIcon", config.showIcons);
        node.add("fastmenu", fastMenuSerializer(config));
        node.add("keys", keyBindsSerializer(config));
    }
    @OnlyIn(Dist.CLIENT)
    private JsonObject fastMenuSerializer(SerializableConfig config){
        JsonObject node = new JsonObject();
        for (int i = 0; i != 8; i++){
            if(config.fastMenuEmotes[i] != null){
                node.addProperty(Integer.toString(i), config.fastMenuEmotes[i].hash);
            }
        }
        return node;
    }
    @OnlyIn(Dist.CLIENT)
    private JsonArray keyBindsSerializer(SerializableConfig config){
        JsonArray array = new JsonArray();
        for(EmoteHolder emote:config.emotesWithKey){
            array.add(keyBindSerializer(emote));
        }
        return array;
    }
    @OnlyIn(Dist.CLIENT)
    private JsonObject keyBindSerializer(EmoteHolder emote){
        JsonObject node = new JsonObject();
        node.addProperty("id", emote.hash);
        node.addProperty("key", emote.keyBinding.getTranslationKey());
        return node;
    }

}
