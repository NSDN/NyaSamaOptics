package club.nsdn.nyasamaoptics.Util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.item.ItemEditableBook;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.nbt.NBTTagList;
import org.lwjgl.opengl.Display;

/**
 * Created by drzzm32 on 2017.1.6.
 */
public class Util {

    public static NBTTagList getTagListFromNGT(ItemStack itemStack) {
        if (itemStack == null) return null;
        if (itemStack.getItem() instanceof ItemWritableBook &&
                itemStack.getItem().getClass().getSimpleName().equals("ItemNGT")) {
            if (!itemStack.hasTagCompound()) return null;
            return itemStack.getTagCompound().getTagList("pages", 8);
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    public static void setTitle() {
        String prevTitle = Display.getTitle();
        if (!prevTitle.contains("NSDN-MC")) {
            Display.setTitle(prevTitle + " | using mods by NSDN-MC");
        }
    }

}
