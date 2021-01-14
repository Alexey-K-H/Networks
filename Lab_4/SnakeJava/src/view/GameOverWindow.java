package view;


import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GameOverWindow
{
    public static void display(TableView<Score> scores)
    {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Results");

        VBox vbox = new VBox();

        Button okButton = new Button("OK");
        okButton.setOnAction( actionEvent -> window.close());

        vbox.getChildren().addAll(scores, okButton);

        Scene scene = new Scene(vbox, 150, 450);

        window.setScene(scene);

        window.showAndWait();
    }
}
