package club.nsdn.nyasamaoptics.util;

import club.nsdn.nyasamaoptics.network.NetworkWrapper;
import club.nsdn.nyasamaoptics.tileblock.light.RGBLight;
import club.nsdn.nyasamatelecom.api.util.NSASM;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
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

    @Override
    public SimpleNetworkWrapper getWrapper() {
        return NetworkWrapper.instance;
    }

    public void setColor(RGBLight.TileLight light, EntityPlayer player, int value) {
        light.color = value & 0xFFFFFF;
        light.updateTileEntity(light);
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
