package com.kosmx.emotecraft;

import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

public class MixinConnector implements IMixinConnector {

    @Override
    public void connect() {
        Mixins.addConfiguration("assets/emotecraft/emotecraft.mixins.json");
    }
}


//Where did Forge learn, how to Mixin???
