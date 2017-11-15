package club.nsdn.nyasamaoptics.Util;

import club.nsdn.nyasamaoptics.TileEntities.PillarHead;
import club.nsdn.nyasamaoptics.TileEntities.RGBLight;
import club.nsdn.nyasamaoptics.Util.Font.FontLoader;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentTranslation;

import java.util.LinkedHashMap;

/**
 * Created by drzzm32 on 2017.11.2.
 */
public abstract class PillarHeadCore extends NSASM {

    public PillarHeadCore(String[][] code) {
        super(code);
    }

    public void setContent(PillarHead.TileText text, String value) {
        text.content = value;
        PillarHead.TileText.updateThis(text);
    }

    public void setColor(PillarHead.TileText text, int value) {
        text.color = value & 0xFFFFFF;
        PillarHead.TileText.updateThis(text);
    }

    public void setThick(PillarHead.TileText text, int value) {
        text.thick = value;
        PillarHead.TileText.updateThis(text);
    }

    public void setScale(PillarHead.TileText text, float value) {
        text.scale = value;
        PillarHead.TileText.updateThis(text);
    }

    public void setFont(PillarHead.TileText text, String value) {
        if (value.toLowerCase().equals("kai"))
            text.font = FontLoader.FONT_KAI;
        else if (value.toLowerCase().equals("hei"))
            text.font = FontLoader.FONT_HEI;
        else if (value.toLowerCase().equals("long"))
            text.font = FontLoader.FONT_LONG;
        else if (value.toLowerCase().equals("song"))
            text.font = FontLoader.FONT_SONG;
        else if (value.toLowerCase().equals("lishu"))
            text.font = FontLoader.FONT_LISHU;
        else
            text.font = FontLoader.FONT_SONG;
        PillarHead.TileText.updateThis(text);
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
        funcList.put("thk", ((dst, src) -> {
            if (src != null) return Result.ERR;
            if (dst == null) return Result.ERR;

            if (dst.type == RegType.INT) {
                setThick(getTile(), (int) dst.data);
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
        funcList.put("font", ((dst, src) -> {
            if (src != null) return Result.ERR;
            if (dst == null) return Result.ERR;

            if (dst.type == RegType.STR) {
                setFont(getTile(), dst.data.toString());
                return Result.OK;
            }
            return Result.ERR;
        }));
    }

    public abstract PillarHead.TileText getTile();

}
