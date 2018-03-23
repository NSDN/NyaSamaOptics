package club.nsdn.nyasamaoptics.util;

import club.nsdn.nyasamaoptics.network.NetworkWrapper;
import club.nsdn.nyasamaoptics.tileblock.screen.TilePlatformPlate;
import club.nsdn.nyasamatelecom.api.util.NSASM;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;

import java.util.LinkedHashMap;

/**
 * Created by drzzm32 on 2017.3.23.
 */
public abstract class PlatformPlateCore extends NSASM {

    public PlatformPlateCore(String[][] code) {
        super(code);
    }

    @Override
    public SimpleNetworkWrapper getWrapper() {
        return NetworkWrapper.instance;
    }

    public void setContent(TilePlatformPlate text, String value) {
        text.content = value;
        TilePlatformPlate.updateThis(text);
    }

    public void setColor(TilePlatformPlate text, int value) {
        text.color = value & 0xFFFFFF;
        TilePlatformPlate.updateThis(text);
    }

    public void setScale(TilePlatformPlate text, float value) {
        text.scale = value;
        TilePlatformPlate.updateThis(text);
    }

    public void setAlign(TilePlatformPlate text, String value) {
        if (value.toLowerCase().equals("center"))
            text.align = TilePlatformPlate.ALIGN_CENTER;
        else if (value.toLowerCase().equals("left"))
            text.align = TilePlatformPlate.ALIGN_LEFT;
        else if (value.toLowerCase().equals("right"))
            text.align = TilePlatformPlate.ALIGN_RIGHT;
        else
            text.align = TilePlatformPlate.ALIGN_CENTER;
        TilePlatformPlate.updateThis(text);
    }

    @Override
    public void loadFunc(LinkedHashMap<String, Operator> funcList) {
        funcList.put("show", ((dst, src) -> {
            if (src != null) return Result.ERR;
            if (dst == null) return Result.ERR;

            if (dst.type == RegType.STR) {
                setContent(getTile(), dst.data.toString());
                return Result.OK;
            }
            return Result.ERR;
        }));
        funcList.put("clr", ((dst, src) -> {
            if (src != null) return Result.ERR;
            if (dst == null) return Result.ERR;

            if (dst.type == RegType.INT) {
                setColor(getTile(), (int) dst.data);
                return Result.OK;
            }
            return Result.ERR;
        }));
        funcList.put("scl", ((dst, src) -> {
            if (src != null) return Result.ERR;
            if (dst == null) return Result.ERR;

            if (dst.type != RegType.STR) {
                if (dst.type == RegType.FLOAT)
                    setScale(getTile(), (float) dst.data);
                else if (dst.type == RegType.INT)
                    setScale(getTile(), (int) dst.data);
                return Result.OK;
            }
            return Result.ERR;
        }));
        funcList.put("aln", ((dst, src) -> {
            if (src != null) return Result.ERR;
            if (dst == null) return Result.ERR;

            if (dst.type == RegType.STR) {
                setAlign(getTile(), dst.data.toString());
                return Result.OK;
            }
            return Result.ERR;
        }));
    }

    public abstract TilePlatformPlate getTile();

}
