package tauri.dev.jsg.tileentity.transportrings;

import net.minecraft.entity.player.EntityPlayerMP;
import tauri.dev.jsg.advancements.JSGAdvancements;
import tauri.dev.jsg.block.transportrings.controller.TRControllerAbstractBlock;
import tauri.dev.jsg.transportrings.SymbolTypeTransportRingsEnum;

import static tauri.dev.jsg.block.JSGBlocks.TR_CONTROLLER_GOAULD_BLOCK;

public class TransportRingsOriTile extends TransportRingsAbstractTile {

    @Override
    public int getDefaultCapacitors() {
        return 2;
    }

    @Override
    public void triggerTeleportAdvancement(EntityPlayerMP playerIn) {
        JSGAdvancements.TR_ORI.trigger(playerIn);
    }

    @Override
    protected SymbolTypeTransportRingsEnum getSymbolTypeByThis() {
        return SymbolTypeTransportRingsEnum.ORI;
    }

    @Override
    public TRControllerAbstractBlock getControllerBlock() {
        //todo(Mine): When ori controller finished, switch this to it
        return TR_CONTROLLER_GOAULD_BLOCK;
    }
}
