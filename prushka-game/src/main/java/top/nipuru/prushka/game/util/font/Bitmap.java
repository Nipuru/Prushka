package top.nipuru.prushka.game.util.font;

public class Bitmap {
    final String[] chars;
    final String string;
    final String font;
    final String name;
    final int width;

    public Bitmap(String name, String font, int width, String... chars) {
        this.name = name;
        this.font = font;
        this.width = width;
        this.chars = chars;
        StringBuilder sb = new StringBuilder();
        for (String s : chars) {
            sb.append(s);
        }
        string = sb.toString();
        if (chars.length == 0) {
            System.out.println("bitmap :" + name + " 为空!");
        }
    }

    public String getName() {
        return name;
    }

    public String getFont() {
        return font;
    }

    public int getWidth() {
        return width;
    }

    public int getSize() {
        return string.length();
    }

    public int getRows() {
        return chars.length;
    }

    public int getRowSize(int row) {
        return chars.length > row ? chars[row].length() : 0;
    }

    public char getChar() {
        return string.charAt(0);
    }

    public char getChar(int index) {
        return string.charAt(index % string.length());
    }

    public char getChar(int row, int col) {
        String str = chars[row % chars.length];
        return str.charAt(col % str.length());
    }

    public String getRange(int startInclude, int endExclude) {
        return string.substring(startInclude % string.length(), Math.min(endExclude, string.length()));
    }

    public String getRange(int row, int startInclude, int endExclude) {
        String str = chars[row % chars.length];
        return str.substring(startInclude % str.length(), Math.min(endExclude, str.length()));
    }
}
