package club.nsdn.nyasamaoptics;

import club.nsdn.nyasamaoptics.api.LightBeam;
import club.nsdn.nyasamaoptics.block.BlockAdsorptionLamp;
import club.nsdn.nyasamaoptics.block.BlockFluorescentLamp;
import club.nsdn.nyasamaoptics.block.BlockLogo;
import club.nsdn.nyasamaoptics.block.BlockNSDNLogo;
import club.nsdn.nyasamaoptics.block.BlockSign;
import club.nsdn.nyasamaoptics.block.BlockSpotLight;
import club.nsdn.nyasamaoptics.tileblock.light.RGBLight;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registration for {@code nyasamaoptics} blocks.
 *
 * <p>Replaces the 1.12.2 {@code BlockLoader}, which used {@code RegistryEvent} plus a hand-built
 * {@code LinkedList<Block>} and a client-only {@code ModelRegistryEvent} handler for item icons.
 * Those are all gone in 1.21.1 (item models are JSON), so what remains is a table walk, exactly as in
 * {@code nyasamabuilding}.</p>
 *
 * <p>The 1.12.2 loader registered roughly 33 blocks: three glass panels (sign, NSDN logo, logo), two
 * lamps, a spotlight, and then the devices -- holo jet, pillar head, LED plate, the two platform
 * plates, station lamp, ad/station/text/guide boards, the two light beams and ten RGB light variants.
 * They are being ported a few at a time; the table below grows with them, and the block entity types
 * those devices need arrive in {@code OpticsBlockEntities}.</p>
 */
public final class OpticsBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(NyaSamaOptics.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NyaSamaOptics.MODID);

    /** Every custom block class needs its codec here (1.20.5+ requirement). */
    public static final DeferredRegister<MapCodec<? extends Block>> BLOCK_TYPES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE, NyaSamaOptics.MODID);

    private OpticsBlocks() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_TYPES.register(modBus);
    }

    static {
        BLOCK_TYPES.register("fluorescent_lamp", () -> BlockFluorescentLamp.CODEC);
        BLOCK_TYPES.register("adsorption_lamp", () -> BlockAdsorptionLamp.CODEC);

        // Glass material: 1.12.2 used Material.GLASS with light level 1 and resistance 10.
        BlockBehaviour.Properties glass = BlockBehaviour.Properties.of()
                .mapColor(MapColor.NONE)
                .sound(SoundType.GLASS)
                .strength(2.0F, 10.0F)
                .lightLevel(state -> 15)
                .noOcclusion();

        DeferredBlock<BlockSign> sign = BLOCKS.register("nso_sign", () -> new BlockSign(glass));
        ITEMS.registerSimpleBlockItem(sign);
        DeferredBlock<BlockNSDNLogo> nsdnLogo = BLOCKS.register("nso_nsdn", () -> new BlockNSDNLogo(glass));
        ITEMS.registerSimpleBlockItem(nsdnLogo);
        DeferredBlock<BlockLogo> logo = BLOCKS.register("nso_logo", () -> new BlockLogo(glass));
        ITEMS.registerSimpleBlockItem(logo);

        DeferredBlock<BlockAdsorptionLamp> adsorptionLamp = BLOCKS.register("adsorption_lamp",
                () -> new BlockAdsorptionLamp(glass));
        ITEMS.registerSimpleBlockItem(adsorptionLamp);

        DeferredBlock<BlockFluorescentLamp> fluorescentLamp = BLOCKS.register("fluorescent_lamp",
                () -> new BlockFluorescentLamp(glass));
        ITEMS.registerSimpleBlockItem(fluorescentLamp);

        // Light beams: invisible, unbreakable, replaceable, no drops.
        BlockBehaviour.Properties beam = BlockBehaviour.Properties.of()
                .mapColor(MapColor.NONE)
                .noCollission()
                .noOcclusion()
                .replaceable()
                .strength(-1.0F, 3600000.0F)
                .lightLevel(state -> 15)
                .noLootTable();

        DeferredBlock<LightBeam> lightBeamDot = BLOCKS.register("light_beam_0",
                () -> new LightBeam(beam, LightBeam.TYPE_DOT));
        ITEMS.registerSimpleBlockItem(lightBeamDot);
        LIGHT_BEAM_DOT = lightBeamDot;
        DeferredBlock<LightBeam> lightBeamLine = BLOCKS.register("light_beam_1",
                () -> new LightBeam(beam, LightBeam.TYPE_LINE));
        ITEMS.registerSimpleBlockItem(lightBeamLine);
        LIGHT_BEAM_LINE = lightBeamLine;

        DeferredBlock<BlockSpotLight> spotLight = BLOCKS.register("spot_light",
                () -> new BlockSpotLight(glass.lightLevel(state ->
                        state.getValue(BlockSpotLight.POWERED) ? 15 : 0)));
        ITEMS.registerSimpleBlockItem(spotLight);

        // The ten RGB light variants: name, id, box size, flood-light flag (from BlockLoader).
        String[][] rgbLights = new String[][] {
                { "PillarBody", "pillar_body", "1", "1", "0.5", "false" },
                { "BlockAdsorptionLampLarge", "adsorption_lamp_large", "1", "0.125", "1", "false" },
                { "BlockAdsorptionLampMono", "adsorption_lamp_mono", "0.25", "0.125", "0.25", "false" },
                { "BlockAdsorptionLampMulti", "adsorption_lamp_multi", "1", "0.125", "0.25", "false" },
                { "BlockFluorescentLight", "fluorescent_light", "1", "0.1875", "0.25", "false" },
                { "BlockFluorescentLightFlock", "fluorescent_light_flock", "1", "0.25", "0.625", "false" },
                { "BlockMosaicLightMono", "mosaic_light_mono", "1", "0.5", "1", "true" },
                { "BlockMosaicLightMonoSmall", "mosaic_light_mono_small", "0.625", "0.25", "0.625", "true" },
                { "BlockMosaicLightMulti", "mosaic_light_multi", "1", "0.5", "1", "false" },
                { "BlockMosaicLightMultiSmall", "mosaic_light_multi_small", "1", "0.25", "0.625", "false" },
                { "BlockPlatformLightFull", "platform_light_full", "1", "1", "0.625", "false" },
                { "BlockPlatformLightHalf", "platform_light_half", "1", "0.5", "0.5", "false" },
                { "BlockCuballLamp", "cuball_lamp", "1", "1", "1", "false" },
        };
        for (String[] info : rgbLights) {
            String id = info[1];
            float x = Float.parseFloat(info[2]);
            float y = Float.parseFloat(info[3]);
            float z = Float.parseFloat(info[4]);
            boolean flood = Boolean.parseBoolean(info[5]);
            DeferredBlock<RGBLight> light = BLOCKS.register(id,
                    () -> new RGBLight(glass, OpticsBlockEntities::rgbLightType, id, x, y, z, flood));
            ITEMS.registerSimpleBlockItem(light);
            RGBLight.noteRegistered(light);
        }
    }

    /** Kept in a field because devices (the spotlight) need the beam block at runtime. */
    private static DeferredBlock<LightBeam> LIGHT_BEAM_LINE;
    private static DeferredBlock<LightBeam> LIGHT_BEAM_DOT;

    /** The line-type light beam, or null before registration completes. */
    public static LightBeam lineLight() {
        return LIGHT_BEAM_LINE == null ? null : LIGHT_BEAM_LINE.get();
    }

    /** The dot-type light beam, or null before registration completes. */
    public static LightBeam dotLight() {
        return LIGHT_BEAM_DOT == null ? null : LIGHT_BEAM_DOT.get();
    }
}
