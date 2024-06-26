package tauri.dev.jsg.state.transportrings;

import io.netty.buffer.ByteBuf;
import tauri.dev.jsg.config.ingame.JSGTileEntityConfig;
import tauri.dev.jsg.renderer.transportrings.Ring;
import tauri.dev.jsg.state.State;
import tauri.dev.jsg.renderer.transportrings.TransportRingsAbstractRenderer;

import java.util.ArrayList;
import java.util.List;

public class TransportRingsRendererState extends State {
	public boolean isAnimationActive;
	public long animationStart;
	public boolean ringsUprising;
	public int ringsDistance;
	public JSGTileEntityConfig ringsConfig;
	public long lastTick;
	public int currentRing;
	public int lastRingAnimated;
	public List<Ring> rings = new ArrayList<>();
	
	public TransportRingsRendererState() {
		this.isAnimationActive = false;
		
		this.animationStart = 0;
		this.ringsUprising = true;
		this.ringsDistance = 2;
		this.currentRing = 0;
		this.lastRingAnimated = -1;
		this.lastTick = -1;
		for (int i = 0; i < TransportRingsAbstractRenderer.RING_COUNT; i++) {
			rings.add(new Ring(i));
		}
		this.ringsConfig = new JSGTileEntityConfig();
	}
	
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(isAnimationActive);
		buf.writeLong(animationStart);
		buf.writeBoolean(ringsUprising);
		buf.writeInt(ringsDistance);
		buf.writeLong(lastTick);
		buf.writeInt(currentRing);
		buf.writeInt(lastRingAnimated);

		buf.writeInt(rings.size());
		for(Ring ring : rings) {
			ring.toBytes(buf);
		}

		ringsConfig.toBytes(buf);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		rings = new ArrayList<>();
		isAnimationActive = buf.readBoolean();
		animationStart = buf.readLong();
		ringsUprising = buf.readBoolean();
		ringsDistance = buf.readInt();
		lastTick = buf.readLong();
		currentRing = buf.readInt();
		lastRingAnimated = buf.readInt();

		int size = buf.readInt();
		for(int i = 0; i < size; i++){
			rings.add(new Ring(buf));
		}

		ringsConfig.fromBytes(buf);
	}
}
