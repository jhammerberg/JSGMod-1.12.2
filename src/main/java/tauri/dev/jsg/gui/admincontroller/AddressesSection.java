package tauri.dev.jsg.gui.admincontroller;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.client.config.GuiUtils;
import tauri.dev.jsg.JSG;
import tauri.dev.jsg.gui.base.JSGTextField;
import tauri.dev.jsg.gui.element.ArrowButton;
import tauri.dev.jsg.gui.element.GuiHelper;
import tauri.dev.jsg.item.JSGItems;
import tauri.dev.jsg.packet.JSGPacketHandler;
import tauri.dev.jsg.packet.gui.entry.EntryActionToServer;
import tauri.dev.jsg.stargate.network.StargateAddress;
import tauri.dev.jsg.stargate.network.StargateAddressDynamic;
import tauri.dev.jsg.stargate.network.StargatePos;
import tauri.dev.jsg.tileentity.stargate.StargateAbstractBaseTile;
import tauri.dev.jsg.util.BlockHelpers;

import javax.annotation.Nonnull;
import java.util.*;

import static tauri.dev.jsg.gui.admincontroller.GuiAdminController.lastStargateNetwork;

public class AddressesSection {

    public ArrayList<StargateEntry> ENTRIES = new ArrayList<>();

    public int guiTop;
    public int height;
    public int guiLeft;
    public int width;

    public static final int OFFSET = 15;

    public int scrolled = 0;
    public GuiAdminController guiBase;

    public AddressesSection(GuiAdminController baseGui) {
        this.guiBase = baseGui;
    }

    public void generateAddressEntries() {
        ENTRIES.clear();
        if (lastStargateNetwork == null) return;

        Map<StargateAddress, StargatePos> m = lastStargateNetwork.getMap().get(Objects.requireNonNull(guiBase.gateTile).getSymbolType());
        for (StargateAddress a : m.keySet()) {
            StargateEntry e = new StargateEntry();
            e.pos = m.get(a);
            e.address = a;
            ENTRIES.add(e);
        }
    }

    public ArrayList<ArrowButton> dialButtons = new ArrayList<>();
    public ArrayList<GuiTextField> entriesTextFields = new ArrayList<>();

    public void generateAddressEntriesBoxes() {
        int index = -1;
        dialButtons.clear();
        entriesTextFields.clear();
        for (StargateEntry e : ENTRIES) {
            index++;
            StargatePos p = e.pos;
            String name = (p.name != null && !Objects.equals(p.name, "") ? p.name : BlockHelpers.blockPosToBetterString(p.gatePos));
            final int finalIndex = index;
            GuiTextField field = new JSGTextField(index, Minecraft.getMinecraft().fontRenderer, guiLeft, 0, 120, 20, name).setActionCallback(() -> renameEntry(finalIndex));
            ArrowButton btn = (ArrowButton) new ArrowButton(index, guiLeft + 120 + 5, 0, ArrowButton.ArrowType.RIGHT).setFgColor(GuiUtils.getColorCode('a', true)).setActionCallback(() -> dialGate(finalIndex));
            if (e.pos.gatePos.equals(Objects.requireNonNull(guiBase.gateTile).getPos()) && e.pos.dimensionID == guiBase.gateTile.world().provider.getDimension()) {
                btn.setEnabled(false);
                btn.setActionCallback(() -> {
                });
            }

            entriesTextFields.add(field);
            dialButtons.add(btn);
        }
    }

    @Nonnull
    public EnumHand getHand() {
        EnumHand hand = EnumHand.MAIN_HAND;
        ItemStack stack = guiBase.player.getHeldItem(hand);
        if (stack.getItem() != JSGItems.ADMIN_CONTROLLER) {
            hand = EnumHand.OFF_HAND;
            stack = guiBase.player.getHeldItem(hand);
            if (stack.getItem() != JSGItems.ADMIN_CONTROLLER) return EnumHand.MAIN_HAND;
        }
        return hand;
    }

