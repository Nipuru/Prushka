package server.bukkit.util.font;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

public class LengthyComponent {

    private static final LengthyComponent EMPTY = new LengthyComponent(Component.empty(), 0);

    private final Component symbol;
    private final int length;

    public LengthyComponent(Component symbol, int length) {
        if (symbol != Component.empty() && symbol.style().decoration(TextDecoration.BOLD) == TextDecoration.State.NOT_SET) {
            throw new IllegalArgumentException("invalid bold state");
        }
        this.symbol = symbol;
        this.length = length;
    }

    public Component getSymbol() {
        return symbol;
    }

    public int getLength() {
        return length;
    }

    public static LengthyComponent empty() {
        return EMPTY;
    }

}
