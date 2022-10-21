package tauri.dev.jsg.crafting.thermalreplace.ingots;

import tauri.dev.jsg.crafting.thermalreplace.ThermalAbstractRecipe;
import tauri.dev.jsg.item.JSGItems;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.HashMap;

public class IngotNaquadahAlloy extends ThermalAbstractRecipe {

    // CONFIGURATION
    public static final int MINIMAL_SLOTS = 4;
    public static final int MAXIMAL_SLOTS = 9;
    public static final String NAME = "thermal_ingot_naquadah_alloy";
    public static final ItemStack OUTPUT_ITEM = new ItemStack(JSGItems.NAQUADAH_ALLOY_RAW);

    // items / patterns
    public HashMap<ItemStack, int[]> PATTERN_LIST = new HashMap<ItemStack, int[]>() {
        {
            put(new ItemStack(JSGItems.NAQUADAH_SHARD), new int[]{
                    0, 1, 0,
                    1, 0, 0,
                    0, 0, 0
            });
            put(new ItemStack(Items.IRON_INGOT), new int[]{
                    1, 0, 0,
                    0, 0, 0,
                    0, 0, 0
            });
        }
    };

    // REGISTER / CONSTRUCTOR
    public IngotNaquadahAlloy() {
        super(NAME, MINIMAL_SLOTS, MAXIMAL_SLOTS, OUTPUT_ITEM);
        setPatterns(PATTERN_LIST);
    }
}