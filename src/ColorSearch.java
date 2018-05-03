import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ColorSearch {
    public static final String DATA_PREFIX = "../";
    public static OcTree<ColorNamePair> data;

    public static void main(String[] args) {
        data = new OcTree<>(Point3D.zero(), new Point3D(255), pair -> {
            Color c = pair.getColor();
            return new Point3D(c.r, c.g, c.b);
        });

        File dataFile = new File(DATA_PREFIX + "satfaces.txt");
        Scanner scanner;
        try {
            scanner = new Scanner(dataFile);
        } catch (FileNotFoundException e) {
            System.out.println("File " + DATA_PREFIX + "satfaces.txt not found");
            return;
        }

        while (scanner.hasNextLine()) {
            String[] split = scanner.nextLine().split("] ");
            Color c = Color.fromString(split[0]);
            ColorNamePair pair = new ColorNamePair(c, split[1]);
            data.add(pair);
        }

        System.out.println("Done loading " + data.size() + " color-name pairs.");
    }
}