package ColorClient.Data;

import java.util.Objects;

public class Color {
    public int r;
    public int g;
    public int b;

    public Color(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public static Color fromString(String s) {
        String color = s.replaceAll("[^0-9.]", " ");
        String[] components = color.split(" ");
        return new Color(Integer.parseInt(components[1]),
                Integer.parseInt(components[3]),
                Integer.parseInt(components[5]));
    }

    @Override
    public String toString(){
        return "[" + r + ", " + g + ", " + b + "]";
    }

    public double distanceTo(Color other) {
        return Math.sqrt(other.r * other.r + other.g + other.g + other.b * other.b);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Color color = (Color) o;
        return r == color.r &&
                g == color.g &&
                b == color.b;
    }

    public java.awt.Color toAwtColor() {
        return new java.awt.Color(r, g, b);
    }

    @Override
    public int hashCode() {
        return Objects.hash(r, g, b);
    }
}
