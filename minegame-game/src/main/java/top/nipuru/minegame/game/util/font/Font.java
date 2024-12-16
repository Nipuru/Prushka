package top.nipuru.minegame.game.util.font;

public class Font {
    public static final String DEFAULT = "default";

    private final String font;
    private final char character;
    private final float width, boldWidth;

    protected Font(String font, char character, float width, float boldWidth) {
        this.font = font;
        this.character = character;
        this.width = width;
        this.boldWidth = boldWidth;
    }

    public String getFont() {
        return font;
    }

    public char getCharacter() {
        return character;
    }

    public float getWidth() {
        return width;
    }

    public float getBoldWidth() {
        return boldWidth;
    }

    public static Font steve(char character, int width) {
        return new Font("default", character, width, width + 1);
    }

    public static Font steve(String font,char character, int width) {
        return new Font(font, character, width, width + 1);
    }

    public static Font slim(char character, float width) {
        return new Font(DEFAULT, character, width, width + 0.5f);
    }

    public static Font slim(String font,char character, float width) {
        return new Font(font, character, width, width + 0.5f);
    }

    public static Font font(char character, float width, float boldWidth) {
        return new Font(DEFAULT, character, width, boldWidth);
    }

    public static Font font(Bitmap bitmap, float width, float boldWidth) {
        return new Font(bitmap.getFont(), bitmap.getChar(), width, boldWidth);
    }

    public static Font[] steves(String string, int width) {
        Font[] fonts = new Font[string.length()];
        for (int i = 0; i < string.length(); i++) {
            fonts[i] = steve(string.charAt(i), width);
        }
        return fonts;
    }

    public static Font[] steves(Bitmap bitmap, int width) {
        Font[] fonts = new Font[bitmap.getSize()];
        for (int i = 0; i < bitmap.getSize(); i++) {
            fonts[i] = steve(bitmap.getChar(i), width);
        }
        return fonts;
    }

    public static Font[] slims(String string, float width) {
        Font[] fonts = new Font[string.length()];
        for (int i = 0; i < string.length(); i++) {
            fonts[i] = slim(string.charAt(i), width);
        }
        return fonts;
    }

    public static Font[] slims(Bitmap bitmap, float width) {
        Font[] fonts = new Font[bitmap.getSize()];
        for (int i = 0; i < bitmap.getSize(); i++) {
            fonts[i] = slim(bitmap.getChar(i), width);
        }
        return fonts;
    }

    public static Font[] fonts(String string, float width, float boldWidth) {
        Font[] fonts = new Font[string.length()];
        for (int i = 0; i < string.length(); i++) {
            fonts[i] = font(string.charAt(i), width, boldWidth);
        }
        return fonts;
    }

    public static Font[] fonts(Bitmap bitmap, float width, float boldWidth) {
        Font[] fonts = new Font[bitmap.getSize()];
        for (int i = 0; i < bitmap.getSize(); i++) {
            fonts[i] = new Font(bitmap.getFont(), bitmap.getChar(i), width, boldWidth);
        }
        return fonts;
    }
}
