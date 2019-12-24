package club.nsdn.nyasamaoptics.util;

import club.nsdn.nyasamaoptics.network.NetworkWrapper;
import club.nsdn.nyasamaoptics.tileblock.deco.TileEntityDecoText;
import club.nsdn.nyasamaoptics.util.font.FontLoader;
import club.nsdn.nyasamatelecom.api.util.NSASM;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

import java.util.LinkedHashMap;

/**
 * Created by drzzm32 on 2019.12.24.
 */
public abstract class DecoTextCore extends NSASM {

    public DecoTextCore(String[][] code) {
        super(code);
    }

    @Override
    public SimpleNetworkWrapper getWrapper() {
        return NetworkWrapper.instance;
    }

    public void setContent(TileEntityDecoText text, String value) {
        text.content = value;
        TileEntityDecoText.updateThis(text);
    }

    public void setColor(TileEntityDecoText text, int value) {
        text.color = value & 0xFFFFFF;
        TileEntityDecoText.updateThis(text);
    }

    public void setThick(TileEntityDecoText text, int value) {
        text.thick = value;
        TileEntityDecoText.updateThis(text);
    }

    public void setScale(TileEntityDecoText text, float value) {
        text.scale = value;
        TileEntityDecoText.updateThis(text);
    }

    public void setAlign(TileEntityDecoText text, String value) {
        if (value.toLowerCase().equals("center"))
            text.align = FontLoader.ALIGN_CENTER;
        else if (value.toLowerCase().equals("up"))
            text.align = FontLoader.ALIGN_UP;
        else if (value.toLowerCase().equals("down"))
            text.align = FontLoader.ALIGN_DOWN;
        else if (value.toLowerCase().equals("left"))
            text.align = FontLoader.ALIGN_LEFT;
        else if (value.toLowerCase().equals("right"))
            text.align = FontLoader.ALIGN_RIGHT;
        else
            text.align = FontLoader.ALIGN_CENTER;
        TileEntityDecoText.updateThis(text);
    }

    public void setFont(TileEntityDecoText text, String value) {
        if (value.toLowerCase().equals("kai"))
            text.font = FontLoader.FONT_KAI;
        else if (value.toLowerCase().equals("hei"))
            text.font = FontLoader.FONT_HEI;
        else if (value.toLowerCase().equals("yan"))
            text.font = FontLoader.FONT_YAN;
        else if (value.toLowerCase().equals("long"))
            text.font = FontLoader.FONT_LONG;
        else if (value.toLowerCase().equals("song"))
            text.font = FontLoader.FONT_SONG;
        else if (value.toLowerCase().equals("lishu"))
            text.font = FontLoader.FONT_LISHU;
        else
            text.font = FontLoader.FONT_SONG;
        TileEntityDecoText.updateThis(text);
    }

    public void setSubContent(TileEntityDecoText text, String value) {
        text.subContent = value;
        TileEntityDecoText.updateThis(text);
    }

    public void setSubColor(TileEntityDecoText text, int value) {
        text.subColor = value & 0xFFFFFF;
        TileEntityDecoText.updateThis(text);
    }

    public void setSubScale(TileEntityDecoText text, float value) {
        text.subScale = value;
        TileEntityDecoText.updateThis(text);
    }

    public void setSubAlign(TileEntityDecoText text, String value) {
        if (value.toLowerCase().equals("center"))
            text.subAlign = TileEntityDecoText.SUB_ALIGN_CENTER;
        else if (value.toLowerCase().equals("left"))
            text.subAlign = TileEntityDecoText.SUB_ALIGN_LEFT;
        else if (value.toLowerCase().equals("right"))
            text.subAlign = TileEntityDecoText.SUB_ALIGN_RIGHT;
        else
            text.subAlign = FontLoader.ALIGN_CENTER;
        TileEntityDecoText.updateThis(text);
    }

    public void setOffsetY(TileEntityDecoText text, float y) {
        text.offsetY = y;
        TileEntityDecoText.updateThis(text);
    }

    public void setSubOffsetY(TileEntityDecoText text, float y) {
        text.subOffsetY = y;
        TileEntityDecoText.updateThis(text);
    }

    public void setOffset(TileEntityDecoText text, float u, float v) {
        text.offsetX = -u;
        text.offsetZ = v;
        TileEntityDecoText.updateThis(text);
    }

