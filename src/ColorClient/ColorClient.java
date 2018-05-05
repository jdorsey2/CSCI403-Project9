package ColorClient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

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