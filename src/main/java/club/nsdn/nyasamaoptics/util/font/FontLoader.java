package club.nsdn.nyasamaoptics.util.font;

import club.nsdn.nyasamaoptics.NyaSamaOptics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.DataInputStream;
import java.util.Optional;

/**
 * Bitmap-font loader, ported from the 1.12.2
 * {@code club.nsdn.nyasamaoptics.util.font.FontLoader}.
 *
 * <p>The class holds seven raw 1bpp glyph tables -- {@code ascii.bin} (128 glyphs of 64 bytes) and six
 * 1 MiB GB2312 tables (128 bytes per glyph, indexed by
 * {@code (first - 0xA1) * 0x5E + (second - 0xA1)}) -- and hands them to {@link TextModel}. The font,
 * align and look-up tables of constants are carried over verbatim; only the way a resource is opened
 * changed.</p>
 *
 * <p>Mapping notes:</p>
 * <ul>
 *   <li>{@code net.minecraft.client.resources.IResourceManager} is gone; the 1.21.1 resource manager
 *       is the side-neutral {@link ResourceManager}. The 1.12.2 call
 *       {@code manager.getResource(ResourceLocation)} now returns {@code Optional<Resource>} and the
 *       stream comes from {@link Resource#open()}, so {@code load} unwraps the optional and keeps the
 *       rest of its body -- including the {@code available()}-sized buffer and the
 *       {@code System.out.println} on failure -- byte for byte.</li>
 *   <li>{@code new ResourceLocation(String)} becomes {@link ResourceLocation#parse(String)}; the paths
 *       are still the old {@code "namespace:path"} strings.</li>
 *   <li>{@code NyaSamaOptics.logger} becomes {@code NyaSamaOptics.LOGGER} (slf4j).</li>
 * </ul>
 *
 * <p><b>No AWT:</b> the 1.12.2 implementation rasterised nothing -- it reads pre-baked bitmaps -- so the
 * {@code 00-PORT-PLAN.md} appendix B decision (no {@code java.awt}/{@code javax.swing} in the game
 * process) needs no code change here. {@link TextModel} is where the old implementation did depend on
 * a discarded pipeline, and that class documents its own mapping.</p>
 *
 * <p><b>Deferred (resources):</b> {@code assets/nyasamaoptics/fonts/*.bin} have not been copied into v3
 * yet (the optics module currently ships only {@code META-INF}), so every table loads as {@code null}
 * and {@link #getModel} returns {@code null} until the assets are moved over from the 1.12.2 tree.
 * This is a packaging task, not a code one; the loader logs the mismatch exactly as 1.12.2 did
 * ("should load: N, then loaded: -1").</p>
 */
public class FontLoader {
    private static final String ASCII_Path = "nyasamaoptics:fonts/ascii.bin";
    private static final String GB2312_Kai_Path = "nyasamaoptics:fonts/gb2312_kai.bin";
    private static final String GB2312_LiShu_Path = "nyasamaoptics:fonts/gb2312_lishu.bin";
    private static final String GB2312_Song_Path = "nyasamaoptics:fonts/gb2312_sourcehanserif.bin";
    private static final String GB2312_Hei_Path = "nyasamaoptics:fonts/gb2312_sourcehansans.bin";
    private static final String GB2312_Long_Path = "nyasamaoptics:fonts/gb2312_long.bin";
    private static final String GB2312_Yan_Path = "nyasamaoptics:fonts/gb2312_yan.bin";
    public static byte[] ASCII;
    public static byte[] GB2312_Kai;
    public static byte[] GB2312_LiShu;
    public static byte[] GB2312_Song;
    public static byte[] GB2312_Hei;
    public static byte[] GB2312_Long;
    public static byte[] GB2312_Yan;

    private byte[] load(ResourceManager manager, String path) {
        int result = -1;
        byte[] buffer;
        try {
            // 1.12.2: manager.getResource(new ResourceLocation(path)).getInputStream().
            Optional<Resource> resource = manager.getResource(ResourceLocation.parse(path));
            DataInputStream stream = new DataInputStream(resource.orElseThrow().open());
            result = stream.available();
            buffer = new byte[result];
            stream.readFully(buffer);
            stream.close();
        } catch (Exception e) {
            System.out.println(e.toString());
            buffer = null;
        }
        NyaSamaOptics.LOGGER.info("FontLoader should load: " + result + ", then loaded: " + ((buffer != null) ? buffer.length : -1));
        return buffer;
    }

    public FontLoader(ResourceManager manager) {
        ASCII = load(manager, ASCII_Path);
        GB2312_Kai = load(manager, GB2312_Kai_Path);
        GB2312_LiShu = load(manager, GB2312_LiShu_Path);
        GB2312_Song = load(manager, GB2312_Song_Path);
        GB2312_Hei = load(manager, GB2312_Hei_Path);
        GB2312_Long = load(manager, GB2312_Long_Path);
        GB2312_Yan = load(manager, GB2312_Yan_Path);
    }

    public static final int FONT_LISHU = 0;
    public static final int FONT_SONG = 1;
    public static final int FONT_KAI = 2;
    public static final int FONT_HEI = 3;
    public static final int FONT_LONG = 4;
    public static final int FONT_YAN = 5;

    public static final int ALIGN_NULL = 0;
    public static final int ALIGN_NOP = 1;
    public static final int ALIGN_LEFT = 2;
    public static final int ALIGN_RIGHT = 3;
    public static final int ALIGN_CENTER = 4;
    public static final int ALIGN_UP = 5;
    public static final int ALIGN_DOWN = 6;

    public static TextModel getModel(int font, int align, String str, int thick, int color) {
        if (font == FONT_KAI) {
            return new TextModel(GB2312_Kai, align, str, thick, color);
        } else if (font == FONT_LISHU) {
            return new TextModel(GB2312_LiShu, align, str, thick, color);
        } else if (font == FONT_SONG) {
            return new TextModel(GB2312_Song, align, str, thick, color);
        } else if (font == FONT_HEI) {
            return new TextModel(GB2312_Hei, align, str, thick, color);
        } else if (font == FONT_LONG) {
            return new TextModel(GB2312_Long, align, str, thick, color);
        } else if (font == FONT_YAN) {
            return new TextModel(GB2312_Yan, align, str, thick, color);
        }
        return null;
    }
}
