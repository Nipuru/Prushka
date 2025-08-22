package server.bukkit.util.font.resolver;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import server.bukkit.util.font.Bitmap;
import server.bukkit.util.font.LengthyComponent;

import java.util.*;

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

    public LengthyComponent resolve(String args) {
        return resolve(Arrays.asList(args.split(":")));
    }

    public LengthyComponent resolve(List<String> args) {
        if (args.isEmpty()) throw new NullPointerException("bitmap name");
        Bitmap bitmap = Objects.requireNonNull(bitmaps.get(args.get(0)));
        StringBuilder sb = new StringBuilder();
        if (args.size() == 3) {
            int row = parseInt(args.get(1));
            int[] nums = splitToInt(args.get(2));
            if (nums.length >= 2) {
                sb.append(bitmap.getRange(row, nums[0], nums[1]));
            } else {
                sb.append(bitmap.getChar(row, nums[0]));
            }
        } else if (args.size() == 2) {
            int[] nums = splitToInt(args.get(1));
            if (nums.length >= 2) {
                sb.append(bitmap.getRange(nums[0], nums[1]));
            } else {
                sb.append(bitmap.getChar(nums[0]));
            }
        } else {
            sb.append(bitmap.getChar());
        }
        // 强制防止变粗体 bitmap 不应该有粗体
        Style style = Style.style().color(NamedTextColor.WHITE).decoration(TextDecoration.BOLD, false).font(Key.key(bitmap.getFont())).build();
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
        List<String> args = new ArrayList<>();
        while (arguments.hasNext()) {
            args.add(arguments.pop().value());
        }
        return Tag.selfClosingInserting(resolve(args).getSymbol());
    }

}
