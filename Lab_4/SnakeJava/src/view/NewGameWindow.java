package view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class NewGameWindow
{
    private static int width = 40;
    private static int height = 30;
    private static int baseFood = 1;
    private static float foodMultiplyer = 1.0f;
    private static int stateDelay = 1000;
    private static float foodDropChance = 0.1f;
    private static int pingDelay = 100;
    private static int nodeTimeout = 800;

    private static TextField widthInput;
    private static TextField heightInput;
    private static TextField baseFoodInput;
    private static TextField foodMultiplyerInput;
    private static TextField stateDelayInput;
    private static TextField foodDropChanceInput;
    private static TextField pingDelayInput;
    private static TextField nodeTimeoutInput;

    private static boolean created = false;

    public static boolean display()
    {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("New Game");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8);
        grid.setHgap(10);


        //width
        Label widthLabel = new Label("Width:");
        GridPane.setConstraints(widthLabel, 0, 0);

        widthInput = new TextField();
        widthInput.setPromptText("40");
        GridPane.setConstraints(widthInput, 1, 0);


        //length
        Label heightLabel = new Label("Width:");
        GridPane.setConstraints(heightLabel, 0, 1);

        heightInput = new TextField();
        heightInput.setPromptText("30");
        GridPane.setConstraints(heightInput, 1, 1);


        //base food label
        Label baseFoodLabel = new Label("Base food count:");
        GridPane.setConstraints(baseFoodLabel, 0, 2);

        baseFoodInput = new TextField();
        baseFoodInput.setPromptText("1");
        GridPane.setConstraints(baseFoodInput, 1, 2);


        //food multiplyer
        Label foodMultiplyerLabel = new Label("Food multiplyer:");
        GridPane.setConstraints(foodMultiplyerLabel, 0, 3);

        foodMultiplyerInput = new TextField();
        foodMultiplyerInput.setPromptText("1.0");
        GridPane.setConstraints(foodMultiplyerInput, 1, 3);

        //state delay
        Label stateDelayLabel = new Label("Delay between states:");
        GridPane.setConstraints(stateDelayLabel, 0, 4);

        stateDelayInput = new TextField();
        stateDelayInput.setPromptText("in range [0, 10000]");
        GridPane.setConstraints(stateDelayInput, 1, 4);

        //drop chance
        Label foodDropChanceLabel = new Label("Food drop chance:");
        GridPane.setConstraints(foodDropChanceLabel, 0, 5);

        foodDropChanceInput = new TextField();
        foodDropChanceInput.setPromptText("in range [0.0, 1.0]");
        GridPane.setConstraints(foodDropChanceInput, 1, 5);

        //ping delay
        Label pingDelayLabel = new Label("Delay between pings:");
        GridPane.setConstraints(pingDelayLabel, 0, 6);

        pingDelayInput = new TextField();
        pingDelayInput.setPromptText("in range [0, 10000]");
        GridPane.setConstraints(pingDelayInput, 1, 6);

        //node timeout
        Label nodeTimeoutLabel = new Label("Timeout:");
        GridPane.setConstraints(nodeTimeoutLabel, 0, 7);

        nodeTimeoutInput = new TextField();
        nodeTimeoutInput.setPromptText("in range [0, 10000]");
        GridPane.setConstraints(nodeTimeoutInput, 1, 7);

        Button createButton = new Button("Create");
        createButton.setOnAction(actionEvent ->
        {
            try
            {
                width = Integer.parseInt(widthInput.getText());
                height = Integer.parseInt(heightInput.getText());
                baseFood = Integer.parseInt(baseFoodInput.getText());
                foodMultiplyer = Float.parseFloat(foodMultiplyerInput.getText());
                foodDropChance = Float.parseFloat(foodDropChanceInput.getText());
                stateDelay = Integer.parseInt(stateDelayInput.getText());
                pingDelay = Integer.parseInt(pingDelayInput.getText());
                nodeTimeout = Integer.parseInt(nodeTimeoutInput.getText());


                if(     width    < 10      || width    > 100         ||
                        height   < 10      || height   > 100         ||
                        baseFood < 0       || baseFood > 100         ||
                        foodMultiplyer < 0 || foodMultiplyer > 100   ||
                        foodDropChance < 0 || foodDropChance > 1.0   ||
                        stateDelay < 0     || stateDelay > 10000     ||
                        pingDelay < 0      || pingDelay > 10000      ||
                        nodeTimeout < 0    || nodeTimeout > 10000)
                {
                    ErrorBox.display("Invalid data");
                    return;
                }

                created = true;
                window.close();

            }
            catch (NumberFormatException ignored) { ErrorBox.display("Invalid data"); }

        });
        GridPane.setConstraints(createButton, 2, 6);

        grid.getChildren().addAll(widthLabel, widthInput,
                                  heightLabel, heightInput,
                                  baseFoodLabel, baseFoodInput,
                                  foodMultiplyerLabel, foodMultiplyerInput,
                                  stateDelayLabel, stateDelayInput,
                                  foodDropChanceLabel, foodDropChanceInput,
                                  pingDelayLabel, pingDelayInput,
                                  nodeTimeoutLabel, nodeTimeoutInput,
                                  createButton);

        Scene scene = new Scene(grid, 400, 350);

        window.setScene(scene);

        window.showAndWait();
        return created;
    }

    public static int getBaseFood()
    {
        return baseFood;
    }

    public static int getWidth()
    {
        return width;
    }

    public static float getFoodDropChance()
    {
        return foodDropChance;
    }

    public static float getFoodMultiplyer()
    {
        return foodMultiplyer;
    }

    public static int getHeight()
    {
        return height;
    }

    public static int getStateDelay() { return stateDelay; }

    public static int getPingDelay() { return pingDelay; }

    public static int getNodeTimeout() { return nodeTimeout; }

}
