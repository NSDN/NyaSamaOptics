package club.nsdn.nyasamaoptics.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.nbt.NBTTagList;
import org.lwjgl.opengl.Display;

/**
 * Created by drzzm32 on 2017.1.6.
 */
public class Util {

    @SideOnly(Side.CLIENT)
    public static void setTitle() {
        String prevTitle = Display.getTitle();
        if (!prevTitle.contains("NSDN-MC")) {
            Display.setTitle(prevTitle + " | using mods by NSDN-MC");
        }
    }

}