    public void setSubOffset(TileEntityDecoText text, float u, float v) {
        text.subOffsetX = -u;
        text.subOffsetZ = v;
        TileEntityDecoText.updateThis(text);
    }

    public void setOptionalBack(TileEntityDecoText text, int value) {
        text.optionalBack = value & 0xFFFFFF;
        TileEntityDecoText.updateThis(text);
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

        funcList.put("sub.show", ((dst, src) -> {
            if (src != null) return Result.ERR;
            if (dst == null) return Result.ERR;

            if (dst.type == RegType.STR) {
                setSubContent(getTile(), dst.data.toString());
                return Result.OK;
            }
            return Result.ERR;
        }));
        funcList.put("sub.clr", ((dst, src) -> {
            if (src != null) return Result.ERR;
            if (dst == null) return Result.ERR;

            if (dst.type == RegType.INT) {
                setSubColor(getTile(), (int) dst.data);
                return Result.OK;
            }
            return Result.ERR;
        }));
        funcList.put("sub.fore", ((dst, src) -> {
            if (src != null) return Result.ERR;
            if (dst == null) return Result.ERR;

            if (dst.type == RegType.INT) {
                setSubColor(getTile(), (int) dst.data);
                return Result.OK;
            }
            return Result.ERR;
        }));
        funcList.put("sub.back", ((dst, src) -> {
            if (src != null) return Result.ERR;
            if (dst == null) return Result.ERR;

            if (dst.type == RegType.INT) {
                setOptionalBack(getTile(), (int) dst.data);
                return Result.OK;
            }
            return Result.ERR;
        }));
        funcList.put("sub.scl", ((dst, src) -> {
            if (src != null) return Result.ERR;
            if (dst == null) return Result.ERR;

            if (dst.type != RegType.STR) {
                if (dst.type == RegType.FLOAT)
                    setSubScale(getTile(), (float) dst.data);
                else if (dst.type == RegType.INT)
                    setSubScale(getTile(), (int) dst.data);
                return Result.OK;
            }
            return Result.ERR;
        }));
        funcList.put("sub.aln", ((dst, src) -> {
            if (src != null) return Result.ERR;
            if (dst == null) return Result.ERR;

            if (dst.type == RegType.STR) {
                setSubAlign(getTile(), dst.data.toString());
                return Result.OK;
            }
            return Result.ERR;
        }));
        funcList.put("offset", ((dst, src) -> {
            if (src == null) return Result.ERR;
            if (dst == null) return Result.ERR;

            float x, y;
            if (src.type == RegType.FLOAT)
                y = (float) src.data;
            else if (src.type == RegType.INT)
                y = (int) src.data;
            else return Result.ERR;
            if (dst.type == RegType.FLOAT)
                x = (float) dst.data;
            else if (dst.type == RegType.INT)
                x = (int) dst.data;
            else return Result.ERR;

            setOffset(getTile(), x, y);

            return Result.OK;
        }));
        funcList.put("sub.offset", ((dst, src) -> {
            if (src == null) return Result.ERR;
            if (dst == null) return Result.ERR;

            float x, y;
            if (src.type == RegType.FLOAT)
                y = (float) src.data;
            else if (src.type == RegType.INT)
                y = (int) src.data;
            else return Result.ERR;
            if (dst.type == RegType.FLOAT)
                x = (float) dst.data;
            else if (dst.type == RegType.INT)
                x = (int) dst.data;
            else return Result.ERR;

            setSubOffset(getTile(), x, y);

            return Result.OK;
        }));

        funcList.put("offset.y", ((dst, src) -> {
            if (src != null) return Result.ERR;
            if (dst == null) return Result.ERR;

            float y;
            if (dst.type == RegType.FLOAT)
                y = (float) dst.data;
            else if (dst.type == RegType.INT)
                y = (int) dst.data;
            else return Result.ERR;

            setOffsetY(getTile(), y);

            return Result.OK;
        }));
        funcList.put("sub.offset.y", ((dst, src) -> {
            if (src != null) return Result.ERR;
            if (dst == null) return Result.ERR;

            float y;
            if (dst.type == RegType.FLOAT)
                y = (float) dst.data;
            else if (dst.type == RegType.INT)
                y = (int) dst.data;
            else return Result.ERR;

            setSubOffsetY(getTile(), y);

            return Result.OK;
        }));
    }

    public abstract TileEntityDecoText getTile();

}
