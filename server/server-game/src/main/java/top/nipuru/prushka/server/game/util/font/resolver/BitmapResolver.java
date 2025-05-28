package top.nipuru.prushka.server.game.util.font.resolver;

import top.nipuru.prushka.server.game.util.font.Bitmap;
import top.nipuru.prushka.server.game.util.font.LengthyComponent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Map;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public class BitmapResolver {

    private static final String BITMAP = "bitmap";

    private final Map<String, Bitmap> bitmaps;

    public BitmapResolver(Map<String, Bitmap> bitmaps) {
        this.bitmaps = bitmaps;
    }

    public TagResolver resolver() {
        return TagResolver.resolver(BITMAP, this::create);
    }

    public LengthyComponent resolve(String code) {
        String[] args = code.split(";");
        if (args.length == 0) throw new NullPointerException("bitmap name");
        Bitmap bitmap = Objects.requireNonNull(bitmaps.get(args[0]));
        StringBuilder sb = new StringBuilder();
        if (args.length == 3) {
            int row = parseInt(args[1]);
            int[] nums = splitToInt(args[2]);
            if (nums.length >= 2) {
                sb.append(bitmap.getRange(row, nums[0], nums[1]));
            } else {
                sb.append(bitmap.getChar(row, nums[0]));
            }
        } else if (args.length == 2) {
            int[] nums = splitToInt(args[1]);
            if (nums.length >= 2) {
                sb.append(bitmap.getRange(nums[0], nums[1]));
            } else {
                sb.append(bitmap.getChar(nums[0]));
            }
        } else {
            sb.append(bitmap.getChar());
        }
        // 强制防止变粗体 bitmap 不应该有粗体
        Style style = Style.style().decoration(TextDecoration.BOLD, false).font(Key.key(bitmap.getFont())).build();
        return new LengthyComponent(Component.text(sb.toString(), style), bitmap.getWidth());
    }

    private static int parseInt(String s) {
        if (s.isEmpty()) return 0;
        return Integer.parseInt(s);
    }

    private static int[] splitToInt(String arg) {
        String[] split = arg.split("-");
        int[] nums = new int[split.length];
        for (int i = 0; i < split.length; i++) {
            nums[i] = parseInt(split[i]);
        }
        return nums;
    }

    private Tag create(ArgumentQueue arguments, Context context) {
        String code = arguments.popOr("Expected to find a bitmap code").value();
        return Tag.selfClosingInserting(resolve(code).getSymbol());
    }

}
