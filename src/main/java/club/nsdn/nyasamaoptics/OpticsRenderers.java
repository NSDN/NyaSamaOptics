package club.nsdn.nyasamaoptics;

import club.nsdn.nyasamaoptics.renderer.tileblock.AdBoardRenderer;
import club.nsdn.nyasamaoptics.renderer.tileblock.DeviceModels;
import club.nsdn.nyasamaoptics.renderer.tileblock.GuideBoardRenderer;
import club.nsdn.nyasamaoptics.renderer.tileblock.HoloJetRevRenderer;
import club.nsdn.nyasamaoptics.renderer.tileblock.LEDPlateRenderer;
import club.nsdn.nyasamaoptics.renderer.tileblock.LightRenderer;
import club.nsdn.nyasamaoptics.renderer.tileblock.PillarHeadRenderer;
import club.nsdn.nyasamaoptics.renderer.tileblock.PlatformPlateRenderer;
import club.nsdn.nyasamaoptics.renderer.tileblock.StationBoardRenderer;
import club.nsdn.nyasamaoptics.renderer.tileblock.StationLampRenderer;
import club.nsdn.nyasamaoptics.renderer.tileblock.TextWallRenderer;
import club.nsdn.nyasamaoptics.tileblock.deco.AdBoard;
import club.nsdn.nyasamaoptics.tileblock.deco.GuideBoard;
import club.nsdn.nyasamaoptics.tileblock.deco.StationBoard;
import club.nsdn.nyasamaoptics.tileblock.deco.TextWall;
import club.nsdn.nyasamaoptics.tileblock.holo.HoloJetRev;
import club.nsdn.nyasamaoptics.tileblock.holo.PillarHead;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

/**
 * Client-side registration for the optics renderers and their extra models.
 *
 * <p>Two things have to happen here that did not exist in 1.12.2:</p>
 * <ul>
 *   <li>{@link EntityRenderersEvent.RegisterRenderers} binds a block entity type to its renderer.
 *       1.12.2 did this with {@code ClientRegistry.bindTileEntitySpecialRenderer} during preInit; the
 *       mapping below is taken one-to-one from that list.</li>
 *   <li>{@link ModelEvent.RegisterAdditional} is required because the optics models are addressed at
 *       runtime (they replaced the custom {@code WavefrontObject} loader, decision D4) instead of being
 *       referenced from blockstate JSON. NeoForge requires the {@code standalone} variant, and the same
 *       {@code ModelResourceLocation} must be used later to fetch the baked model -- that is exactly
 *       what {@code DeviceModels} does, so the catalogue drives both sides.</li>
 * </ul>
 *
 * <p>Screen devices bind to the centralised types in {@link OpticsBlockEntities}, the decoration and
 * hologram devices to their own nested {@code TypeHolder}s -- the same split as the registration in
 * {@link OpticsBlocks}.</p>
 */
@EventBusSubscriber(modid = NyaSamaOptics.MODID, value = Dist.CLIENT)
public final class OpticsRenderers {

    private OpticsRenderers() {
    }

    @SubscribeEvent
    static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        bind(event, AdBoard.TypeHolder.HOLDER.get(), AdBoardRenderer::new);
        bind(event, GuideBoard.TypeHolder.HOLDER.get(), GuideBoardRenderer::new);
        bind(event, StationBoard.TypeHolder.HOLDER.get(), StationBoardRenderer::new);
        bind(event, TextWall.TypeHolder.HOLDER.get(), TextWallRenderer::new);
        bind(event, HoloJetRev.TypeHolder.HOLDER.get(), HoloJetRevRenderer::new);
        bind(event, PillarHead.TypeHolder.HOLDER.get(), PillarHeadRenderer::new);

        bind(event, OpticsBlockEntities.LED_PLATE.get(), LEDPlateRenderer::new);
        // PlatformPlateRenderer has no single-argument constructor: the flag distinguishes the full
        // and half plates, which 1.12.2 expressed by constructing the renderer with a boolean.
        bind(event, OpticsBlockEntities.PLATFORM_PLATE_FULL.get(),
                context -> new PlatformPlateRenderer(false));
        bind(event, OpticsBlockEntities.PLATFORM_PLATE_HALF.get(),
                context -> new PlatformPlateRenderer(true));
        bind(event, OpticsBlockEntities.STATION_LAMP.get(), StationLampRenderer::new);
        bind(event, OpticsBlockEntities.RGB_LIGHT.get(), LightRenderer::new);
    }

    /**
     * Binds a type to a renderer despite the wildcard.
     *
     * <p>{@code registerBlockEntityRenderer} is generic in the block entity type, while the optics types
     * are held as {@code BlockEntityType<?>} (the deferred registers erase their element type), so the
     * compiler cannot infer {@code T}. This helper performs the one unavoidable unchecked cast in a
     * single place instead of scattering casts through the registration list. The cast is safe by
     * construction: each provider either takes the context only, or is a lambda producing a renderer
     * whose type parameter matches the block entity class registered for that type.</p>
     */
    @SuppressWarnings("unchecked")
    private static <T extends BlockEntity> void bind(EntityRenderersEvent.RegisterRenderers event,
                                                     BlockEntityType<?> type,
                                                     BlockEntityRendererProvider<T> provider) {
        event.registerBlockEntityRenderer((BlockEntityType<T>) type, provider);
    }

    @SubscribeEvent
    static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        for (ResourceLocation id : DeviceModels.catalogue()) {
            event.register(ModelResourceLocation.standalone(id));
        }
    }
}