package tauri.dev.jsg.gui.element.tabs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import tauri.dev.jsg.JSG;
import tauri.dev.jsg.beamer.BeamerModeEnum;
import tauri.dev.jsg.gui.element.GuiHelper;
import tauri.dev.jsg.packet.BeamerChangedInactivityToServer;
import tauri.dev.jsg.packet.BeamerChangedLevelsToServer;
import tauri.dev.jsg.packet.ChangeRedstoneModeToServer;
import tauri.dev.jsg.packet.JSGPacketHandler;
import tauri.dev.jsg.tileentity.util.RedstoneModeEnum;

import java.util.ArrayList;
import java.util.List;

public class TabRedstone extends Tab {

    private final List<LabeledTextBox> textBoxes = new ArrayList<>();
    private final RedstoneModeGetter modeGetter;
    private final BeamerModeGetter beamerModeGetter;
    private final BlockPos pos;
    private final GuiButton saveButton;
    private boolean invalid;

    protected TabRedstone(TabRedstoneBuilder builder) {
        super(builder);
        this.modeGetter = builder.modeGetter;
        this.beamerModeGetter = builder.beamerModeGetter;
        this.pos = builder.pos;

        textBoxes.add(new LabeledTextBox(0, defaultY + 52, builder.fontRenderer, "gui.beamer.activate_below"));
        textBoxes.add(new LabeledTextBox(0, defaultY + 52 + 33, builder.fontRenderer, "gui.beamer.deactivate_over"));
        textBoxes.add(new LabeledTextBox(0, defaultY + 52, builder.fontRenderer, "gui.beamer.deactivate", "gui.beamer.after_inactivity"));

        saveButton = new GuiButton(0, 0, guiTop + defaultY + 102, 35, 20, I18n.format("gui.beamer.save"));
    }

    public void setText(int index, int number) {
        textBoxes.get(index).setText(number);
    }

    private List<LabeledTextBox> getTextBoxes(BeamerModeEnum beamerMode) {
        if (beamerMode == BeamerModeEnum.ITEMS)
            return textBoxes.subList(2, 3);
        else if (beamerMode != BeamerModeEnum.NONE)
            return textBoxes.subList(0, 2);

        return new ArrayList<>();
    }

    @Override
    public void render(FontRenderer fontRenderer, int mouseX, int mouseY) {
        super.render(fontRenderer, mouseX, mouseY);

        Minecraft mc = Minecraft.getMinecraft();
        mc.renderEngine.bindTexture(bgTexLocation);

        GlStateManager.enableBlend();
        GlStateManager.color(1, 1, 1, 1);
        RedstoneModeEnum currentMode = modeGetter.get();

        for (int i = 0; i < RedstoneModeEnum.values().length; i++) {
            RedstoneModeEnum redstoneMode = RedstoneModeEnum.values()[i];

            ButtonXY b = getButtonXY(i);

            boolean active = (redstoneMode == currentMode);
            active |= GuiHelper.isPointInRegion(b.x, b.y, 20, 20, mouseX, mouseY);

            int yTex = (active ? 211 + 20 : 211);

            // draw btn background
            Gui.drawModalRectWithCustomSizedTexture(b.x, b.y, 128, yTex, 20, 20, textureSize, textureSize);

            // draw btn icon
            Gui.drawModalRectWithCustomSizedTexture(b.x, b.y, 148 + 20 * i, 211, 20, 20, textureSize, textureSize);
        }

        if (currentMode == RedstoneModeEnum.AUTO) {
            for (LabeledTextBox textBox : getTextBoxes(beamerModeGetter.get()))
                textBox.draw();

            saveButton.x = guiLeft + currentOffsetX + 90;
            saveButton.drawButton(mc, mouseX, mouseY, 0);
            int y = guiTop + defaultY + 68;

            if (beamerModeGetter.get() == BeamerModeEnum.ITEMS)
                y += 10;

            if (invalid) {
                fontRenderer.drawString(TextFormatting.DARK_RED + I18n.format("gui.beamer.invalid"), guiLeft + currentOffsetX + 90, y, 0x00FFFFFF);
            }
        }

        GlStateManager.disableBlend();
    }


    @Override
    public void renderFg(GuiScreen screen, FontRenderer fontRenderer, int mouseX, int mouseY) {
        if (isOpen()) {
            for (int i = 0; i < RedstoneModeEnum.values().length; i++) {
                ButtonXY b = getButtonXY(i);

                if (GuiHelper.isPointInRegion(b.x, b.y, 20, 20, mouseX, mouseY)) {
                    RedstoneModeEnum mode = RedstoneModeEnum.valueOf(i);
                    String text = I18n.format(mode.translationKey);

                    if (mode == RedstoneModeEnum.IGNORED && JSG.ocWrapper.isModLoaded())
                        text += " (OpenComputers)";

                    screen.drawHoveringText(text, mouseX - guiLeft, mouseY - guiTop);
                }
            }
        }

        super.renderFg(screen, fontRenderer, mouseX, mouseY);
    }

