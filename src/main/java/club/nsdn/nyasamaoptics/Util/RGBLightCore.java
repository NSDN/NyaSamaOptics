package club.nsdn.nyasamaoptics.Util;

import club.nsdn.nyasamaoptics.TileEntities.RGBLight;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentTranslation;

import java.util.LinkedHashMap;

/**
 * Created by drzzm32 on 2016.10.25.
 */
public abstract class RGBLightCore extends NSASM {

    public RGBLightCore(String[][] code) {
        super(code);
    }

    public void setColor(RGBLight.TileLight light, EntityPlayer player, int value) {
        light.color = value & 0xFFFFFF;
        RGBLight.TileLight.updateTileEntity(light);
        player.addChatComponentMessage(
                new ChatComponentTranslation("info.light.color", Integer.toHexString(light.color).toUpperCase()));
    }

    @Override
    public void loadFunc(LinkedHashMap<String, Operator> funcList) {
        funcList.put("clr", ((dst, src) -> {
            if (src != null) return Result.ERR;
            if (dst == null) return Result.ERR;

            if (dst.type == RegType.INT) {
                setColor(getLight(), getPlayer(), (int) dst.data);
                return Result.OK;
            }
            return Result.ERR;
        }));
    }

    public abstract RGBLight.TileLight getLight();

}
