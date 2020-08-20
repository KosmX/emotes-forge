package com.kosmx.emotecraft.config;

import com.kosmx.emotecraft.Main;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.Tuple;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.List;

public class SerializableConfig {
    //@OnlyIn(Dist.CLIENT)
    public List<EmoteHolder> emotesWithKey = new ArrayList<>();
    //@OnlyIn(Dist.CLIENT)
    public final EmoteHolder[] fastMenuEmotes = new EmoteHolder[8];

    public boolean validateEmote = false;
    public boolean showDebug = false;
    //@OnlyIn(Dist.CLIENT)
    public boolean dark = false;
    //@OnlyIn(Dist.CLIENT)
    public boolean enableQuark = false;
    //@OnlyIn(Dist.CLIENT)
    public boolean showIcons = true;

    public int[] fastMenuHash = new int[8];
    public List<Tuple<Integer, String>> emotesWithHash = new ArrayList<>();

    public void assignEmotes(){
        this.emotesWithKey = new ArrayList<>();
        for (int i = 0; i != 8; i++){
            if(fastMenuHash[i] == 0)continue;
            EmoteHolder emote = EmoteHolder.getEmoteFromHash(fastMenuHash[i]);
            this.fastMenuEmotes[i] = emote;
            if(emote == null){
                Main.log(Level.ERROR, "Can't find emote from hash: " + fastMenuHash[i]);
            }
        }

        for (Tuple<Integer, String> pair : emotesWithHash){
            EmoteHolder emote = EmoteHolder.getEmoteFromHash(pair.getA());
            if (emote != null){
                emote.keyBinding = InputMappings.getInputByName(pair.getB());
            }
            else {
                Main.log(Level.ERROR, "Can't find emote from hash: " + pair.getA());
            }
        }

        EmoteHolder.bindKeys(this);
    }
}
