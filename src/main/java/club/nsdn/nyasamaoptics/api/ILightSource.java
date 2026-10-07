package club.nsdn.nyasamaoptics.api;

/**
 * Marker interface for blocks that emit a light beam, ported unchanged from the 1.12.2
 * {@code ILightSource}.
 *
 * <p>It stays empty on purpose: {@code LightBeam} only needs to ask "is the neighbour a light
 * source", and the information a source would otherwise have to expose is already on its block state
 * (facing, powered). Implementing it is what opts a block into the beam system.</p>
 */
public interface ILightSource {
}
