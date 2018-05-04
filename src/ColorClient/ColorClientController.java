package ColorClient;

import Data.*;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.stream.Collectors;

public class ColorClientController implements Initializable {
    public static final String DATA_PREFIX = "./data/";
    public static OcTree<ColorNamePair> data;

    public static final String COLORS_BY_NAME =
            "SELECT answers.r, answers.g, answers.b, names.colorname, names.numusers FROM jdorsey.answers, jdorsey.names WHERE names.colorname = answers.colorname";

    @FXML
    private ColorPicker picker;
    @FXML
    private TableView<ColorNamePairWrapper> table;
    @FXML
    private TableColumn<ColorNamePairWrapper, Color> colorColumn;
    @FXML
    private TableColumn<ColorNamePairWrapper, String> nameColumn;
    @FXML
    private TableColumn<ColorNamePairWrapper, Integer> frequencyColumn;
    @FXML
    private TextArea console;

    public ColorClientController() {
        data = new OcTree<>(Point3D.zero(), new Point3D(256), ColorUtils::toRGBSpace, 3);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colorColumn.setCellValueFactory(data -> data.getValue().getColorProperty());
        colorColumn.setComparator((a, b) -> {
            Color picked = ColorUtils.fromFxColor(picker.getValue());
            return a.distanceTo(picked) > b.distanceTo(picked) ? 1 : 0;
        });
        colorColumn.setCellFactory(column -> new ColorCell());
        nameColumn.setCellValueFactory(data -> data.getValue().getNameProperty());
        frequencyColumn.setCellValueFactory(data -> data.getValue().getFrequencyProperty());
    }

    private void updateTable() {
        javafx.scene.paint.Color c = picker.getValue();
        logToConsole("Retrieving OcTree leaf for " + c);
        OcTree<ColorNamePair> tree = data.getOctantBy(data.getCordMapper().apply(ColorUtils.toColorNamePair(c)));
        logToConsole("Updating table...");
        table.getItems().clear();
        // Convert the items to wrapped items
        Collection<ColorNamePairWrapper> items = tree.collectValues().parallelStream()
                .map(ColorNamePairWrapper::new)
                .collect(Collectors.toList());
        table.getItems().addAll(items);
        logToConsole("Table updated.");
    }

    private void logToConsole(String toLog) {
        console.setText(console.getText() + toLog + "\n");
    }

    @FXML
    private void loadFromDatabase() {
        logToConsole("OcTree cleared... querying database.");
        data.clear();
        ResultSet colors = DatabaseManager.runQuery(COLORS_BY_NAME);
        try {
            while (colors.next()) {
                int r = colors.getInt("r");
                int g = colors.getInt("g");
                int b = colors.getInt("b");
                int frequency = colors.getInt("numusers");
                String colorname = colors.getString("colorname");
                ColorNamePair pair = new ColorNamePair(new Color(r, g, b), colorname, frequency);
                data.add(pair);
            }
            logToConsole("Loaded " + data.size() + " colors into OcTree.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getSQLState());
        }
        updateTable();
    }

//    @FXML
//    private void loadFromFile() {
//        File dataFile = new File(DATA_PREFIX + fileName.getText());
//        Scanner scanner;
//        data.clear();
//
//        try {
//            scanner = new Scanner(dataFile);
//            System.out.println("File " + DATA_PREFIX + fileName.getText() + " found.");
//        } catch (FileNotFoundException e) {
//            System.out.println("File " + DATA_PREFIX + fileName.getText() + " not found");
//            return;
//        }
//
//        while (scanner.hasNextLine()) {
//            String[] split = scanner.nextLine().split("] ");
//            if (split.length == 2) {
//                Color c = Color.fromString(split[0]);
//                ColorNamePair pair = new ColorNamePair(c, split[1], 0);
//                data.add(pair);
//            }
//        }
//        scanner.close();
//        updateTable();
//    }

    @FXML
    private void colorPickerAction() {
        updateTable();
    }

    private class ColorNamePairWrapper {
        private StringProperty nameProperty;
        private ObservableValue<Integer> frequencyProperty;
        private ObjectProperty<Color> colorProperty;

        ColorNamePairWrapper(ColorNamePair pair) {
            nameProperty = new SimpleStringProperty(pair.getName());
            frequencyProperty = new SimpleIntegerProperty(pair.getFrequency()).asObject();
            colorProperty = new SimpleObjectProperty<>(pair.getColor());
        }

        ObservableValue<String> getNameProperty() {
            return nameProperty;
        }

        ObservableValue<Integer> getFrequencyProperty() {
            return frequencyProperty;
        }

        ObservableValue<Color> getColorProperty() {
            return colorProperty;
        }
    }

    private class ColorCell extends TableCell<ColorNamePairWrapper, Color> {
        @Override
        protected void updateItem(Color item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty) {
                String colorString = ColorUtils.toHexColor(item);
                setStyle("-fx-background-color:" + colorString);
                setTooltip(new Tooltip(colorString));
            }
        }
    }
}
