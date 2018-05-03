package ColorClient.Data;

public class ColorNamePair {
    public Color color;
    public String name;

    public ColorNamePair(Color c, String name){
        color = c;
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public Color getColor(){
        return color;
    }

    @Override
    public String toString(){
        return color + ", " + name;
    }
}
