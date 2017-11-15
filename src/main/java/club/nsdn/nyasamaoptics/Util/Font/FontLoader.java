package club.nsdn.nyasamaoptics.Util.Font;

import club.nsdn.nyasamaoptics.NyaSamaOptics;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.io.DataInputStream;

/**
 * Created by drzzm on 2017.1.8.
 */
public class FontLoader {
    private static final String ASCII_Path = "nyasamaoptics:fonts/ASCII.bin";
    private static final String GB2312_Kai_Path = "nyasamaoptics:fonts/GB2312_Kai.bin";
    private static final String GB2312_LiShu_Path = "nyasamaoptics:fonts/GB2312_LiShu.bin";
    private static final String GB2312_Song_Path = "nyasamaoptics:fonts/GB2312_SourceHanSerif.bin";
    private static final String GB2312_Hei_Path = "nyasamaoptics:fonts/GB2312_SourceHanSans.bin";
    private static final String GB2312_Long_Path = "nyasamaoptics:fonts/GB2312_Long.bin";
    public static byte[] ASCII;
    public static byte[] GB2312_Kai;
    public static byte[] GB2312_LiShu;
    public static byte[] GB2312_Song;
    public static byte[] GB2312_Hei;
    public static byte[] GB2312_Long;

    private byte[] load(IResourceManager manager, String path) {
        int result = -1;
        byte[] buffer;
        try {
            DataInputStream stream = new DataInputStream(manager.getResource(new ResourceLocation(path)).getInputStream());
            result = stream.available();
            buffer = new byte[result];
            stream.readFully(buffer);
            stream.close();
        } catch (Exception e) {
            System.out.println(e.toString());
            buffer = null;
        }
        NyaSamaOptics.log.info("FontLoader should load: " + result + ", then loaded: " + ((buffer != null) ? buffer.length : -1));
        return buffer;
    }

    public FontLoader(IResourceManager manager) {
        ASCII = load(manager, ASCII_Path);
        GB2312_Kai = load(manager, GB2312_Kai_Path);
        GB2312_LiShu = load(manager, GB2312_LiShu_Path);
        GB2312_Song = load(manager, GB2312_Song_Path);
        GB2312_Hei = load(manager, GB2312_Hei_Path);
        GB2312_Long = load(manager, GB2312_Long_Path);
    }

    public static final int FONT_LISHU = 0;
    public static final int FONT_SONG = 1;
    public static final int FONT_KAI = 2;
    public static final int FONT_HEI = 3;
    public static final int FONT_LONG = 4;

    public static final int ALIGN_NULL = 0;
    public static final int ALIGN_NOP = 1;
    public static final int ALIGN_LEFT = 2;
    public static final int ALIGN_RIGHT = 3;
    public static final int ALIGN_CENTER = 4;
    public static final int ALIGN_UP = 5;
    public static final int ALIGN_DOWN = 6;

    public static TextModel getModel(int font, int align, String str, int color, int thick) {
        if (font == FONT_KAI) {
            return new TextModel(GB2312_Kai, align, str, color, thick);
        } else if (font == FONT_LISHU) {
            return new TextModel(GB2312_LiShu, align, str, color, thick);
        } else if (font == FONT_SONG) {
            return new TextModel(GB2312_Song, align, str, color, thick);
        } else if (font == FONT_HEI) {
            return new TextModel(GB2312_Hei, align, str, color, thick);
        } else if (font == FONT_LONG) {
            return new TextModel(GB2312_Long, align, str, color, thick);
        }
        return null;
    }
}