    private ButtonXY getButtonXY(int i) {
        int x = guiLeft + currentOffsetX + 33 + 23 * i;
        int y = guiTop + defaultY + 23;

        return new ButtonXY(x, y);
    }

    private static class ButtonXY {
        public final int x;
        public final int y;

        public ButtonXY(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private class LabeledTextBox {

        private final int y;
        private final List<String> text;
        private final GuiTextField textField;
        private final FontRenderer fontRenderer;

        public LabeledTextBox(int id, int y, FontRenderer fontRenderer, String... translationKey) {
            this.y = guiTop + y;
            this.fontRenderer = fontRenderer;
            this.text = new ArrayList<>();

            for (String tr : translationKey)
                this.text.add(I18n.format(tr));

            this.textField = new GuiTextField(id, fontRenderer, 0, this.y + 11 * translationKey.length, 50, 16);
        }

        public LabeledTextBox setText(int number) {
            this.textField.setText("" + number);
            return this;
        }

        public void draw() {
            textField.x = guiLeft + currentOffsetX + 31;

            int y = this.y;
            for (String text : this.text) {
                fontRenderer.drawString(text, guiLeft + currentOffsetX + 29, y, 4210752);
                y += 11;
            }

            textField.drawTextBox();
        }
    }

    public boolean keyTyped(char typedChar, int keyCode) {
        boolean typed = false;
        for (LabeledTextBox tf : getTextBoxes(beamerModeGetter.get())) {
            if (tf.textField.textboxKeyTyped(typedChar, keyCode))
                typed = true;
        }
        return typed;
    }

    public void updateScreen() {
        for (LabeledTextBox tf : getTextBoxes(beamerModeGetter.get()))
            tf.textField.updateCursorCounter();
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!isOpen())
            return;

        RedstoneModeEnum currentMode = modeGetter.get();

        for (LabeledTextBox tf : getTextBoxes(beamerModeGetter.get()))
            tf.textField.mouseClicked(mouseX, mouseY, mouseButton);

        for (int i = 0; i < RedstoneModeEnum.values().length; i++) {
            ButtonXY b = getButtonXY(i);

            if (GuiHelper.isPointInRegion(b.x, b.y, 20, 20, mouseX, mouseY)) {
                JSGPacketHandler.INSTANCE.sendToServer(new ChangeRedstoneModeToServer(pos, RedstoneModeEnum.valueOf(i)));
                break;
            }
        }

        if (currentMode == RedstoneModeEnum.AUTO && saveButton.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
            switch (beamerModeGetter.get()) {
                case POWER:
                case FLUID:
                    int start;
                    int stop;

                    try {
                        start = Integer.parseInt(textBoxes.get(0).textField.getText());
                        stop = Integer.parseInt(textBoxes.get(1).textField.getText());

                        if (start < 0 || stop > 100 || start >= stop)
                            throw new NumberFormatException();

                        JSGPacketHandler.INSTANCE.sendToServer(new BeamerChangedLevelsToServer(pos, start, stop));
                        invalid = false;
                    } catch (NumberFormatException e) {
                        invalid = true;
                    }

                    break;

                case ITEMS:
                    int inactivity;

                    try {
                        inactivity = Integer.parseInt(textBoxes.get(2).textField.getText());

                        if (inactivity < 0)
                            throw new NumberFormatException();

                        JSGPacketHandler.INSTANCE.sendToServer(new BeamerChangedInactivityToServer(pos, inactivity));
                        invalid = false;
                    } catch (NumberFormatException e) {
                        invalid = true;
                    }

                    break;

                default:
                    break;
            }
        }
    }


    // ------------------------------------------------------------------------------------------------
    // Builder

    public static TabRedstoneBuilder builder() {
        return new TabRedstoneBuilder();
    }

    public static class TabRedstoneBuilder extends TabBuilder {

        private FontRenderer fontRenderer;
        private RedstoneModeGetter modeGetter;
        private BeamerModeGetter beamerModeGetter;
        private BlockPos pos;

        public TabRedstoneBuilder setFontRenderer(FontRenderer fontRenderer) {
            this.fontRenderer = fontRenderer;

            return this;
        }

        public TabRedstoneBuilder setRedstoneModeGetter(RedstoneModeGetter modeGetter) {
            this.modeGetter = modeGetter;

            return this;
        }

        public TabRedstoneBuilder setBeamerModeGetter(BeamerModeGetter beamerModeGetter) {
            this.beamerModeGetter = beamerModeGetter;

            return this;
        }

        public TabRedstoneBuilder setBlockPos(BlockPos pos) {
            this.pos = pos;

            return this;
        }

        @Override
        public TabRedstone build() {
            return new TabRedstone(this);
        }
    }

    public interface RedstoneModeGetter {
        RedstoneModeEnum get();
    }

    public interface BeamerModeGetter {
        BeamerModeEnum get();
    }
}
