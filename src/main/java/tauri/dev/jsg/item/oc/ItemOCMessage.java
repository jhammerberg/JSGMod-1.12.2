package tauri.dev.jsg.item.oc;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public class ItemOCMessage implements INBTSerializable<NBTTagCompound> {

	public String name;
	public String address;
	public short port;
	public String dataStr;
	
	public ItemOCMessage(String name, String address, short port, String dataStr) {
		this.name = name;
		this.address = address;
		this.port = port;
		this.dataStr = dataStr;
	}
	
	public ItemOCMessage(NBTTagCompound compound) {
		deserializeNBT(compound);
	}
	
	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		
		compound.setString("name", name);
		compound.setString("address", address);
		compound.setShort("port", port);
		compound.setString("data", dataStr);
		
		return compound;
	}

	@Override
	public void deserializeNBT(NBTTagCompound compound) {
		name = compound.getString("name");
		address = compound.getString("address");
		port = compound.getShort("port");
		dataStr = compound.getString("data");
	}
	
	public Object[] getData() {
		return dataStr.split(",");
	}
	
	@Override
	public String toString() {
		return String.format("{name=%s, addr='%s':%d, data='%s'}", name, address, port, dataStr);
	}
}
