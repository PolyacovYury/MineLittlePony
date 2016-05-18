package com.voxelmodpack.hdskins.gui;

import java.awt.Color;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.input.Keyboard;

import com.google.common.base.Converter;
import com.google.common.base.Enums;
import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mumfrey.liteloader.client.gui.GuiCheckbox;
import com.mumfrey.webprefs.WebPreferencesManager;
import com.mumfrey.webprefs.interfaces.IWebPreferences;
import com.voxelmodpack.hdskins.HDSkinManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlider;
import net.minecraft.client.gui.GuiSlider.FormatHelper;
import net.minecraft.client.resources.I18n;

public class MetaHandler extends GuiScreen {

    private GuiScreen parent;
    private List<Opt<?>> options = Lists.newArrayList();
    protected int optionHeight = 5;
    protected int optionPosX;

    public MetaHandler(GuiScreen parent) {
        this.parent = parent;
    }

    public <E extends Enum<E>> void selection(String name, Class<E> options) {
        this.options.add(new Sel<E>(name, options));
    }

    public void bool(String name) {
        this.options.add(new Bol(name));
    }

    public void number(String name, int min, int max) {
        this.options.add(new Num(name, min, max));
    }

    public void color(String name) {
        this.options.add(new Col(name));
    }

    @Override
    public void initGui() {
        super.initGui();
        optionHeight = 30;
        optionPosX = this.width / 8;
        this.buttonList.add(new GuiButton(0, width / 2 - 100, height - 40, 80, 20, "Cancel"));
        this.buttonList.add(new GuiButton(1, width / 2 + 20, height - 40, 80, 20, "Apply"));
        for (Opt<?> opt : options) {
            opt.init();
        }
        fetch();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
        case 1:
            push();
        case 0:
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parent);
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float ticks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, ticks);
        for (Opt<?> opt : options) {
            opt.drawOption(mouseX, mouseY);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        for (Opt<?> opt : options) {
            if (opt.mouseClicked(mouseX, mouseY))
                break;

        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        for (Opt<?> opt : options) {
            opt.mouseReleased(mouseX, mouseY);
        }
    }

    public void push() {
        String data = new Gson().toJson(toMap());

        IWebPreferences prefs = WebPreferencesManager.getDefault().getLocalPreferences(false);
        prefs.set(HDSkinManager.METADATA_KEY, data);
        prefs.commit(false);
    }

    public void fetch() {
        IWebPreferences prefs = WebPreferencesManager.getDefault().getLocalPreferences(false);
        String json = prefs.get(HDSkinManager.METADATA_KEY, "{}");
        Map<String, String> data = new Gson().fromJson(json, new TypeToken<Map<String, String>>() {}.getType());
        fromMap(data);
    }

    private Map<String, String> toMap() {
        Map<String, String> map = Maps.newHashMap();
        for (Opt<?> opt : options) {
            if (opt.isEnabled()) {
                map.put(opt.getName(), opt.toString());
            }
        }
        return map;
    }

    private void fromMap(Map<String, String> data) {
        for (Entry<String, String> e : data.entrySet()) {
            for (Opt<?> opt : options) {
                if (opt.name.equals(e.getKey())) {
                    opt.setEnabled(true);
                    opt.fromString(e.getValue());
                    break;
                }
            }
        }
    }

    private abstract class Opt<T> {

        protected Minecraft mc = Minecraft.getMinecraft();
        protected final String name;
        protected Optional<T> value = Optional.absent();

        private GuiCheckbox enabled;

        public Opt(String name) {
            this.name = name;
        }

        private String getName() {
            return name;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled.checked = enabled;
        }

        public boolean isEnabled() {
            return this.enabled.checked;
        }

        protected void init() {
            this.enabled = new GuiCheckbox(0, optionPosX + 2, optionHeight, "");
        }

        protected void drawOption(int mouseX, int mouseY) {
            this.enabled.drawButton(mc, mouseX, mouseY);
        }

        protected boolean mouseClicked(int mouseX, int mouseY) {
            if (this.enabled.mousePressed(mc, mouseX, mouseY)) {
                this.enabled.checked = !this.enabled.checked;
                return true;
            }
            return false;
        }

        protected void mouseReleased(int mouseX, int mouseY) {

        }

        @Override
        public abstract String toString();

        public abstract void fromString(String s);
    }

    private class Bol extends Opt<Boolean> {

        private GuiCheckbox chk;

        public Bol(String name) {
            super(name);
        }

        @Override
        public void init() {
            super.init();
            this.chk = new GuiCheckbox(0, optionPosX + 20, optionHeight, I18n.format(this.name));
            optionHeight += 14;
        }

        @Override
        protected void drawOption(int mouseX, int mouseY) {
            super.drawOption(mouseX, mouseY);
            chk.drawButton(mc, mouseX, mouseY);

        }

        @Override
        protected boolean mouseClicked(int mouseX, int mouseY) {
            boolean clicked = super.mouseClicked(mouseX, mouseY);
            if (!clicked && chk.mousePressed(mc, mouseX, mouseY)) {
                chk.checked = !chk.checked;
                return true;
            }
            return clicked;
        }

        @Override
        public String toString() {
            return this.value.transform(Functions.toStringFunction()).orNull();
        }

        @Override
        public void fromString(String s) {
            value = Optional.of(Boolean.parseBoolean(s));
        }
    }

    private class Num extends Opt<Integer> implements GuiResponder, FormatHelper {

        private final int min;
        private final int max;

        private GuiSlider guiSlider;

        public Num(String name, int min, int max) {
            super(name);
            this.min = min;
            this.max = max;
        }

        @Override
        public void init() {
            super.init();
            this.guiSlider = new GuiSlider(this, 0, optionPosX + 20, optionHeight, this.name, min, max, min, this);
            optionHeight += 22;
        }

        @Override
        protected void drawOption(int mouseX, int mouseY) {
            super.drawOption(mouseX, mouseY);
            this.guiSlider.drawButton(mc, mouseX, mouseY);
        }

        @Override
        protected boolean mouseClicked(int mouseX, int mouseY) {
            boolean clicked = super.mouseClicked(mouseX, mouseY);
            return clicked || this.guiSlider.mousePressed(mc, mouseX, mouseY);
        }

        @Override
        protected void mouseReleased(int mouseX, int mouseY) {
            this.guiSlider.mouseReleased(mouseX, mouseY);
        }

        @Override
        public void setEntryValue(int id, float value) {
            this.value = Optional.of((int) value);
        }

        @Override
        public String getText(int id, String name, float value) {
            return name + ": " + (int) value;
        }

        @Override
        public String toString() {
            return this.value.transform(Functions.toStringFunction()).orNull();
        }

        @Override
        public void fromString(String s) {
            value = Optional.fromNullable(Ints.tryParse(s));
        }

        @Override
        public void setEntryValue(int id, boolean value) {}

        @Override
        public void setEntryValue(int id, String value) {}

    }

    private class Sel<E extends Enum<E>> extends Opt<E> {

        private Class<E> type;
        private final List<E> options;

        private int index;

        private GuiButton button;

        public Sel(String name, Class<E> enumType) {
            super(name);
            this.type = enumType;
            this.options = ImmutableList.copyOf(enumType.getEnumConstants());
        }

        @Override
        protected void init() {
            super.init();
            this.button = new GuiButton(0, optionPosX + 20, optionHeight, 100, 20, name + ": " + I18n.format(this.get().toString()));
            optionHeight += 22;
        }

        @Override
        protected void drawOption(int mouseX, int mouseY) {
            super.drawOption(mouseX, mouseY);
            this.button.drawButton(mc, mouseX, mouseY);
        }

        @Override
        protected boolean mouseClicked(int mouseX, int mouseY) {
            boolean clicked = super.mouseClicked(mouseX, mouseY);
            if (!clicked && this.button.mousePressed(mc, mouseX, mouseY)) {
                this.index++;
                if (this.index >= this.options.size()) {
                    this.index = 0;
                }
                this.value = Optional.of(get());
                this.button.displayString = name + ": " + I18n.format(this.toString());
                return true;
            }
            return clicked;
        }

        private E get() {
            return this.options.get(this.index);
        }

        @Override
        public String toString() {
            return this.value.transform(Enums.stringConverter(type).reverse()).orNull();
        }

        @Override
        public void fromString(String s) {
            value = Enums.getIfPresent(type, s);
            this.index = value.isPresent() ? value.get().ordinal() : 0;
            this.button.displayString = name + ": " + I18n.format(this.toString());
        }

    }

    private class Col extends Opt<Color> {

        private GuiColorButton color;

        private Converter<Color, Integer> colorConverter = new Converter<Color, Integer>() {
            @Override
            protected Color doBackward(Integer b) {
                return new Color(b);
            }

            @Override
            protected Integer doForward(Color a) {
                return a.getRGB();
            }
        };

        public Col(String name) {
            super(name);
        }

        @Override
        protected void init() {
            super.init();
            this.color = new GuiColorButton(mc, 0, optionPosX + 20, optionHeight, 20, 20, value.transform(colorConverter).or(-1), name);
        }

        @Override
        protected void drawOption(int mouseX, int mouseY) {
            super.drawOption(mouseX, mouseY);
            this.color.drawButton(mc, mouseX, mouseY);
        }

        @Override
        protected boolean mouseClicked(int mouseX, int mouseY) {
            boolean clicked = super.mouseClicked(mouseX, mouseY);
            if (!clicked && this.color.mousePressed(mc, mouseX, mouseY)) {
                this.value = Optional.of(new Color(this.color.getColor()));
                return true;
            }
            return clicked;
        }

        @Override
        protected void mouseReleased(int mouseX, int mouseY) {
            this.color.mouseReleased(mouseX, mouseY);
        }

        @Override
        public String toString() {
            return this.value.transform(Functions.toStringFunction()).orNull();
        }

        @Override
        public void fromString(String s) {
            this.value = Optional.fromNullable(Ints.tryParse(s)).transform(colorConverter.reverse());
        }
    }
}
