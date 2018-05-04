package Data;

public class ColorNamePair {
    private Color color;
    private String name;
    private int frequency;

    public ColorNamePair(Color c, String name, int frequency) {
        color = c;
        this.name = name;
        this.frequency = frequency;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public int getFrequency() {
        return frequency;
    }

    @Override
    public String toString() {
        return color + " " + name;
    }
}
