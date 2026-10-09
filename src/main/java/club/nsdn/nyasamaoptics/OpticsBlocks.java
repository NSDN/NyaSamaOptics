package club.nsdn.nyasamaoptics;

import club.nsdn.nyasamaoptics.api.LightBeam;
import club.nsdn.nyasamaoptics.block.BlockAdsorptionLamp;
import club.nsdn.nyasamaoptics.block.BlockFluorescentLamp;
import club.nsdn.nyasamaoptics.block.BlockLogo;
import club.nsdn.nyasamaoptics.block.BlockNSDNLogo;
import club.nsdn.nyasamaoptics.block.BlockSign;
import club.nsdn.nyasamaoptics.block.BlockSpotLight;
import club.nsdn.nyasamaoptics.tileblock.deco.AdBoard;
import club.nsdn.nyasamaoptics.tileblock.deco.GuideBoard;
import club.nsdn.nyasamaoptics.tileblock.deco.StationBoard;
import club.nsdn.nyasamaoptics.tileblock.deco.TextWall;
import club.nsdn.nyasamaoptics.tileblock.holo.HoloJetRev;
import club.nsdn.nyasamaoptics.tileblock.holo.PillarHead;
import club.nsdn.nyasamaoptics.tileblock.light.RGBLight;
import club.nsdn.nyasamaoptics.tileblock.screen.LEDPlate;
import club.nsdn.nyasamaoptics.tileblock.screen.PlatformPlateFull;
import club.nsdn.nyasamaoptics.tileblock.screen.PlatformPlateHalf;
import club.nsdn.nyasamaoptics.tileblock.screen.StationLamp;
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

    /**
     * 1.12.2 {@code BlockLoader.logo} (id {@code nso_logo}); also the icon of the module's creative tab,
     * which asked for it as {@code itemBlocks.get(BlockLoader.logo).getDefaultInstance()} -- see
     * {@code creativetab.CreativeTabLoader}.
     */
    public static DeferredBlock<BlockLogo> LOGO;

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
        //
        // NOTE: this used to be a single `BlockBehaviour.Properties glass` local shared by every block
        // below. `Properties` chains mutate in place and return `this`, so `spot_light` -- which bought
        // its own light level by writing `glass.lightLevel(...)` -- silently handed that function to
        // every later consumer of `glass`, including the RGB lights whose state has no POWERED. That is
        // the sixth runClient crash -- integration/run/crash-reports/crash-2026-10-09_22.23.44-fml.txt,
        // PORT-STATUS.md 8.3. Each block now asks for a fresh instance instead; see
        // glassProps()/spotlightProps() at the bottom of this class.
        DeferredBlock<BlockSign> sign = BLOCKS.register("nso_sign", () -> new BlockSign(glassProps()));
        ITEMS.registerSimpleBlockItem(sign);
        DeferredBlock<BlockNSDNLogo> nsdnLogo = BLOCKS.register("nso_nsdn", () -> new BlockNSDNLogo(glassProps()));
        ITEMS.registerSimpleBlockItem(nsdnLogo);
        LOGO = BLOCKS.register("nso_logo", () -> new BlockLogo(glassProps()));
        ITEMS.registerSimpleBlockItem(LOGO);

        DeferredBlock<BlockAdsorptionLamp> adsorptionLamp = BLOCKS.register("adsorption_lamp",
                () -> new BlockAdsorptionLamp(glassProps()));
        ITEMS.registerSimpleBlockItem(adsorptionLamp);

        DeferredBlock<BlockFluorescentLamp> fluorescentLamp = BLOCKS.register("fluorescent_lamp",
                () -> new BlockFluorescentLamp(glassProps()));
        ITEMS.registerSimpleBlockItem(fluorescentLamp);

        // Light beams: invisible, unbreakable, replaceable, no drops.
        //
        // `beam` is still one shared instance, and deliberately so: nothing below ever calls a chained
        // mutator on it, so there is nothing to leak. Sharing is only unsafe when a *reuse* site writes
        // to the instance (configure once, then read only).
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
                () -> new BlockSpotLight(spotlightProps()));
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
                    () -> new RGBLight(glassProps(), OpticsBlockEntities::rgbLightType, id, x, y, z, flood));
            ITEMS.registerSimpleBlockItem(light);
            RGBLight.noteRegistered(light);
        }

        // Decoration / hologram devices. Each supplies its own codec to the shared block-type table.
        //
        // noteRegistered below is handed the *holder*, never holder.get(): this whole block runs from
        // OpticsBlocks' class initialiser, i.e. during mod construction, and a DeferredHolder only
        // becomes bound while its DeferredRegister consumes the RegisterEvent -- so holder.get() here
        // throws NullPointerException: "Trying to access unbound value". Each device resolves the
        // holders in registeredBlocks(), which its block entity type's supplier calls at registration
        // time (same shape as RGBLight / LEDPlate / the platform plates / StationLamp further down).
        AdBoard.registerCodec(BLOCK_TYPES);
        GuideBoard.registerCodec(BLOCK_TYPES);
        StationBoard.registerCodec(BLOCK_TYPES);
        TextWall.registerCodec(BLOCK_TYPES);
        HoloJetRev.registerCodec(BLOCK_TYPES);
        PillarHead.registerCodec(BLOCK_TYPES);

        DeferredBlock<AdBoard> adBoard = BLOCKS.register("ad_board", () -> new AdBoard(deviceProps(false)));
        ITEMS.registerSimpleBlockItem(adBoard);
        AdBoard.noteRegistered(adBoard);

        DeferredBlock<StationBoard> stationBoard = BLOCKS.register("station_board", () -> new StationBoard(deviceProps(false)));
        ITEMS.registerSimpleBlockItem(stationBoard);
        StationBoard.noteRegistered(stationBoard);

        // TextWall and GuideBoard each have a lit variant; the difference is the light level.
        DeferredBlock<TextWall> textWall = BLOCKS.register("text_wall", () -> new TextWall(deviceProps(false)));
        ITEMS.registerSimpleBlockItem(textWall);
        TextWall.noteRegistered(textWall);
        DeferredBlock<TextWall> textWallLit = BLOCKS.register("text_wall_lit", () -> new TextWall(deviceProps(true)));
        ITEMS.registerSimpleBlockItem(textWallLit);
        TextWall.noteRegistered(textWallLit);

        // GuideBoard: name, id, lit, noPillar -- from the 1.12.2 BlockLoader table.
        String[][] guideBoards = new String[][] {
                { "guide_board_np", "false", "true" },
                { "guide_board_sp", "false", "false" },
                { "guide_board_dp", "false", "false" },
                { "guide_board_np_lit", "true", "true" },
                { "guide_board_sp_lit", "true", "false" },
                { "guide_board_dp_lit", "true", "false" },
        };
        for (String[] info : guideBoards) {
            boolean lit = Boolean.parseBoolean(info[1]);
            boolean noPillar = Boolean.parseBoolean(info[2]);
            DeferredBlock<GuideBoard> board = BLOCKS.register(info[0],
                    () -> new GuideBoard(deviceProps(lit)).withNoPillar(noPillar));
            ITEMS.registerSimpleBlockItem(board);
            GuideBoard.noteRegistered(board);
        }

        DeferredBlock<HoloJetRev> holoJetRev = BLOCKS.register("holo_jet_rev", () -> new HoloJetRev(deviceProps(false)));
        ITEMS.registerSimpleBlockItem(holoJetRev);
        HoloJetRev.noteRegistered(holoJetRev);

        DeferredBlock<PillarHead> pillarHead = BLOCKS.register("pillar_head", () -> new PillarHead(deviceProps(false)));
        ITEMS.registerSimpleBlockItem(pillarHead);
        PillarHead.noteRegistered(pillarHead);

        // Screen devices: centralised block entity types, no custom codec (they inherit Block#codec).
        DeferredBlock<LEDPlate> ledPlate = BLOCKS.register("led_plate",
                () -> new LEDPlate(deviceProps(false), OpticsBlockEntities::ledPlateType));
        ITEMS.registerSimpleBlockItem(ledPlate);
        LEDPlate.noteRegistered(ledPlate);

        DeferredBlock<PlatformPlateFull> platformPlateFull = BLOCKS.register("platform_plate_full",
                () -> new PlatformPlateFull(deviceProps(false), OpticsBlockEntities::platformPlateFullType));
        ITEMS.registerSimpleBlockItem(platformPlateFull);
        PlatformPlateFull.noteRegistered(platformPlateFull);

        DeferredBlock<PlatformPlateHalf> platformPlateHalf = BLOCKS.register("platform_plate_half",
                () -> new PlatformPlateHalf(deviceProps(false), OpticsBlockEntities::platformPlateHalfType));
        ITEMS.registerSimpleBlockItem(platformPlateHalf);
        PlatformPlateHalf.noteRegistered(platformPlateHalf);

        DeferredBlock<StationLamp> stationLamp = BLOCKS.register("station_lamp",
                () -> new StationLamp(deviceProps(false), OpticsBlockEntities::stationLampType));
        ITEMS.registerSimpleBlockItem(stationLamp);
        StationLamp.noteRegistered(stationLamp);
    }

    /**
     * Fresh glass-material properties, one instance per block: 1.12.2 {@code Material.GLASS} with
     * {@code setLightLevel(1)} (which 1.12.2 scaled to light level 15), {@code setHardness(2.0F)} and
     * {@code setResistance(10.0F)}. A new instance every call on purpose -- {@code Properties} chains
     * mutate in place, so a shared instance would leak one block's tweaks into every sibling.
     */
    private static BlockBehaviour.Properties glassProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.NONE)
                .sound(SoundType.GLASS)
                .strength(2.0F, 10.0F)
                .lightLevel(state -> 15)
                .noOcclusion();
    }

    /**
     * The spotlight's own properties: {@link #glassProps()} with the light level replaced by the
     * 1.12.2 {@code BlockSpotLight#getLightValue(IBlockState, IBlockAccess, BlockPos)} override,
     * {@code state.getValue(POWERED) ? 15 : 0}.
     *
     * <p>The replacement has to go through the mutator because 1.21.1 {@code Properties} has no
     * copy-constructor and no setter that returns a new instance (verified with {@code javap} on the
     * merged jar: the only factories are {@code of()}, {@code ofFullCopy(BlockBehaviour)} and
     * {@code ofLegacyCopy(BlockBehaviour)}, all of which take a <em>Block</em> or nothing). Calling it
     * on the result of {@link #glassProps()} is safe precisely because that result is a brand-new
     * instance with no other consumer. {@code POWERED}'s function must reach this block and no other:
     * the RGB lights' state definition has no {@code POWERED}, so a shared instance makes them throw
     * {@code IllegalArgumentException} while {@code StateHolder} is built.</p>
     */
    private static BlockBehaviour.Properties spotlightProps() {
        return glassProps().lightLevel(state -> state.getValue(BlockSpotLight.POWERED) ? 15 : 0);
    }

    /**
     * Fresh properties for a device block. A new instance per block on purpose: Properties is mutable,
     * so sharing one across dozens of blocks would leak later tweaks into every sibling.
     */
    private static BlockBehaviour.Properties deviceProps(boolean lit) {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
                .mapColor(MapColor.NONE)
                .sound(SoundType.GLASS)
                .strength(2.0F, 10.0F)
                .noOcclusion();
        return lit ? properties.lightLevel(state -> 15) : properties;
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
