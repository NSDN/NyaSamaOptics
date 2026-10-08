package club.nsdn.nyasamaoptics.item;

import club.nsdn.nyasamaoptics.tileblock.holo.TileEntityHoloText;
import club.nsdn.nyasamaoptics.tileblock.light.RGBLight;
import club.nsdn.nyasamaoptics.tileblock.screen.LEDPlate;
import club.nsdn.nyasamaoptics.tileblock.screen.TileEntityPlatformPlate;
import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import club.nsdn.nyasamatelecom.api.tool.ToolBase;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * NSO colour/log probe tool, ported from the 1.12.2
 * {@code club.nsdn.nyasamaoptics.item.NSOConv}.
 *
 * <p>The 1.12.2 source was a {@code ToolBase} registered by {@code ItemLoader} (line 52) as
 * {@code setRegistryName(NyaSamaOptics.MODID, "nso_conv")} with
 * {@code setUnlocalizedName("NSOConv")} and {@code ToolMaterial.IRON}. Clicking a block entity read
 * information out of it and printed it to chat; clicking anything else did nothing.</p>
 *
 * <p><b>Behaviour carried over verbatim</b> (the strings are the 1.12.2 strings, including the
 * {@code "[NSO] "} prefix and the upper-case hex):</p>
 * <ul>
 *   <li>not sneaking, on a colour-carrying device: {@code "[NSO] Color: 0x<hex>"}, except the LED
 *       plate which reports both faces: {@code "[NSO] Fore: 0x<hex>, Back: 0x<hex>"};</li>
 *   <li>sneaking, on a {@link TileEntityBase}: {@code "[NSO] META: 0x<hex>"};</li>
 *   <li>sneaking, on any other optical device: {@code "[NSO] I: <n.n> mW, O: <n.n> mW"}, aqua
 *       coloured;</li>
 *   <li>no block entity at the clicked position: the interaction fails and nothing is printed.</li>
 * </ul>
 *
 * <p>The 1.12.2 code only ran the read while {@code !world.isRemote}, i.e. server side, and sent the
 * result back through {@code player.sendMessage}. Here that is
 * {@code !level.isClientSide} plus {@code displayClientMessage(..., false)} (chat, not the action
 * bar), which is what 1.12.2's {@code sendMessage} did.</p>
 *
 * <p><b>Not ported: the {@code IOpticalDevice} reporting branch.</b> That branch was the
 * {@code else} of the {@code TileEntityBase} test and printed
 * {@code "[NSO] I: <mWInput> mW, O: <mWOutput> mW"} in
 * {@code net.minecraft.util.text.TextFormatting.AQUA}. The interface itself <i>has</i> been ported
 * ({@code core}: {@code org.thewdj.optics.IOpticalDevice} declares {@code mWInput()} and
 * {@code mWOutput()}), but its only implementor in this tree is {@code opticzCraft}'s
 * {@code AbsOpticzDev}, which lives in another module and is not reachable from {@code nyasamaoptics};
 * no ported optics block entity implements it yet. The branch is therefore omitted rather than
 * faked, and is recorded here so it is not mistaken for an oversight.</p>
 */
public class NSOConv extends ToolBase {

    public NSOConv(Item.Properties properties) {
        super(properties, Tiers.IRON);
    }

    /**
     * 1.12.2 {@code onItemRightClick} returned {@code SUCCESS} with the held stack and changed
     * nothing; {@code use} is that method's 1.21.1 shape.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }

    /**
     * 1.12.2 {@code onItemUseFirst(player, world, pos, facing, hitX, hitY, hitZ, hand)}. In 1.21.1 the
     * level, clicked position, player and hand all arrive in the {@link UseOnContext}, and
     * {@code world.getTileEntity(pos)} is {@code level.getBlockEntity(pos)}.
     */
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.FAIL;
        }

        BlockEntity blockEntity = level.getBlockEntity(context.getClickedPos());
        if (blockEntity == null) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide) {
            if (!player.isShiftKeyDown()) {
                if (blockEntity instanceof TileEntityHoloText holoText) {
                    say(player, "[NSO] Color: 0x" + hex(holoText.color));
                    return InteractionResult.SUCCESS;
                } else if (blockEntity instanceof TileEntityPlatformPlate plate) {
                    say(player, "[NSO] Color: 0x" + hex(plate.color));
                    return InteractionResult.SUCCESS;
                } else if (blockEntity instanceof RGBLight.TileEntityRGBLight light) {
                    say(player, "[NSO] Color: 0x" + hex(light.color));
                    return InteractionResult.SUCCESS;
                } else if (blockEntity instanceof LEDPlate.TileEntityLEDPlate plate) {
                    say(player, "[NSO] Fore: 0x" + hex(plate.color)
                            + ", Back: 0x" + hex(plate.back));
                    return InteractionResult.SUCCESS;
                }
            } else {
                if (blockEntity instanceof TileEntityBase tileEntityBase) {
                    say(player, "[NSO] META: 0x" + hex(tileEntityBase.META));
                    return InteractionResult.SUCCESS;
                }
                // NOT PORTED: the IOpticalDevice branch -- see the class note.
            }
        }

        return InteractionResult.FAIL;
    }

    /** 1.12.2 sent a plain chat {@code TextComponentString} to the player. */
    private static void say(Player player, String message) {
        player.displayClientMessage(Component.literal(message), false);
    }

    /**
     * 1.12.2: {@code Integer.toHexString(value).toUpperCase()}. The locale is pinned so the result
     * does not depend on the JVM default locale.
     */
    private static String hex(int value) {
        return Integer.toHexString(value).toUpperCase(java.util.Locale.ROOT);
    }
}
