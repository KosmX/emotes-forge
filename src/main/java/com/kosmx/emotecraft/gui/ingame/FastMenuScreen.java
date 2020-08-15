package com.kosmx.emotecraft.gui.ingame;

import com.kosmx.emotecraft.gui.widget.AbstractFastChooseWidget;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class FastMenuScreen extends Screen {
    private FastMenuWidget widget;

    public FastMenuScreen(TextComponent title) {
        super(title);
    }

    @Override
    public void func_231160_c_(){
        int x = (int) Math.min(this.field_230708_k_*0.8, this.field_230709_l_*0.8);
        this.widget = new FastMenuWidget((this.field_230708_k_ - x)/2, (this.field_230709_l_ - x)/2, x);
        this.field_230705_e_.add(widget);
        this.field_230710_m_.add(new Button(this.field_230708_k_ - 120, this.field_230709_l_ - 30, 96, 20, new TranslationTextComponent("emotecraft.emotelist"), (button -> this.field_230706_i_.displayGuiScreen(new FullMenuScreen(new TranslationTextComponent("emotecraft.emotelist"))))));
        this.field_230705_e_.addAll(this.field_230710_m_);
    }

    @Override
    public boolean func_231177_au__() {
        return false;
    }

    @Override
    public void func_230430_a_(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.func_230446_a_(matrices);
        widget.func_230430_a_(matrices, mouseX, mouseY, delta);
        super.func_230430_a_(matrices, mouseX, mouseY, delta);
    }


    private class FastMenuWidget extends AbstractFastChooseWidget{

        public FastMenuWidget(int x, int y, int size) {
            super(x, y, size);
        }

        @Override
        protected boolean doHoverPart(FastChooseElement part) {
            return part.hasEmote();
        }

        @Override
        protected boolean isValidClickButton(int button) {
            return button == 0;
        }

        @Override
        protected boolean onClick(FastChooseElement element, int button) {
            if(element.getEmote() != null) {
                boolean bl = element.getEmote().playEmote((PlayerEntity) Minecraft.getInstance().getRenderViewEntity());
                field_230706_i_.displayGuiScreen(null);
                return bl;
            }
            return false;
        }
    }
}
