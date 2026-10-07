package club.nsdn.nyasamaoptics.renderer.tileblock;

import net.minecraft.resources.ResourceLocation;

/**
 * The texture identifiers the ported optics renderers bind, collected in one place because four of the
 * ten renderers share the same two and three of them repeat the "text" plate.
 *
 * <p>These strings are <b>verbatim from 1.12.2</b>, including the {@code textures/blocks/} prefix, which
 * is the plural of the folder the assets actually live in ({@code assets/nyasamaoptics/textures/block/}).
 * The prefix is kept deliberately:</p>
 * <ul>
 *   <li>The path is passed to {@code RenderType.entityCutoutNoCull(...)}, and a render type's texture
 *       location is taken as-is (no {@code textures/} prefix is assumed), so
 *       {@code nyasamaoptics:textures/blocks/light_shell.png} is exactly the right path today.</li>
 *   <li>Renaming it to {@code .../textures/block/...} would change four files' worth of literal strings
 *       for no behavioural gain, and the 1.12.2 diff stays readable this way.</li>
 * </ul>
 * <p>Note that this is <i>not</i> the same naming question as the models: {@code docs/08-OBJ-PIPELINE.md}
 * generates model files under {@code models/blocks/} (plural) while the shipping OBJ assets live under
 * {@code models/block/} (singular); see {@link DeviceModels} for how the model ids are formed.</p>
 */
public final class ResourceLocations {

    /** 1.12.2 {@code textureShell} on the lamp/board renderers. */
    public static final ResourceLocation LIGHT_SHELL = blocks("light_shell.png");
    /** 1.12.2 {@code textureLight}. */
    public static final ResourceLocation LIGHT_BASE = blocks("light_base.png");
    /** 1.12.2 {@code _text}: the plate the holographic text used to be drawn over. */
    public static final ResourceLocation WHITE = blocks("white.png");
    /** 1.12.2 {@code textureBase} of the station lamp. */
    public static final ResourceLocation STATION_LAMP_BASE = blocks("station_lamp_base.png");
    /** 1.12.2 {@code textureBack} of the station lamp. */
    public static final ResourceLocation STATION_LAMP_BACK = blocks("station_lamp_back.png");
    /** 1.12.2 {@code textureLogo} of the station lamp. */
    public static final ResourceLocation STATION_LAMP_LOGO = blocks("station_lamp_logo.png");

    private ResourceLocations() {
    }

    private static ResourceLocation blocks(String file) {
        return ResourceLocation.fromNamespaceAndPath("nyasamaoptics", "textures/blocks/" + file);
    }
}
