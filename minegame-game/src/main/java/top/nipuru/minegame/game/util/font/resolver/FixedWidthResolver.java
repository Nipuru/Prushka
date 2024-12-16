package top.nipuru.minegame.game.util.font.resolver;

import top.nipuru.minegame.game.util.font.FontRepository;
import top.nipuru.minegame.game.util.font.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.internal.serializer.Emitable;
import net.kyori.adventure.text.minimessage.tag.Modifying;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tree.Node;
import org.jetbrains.annotations.NotNull;

public class FixedWidthResolver {

    private static final String FIXED_WIDTH = "fixed_width";

    private final SplitResolver splitResolver;
    private final FontRepository fontRepository;

    public FixedWidthResolver(SplitResolver splitResolver, FontRepository fontRepository) {
        this.splitResolver = splitResolver;
        this.fontRepository = fontRepository;
    }

    public TagResolver resolver() {
        return TagResolver.resolver(FIXED_WIDTH, this::create);
    }

    private Tag create(ArgumentQueue arguments, Context context) {
        Position position = Position.valueOf(arguments.popOr("Expected to find a fixed position").value().toUpperCase());
        float fixedWidth = Float.parseFloat(arguments.popOr("Expected to find a fixed width").value());
        return new FixedWidthTag(position, fixedWidth);
    }

    private Emitable emit(Component component) {
        return null;
    }

    public enum Position {
        LEFT, CENTER, RIGHT;
    }

    private class FixedWidthTag implements Modifying {

        public final int rootNode = 0;
        private final Position position;
        private final float fixedWidth;
        private String font;
        private boolean bold;

        private FixedWidthTag(Position position, float fixedWidth) {
            this.position = position;
            this.fixedWidth = fixedWidth;
        }

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
            // 运用宽度并返回组件
            float textWidth = fontRepository.getTotalWidth(current, bold, font);
            Component left = Component.empty();
            Component right = Component.empty();
            if (position == Position.LEFT) {
                right = splitResolver.resolve((int) (fixedWidth - textWidth));
            } else if (position == Position.RIGHT) {
                left = splitResolver.resolve((int) (fixedWidth - textWidth));
            } else {
                int rightWidth = (int) ((fixedWidth - textWidth) / 2);
                int leftWidth = (int) (fixedWidth - textWidth - rightWidth);
                left = splitResolver.resolve(leftWidth);
                right = splitResolver.resolve(rightWidth);
            }
            return Component.empty().append(left).append(current).append(right);
        }
    }


}
