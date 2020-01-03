package club.nsdn.nyasamaoptics.util;

import club.nsdn.nyasamaoptics.network.NetworkWrapper;
import club.nsdn.nyasamaoptics.tileblock.screen.StationLamp;
import club.nsdn.nyasamatelecom.api.util.NSASM;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

import java.util.LinkedHashMap;

/**
 * Created by drzzm32 on 2019.1.30.
 */
public abstract class StationLampCore extends NSASM {

    public StationLampCore(String[][] code) {
        super(code);
    }

    @Override
    public SimpleNetworkWrapper getWrapper() {
        return NetworkWrapper.instance;
    }

    private StationLamp.TileEntityStationLamp.LampInfo buffer = StationLamp.TileEntityStationLamp.LampInfo.def();

    public void setContent(StationLamp.TileEntityStationLamp lamp, String value) {
        buffer.content = value;
        StationLamp.TileEntityStationLamp.updateThis(lamp);
    }

    public void setColor(StationLamp.TileEntityStationLamp lamp, int value) {
        buffer.color = value & 0xFFFFFF;
        StationLamp.TileEntityStationLamp.updateThis(lamp);
    }

    public void setBack(StationLamp.TileEntityStationLamp lamp, int value) {
        buffer.back = value;
        StationLamp.TileEntityStationLamp.updateThis(lamp);
    }

    public void setScale(StationLamp.TileEntityStationLamp lamp, float value) {
        buffer.scale = value;
        StationLamp.TileEntityStationLamp.updateThis(lamp);
    }

    public void setLogo(StationLamp.TileEntityStationLamp lamp, String value) {
        lamp.logo = value;
        StationLamp.TileEntityStationLamp.updateThis(lamp);
    }

    @Override
    public void loadFunc(LinkedHashMap<String, Operator> funcList) {
        funcList.put("inj", ((dst, src) -> {
            if (src != null) return Result.ERR;
            if (dst == null) {
                getTile().lampInfo.add(buffer);
                buffer = StationLamp.TileEntityStationLamp.LampInfo.def();
                getTile().refresh();
                return Result.OK;
            } else if (dst.type == RegType.INT) {
                int index = (int) dst.data;
                if (index < 0 || index > getTile().lampInfo.size())
                    return Result.ERR;
                getTile().lampInfo.set(index, buffer);
                buffer = StationLamp.TileEntityStationLamp.LampInfo.def();
                getTile().refresh();
                return Result.OK;
            }
            return Result.ERR;
        }));
        funcList.put("rej", ((dst, src) -> {
            if (src != null) return Result.ERR;
            if (dst == null) {
                getTile().lampInfo.removeLast();
                buffer = StationLamp.TileEntityStationLamp.LampInfo.def();
                if (getTile().lampInfo.isEmpty())
                    getTile().lampInfo.add(buffer);
                getTile().refresh();
                return Result.OK;
            } else if (dst.type == RegType.INT) {
                int index = (int) dst.data;
                if (index < 0 || index > getTile().lampInfo.size())
                    return Result.ERR;
                getTile().lampInfo.remove(index);
                buffer = StationLamp.TileEntityStationLamp.LampInfo.def();
                if (getTile().lampInfo.isEmpty())
                    getTile().lampInfo.add(buffer);
                getTile().refresh();
                return Result.OK;
            }
            return Result.ERR;
        }));
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
                getPlayer().sendMessage(new TextComponentString(
                    "[NSO] Logo -> " + getTile().logo
                ));
                return Result.OK;
            }
            return Result.ERR;
        }));
    }

    public abstract StationLamp.TileEntityStationLamp getTile();

}
