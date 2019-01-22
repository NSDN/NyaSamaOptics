package club.nsdn.nyasamaoptics.util;

import club.nsdn.nyasamaoptics.network.NetworkWrapper;
import club.nsdn.nyasamaoptics.tileblock.screen.StationLamp;
import club.nsdn.nyasamatelecom.api.util.NSASM;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraft.util.ChatComponentText;

import java.util.LinkedHashMap;

/**
 * Created by drzzm32 on 2019.1.21.
 */
public abstract class StationLampCore extends NSASM {

    public StationLampCore(String[][] code) {
        super(code);
    }

    @Override
    public SimpleNetworkWrapper getWrapper() {
        return NetworkWrapper.instance;
    }

    public void setContent(StationLamp.TileLamp lamp, String value) {
        lamp.content = value;
        StationLamp.TileLamp.updateThis(lamp);
    }

    public void setColor(StationLamp.TileLamp lamp, int value) {
        lamp.color = value & 0xFFFFFF;
        StationLamp.TileLamp.updateThis(lamp);
    }

    public void setBack(StationLamp.TileLamp lamp, int value) {
        lamp.back = value;
        StationLamp.TileLamp.updateThis(lamp);
    }

    public void setScale(StationLamp.TileLamp lamp, float value) {
        lamp.scale = value;
        StationLamp.TileLamp.updateThis(lamp);
    }

    public void setLogo(StationLamp.TileLamp lamp, String value) {
        lamp.logo = value;
        StationLamp.TileLamp.updateThis(lamp);
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
        funcList.put("fore", ((dst, src) -> {
            if (src != null) return Result.ERR;
            if (dst == null) return Result.ERR;

            if (dst.type == RegType.INT) {
                setColor(getTile(), (int) dst.data);
                return Result.OK;
            }
            return Result.ERR;
        }));
        funcList.put("back", ((dst, src) -> {
            if (src != null) return Result.ERR;
            if (dst == null) return Result.ERR;

            if (dst.type == RegType.INT) {
                setBack(getTile(), (int) dst.data);
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
        funcList.put("logo", ((dst, src) -> {
            if (src != null) return Result.ERR;
            if (dst == null) return Result.ERR;

            if (dst.type == RegType.STR) {
                setLogo(getTile(), dst.data.toString());
                getPlayer().addChatComponentMessage(new ChatComponentText(
                    "[NSO] Logo -> " + getTile().logo
                ));
                return Result.OK;
            }
            return Result.ERR;
        }));
    }

    public abstract StationLamp.TileLamp getTile();

}
