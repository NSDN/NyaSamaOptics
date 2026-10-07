package club.nsdn.nyasamaoptics.renderer.tileblock;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;

/**
 * The "sub text" / label drawing shared by the optics renderers, replacing 1.12.2's
 * {@code Minecraft.getMinecraft().fontRenderer.drawString(...)}.
 *
 * <p>Every renderer in this package carried a private, identical {@code renderString(String, double,
 * int, int)} that faithfully reproduced the old {@code FontRenderer} behaviour: the caller's matrix was
 * already scaled by the text's own scale, then {@code glScalef(0.02F, 0.02F, 1.0F)} shrank the font's
 * 9-unit line height and variable glyph width into block space, a vertical centring offset of
 * {@code -lines/2 * FONT_HEIGHT} was applied, each {@code "\n"}-separated line was positioned by the
 * {@code ALIGN_*} switch, and {@code drawString} drew it with the drop shadow the 4-argument overload
 * enables. Modern {@code Font} has no immediate mode, and the four 1.12.2 renderers that used it also
 * differed only in which {@code ALIGN_*} constant set they compared against -- so the logic lives here
 * once and the alignment constants are passed in as {@code (center, left, right)} rather than imported
 * from four different device classes.</p>
 *
 * <p><b>Mapping notes:</b></p>
 * <ul>
 *   <li>{@code getStringWidth(s)} becomes {@link Font#width(String)} and {@code FONT_HEIGHT} becomes
 *       {@link Font#lineHeight}; the arithmetic is otherwise untouched.</li>
 *   <li>{@code drawString(s, x, y, color)} becomes {@link Font#drawInBatch(FormattedCharSequence, float,
 *       float, int, boolean, Matrix4f, MultiBufferSource, Font.DisplayMode, int, int)} -- the
 *       {@code drawShadow = true} argument is the equivalent of that overload, and
 *       {@code Component.literal(line).getVisualOrderText()} is what the {@code String} overload wraps
 *       the text in.</li>
 *   <li>Text is drawn with {@link LightTexture#FULL_BRIGHT}. 1.12.2 relied on the render pass being
 *       {@code RenderType} -independent; these devices light their own displays, so the fixed light is
 *       the faithful choice and matches what the board shells do.</li>
 * </ul>
 */
public final class DeviceText {

    private DeviceText() {
    }

    /**
     * Draws the multi-line string in the current pose.
     *
     * @param str    the text; {@code "\n"} separates lines, as in 1.12.2
     * @param scale  the per-device font scale (the caller has already scaled the pose by it, exactly as
     *               the 1.12.2 code did before calling its private {@code renderString})
     * @param align  the device's alignment value; compare order is center, left, right
     * @param color  packed {@code 0xRRGGBB} colour, the argument of the old {@code drawString}
     */
    public static void drawString(String str, double scale, int align, int color,
                                  int center, int left, int right,
                                  PoseStack poseStack, MultiBufferSource bufferSource) {
        Font font = Minecraft.getInstance().font;
        float s = (float) scale;

        poseStack.pushPose();
        // GL11.glScaled(scale, scale, 1.0)
        poseStack.scale(s, s, 1.0F);
        poseStack.pushPose();
        // GL11.glScalef(0.02F, 0.02F, 1.0F) -- the old font-unit-to-block conversion
        poseStack.scale(0.02F, 0.02F, 1.0F);
        poseStack.pushPose();

        String[] lines = str.split("\n");
        // GL11.glTranslatef(0.0F, -(float) lines.length / 2.0F * renderer.FONT_HEIGHT, 0.0F)
        poseStack.translate(0.0F, -(float) lines.length / 2.0F * font.lineHeight, 0.0F);

        Matrix4f pose = poseStack.last().pose();
        int x = 0;
        int y = 0;
        for (String line : lines) {
            if (align == center) {
                x = -font.width(line) / 2;
            } else if (align == left) {
                x = 0;
            } else if (align == right) {
                x = -font.width(line);
            }
            font.drawInBatch(Component.literal(line).getVisualOrderText(),
                    (float) x, (float) y, color, true, pose, bufferSource,
                    Font.DisplayMode.NORMAL, LightTexture.FULL_BRIGHT, 0);
            y += font.lineHeight;
        }

        poseStack.popPose();
        poseStack.popPose();
        poseStack.popPose();
    }
}
