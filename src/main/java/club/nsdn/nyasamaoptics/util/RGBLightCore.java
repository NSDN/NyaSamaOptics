package club.nsdn.nyasamaoptics.util;

import club.nsdn.nyasamaoptics.tileblock.light.RGBLight;
import club.nsdn.nyasamatelecom.api.util.NSASM;
import club.nsdn.nyasamatelecom.api.util.Util;
import net.minecraft.world.entity.player.Player;

import java.util.LinkedHashMap;

/**
 * Script dialect of the RGB light, ported from the 1.12.2
 * {@code club.nsdn.nyasamaoptics.util.RGBLightCore}.
 *
 * <p>Two functions are registered -- {@code clr} and {@code fore}, both setting the light colour --
 * and their bodies are carried over word for word, including the deliberate quirk that
 * {@code fore} is an alias of {@code clr} rather than a separate foreground colour.</p>
 *
 * <p>What changed, and why:</p>
 * <ul>
 *   <li><b>{@code getWrapper()} no longer exists.</b> The 1.12.2 class overrode it to hand the script
 *       engine a {@code SimpleNetworkWrapper}; the ported telecom {@link NSASM} removed that hook
 *       (1.21.1 sends packets through {@code ServerLevel}, and {@code getWorld()} already exposes the
 *       level), so the override is dropped here rather than re-invented. Same decision, same reason as
 *       the telecom dialect.</li>
 *   <li>{@code EntityPlayer} becomes {@link Player}; {@code Util.say} keeps its
 *       {@code (player, translationKey, args...)} shape, so the message
 *       {@code info.light.color -> <hex>} is unchanged.</li>
 * </ul>
 *
 * <p>{@link #setColor} keeps the 1.12.2 order of operations: mask to {@code 0xFFFFFF}, push the block
 * update through {@code refresh()}, then report the new colour to the acting player.</p>
 */
public abstract class RGBLightCore extends NSASM {

    public RGBLightCore(String[][] code) {
        super(code);
    }

    public void setColor(RGBLight.TileEntityRGBLight light, Player player, int value) {
        light.color = value & 0xFFFFFF;
        light.refresh();
        Util.say(player, "info.light.color", Integer.toHexString(light.color).toUpperCase());
    }

    @Override
    public void loadFunc(LinkedHashMap<String, Operator> funcList) {
        funcList.put("clr", ((dst, src) -> {
            if (src != null) return Result.ERR;
            if (dst == null) return Result.ERR;

            if (dst.type == RegType.INT) {
                setColor(getLight(), getPlayer(), (int) dst.data);
                return Result.OK;
            }
            return Result.ERR;
        }));
        funcList.put("fore", ((dst, src) -> {
            if (src != null) return Result.ERR;
            if (dst == null) return Result.ERR;

            if (dst.type == RegType.INT) {
                setColor(getLight(), getPlayer(), (int) dst.data);
                return Result.OK;
            }
            return Result.ERR;
        }));
    }

    public abstract RGBLight.TileEntityRGBLight getLight();

}
