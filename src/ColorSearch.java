import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

public class ColorSearch {
    public static final String DATA_PREFIX = "../";
    public static Collection<ColorNamePair> data = new ArrayList<>();

    public static void main(String[] args) {
        File dataFile = new File(DATA_PREFIX + "satfaces.txt");
        Scanner scanner;
        try {
            scanner = new Scanner(dataFile);
        } catch (FileNotFoundException e){
            System.out.println("File " + DATA_PREFIX + "satfaces.txt not found");
            return;
        }

        while(scanner.hasNextLine()){
            String[] split = scanner.nextLine().split("] ");
            Color c = Color.fromString(split[0]);
            ColorNamePair pair = new ColorNamePair(c, split[1]);
            data.add(pair);
        }

        System.out.println("Done loading " + data.size() + " color-name pairs.");
    }
}