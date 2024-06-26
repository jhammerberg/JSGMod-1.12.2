package tauri.dev.jsg.packet.stargate;

import io.netty.buffer.ByteBuf;
import tauri.dev.jsg.advancements.JSGAdvancements;
import tauri.dev.jsg.packet.PositionedPacket;
import tauri.dev.jsg.stargate.EnumStargateState;
import tauri.dev.jsg.stargate.StargateClosedReasonEnum;
import tauri.dev.jsg.stargate.StargateOpenResult;
import tauri.dev.jsg.stargate.network.SymbolMilkyWayEnum;
import tauri.dev.jsg.tileentity.dialhomedevice.DHDMilkyWayTile;
import tauri.dev.jsg.tileentity.stargate.StargateMilkyWayBaseTile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.items.CapabilityItemHandler;

public class DHDButtonClickedToServer extends PositionedPacket {
	public DHDButtonClickedToServer() {}
	
	public SymbolMilkyWayEnum symbol;
	
	public DHDButtonClickedToServer(BlockPos pos, SymbolMilkyWayEnum symbol) {
		super(pos);
		this.symbol = symbol;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		
		buf.writeInt(symbol.id);
	}

	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		
		symbol = SymbolMilkyWayEnum.valueOf(buf.readInt());
	}
	
	
	public static class DHDButtonClickedServerHandler implements IMessageHandler<DHDButtonClickedToServer, IMessage> {
		
		@Override
		public IMessage onMessage(DHDButtonClickedToServer message, MessageContext ctx) {

			EntityPlayerMP player = ctx.getServerHandler().player;
			WorldServer world = player.getServerWorld();
			if(world.getTileEntity(message.pos) instanceof DHDMilkyWayTile) {

				world.addScheduledTask(() -> {
					DHDMilkyWayTile dhdMilkyWayTile = (DHDMilkyWayTile) world.getTileEntity(message.pos);

					if (dhdMilkyWayTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).getStackInSlot(0).isEmpty()) {
						player.sendStatusMessage(new TextComponentTranslation("tile.jsg.dhd_block.no_crystal_warn"), true);
						return;
					}

					if (!dhdMilkyWayTile.isLinked()) {
						player.sendStatusMessage(new TextComponentTranslation("tile.jsg.dhd_block.not_linked_warn"), true);
						return;
					}

					StargateMilkyWayBaseTile gateTile = (StargateMilkyWayBaseTile) dhdMilkyWayTile.getLinkedGate(world);
					EnumStargateState gateState = gateTile.getStargateState();

					if (gateState.engaged() && message.symbol.brb()) {
						// Gate is open, BRB was press, possible closure attempt

						if (gateState.initiating())
							gateTile.attemptClose(StargateClosedReasonEnum.REQUESTED);
						else
							player.sendStatusMessage(new TextComponentTranslation("tile.jsg.dhd_block.incoming_wormhole_warn"), true);
					} else if (gateState.idle()) {
						// Gate is idle, some glyph was pressed

						if (message.symbol.brb()) {
							// BRB pressed on idling gate, attempt to open

							StargateOpenResult openResult = gateTile.attemptOpenAndFail();

							if(openResult.ok())
								JSGAdvancements.CHEVRON_SEVEN_LOCKED.trigger(player);

							if (openResult == StargateOpenResult.NOT_ENOUGH_POWER) {
								player.sendStatusMessage(new TextComponentTranslation("tile.jsg.dhd_block.not_enough_power"), true);
							}
						} else if (gateTile.canAddSymbol(message.symbol)) {
							// Not BRB, some other glyph pressed on idling gate, we can add this symbol now

							gateTile.addSymbolToAddressDHD(message.symbol);
						}
					}


				});
			}
			return null;
		}
	}
}
