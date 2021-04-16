package io.github.kosmx.emotes.forge;

import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.forge.executor.ForgeEmotesMain;
import io.github.kosmx.emotes.forge.network.ServerNetwork;
import io.github.kosmx.emotes.main.MainLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.logging.Logger;

@Mod(modid = ForgeWrapper.MODID, name = ForgeWrapper.NAME, version = ForgeWrapper.VERSION)
public class ForgeWrapper {
    public static final String MODID = "emotecraft";
    public static final String NAME = "Emotecraft";
    public static final String VERSION = "2.0.1";

    public static final Logger LOGGER = Logger.getLogger("Emotecraft");


    @EventHandler
    public void preInit(FMLPreInitializationEvent event){
        EmoteInstance.instance = new ForgeEmotesMain();
    }

    @EventHandler
    private void setup(final FMLInitializationEvent event){
        MainLoader.main(new String[]{"FML"});
        if(FMLLoader.getDist() == Dist.CLIENT){
            ClientInit.initClient();
        }
        ServerNetwork.instance.init();

    }
}