    public void dialGate(int index) {
        try {
            EnumHand hand = getHand();
            StargateEntry entry = ENTRIES.get(index);
            StargatePos pos = entry.pos;
            StargateAbstractBaseTile tile = pos.getTileEntity();
            if (!tile.getStargateState().idle()) return;

            int symbolsCount = Objects.requireNonNull(guiBase.gateTile).getMinimalSymbolsToDial(pos.getGateSymbolType(), pos);

            JSGPacketHandler.INSTANCE.sendToServer(new EntryActionToServer(hand, new StargateAddressDynamic(entry.address), symbolsCount, guiBase.gateTile.getPos()));
        } catch (Exception e) {
            JSG.error(e);
        }
    }

    public void renameEntry(int index) {
        try {
            EnumHand hand = getHand();
            GuiTextField field = entriesTextFields.get(index);
            JSGPacketHandler.INSTANCE.sendToServer(new EntryActionToServer(hand, field.getText(), Objects.requireNonNull(guiBase.gateTile).getPos()));
        } catch (Exception e) {
            JSG.error(e);
        }
    }

    public void updateY() {
        for (GuiTextField f : entriesTextFields) {
            f.y = (scrolled + (f.getId() * 23)) + OFFSET + guiTop;
        }
        for (ArrowButton f : dialButtons) {
            f.y = (scrolled + (f.id * 23)) + OFFSET + guiTop;
        }
    }

    public void renderEntries() {
        updateY();

        Gui.drawRect(guiLeft, guiTop, width, height, 0x7C7C7C);
        GlStateManager.color(1, 1, 1, 1);

        GlStateManager.pushMatrix();
        for (GuiTextField f : entriesTextFields) {
            if (canNotRenderEntry(f.y)) continue;
            f.drawTextBox();
        }
        for (ArrowButton b : dialButtons) {
            if (canNotRenderEntry(b.y)) continue;
            b.drawButton(Minecraft.getMinecraft(), guiBase.mouseX, guiBase.mouseY, guiBase.partialTicks);
        }
        GlStateManager.popMatrix();
    }

    public void renderFg() {
        // Render tooltips
        for (GuiTextField f : entriesTextFields) {
            StargateEntry e = ENTRIES.get(f.getId());
            if (!canNotRenderEntry(f.y) && GuiHelper.isPointInRegion(f.x, f.y, f.width, f.height, guiBase.mouseX, guiBase.mouseY)) {
                guiBase.drawHoveringText(Arrays.asList(
                        "Type: " + e.pos.getGateSymbolType().toString(),
                        "Pos: " + e.pos.gatePos.toString(),
                        "Dim: " + e.pos.dimensionID + " (" + DimensionManager.getProviderType(e.pos.dimensionID).getName() + ")"
                ), guiBase.mouseX, guiBase.mouseY);
            }
        }
        for (ArrowButton f : dialButtons) {
            if (!canNotRenderEntry(f.y) && f.enabled && GuiHelper.isPointInRegion(f.x, f.y, f.width, f.height, guiBase.mouseX, guiBase.mouseY)) {
                guiBase.drawHoveringText(Collections.singletonList("Dial this address"), guiBase.mouseX, guiBase.mouseY);
            }
        }
    }

    // ----------------------------------------------------------
    protected static final int SCROLL_AMOUNT = 5;

    public void scroll(int k) {
        if (k == 0) return;
        if (k < 0) k = -1;
        if (k > 0) k = 1;
        if (canContinueScrolling(k)) {
            scrolled += (SCROLL_AMOUNT * k);
        }
    }

    public boolean canContinueScrolling(int k) {
        int top = guiTop + OFFSET;
        int bottom = guiTop + height - OFFSET;
        if (entriesTextFields.size() < 1 && dialButtons.size() < 1) return false;

        boolean isTop = ((entriesTextFields.size() > 0 && entriesTextFields.get(0).getId() < dialButtons.get(0).id) ? entriesTextFields.get(0).y > top : dialButtons.get(0).y > top);
        boolean isBottom = ((entriesTextFields.size() > 0 && entriesTextFields.get(entriesTextFields.size() - 1).getId() >= dialButtons.get(dialButtons.size() - 1).id) ? entriesTextFields.get(entriesTextFields.size() - 1).y < bottom : dialButtons.get(dialButtons.size() - 1).y < bottom);

        return (!isTop && k == 1) || (!isBottom && k == -1);
    }

    public boolean canNotRenderEntry(int y) {
        int top = guiTop + OFFSET;
        int bottom = guiTop + height - OFFSET;
        int height = 23;
        return y < top || (y + height) > bottom;
    }
}
