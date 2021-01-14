package view;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class LoginWindow
{

    private static String name;

    private static boolean loggined = false;

    public static boolean display()
    {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Login");

        Label loginLabel = new Label("Enter name");

        TextField loginInput = new TextField();

        Button loginButton = new Button("login");

        loginButton.setOnAction(actionEvent ->
        {
            name = loginInput.getText();
            if(name.equals("") || name.length() > 20)
            {
                ErrorBox.display("Name should be not null and not longer that 20 symbols");
                return;
            }

            loggined = true;
            window.close();

        });

        VBox vbox = new VBox();

        vbox.getChildren().addAll(loginLabel, loginInput, loginButton);

        Scene scene = new Scene(vbox, 250, 75);

        window.setScene(scene);

        window.showAndWait();

        return loggined;
    }

    public static String getName() { return name; }
}
