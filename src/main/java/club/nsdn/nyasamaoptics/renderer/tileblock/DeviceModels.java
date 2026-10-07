package club.nsdn.nyasamaoptics.renderer.tileblock;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Model-identifier decision for the optics block entity renderers (decision D4:
 * {@code cn.ac.nya.forgeobj.WavefrontObject} is gone, so every renderer asks for a
 * {@link BakedModel} instead).
 *
 * <p><b>How a "model identifier" replaces a {@code WavefrontObject}.</b> In 1.12.2 a renderer wrote
 * {@code new WavefrontObject(new ResourceLocation("nyasamaoptics", "models/blocks/foo_base.obj"))}
 * and the custom loader opened the file directly. The modern loader only knows model <i>ids</i>, so the
 * port uses the single officially supported way to obtain a model that is not reached through a
 * blockstate: {@link ModelResourceLocation#standalone(ResourceLocation)} plus the
 * {@code ModelEvent.RegisterAdditional} event (NeoForge 1.21.1:
 * "models registered through this event must use the {@code standalone} variant", and the location
 * passed to {@code register} "must later be used to recover the loaded model").</p>
 *
 * <p><b>The identifiers are the 1.12.2 OBJ stems, re-based onto {@code models/}.</b> The 1.12.2 path
 * {@code models/blocks/<stem>.obj} becomes the id {@code nyasamaoptics:<model>}, i.e.</p>
 * <ul>
 *   <li>{@code models/blocks/ad_board_base.obj} &rarr; {@code nyasamaoptics:block/ad_board_base}</li>
 *   <li>{@code models/blocks/station_lamp_top.obj} &rarr; {@code nyasamaoptics:block/station_lamp_top}</li>
 * </ul>
 * <p>i.e. the {@code models/} prefix and the {@code .obj} suffix are dropped and the namespace is the
 * mod's own. {@code block} (singular) is used because that is where the assets already ship
 * ({@code assets/nyasamaoptics/models/block/*.obj}); note that {@code docs/08-OBJ-PIPELINE.md} / the
 * generated part manifest write the same stems under {@code models/blocks/} (plural). The two differ by
 * one letter on purpose in this port -- the pipeline writing to {@code blocks/} is an asset-side
 * detail, and it will need to be aligned when the generated part JSON/MTL files are copied into
 * {@code src/main/resources}. It does <b>not</b> change any identifier below.</p>
 *
 * <p><b>What the integration step must do:</b> fire
 * {@code ModelEvent.RegisterAdditional} and call {@code register(...)} once per id returned by
 * {@link #catalogue()} -- every id named in this package is listed there, so nobody has to grep the
 * renderers. Each id needs a matching {@code assets/nyasamaoptics/models/<id>.json} at that moment;
 * for the OBJ models that JSON is the {@code neoforge:obj} wrapper produced by
 * {@code tools/obj-pipeline}. Until then the lookup below resolves to the missing model and simply
 * draws nothing, which is the same "safe empty" behaviour a missing {@code WavefrontObject} had in
 * 1.12.2 (the custom loader logged and rendered nothing).</p>
 *
 * <p><b>Track B (per-part models) is not consumed here.</b> {@code RendererHelper.renderPart*} /
 * {@code renderAllExcept*} are still absent (see {@code docs/08-OBJ-PIPELINE.md} §5). Only one of the
 * ten renderers needs parts at all -- {@code StationBoardRenderer}, which draws {@code base}, {@code 1}
 * and {@code 2} of {@code station_board.obj}. It uses the correctly-named part ids
 * ({@code nyasamaoptics:block/station_board__base} etc., the {@code <stem>__<sanitised-part>} rule from
 * the pipeline) and falls back to the whole model when a part id does not resolve, rather than
 * inventing a {@code renderPart} helper. See that class's note.</p>
 */
public final class DeviceModels {

    /** {@code models/blocks/<stem>_base.obj} etc. live under {@code models/block/} in this project. */
    private static final String FOLDER = "block";

    private static final Map<ResourceLocation, BakedModel> CACHE = new HashMap<>();

    private DeviceModels() {
    }

    /** The id of a model whose 1.12.2 file was {@code models/blocks/<name>.obj}. */
    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath("nyasamaoptics", FOLDER + "/" + name);
    }

    /**
     * Resolves a model id to its baked model, or to the missing model if it is not loaded.
     *
     * <p>Resolution goes through {@code ModelManager#getModel(ModelResourceLocation)} with the
     * {@code standalone} variant -- the exact key {@code ModelEvent.RegisterAdditional} was given, as
     * NeoForge requires. Results are cached; {@code ModelManager#getModel} throws when the id is absent
     * (rather than returning null), so a failed lookup is cached as empty and never asked twice. The
     * cache is cleared by {@link #invalidate()} from a resource reload.</p>
     */
    public static BakedModel resolve(ResourceLocation id) {
        BakedModel cached = CACHE.get(id);
        if (cached != null) {
            return cached;
        }
        BakedModel model = Minecraft.getInstance().getModelManager().getMissingModel();
        try {
            model = Minecraft.getInstance().getModelManager()
                    .getModel(ModelResourceLocation.standalone(id));
        } catch (RuntimeException e) {
            // Not registered yet (see the class note): draw nothing rather than crash the chunk pass.
            model = Minecraft.getInstance().getModelManager().getMissingModel();
        }
        CACHE.put(id, model);
        return model;
    }

    /** Drops the resolution cache; call on a client resource reload. */
    public static void invalidate() {
        CACHE.clear();
    }

    /**
     * Every id this package can ask for, so the integration step can feed
     * {@code ModelEvent.RegisterAdditional} from one place.
     *
     * <p>Grouped by renderer. The {@code *_base}/{@code *_light} pairs below the
     * {@code LightRenderer} comment are the ten {@code RGBLight} variants: that renderer takes its model
     * name from {@code RGBLight#resource} read off the block at run time ({@code resource + "_base"} /
     * {@code resource + "_light"}), so it cannot be enumerated statically -- the ten names the
     * registration table passes as {@code resource} are listed instead, and any future variant must be
     * appended here.</p>
     */
    public static ResourceLocation[] catalogue() {
        String[] names = {
                // AdBoardRenderer
                "ad_board_base", "ad_board_light",
                // GuideBoardRenderer - draws no shell of its own (1.12.2 did the same)
                // HoloJetRevRenderer
                "holo_jet_rev_base", "holo_jet_rev_light",
                // LEDPlateRenderer
                "led_plate_base", "led_plate_light",
                // LightRenderer: the ten RGBLight variants (RGBLight#resource + "_base"/"_light")
                "pillar_body_base", "pillar_body_light",
                "adsorption_lamp_mono_base", "adsorption_lamp_mono_light",
                "adsorption_lamp_multi_base", "adsorption_lamp_multi_light",
                "adsorption_lamp_large_base", "adsorption_lamp_large_light",
                "fluorescent_light_base", "fluorescent_light_light",
                "fluorescent_light_flock_base", "fluorescent_light_flock_light",
                "mosaic_light_mono_base", "mosaic_light_mono_light",
                "mosaic_light_mono_small_base", "mosaic_light_mono_small_light",
                "mosaic_light_multi_base", "mosaic_light_multi_light",
                "mosaic_light_multi_small_base", "mosaic_light_multi_small_light",
                "cuball_lamp_base", "cuball_lamp_light",
                "platform_light_full_base", "platform_light_full_light",
                "platform_light_half_base", "platform_light_half_light",
                // PillarHeadRenderer
                "pillar_head_base",
                // PlatformPlateRenderer
                "platform_plate_full_base", "platform_plate_half_base",
                // StationBoardRenderer (whole model, plus the two parts it draws separately)
                "station_board", "station_board__base", "station_board__1", "station_board__2",
                // StationLampRenderer
                "station_lamp_top", "station_lamp_mid", "station_lamp_end", "station_lamp_logo",
                "station_lamp_back",
        };
        ResourceLocation[] ids = new ResourceLocation[names.length];
        for (int i = 0; i < names.length; i++) {
            ids[i] = id(names[i]);
        }
        return ids;
    }
}
