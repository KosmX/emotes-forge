package com.kosmx.emotecraft.gui.ingame;

import com.kosmx.emotecraft.config.EmoteHolder;
import com.kosmx.emotecraft.gui.widget.AbstractEmoteListWidget;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

public class FullMenuScreen extends Screen {

    private TextFieldWidget searchBox;
    private EmoteList emoteList;

    public FullMenuScreen(ITextComponent title) {
        super(title);
    }

    @Override
    public void func_231160_c_() {
        int x = (int) Math.min(this.field_230708_k_*0.8, this.field_230709_l_ - 120);
        this.searchBox = new TextFieldWidget(this.field_230712_o_, (this.field_230708_k_ - x)/2, 12, x, 20, this.searchBox, new TranslationTextComponent("emotecraft.search"));
        this.searchBox.setResponder((string)-> this.emoteList.filter(string::toLowerCase));
        this.emoteList = new EmoteList(this.field_230706_i_, x, x,(this.field_230709_l_ - x)/2, (this.field_230709_l_ + x)/2, 36, this);
        this.emoteList.func_230959_g_((this.field_230708_k_ - x)/2);
        emoteList.setEmotes(EmoteHolder.list);
        this.field_230705_e_.add(searchBox);
        this.field_230705_e_.add(emoteList);
        this.setFocusedDefault(this.searchBox);
        this.field_230710_m_.add(new Button(this.field_230708_k_ - 120, this.field_230709_l_ - 30, 96, 20, DialogTexts.field_240633_d_, (button -> this.field_230706_i_.displayGuiScreen(null))));
        this.field_230705_e_.addAll(this.field_230710_m_);
    }

    @Override
    public boolean func_231177_au__() {
        return false;
    }

    @Override
    public void func_230430_a_(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.func_231165_f_(0);
        this.emoteList.func_230430_a_(matrices, mouseX, mouseY, delta);
        this.searchBox.func_230430_a_(matrices, mouseX, mouseY, delta);
        super.func_230430_a_(matrices, mouseX, mouseY, delta);
    }

    private class EmoteList extends AbstractEmoteListWidget<EmoteList.EmoteEntry>{

        public EmoteList(Minecraft minecraftClient, int i, int j, int k, int l, int m, Screen screen) {
            super(minecraftClient, i, j, k, l, m, screen);
        }

        @Override
        public void setEmotes(List<EmoteHolder> list) {
            for(EmoteHolder emote:list){
                this.emotes.add(new EmoteEntry(this.field_230668_b_, emote));
            }
            filter(()->"");
        }

        private class EmoteEntry extends AbstractEmoteListWidget.AbstractEmoteEntry<EmoteEntry>{

            public EmoteEntry(Minecraft client, EmoteHolder emote) {
                super(client, emote);
            }

            @Override
            protected void onPressed() {
                if(Minecraft.getInstance().getRenderViewEntity() instanceof ClientPlayerEntity){
                    this.emote.playEmote(Minecraft.getInstance().player);
                    this.client.displayGuiScreen(null);
                }
            }
        }
    }
}
