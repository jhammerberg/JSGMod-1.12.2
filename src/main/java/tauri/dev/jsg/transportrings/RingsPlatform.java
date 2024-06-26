package tauri.dev.jsg.transportrings;

import tauri.dev.jsg.block.props.TRPlatformBlock;
import net.minecraft.util.math.BlockPos;

public class RingsPlatform {
    public int x;
    public int y;
    public int z;

    public TRPlatformBlock platformBlock;

    public RingsPlatform(BlockPos pos, TRPlatformBlock platformBlock){
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();

        this.platformBlock = platformBlock;
    }
}
