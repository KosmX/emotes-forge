package com.kosmx.emotecraft.gui.widget;

import com.kosmx.emotecraft.config.EmoteHolder;
import com.kosmx.emotecraft.math.Helper;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractEmoteListWidget<E extends AbstractEmoteListWidget.AbstractEmoteEntry<E>> extends ExtendedList<E> {

    protected List<E> emotes = new ArrayList<>();
    private final Screen screen;

    public AbstractEmoteListWidget(Minecraft minecraftClient, int i, int j, int k, int l, int m, Screen screen) {
        super(minecraftClient, i, j, k, l, m);
        this.field_230676_m_ = false;
        this.screen = screen;
    }


    @Override
    public int func_230949_c_() {
        return this.field_230670_d_-5;
    }

    public abstract void setEmotes(List<EmoteHolder> list);

    public void filter(Supplier<String> string){
        this.func_230963_j_();
        for(E emote : this.emotes){
            if(emote.emote.name.toString().toLowerCase().contains(string.get()) || emote.emote.description.toString().toLowerCase().contains(string.get()) || emote.emote.author.toString().toLowerCase().equals(string.get())){
                this.func_230513_b_(emote);
            }
        }
    }

    @Override
    protected int func_230952_d_() {
        return this.field_230674_k_ - 6;
    }

    @Override
    protected boolean func_230971_aw__() {
        return screen.func_241217_q_() == this;
    }

    public static abstract class AbstractEmoteEntry<T extends AbstractEmoteEntry<T>> extends AbstractList.AbstractListEntry<T> {
        protected final Minecraft client;
        public final EmoteHolder emote;

        public AbstractEmoteEntry(Minecraft client, EmoteHolder emote){
            this.client = client;
            this.emote = emote;
        }

        @Override
        public void func_230432_a_(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {

            if(this.client.gameSettings.touchscreen || hovered){
                RenderSystem.color4f(1, 1, 1, 1);
                AbstractGui.func_238467_a_(matrices, x - 1, y - 1, x + entryWidth - 9, y + entryHeight + 1, Helper.colorHelper(66, 66, 66, 128));
            }
            this.client.fontRenderer.func_238407_a_(matrices, this.emote.name, x + 38, y + 1, 16777215);
            this.client.fontRenderer.func_238407_a_(matrices, this.emote.description, x + 38, y + 12, 8421504);
            if(!this.emote.author.getString().equals(""))this.client.fontRenderer.func_238407_a_(matrices, new TranslationTextComponent("emotecraft.emote.author").func_240699_a_(TextFormatting.GOLD).func_230529_a_(this.emote.author), x + 38, y + 23, 8421504);
            if(this.emote.getIcon() != null) {
                RenderSystem.color4f(1, 1, 1, 1);
                Minecraft.getInstance().getTextureManager().bindTexture(this.emote.getIcon());
                RenderSystem.enableBlend();
                func_238466_a_(matrices, x, y, 32, 32, 0, 0, 256, 256, 256, 256);
                RenderSystem.disableBlend();
            }
        }

        @Override
        public boolean func_231044_a_(double mouseX, double mouseY, int button) {
            if(button == 0){
                this.onPressed();
                return true;
            }
            else {
                return false;
            }
        }

        protected abstract void onPressed();
    }
}
