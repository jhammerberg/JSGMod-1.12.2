package mrjake.aunis.config.ingame;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.gui.element.NumberOnlyTextField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.nbt.NBTTagCompound;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AunisConfigOption {
    public int id;
    public List<String> comment = new ArrayList<>();
    public AunisConfigOptionTypeEnum type = AunisConfigOptionTypeEnum.TEXT;
    public String value = "";
    private String label = "";

    public AunisConfigOption(int id) {
        this.id = id;
    }

    public AunisConfigOption(NBTTagCompound compound) {
        this.deserializeNBT(compound);
    }

    public AunisConfigOption(ByteBuf buf) {
        this.fromBytes(buf);
    }

    public GuiTextField createField(int y) {
        int componentId = id + 100;
        GuiTextField field = null;


        if (this.type == AunisConfigOptionTypeEnum.TEXT)
            field = new GuiTextField(componentId, Minecraft.getMinecraft().fontRenderer, -25, y, 90, 15);
        else if (this.type == AunisConfigOptionTypeEnum.NUMBER)
            field = new NumberOnlyTextField(componentId, Minecraft.getMinecraft().fontRenderer, -25, y, 90, 15);
        else if (this.type == AunisConfigOptionTypeEnum.BOOLEAN)
            field = new GuiTextField(componentId, Minecraft.getMinecraft().fontRenderer, -25, y, 90, 15);


        if(field != null)
            field.setText(this.value);
        return field;
    }

    public String getLabel() {
        return label;
    }

    public AunisConfigOption setLabel(String label) {
        this.label = label;
        return this;
    }

    public List<String> getComment() {
        return comment;
    }

    public AunisConfigOption setComment(String... comment) {
        this.comment = Arrays.asList(comment);
        return this;
    }

    public AunisConfigOption setType(AunisConfigOptionTypeEnum type) {
        this.type = type;
        return this;
    }

    public AunisConfigOption setValue(String value) {
        if (this.type == AunisConfigOptionTypeEnum.NUMBER)
            return this.setIntValue(value);
        else if (this.type == AunisConfigOptionTypeEnum.BOOLEAN)
            return this.setBooleanValue(value);
        return this.setStringValue(value);
    }

    public boolean getBooleanValue() {
        if (this.value == null) return false;
        return this.value.equals("true");
    }

    private AunisConfigOption setBooleanValue(String value) {
        if (value.equals("true"))
            this.value = "true";
        else
            this.value = "false";
        return this;
    }

    public int getIntValue() {
        if (this.value == null) return -1;
        try {
            return Integer.parseInt(this.value);
        } catch (Exception e) {
            return -1;
        }
    }

    private AunisConfigOption setIntValue(String value) {
        try {
            this.value = Integer.parseInt(value) + "";
        } catch (Exception ignored) {
        }
        return this;
    }

    public String getStringValue() {
        return this.value;
    }

    private AunisConfigOption setStringValue(String value) {
        this.value = value;
        return this;
    }

    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("id", id);
        compound.setString("label", label);
        compound.setInteger("commentSize", comment.size());
        for (int i = 0; i < comment.size(); i++) {
            compound.setString("comment" + i, comment.get(i));
        }
        compound.setInteger("type", type.id);
        compound.setString("value", value);

        return compound;
    }

    public void deserializeNBT(NBTTagCompound compound) {
        this.id = compound.getInteger("id");
        this.label = compound.getString("label");
        int size = compound.getInteger("commentSize");
        comment.clear();
        for (int i = 0; i < size; i++) {
            comment.add(compound.getString("comment" + i));
        }
        this.type = AunisConfigOptionTypeEnum.byId(compound.getInteger("type"));
        this.value = compound.getString("value");
    }

    public void toBytes(ByteBuf buf) {
        buf.writeInt(id);
        buf.writeInt(label.length());
        buf.writeCharSequence(label, StandardCharsets.UTF_8);
        buf.writeInt(comment.size());
        for (String com : comment) {
            buf.writeInt(com.length());
            buf.writeCharSequence(com, StandardCharsets.UTF_8);
        }
        buf.writeInt(type.id);
        buf.writeInt(value.length());
        buf.writeCharSequence(value, StandardCharsets.UTF_8);
    }

    public void fromBytes(ByteBuf buf) {
        this.id = buf.readInt();
        int labelSize = buf.readInt();
        this.label = buf.readCharSequence(labelSize, StandardCharsets.UTF_8).toString();
        int commentsSize = buf.readInt();
        comment.clear();
        for (int i = 0; i < commentsSize; i++) {
            int x = buf.readInt();
            comment.add(buf.readCharSequence(x, StandardCharsets.UTF_8).toString());
        }
        this.type = AunisConfigOptionTypeEnum.byId(buf.readInt());
        int valueSize = buf.readInt();
        this.value = buf.readCharSequence(valueSize, StandardCharsets.UTF_8).toString();
    }
}