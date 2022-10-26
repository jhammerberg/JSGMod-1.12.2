package tauri.dev.jsg.item.mysterious;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import tauri.dev.jsg.JSG;
import tauri.dev.jsg.config.JSGConfig;
import tauri.dev.jsg.item.JSGItems;
import tauri.dev.jsg.item.notebook.PageNotebookItem;
import tauri.dev.jsg.stargate.network.SymbolTypeEnum;
import tauri.dev.jsg.util.main.loader.JSGCreativeTabsHandler;
import tauri.dev.jsg.worldgen.StargateGenerator;
import tauri.dev.jsg.worldgen.StargateGenerator.GeneratedStargate;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class AbstractPageMysteriousItem extends Item {
    public static final String BASE_NAME = "page_mysterious";
    protected final SymbolTypeEnum symbolType;
    protected final int dimensionToSpawn;

    public AbstractPageMysteriousItem(String typeName, SymbolTypeEnum symbolType, int dimensionToSpawn) {
        this.symbolType = symbolType;
        this.dimensionToSpawn = dimensionToSpawn;

        setRegistryName(JSG.MOD_ID + ":" + BASE_NAME + "_" + typeName);
        setUnlocalizedName(JSG.MOD_ID + "." + BASE_NAME + "_" + typeName);

        setCreativeTab(JSGCreativeTabsHandler.jsgItemsCreativeTab);
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, World worldIn, List<String> tooltip, @Nonnull ITooltipFlag flagIn) {
        tooltip.add(TextFormatting.ITALIC + JSG.proxy.localize("item.jsg.page_mysterious.tooltip"));
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, @Nonnull EntityPlayer player, @Nonnull EnumHand hand) {

        if (!world.isRemote) {
            GeneratedStargate stargate = StargateGenerator.generateStargate(world, symbolType, dimensionToSpawn);

            if (stargate != null) {
                NBTTagCompound compound = PageNotebookItem.getCompoundFromAddress(stargate.address, stargate.hasUpgrade, stargate.path);

                ItemStack stack = new ItemStack(JSGItems.PAGE_NOTEBOOK_ITEM, 1, 1);
                stack.setTagCompound(compound);

                ItemStack held = player.getHeldItem(hand);
                held.shrink(1);

                if (held.isEmpty())
                    player.setHeldItem(hand, stack);

                else {
                    player.setHeldItem(hand, held);
                    player.addItemStackToInventory(stack);
                }

                if (JSGConfig.mysteriousConfig.pageCooldown > 0)
                    player.getCooldownTracker().setCooldown(this, JSGConfig.mysteriousConfig.pageCooldown);
            }
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }
}
