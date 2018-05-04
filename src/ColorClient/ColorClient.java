package ColorClient;

import Data.Color;
import Data.ColorNamePair;
import Data.OcTree;
import Data.Point3D;
import Data.DatabaseManager;
import Data.PLYExporter;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class ColorClient extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("ColorClient.fxml"));

        primaryStage.setTitle("XKCD Color Survey Explorer");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }
}