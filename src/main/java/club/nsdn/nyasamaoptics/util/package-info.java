/**
 * Optics script dialects and the bitmap-font text geometry.
 *
 * <p>Ported from the 1.12.2 {@code club.nsdn.nyasamaoptics.util}: seven NSASM script cores plus the
 * {@code util.font} subpackage. The flavour is the same as everywhere else in v3 -- original package and
 * class names, method semantics word for word, and every deviation explained in the class javadoc.</p>
 *
 * <h2>Contents</h2>
 * <ul>
 *   <li>{@link club.nsdn.nyasamaoptics.util.RGBLightCore} -- {@code clr}/{@code fore} for the RGB
 *       light.</li>
 *   <li>{@link club.nsdn.nyasamaoptics.util.DecoTextCore} -- the largest dialect ({@code show},
 *       {@code clr}/{@code fore}, {@code thk}, {@code scl}, {@code aln}, {@code font}, the
 *       {@code sub.*} family and the offsets) for {@code TileEntityDecoText} and its subclasses
 *       (ad board, station board, text wall, guide board).</li>
 *   <li>{@link club.nsdn.nyasamaoptics.util.HoloJetRevCore} and
 *       {@link club.nsdn.nyasamaoptics.util.PillarHeadCore} -- the two hologram dialects (the latter has
 *       no {@code aln}, exactly as in 1.12.2).</li>
 *   <li>{@link club.nsdn.nyasamaoptics.util.LEDPlateCore} and
 *       {@link club.nsdn.nyasamaoptics.util.PlatformPlateCore} -- the panel dialects.</li>
 *   <li>{@link club.nsdn.nyasamaoptics.util.StationLampCore} -- the station lamp, including the
 *       {@code inj}/{@code rej} list editing and the {@code logo} report.</li>
 *   <li>{@link club.nsdn.nyasamaoptics.util.font.FontLoader} -- the seven 1bpp glyph tables
 *       ({@code assets/nyasamaoptics/fonts/*.bin}).</li>
 *   <li>{@link club.nsdn.nyasamaoptics.util.font.TextModel} -- glyph bitmap to cube geometry plus the
 *       render entry point; the one place in this package where the 1.12.2 body could not be carried
 *       over as-is (the discarded {@code cn.ac.nya.rawmdl}/{@code cn.ac.nya.mutable} stack, decision
 *       D4), documented in that class.</li>
 * </ul>
 *
 * <p>The cores are abstract and stay abstract: {@code getWorld()}/{@code getX()}/{@code getY()}/
 * {@code getZ()}/{@code getPlayer()} and a per-device {@code getTile()} are supplied by the anonymous
 * subclass in each device's interaction hook, exactly as in 1.12.2.</p>
 *
 * <p>Every core lost one method on the way over: the 1.12.2 {@code getWrapper()} override, which handed
 * the script engine a {@code SimpleNetworkWrapper}. The ported telecom {@code NSASM} removed that hook
 * (1.21.1 sends packets through a {@code ServerLevel}, and {@code getWorld()} already exposes the
 * level), so the copy is not re-invented here.</p>
 *
 * <p>Two deprecation notes are expected in this package and are deliberate:
 * {@code cn.ac.nya.nsasm.NSASM.Operator} is {@code @Deprecated} in v3 (with {@code Operator3} as the
 * newer shape), but the whole script dialect family -- telecom's own {@code NSASM} included -- keeps the
 * 1.12.2 {@code loadFunc(LinkedHashMap&lt;String, Operator&gt;)} signature, and changing that is a
 * project-wide decision rather than a local one; and
 * {@code TextureAtlas.LOCATION_BLOCKS} is {@code @Deprecated} in 21.1.256 (verified with javap) while
 * remaining the only public handle on the block sheet, which is why {@code TextModel} still uses it --
 * see that class.</p>
 */
package club.nsdn.nyasamaoptics.util;
