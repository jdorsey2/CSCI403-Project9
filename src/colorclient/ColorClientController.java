package colorclient;

import data.*;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import tools.QueryRunner;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ColorClientController implements Initializable {
    public static final String DATA_PREFIX = "./data/";
    public static final int TABLE_MAX_SIZE = 1000;
    public static OcTree<ColorNamePair> data;

    private static final String COLORS_BY_NAME =
            "SELECT answers.r, answers.g, answers.b, names.colorname, names.numusers FROM jdorsey.answers, jdorsey.names WHERE names.colorname = answers.colorname";
    private static final String COLORS_BY_NAME_FREQ = "SELECT AVG(answers.r) AS r, AVG(answers.g) AS g, AVG(answers.b) AS b, names.numusers, names.colorname FROM jdorsey.answers, jdorsey.names WHERE answers.colorname = names.colorname AND names.numusers >= 1 GROUP BY names.colorname, names.numusers HAVING (STDDEV_SAMP(r) < 60 OR STDDEV_SAMP(g) < 60 OR STDDEV_SAMP(b) < 60) OR (STDDEV_SAMP(r) IS NULL OR STDDEV_SAMP(g) IS NULL OR STDDEV_SAMP(b) IS NULL);";

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
    @FXML
    private TextField textSearch;
    @FXML
    private ToggleGroup searchToggle;
    @FXML
    private ToggleButton colorButton;
    @FXML
    private ToggleButton textButton;
    private ChangeListener<Toggle> toggleChangeListener;

    public ColorClientController() {
        data = new OcTree<>(Point3D.zero(), new Point3D(256.1), ColorUtils::toRGBSpace, 3);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set up the color column to:
        //     a) Sort by how far a color is from the currently selected one, if applicable
        //     b) Use a special cell factory to show the color in the cell
        colorColumn.setCellValueFactory(data -> data.getValue().getColorProperty());
        colorColumn.setComparator((a, b) -> {
            if (searchToggle.getSelectedToggle() == picker) {
                Color picked = ColorUtils.fromFxColor(picker.getValue());
                return a.distanceTo(picked) < b.distanceTo(picked) ? 1 : -1;
            } else {
                String webColorA = ColorUtils.toHexColor(a);
                String webColorB = ColorUtils.toHexColor(b);
                return webColorA.compareTo(webColorB);
            }
        });
        colorColumn.setCellFactory(column -> new ColorCell());

        // Set up the name column to get the name
        nameColumn.setCellValueFactory(data -> data.getValue().getNameProperty());

        // Set up the frequency column to get the frequency
        frequencyColumn.setCellValueFactory(data -> data.getValue().getFrequencyProperty());

        // Make the color picker and text field not affect layout when they're hidden
        picker.managedProperty().bind(picker.visibleProperty());
        textSearch.managedProperty().bind(textSearch.visibleProperty());

        // Search behavior
        toggleChangeListener = (observable, oldValue, newValue) -> {
            if (newValue == colorButton) {
                picker.setVisible(true);
                textSearch.setVisible(false);
                colorPickerAction();
            } else if (newValue == textButton) {
                picker.setVisible(false);
                textSearch.setVisible(true);
                textSearchAction();
            }
            if (newValue == null) {
                // It's hacky, but this causes the selectToggle call not to fire the changelistener
                // which just avoids an unnecessary call to colorPickerAction() or textSearchAction().
                searchToggle.selectedToggleProperty().removeListener(toggleChangeListener);
                searchToggle.selectToggle(oldValue);
                searchToggle.selectedToggleProperty().addListener(toggleChangeListener);
            }
        };
        searchToggle.selectedToggleProperty().addListener(toggleChangeListener);

        // Auto-scroll console to bottom
        console.textProperty().addListener(((observable, oldValue, newValue) -> console.setScrollTop(Double.MAX_VALUE)));
    }

    private void updateTable(Collection<ColorNamePairWrapper> items) {
        table.getItems().clear();
        table.getItems().addAll(items);
        logToConsole("Table updated.");
    }

    private void logToConsole(String toLog) {
        console.appendText(toLog + "\n");
        System.out.println(toLog);
    }

    @FXML
    private void loadFromDatabase() {
        Platform.runLater(() -> logToConsole("OcTree cleared... querying database."));
        data.clear();
        QueryTimer timer = new QueryTimer(time -> Platform.runLater(() -> logToConsole(String.format("Query running... %.2fs", time))),
                time -> Platform.runLater(() -> logToConsole(String.format("Finished database query in %.2fs", time))), 2000);
        timer.start();
        QueryRunner runner = new QueryRunner(COLORS_BY_NAME_FREQ, colors -> {
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
                Platform.runLater(() -> logToConsole("Loaded " + data.size() + " colors into OcTree."));
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println(e.getSQLState());
            }
            Platform.runLater(this::colorPickerAction);
            timer.finish();
        });
        runner.start();
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
        javafx.scene.paint.Color c = picker.getValue();
        logToConsole("Retrieving OcTree leaf for " + c);
        ColorNamePair pickerPair = ColorUtils.toColorNamePair(c);
        OcTree<ColorNamePair> tree = data.getOctantBy(data.getCordMapper().apply(pickerPair));
        Point3D pickerPosition = tree.getCordMapper().apply(pickerPair);

        // Convert the items to wrapped items
        Collection<ColorNamePair> values = tree.collectValues();
        logToConsole("Retrieved " + values.size() + " nodes.");
        if (values.size() < TABLE_MAX_SIZE) {
            int sizeBeforeAdjacent = values.size();
            logToConsole("Retrieving adjacent nodes for " + c);
            tree.getAdjacentLeaves().forEach(leaf -> values.addAll(leaf.collectValues()));
            logToConsole("Retrieved " + (values.size() - sizeBeforeAdjacent) + " colors from adjacent nodes.");
        }
        Collection<ColorNamePairWrapper> items = tree.collectValues().parallelStream()
                .limit(TABLE_MAX_SIZE)
                .sorted(Comparator.comparing(pair -> Point3D.manhattanDistance(pickerPosition, tree.getCordMapper().apply(pair))))
                .map(ColorNamePairWrapper::new)
                .collect(Collectors.toList());
        updateTable(items);
    }

    @FXML
    private void textSearchAction() {
        String searchText = textSearch.getText();
        if (!searchText.isEmpty()) {
            Collection<ColorNamePairWrapper> filtered = data.collectValues().stream()
                    .filter(pair -> pair.getName().contains(searchText))
                    .limit(TABLE_MAX_SIZE)
                    .sorted(Comparator.comparing(ColorNamePair::getName))
                    .map(ColorNamePairWrapper::new)
                    .collect(Collectors.toList());
            updateTable(filtered);
        } else {
            Collection<ColorNamePairWrapper> filtered = data.collectValues().stream()
                    .sorted(Comparator.comparing(ColorNamePair::getName))
                    .limit(TABLE_MAX_SIZE)
                    .map(ColorNamePairWrapper::new)
                    .collect(Collectors.toList());
            updateTable(filtered);
        }
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
            } else {
                setStyle("-fx-background-color: inherit");
            }
        }
    }

    //TODO This should probably create new Timer objects, since cancelled Timers can't be restarted
    private class QueryTimer extends Timer {
        private long startTime;
        private Consumer<Double> task;
        private Consumer<Double> finish;
        private long interval;

        public QueryTimer(Consumer<Double> scheduled, Consumer<Double> finish, long interval) {
            super("Database Query Timer", true);
            this.task = scheduled;
            this.finish = finish;
            this.interval = interval;
        }

        public void start() {
            startTime = System.nanoTime();
            scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    double elapsedTime = (System.nanoTime() - startTime) / 1_000_000_000.;
                    task.accept(elapsedTime);
                }
            }, interval, interval);
        }

        public void finish() {
            cancel();
            finish.accept((System.nanoTime() - startTime) / 1_000_000_000.);
        }
    }
}
