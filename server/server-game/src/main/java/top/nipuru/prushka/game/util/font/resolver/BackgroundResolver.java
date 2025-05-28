package top.nipuru.prushka.game.util.font.resolver;

import top.nipuru.prushka.game.util.font.FontRepository;
import top.nipuru.prushka.game.util.font.LengthyComponent;
import top.nipuru.prushka.game.util.font.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.Modifying;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tree.Node;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class BackgroundResolver {

    private static final TextColor OFFSET = TextColor.fromHexString("#fffefd");

    private final String tag;
    private final SplitResolver splitResolver;
    private final FontRepository fontRepository;
    private final LengthyComponent prefix;
    private final LengthyComponent suffix;
    private final List<LengthyComponent> middle;
    private final boolean centre;
    private final boolean covered;
    private final Style style;


    public BackgroundResolver(String tag, SplitResolver splitResolver, FontRepository fontRepository, LengthyComponent prefix, LengthyComponent suffix, List<LengthyComponent> middle, boolean centre, boolean covered, Style style) {
        this.tag = tag;
        this.splitResolver = splitResolver;
        this.fontRepository = fontRepository;
        this.prefix = prefix;
        this.suffix = suffix;
        this.middle = middle.stream().sorted(Comparator.comparing(LengthyComponent::getLength).reversed()).toList();
        this.centre = centre;
        this.covered = covered;
        this.style = style;
    }

    public TagResolver resolver() {
        return TagResolver.resolver(tag, new ExtensionBGTag());
    }

    private class ExtensionBGTag implements Modifying {

        private final int rootNode = 0;
        private String font;
        private boolean bold;


        @Override
        public void visit(@NotNull Node current, int depth) {
            if (depth != rootNode) return;
            // 找到 样式节点 获取根节点样式类型
            font = Util.getFont(current);
            bold = Util.isBold(current);
        }

        @Override
        public Component apply(@NotNull Component current, int depth) {
            if (depth != rootNode) return Component.empty();
            TextComponent.Builder head = Component.text();
            TextComponent.Builder bg = Component.text();
            bg.style(style);

            float sw = suffix.getLength() ;
            float width = fontRepository.getTotalWidth(current, bold, font);

            Component split_0 = splitResolver.resolve(-1);

            float fw = 0;
            Component mid = Component.empty();
            for (LengthyComponent text : middle) {
                float lw = width - fw;
                int c = (int) (lw / text.getLength());
                if (c == 0)
                    continue;
                fw += c * text.getLength();
                for (int i = 0; i < c; i++) {
                    mid = mid.append(text.getSymbol())
                            .append(split_0);
                }
            }
            if (fw < width && !covered) {
                LengthyComponent text = middle.get(middle.size() - 1);
                fw += text.getLength();
                mid = mid.append(text.getSymbol())
                        .append(split_0);
            }
            float back = sw + fw;
            if (centre) {
                back += (fw - width) / 2;
            } else if (fw + sw < width) {
                back += width - fw - sw;
            }
            if (style.color() != null && Objects.equals(style.color().value(), OFFSET.value()))
                back -= 1;

            Component split_text = splitResolver.resolve((int) -back - 1);
            Component split_end = splitResolver.resolve((int) Math.ceil(back - width - 1));


            bg.append(prefix.getSymbol())
                    .append(split_0)
                    .append(mid)
                    .append(suffix.getSymbol());

            head.append(bg)
                    .append(split_text)
                    .append(current)
                    .append(split_end);
            return head.build();
        }
    }
}
