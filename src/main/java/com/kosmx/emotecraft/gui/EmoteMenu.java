package com.kosmx.emotecraft.gui;

import com.kosmx.emotecraft.Client;
import com.kosmx.emotecraft.Main;
import com.kosmx.emotecraft.config.EmoteHolder;
import com.kosmx.emotecraft.config.Serializer;
import com.kosmx.emotecraft.math.Helper;
import com.kosmx.emotecraft.gui.widget.AbstractEmoteListWidget;
import com.kosmx.emotecraft.gui.widget.AbstractFastChooseWidget;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslationTextComponent();
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class EmoteMenu extends Screen {
    private final Screen parent;
    private int activeKeyTime = 0;
    private EmoteListWidget emoteList;
    private FastChooseWidget fastMenu;
    //protected List<field_230710_m_> field_230710_m_ is already exists
    private static final ITextComponent unboundText = InputMappings.INPUT_INVALID.func_237520_d_();
    private Button setKeyButton;
    public boolean save = false;
    public boolean warn = false;
    private TextFieldWidget searchBox;
    private List<PositionedText> texts = new ArrayList<>();
    private Button resetKey;


    public EmoteMenu(Screen parent){
        super(new TranslationTextComponent("menu_title"));
        this.parent = parent;
    }

    @Override
    protected void func_231160_c_() {
        if(warn && Main.config.enableQuark){
            warn = false;
            ConfirmScreen csr = new ConfirmScreen((bool)->{
                Main.config.enableQuark = bool;
                Minecraft.getInstance().displayGuiScreen(this);
            },new TranslationTextComponent("emotecraft.quark"), new TranslationTextComponent("emotecraft.quark2"));
            this.field_230706_i_.displayGuiScreen(csr);
            csr.setButtonDelay(56);
        }

        this.texts = new ArrayList<>();

        Client.initEmotes();
        this.searchBox = new TextFieldWidget(this.field_230712_o_, this.field_230708_k_/2-(int)(this.field_230708_k_/2.2-16)-12, 12, (int)(this.field_230708_k_/2.2-16), 20, this.searchBox, new TranslationTextComponent("emotecraft.search"));

        this.searchBox.setResponder((string)-> this.emoteList.filter(string::toLowerCase));
        this.field_230705_e_.add(searchBox);

        this.field_230710_m_.add(new Button(this.field_230708_k_ / 2 - 154, this.field_230709_l_ - 30, 150, 20, new TranslationTextComponent("emotecraft.openFolder"), (buttonWidget) -> Util.getOSType().openFile(Client.externalEmotes)));

        this.emoteList = new EmoteListWidget(this.field_230706_i_, (int) (this.field_230708_k_ / 2.2 - 16), this.field_230709_l_, this);
        this.emoteList.setLeftPos(this.field_230708_k_/2-(int)(this.field_230708_k_/2.2-16)-12);
        this.field_230705_e_.add(this.emoteList);
        int x = Math.min(this.field_230708_k_/4, (int)(this.field_230709_l_/2.5));
        this.fastMenu = new FastChooseWidget(this.field_230708_k_/2 + 2, this.field_230709_l_/2 - 8, x-7);
        this.field_230705_e_.add(fastMenu);
        this.field_230710_m_.add(new Button(this.field_230708_k_ - 100, 4, 96, 20, new TranslationTextComponent("emotecraft.options.options"), (button -> this.field_230706_i_.displayGuiScreen(ClothConfigScreen.getConfigScreen(this)))));
        this.field_230710_m_.add(new Button(this.field_230708_k_/2 + 10, this.field_230709_l_ - 30, 96, 20, DialogTexts.field_240632_c_, (button -> this.field_230706_i_.displayGuiScreen(this.parent))));
        setKeyButton = new Button(this.field_230708_k_/2 + 6, 60, 96, 20, unboundText, button -> this.activateKey());
        this.field_230710_m_.add(setKeyButton);
        resetKey =  new Button(this.field_230708_k_/2 + 124, 60, 96, 20, new TranslationTextComponent("controls.reset"), (button -> {
            if(emoteList.getSelected() != null){
                emoteList.getSelected().emote.keyBinding = InputMappings.INPUT_INVALID;
                this.save = true;
            }
        }));
        this.field_230710_m_.add(resetKey);
        emoteList.setEmotes(EmoteHolder.list);
        this.field_230705_e_.addAll(field_230710_m_);
        super.func_231160_c_();
        this.setInitialFocus(this.searchBox);
        this.texts.add(new PositionedText(new TranslationTextComponent("emotecraft.options.keybind"), this.field_230708_k_/2 +115, 40));
        this.texts.add(new PositionedText(new TranslationTextComponent("emotecraft.options.fastmenu"), this.field_230708_k_/2 + 10 + x/2, height/2 - 54));
        this.texts.add(new PositionedText(new TranslationTextComponent("emotecraft.options.fastmenu2"), this.field_230708_k_/2 + 10 + x/2, height/2 - 40));
        this.texts.add(new PositionedText(new TranslationTextComponent("emotecraft.options.fastmenu3"), this.field_230708_k_/2 + 10 + x/2, height/2 - 26));
    }

    private void activateKey(){
        if(emoteList.getSelected() != null) {
            this.setFocused(setKeyButton);
            activeKeyTime = 200;
        }
    }

    @Override
    public void setFocused(@Nullable Element focused) {
        if(activeKeyTime == 0) super.setFocused(focused);
    }

    @Override
    public void func_231023_e_() {
        super.func_231023_e_();
        if(activeKeyTime == 1){
            setFocused(null);
        }
        if(activeKeyTime != 0){
            activeKeyTime--;
        }
    }

    @Override
    public boolean func_231044_a_(double mouseX, double mouseY, int button) {
        if(this.activeKeyTime != 0 && emoteList.getSelected() != null){
            return setKey(InputMappings.Type.MOUSE.getOrMakeInput(button));
        }
        else return super.func_231044_a_(mouseX, mouseY, button);
    }


    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.func_231165_f_(0);
        if(this.emoteList.getSelected() == null){
            this.setKeyButton.active = false;
            this.resetKey.active = false;
        }
        else {
            this.setKeyButton.active = true;
            this.resetKey.active = !this.emoteList.getSelected().emote.keyBinding.equals(InputMappings.INPUT_INVALID);
        }
        for(PositionedText str:texts){
            str.render(matrices, field_230712_o_);
        }
        this.emoteList.render(matrices, mouseX, mouseY, delta);
        this.searchBox.render(matrices, mouseX, mouseY, delta);
        this.fastMenu.render(matrices, mouseX, mouseY, delta);
        updateKeyText();
        super.render(matrices, mouseX, mouseY, delta);
    }
    private boolean setKey(InputMappings.Input key){
        boolean bl = false;
        if(emoteList.getSelected()!= null){
            bl = true;
            if(!applyKey(false, emoteList.getSelected().emote, key)){
                this.field_230706_i_.displayGuiScreen(new ConfirmScreen((bool)-> confirmReturn(bool, emoteList.getSelected().emote, key), new TranslationTextComponent("emotecraft.sure"), new TranslationTextComponent("emotecraft.sure2")));
            }
        }
        return bl;
    }
    private void confirmReturn(boolean choice, EmoteHolder emoteHolder, InputMappings.Input key){
        if(choice){
            applyKey(true, emoteHolder, key);
            this.saveConfig();
        }
        this.field_230706_i_.displayGuiScreen(this);
    }

    private boolean applyKey(boolean force, EmoteHolder emote, InputMappings.Input key){
        boolean bl = true;
        for (EmoteHolder emoteHolder:EmoteHolder.list){
            if(!key.equals(InputMappings.INPUT_INVALID) && emoteHolder.keyBinding.equals(key)){
                bl = false;
                if(force){
                    emoteHolder.keyBinding = InputMappings.INPUT_INVALID;
                }
            }
        }
        if (bl || force){
            emote.keyBinding = key;
            this.save = true;
        }
        this.activeKeyTime = 0;
        return bl;
    }

    @Override
    public void func_231175_as__(){
        this.field_230706_i_.displayGuiScreen(this.parent);
    }

    @Override
    public void func_231164_f_() {
        if(save){
            this.saveConfig();
        }
        super.func_231164_f_();
    }

    private void saveConfig(){
        EmoteHolder.bindKeys(Main.config);
        try {
            BufferedWriter writer = Files.newBufferedWriter(Main.CONFIGPATH);
            Serializer.serializer.toJson(Main.config, writer);
            writer.close();
            //FileUtils.write(Main.CONFIGPATH, Serializer.serializer.toJson(Main.config), "UTF-8", false);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateKeyText(){
        if(emoteList.getSelected() != null){
            ITextComponent message = emoteList.getSelected().emote.keyBinding.getLocalizedText();
            if(activeKeyTime != 0)message = (new StringTextComponent("> ")).func_230529_a_(message.func_230532_e_().func_240699_a_(TextFormatting.YELLOW)).func_240702_b_(" <").func_240699_a_(TextFormatting.YELLOW);
            setKeyButton.setMessage(message);
        }
    }

    @Override
    public boolean func_231046_a_(int keyCode, int scanCode, int mod) {
        if(emoteList.getSelected() != null && activeKeyTime != 0){
            if(keyCode == 256){
                return setKey(InputMappings.INPUT_INVALID);
            }
            else {
                return setKey(InputMappings.fromKeyCode(keyCode, scanCode));
            }
        }
        else {
            return super.func_231046_a_(keyCode, scanCode, mod);
        }
    }



    public static class EmoteListWidget extends AbstractEmoteListWidget<EmoteListWidget.EmoteListEntry> {
        public EmoteListWidget(Minecraft minecraftClient, int width, int height, Screen screen) {
            super(minecraftClient, width, height - 51 - 32, 51, height-32, 36, screen);
        }

        @Override
        public void setEmotes(List<EmoteHolder> list) {
            for(EmoteHolder emote : list){
                this.emotes.add(new EmoteListEntry(this.client, emote));
            }
            filter(() -> "");
        }

        public  class EmoteListEntry extends AbstractEmoteListWidget.AbstractEmoteEntry<EmoteListEntry> {
            public EmoteListEntry(Minecraft client, EmoteHolder emote) {
                super(client, emote);
            }

            protected void onPressed() {        //setup screen -> select pack, play screen -> play
                EmoteListWidget.this.setSelected(this);
            }
        }
    }

    private class FastChooseWidget extends AbstractFastChooseWidget{

        public FastChooseWidget(int x, int y, int size) {
            super(x, y, size);
        }

        @Override
        protected boolean isValidClickButton(int button) {
            return (button == 0 || button == 1) && activeKeyTime == 0;
        }

        @Override
        protected boolean onClick(FastChooseElement element, int button) {
            if(activeKeyTime != 0)return false;
            if(button == 1){
                element.clearEmote();
                save = true;
                return true;
            }
            else if (emoteList.getSelected() != null){
                element.setEmote(emoteList.getSelected().emote);
                save = true;
                return true;
            }
            else {
                return false;
            }
        }

        @Override
        protected boolean doHoverPart(FastChooseElement part) {
            return activeKeyTime == 0;
        }
    }
    private class PositionedText {
        private final ITextComponent str;
        private final int x;
        private final int y;

        private PositionedText(ITextComponent str, int x, int y){
            this.str = str;
            this.x = x;
            this.y = y;
        }
        private void render(MatrixStack matrixStack, FontRenderer textRenderer){
            drawCenteredText(matrixStack, textRenderer, this.str, this.x, this.y, Helper.colorHelper(255,255,255,255));
            textRenderer.getClass();
        }
    }
}
