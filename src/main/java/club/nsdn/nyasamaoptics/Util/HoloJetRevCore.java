package club.nsdn.nyasamaoptics.Util;

import club.nsdn.nyasamaoptics.TileEntities.HoloJetRev;
import club.nsdn.nyasamaoptics.TileEntities.PillarHead;
import club.nsdn.nyasamaoptics.Util.Font.FontLoader;

import java.util.LinkedHashMap;

/**
 * Created by drzzm32 on 2017.11.3.
 */
public abstract class HoloJetRevCore extends NSASM {

    public HoloJetRevCore(String[][] code) {
        super(code);
    }

    public void setContent(HoloJetRev.TileText text, String value) {
        text.content = value;
        HoloJetRev.TileText.updateThis(text);
    }

    public void setColor(HoloJetRev.TileText text, int value) {
        text.color = value & 0xFFFFFF;
        HoloJetRev.TileText.updateThis(text);
    }

    public void setThick(HoloJetRev.TileText text, int value) {
        text.thick = value;
        HoloJetRev.TileText.updateThis(text);
    }

    public void setScale(HoloJetRev.TileText text, float value) {
        text.scale = value;
        HoloJetRev.TileText.updateThis(text);
    }

    public void setAlign(HoloJetRev.TileText text, String value) {
        if (value.toLowerCase().equals("center"))
            text.align = FontLoader.ALIGN_CENTER;
        else if (value.toLowerCase().equals("vertical"))
            text.align = FontLoader.ALIGN_VERTICAL;
        else if (value.toLowerCase().equals("left"))
            text.align = FontLoader.ALIGN_LEFT;
        else if (value.toLowerCase().equals("right"))
            text.align = FontLoader.ALIGN_RIGHT;
        else
            text.align = FontLoader.ALIGN_CENTER;
        HoloJetRev.TileText.updateThis(text);
    }

    public void setFont(HoloJetRev.TileText text, String value) {
        if (value.toLowerCase().equals("kai"))
            text.font = FontLoader.FONT_KAI;
        else if (value.toLowerCase().equals("song"))
            text.font = FontLoader.FONT_SONG;
        else if (value.toLowerCase().equals("lishu"))
            text.font = FontLoader.FONT_LISHU;
        else
            text.font = FontLoader.FONT_SONG;
        HoloJetRev.TileText.updateThis(text);
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
        funcList.put("aln", ((dst, src) -> {
            if (src != null) return Result.ERR;
            if (dst == null) return Result.ERR;

            if (dst.type == RegType.STR) {
                setAlign(getTile(), dst.data.toString());
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

    public abstract HoloJetRev.TileText getTile();

}
