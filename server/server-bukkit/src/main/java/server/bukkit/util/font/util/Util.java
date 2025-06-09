package server.bukkit.util.font.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.internal.parser.node.TagNode;
import net.kyori.adventure.text.minimessage.tag.Inserting;
import net.kyori.adventure.text.minimessage.tree.Node;
import org.jetbrains.annotations.Nullable;
import server.bukkit.util.font.Font;

public class Util {

    public static boolean isBold(Node node) {
        if (node == null) return false;
        Style style = getStyle(node);
        if (style != null) {
            if (style.decorations().get(TextDecoration.BOLD) == TextDecoration.State.TRUE) {
                return true;
            }
        }
        return isBold(node.parent());
    }

    public static String getFont(Node node) {
        if (node == null) {
            return Font.DEFAULT;
        }
        Style style = getStyle(node);
        if (style != null && style.font() != null) {
            return style.font().value();
        }

        return getFont(node.parent());
    }

    public static @Nullable Style getStyle(Node node) {
        if (node instanceof TagNode tagNode) {
            if (tagNode.tag() instanceof Inserting inserting) {
                Component component = inserting.value();
                return component.style();
            }
        }
        return null;
    }
}
