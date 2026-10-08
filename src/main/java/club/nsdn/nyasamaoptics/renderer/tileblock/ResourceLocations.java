package club.nsdn.nyasamaoptics.renderer.tileblock;

import net.minecraft.resources.ResourceLocation;

/**
 * The texture identifiers the ported optics renderers bind, collected in one place because four of the
 * ten renderers share the same two and three of them repeat the "text" plate.
 *
 * <p><b>The prefix must name a folder that exists.</b> These strings came from 1.12.2, where the device
 * textures lived under the <b>plural</b> {@code textures/blocks/}, and the port carried that plural
 * prefix over verbatim. In 1.21.1 a texture location is resolved <b>literally</b>: {@code SimpleTexture}
 * passes the {@link ResourceLocation} straight to {@code ResourceManager#getResourceOrThrow(...)}, which
 * appends neither a {@code textures/} prefix nor a {@code .png} suffix. The path therefore has to spell
 * the directory the asset actually sits in. In this project that directory is singular --
 * {@code assets/nyasamaoptics/textures/block/} (33 files) plus
 * {@code assets/nyasamaoptics/textures/item/} (1 file), with no {@code textures/blocks/} at all. The
 * plural path consequently resolved to nothing and all six textures below fell back to the missing
 * texture <b>silently</b>, with nothing written to the log; hence {@code textures/block/} in
 * {@link #block(String)}.</p>
 *
 * <p>This file is the port's whole texture catalogue: the renderers reference the constants below rather
 * than spelling paths themselves, so correcting the single helper fixed all six at once -- the "four
 * files' worth of literal strings" the note here used to cite never existed. The plural spelling survives
 * only as the 1.12.2 historical form quoted in the renderer notes, and in the raw pipeline output under
 * {@code tools/obj-pipeline}, which rewrites {@code textures/blocks/} to {@code textures/block/} on copy
 * ({@code Copy-OpticsObjAssets.ps1}); the JSON/MTL assets that ship with the mod name the singular folder
 * already.</p>
 *
 * <p>Note that this is <i>not</i> the same naming question as the models: {@code docs/08-OBJ-PIPELINE.md}
 * generates model files under {@code models/blocks/} (plural) while the shipping OBJ assets live under
 * {@code models/block/} (singular); see {@link DeviceModels} for how the model ids are formed.</p>
 */
public final class ResourceLocations {

    /** 1.12.2 {@code textureShell} on the lamp/board renderers. */
    public static final ResourceLocation LIGHT_SHELL = block("light_shell.png");
    /** 1.12.2 {@code textureLight}. */
    public static final ResourceLocation LIGHT_BASE = block("light_base.png");
    /** 1.12.2 {@code _text}: the plate the holographic text used to be drawn over. */
    public static final ResourceLocation WHITE = block("white.png");
    /** 1.12.2 {@code textureBase} of the station lamp. */
    public static final ResourceLocation STATION_LAMP_BASE = block("station_lamp_base.png");
    /** 1.12.2 {@code textureBack} of the station lamp. */
    public static final ResourceLocation STATION_LAMP_BACK = block("station_lamp_back.png");
    /** 1.12.2 {@code textureLogo} of the station lamp. */
    public static final ResourceLocation STATION_LAMP_LOGO = block("station_lamp_logo.png");

    private ResourceLocations() {
    }

    /** Resolves a file name against {@code assets/nyasamaoptics/textures/block/} (singular folder). */
    private static ResourceLocation block(String file) {
        return ResourceLocation.fromNamespaceAndPath("nyasamaoptics", "textures/block/" + file);
    }
}
