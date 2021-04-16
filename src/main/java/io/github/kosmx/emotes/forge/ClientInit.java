package io.github.kosmx.emotes.forge;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.kosmx.emotes.forge.executor.ForgeClientMethods;
import io.github.kosmx.emotes.arch.gui.EmoteMenuImpl;
import io.github.kosmx.emotes.arch.gui.screen.ingame.FastChosseScreen;
import io.github.kosmx.emotes.forge.network.ClientNetworkInstance;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientInit {

    static KeyMapping openMenuKey;
    static KeyMapping stopEmote;
    static Consumer<Minecraft> keyBindingFunction;

    static void initClient(){

        initKeyBinding();

        ClientNetworkInstance.networkInstance.init(); //init network
        /*
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            FabricClientMethods.tick++;

            keyBindingFunction.accept(client);
        });

         */
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> (minecraft, screen) -> new EmoteMenuImpl(screen));
    }

    @SubscribeEvent
    public static void endClientTick(TickEvent.ClientTickEvent event){
        ForgeClientMethods.tick++;
    }

    @SubscribeEvent
    public static void keyListenerEvent(InputEvent.KeyInputEvent event){
        keyBindingFunction.accept(null);
    }


    private static void initKeyBinding(){
        openMenuKey = new KeyMapping("key.emotecraft.fastchoose", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, "category.emotecraft.keybinding");
        //KeyBindingRegistryImpl.registerKeyBinding(openMenuKey);
        ClientRegistry.registerKeyBinding(openMenuKey);

        stopEmote = new KeyMapping("key.emotecraft.stop", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.emotecraft.keybinding");
        ClientRegistry.registerKeyBinding(stopEmote);

        keyBindingFunction = client -> {

            if(openMenuKey.consumeClick()){
                if(Minecraft.getInstance().player == Minecraft.getInstance().getCameraEntity()){
                    Minecraft.getInstance().setScreen(new FastChosseScreen(null));
                }
            }
            if(stopEmote.consumeClick()){
                ClientEmotePlay.clientStopLocalEmote();
            }
        };
    }
}
