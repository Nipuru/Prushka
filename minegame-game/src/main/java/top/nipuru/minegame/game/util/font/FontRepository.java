package top.nipuru.minegame.game.util.font;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class FontRepository {
    private final Map<String, Map<Character, Font>> fonts;

    public FontRepository() {
        this.fonts = new HashMap<>();
    }

    public void register(Font... fonts) {
        for (Font font : fonts) {
            this.fonts.computeIfAbsent(font.getFont(), k -> new HashMap<>())
                    .put(font.getCharacter(), font);
        }
    }

    @Nullable
    public Font getFont(String font, char ch) {
        Map<Character, Font> map = fonts.get(font);
        if (map == null) {
            return null;
        }
        return map.get(ch);
    }

    public float getWidth(String font, String text, boolean bold) {
        float width = 0;
        for (char c : text.toCharArray()) {
            Font f = getFont(font, c);
            if (f == null) continue;
            width += bold ? f.getBoldWidth() : f.getWidth();
        }
        return width;
    }

    public float getTotalWidth(Component component, boolean bold, @Nullable String font) {
        String text = component instanceof TextComponent textComponent ? textComponent.content() : "";
        font = component.font() != null ? component.font().value() : font;
        TextDecoration.State state = component.decorations().get(TextDecoration.BOLD);
        boolean b = state == TextDecoration.State.NOT_SET && bold || state == TextDecoration.State.TRUE;
        float width = getWidth(font, text, b);
        if (!component.children().isEmpty()) {
            for (Component c : component.children()) {
                width += getTotalWidth(c, b, font);
            }
        }
        return width;
    }

}
