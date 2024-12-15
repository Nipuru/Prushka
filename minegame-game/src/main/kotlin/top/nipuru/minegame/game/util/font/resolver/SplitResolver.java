package top.nipuru.minegame.game.util.font.resolver;

import top.nipuru.minegame.game.util.font.LengthyComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Comparator;
import java.util.List;

public class SplitResolver {

    private static final String SPLIT = "split";

    private final LengthyComponent[] splits = new LengthyComponent[513];

    public SplitResolver(List<LengthyComponent> initials) {
        List<LengthyComponent> sorted = initials.stream().sorted(Comparator.comparing(LengthyComponent::getLength)).toList();
        for (int i = -256; i <= 256; i++) {
            push(answer(sorted, i));
        }
    }

    public Component resolve(int length) {
        if (length < -256 || length > 256) {
            TextComponent.Builder sb = Component.text();
            int left = length;
            while (left != 0) {
                if (left > 256) {
                    sb.append(resolve(256));
                    left -= 256;
                } else if (left < -256) {
                    sb.append(resolve(-256));
                    left += 256;
                } else {
                    sb.append(resolve(left));
                    left = 0;
                }
            }
            return sb.build();
        } else {
            return splits[index(length)].getSymbol();
        }
    }

    public TagResolver resolver() {
        return TagResolver.resolver(SPLIT, this::create);
    }

    private Tag create(ArgumentQueue arguments, Context context) {
        int length = Integer.parseInt(arguments.popOr("Expected to find a split length").value());
        return Tag.selfClosingInserting(resolve(length));
    }

    private int index(int length) {
        return length + 256;
    }

    private LengthyComponent answer(List<LengthyComponent> sorted, int length) {
        if (length == 0) {
            return LengthyComponent.empty();
        }
        boolean reverse = length > 0;
        int i = reverse ? sorted.size() - 1 : 0;
        int left = length;
        TextComponent.Builder builder = Component.text();
        // 强制防止变粗体 split 不应该有粗体
        builder.style(Style.style().decoration(TextDecoration.BOLD, false).build());
        while (left != 0) {
            LengthyComponent sp = sorted.get(i);
            while (reverse && sp.getLength() <= left || !reverse && sp.getLength() >= left) {
                left -= sp.getLength();
                builder.append(sp.getSymbol());
            }
            i += (reverse ? -1 : 1);
        }

        return new LengthyComponent(builder.build(), length);
    }

    private void push(LengthyComponent split) {
        if (split.getLength() <= 256 && split.getLength() >= -256) {
            splits[index(split.getLength())] = split;
        }
    }
}
