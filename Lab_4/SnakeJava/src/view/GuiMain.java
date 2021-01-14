package view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import game_search.GameSearcher;
import message_decoder.SnakesProto;
import message_manager.HostInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GuiMain extends Application
{
    private ConcurrentHashMap<HostInfo, SnakesProto.GameMessage.AnnouncementMsg> sessionInfoMap = new ConcurrentHashMap<>();
    private GameSearcher gameSearcher;

    private String name;

    public static void GUIFx(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage stage)
    {

        if(!LoginWindow.display())
        {
            return;
        }

        name = LoginWindow.getName();


        stage.setTitle("Snake game\t    " + name);

        Thread t;
        try
        {
            gameSearcher = new GameSearcher(sessionInfoMap);

            t = new Thread(gameSearcher);
            t.start();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return;
        }

        stage.setOnCloseRequest(actionEvent -> {
            System.out.println("Closing application...");
            t.interrupt();

            try{
                t.join();
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }

            Platform.exit();
            System.out.println("Finish application");
            System.exit(0);
        } );

        TableView<SessionInfo> tableView = createTableViewColumns();

        Button newGameButton = new Button("New game");
        newGameButton.setOnAction(actionEvent ->
        {
            if(NewGameWindow.display())
            {
                SnakesProto.GameConfig gameConfig = createGameConfig();

                GameWindow gameWindow = new GameWindow(gameConfig,
                        gameSearcher, name, SnakesProto.NodeRole.MASTER);
            }
        });

        Button connectButton = new Button("Connect");
        connectButton.setOnAction(actionEvent ->
        {
            ObservableList<SessionInfo> sessionSelected;
            sessionSelected  = tableView.getSelectionModel().getSelectedItems();

            if(sessionSelected.size() == 0)
            {
                return;
            }

            SessionInfo si = sessionSelected.get(0);

            HostInfo hostInfo = new HostInfo(si.getIp(), si.getPort());

            GameWindow gameWindow = new GameWindow(si.getGameConfig(),
                    gameSearcher, name, SnakesProto.NodeRole.NORMAL, hostInfo);

        });

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(actionEvent ->
        {

            tableView.getItems().removeAll();
            tableView.setItems(getSessionsInfo());
        });

        HBox bottomMenu = new HBox(100);
        bottomMenu.setMinHeight(100);
        bottomMenu.getChildren().addAll(newGameButton, connectButton, refreshButton);
        bottomMenu.setAlignment(Pos.CENTER);

        VBox layout = new VBox();
        layout.getChildren().addAll(tableView, bottomMenu);
        layout.setMinWidth(250 + 50 + 100 + 50 + 50 + 75 + 75 + 75 + 75 + 170);

        Scene scene = new Scene(layout);

        stage.setScene(scene);

        stage.show();
    }

    private TableView<SessionInfo> createTableViewColumns()
    {
        TableView<SessionInfo> tableView;

        TableColumn<SessionInfo, String> nameColumn = new TableColumn<>("NAME");
        nameColumn.setMaxWidth(250);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<SessionInfo, InetAddress> ipColumn = new TableColumn<>("IP");
        ipColumn.setMaxWidth(250);
        ipColumn.setCellValueFactory(new PropertyValueFactory<>("ip"));


        TableColumn<SessionInfo, Integer> portColumn = new TableColumn<>("Port");
        portColumn.setMaxWidth(50);
        portColumn.setCellValueFactory(new PropertyValueFactory<>("port"));

        TableColumn<SessionInfo, Integer> widthColumn = new TableColumn<>("Width");
        widthColumn.setMaxWidth(50);
        widthColumn.setCellValueFactory(new PropertyValueFactory<>("width"));

        TableColumn<SessionInfo, Integer> heightColumn = new TableColumn<>("Height");
        heightColumn.setMaxWidth(50);
        heightColumn.setCellValueFactory(new PropertyValueFactory<>("height"));

        TableColumn<SessionInfo, Integer> baseFoodColumn = new TableColumn<>("Base food");
        baseFoodColumn.setMaxWidth(75);
        baseFoodColumn.setCellValueFactory(new PropertyValueFactory<>("baseFood"));

        TableColumn<SessionInfo, Double> foodMultiplyerColumn = new TableColumn<>("Food multi");
        foodMultiplyerColumn.setMaxWidth(75);
        foodMultiplyerColumn.setCellValueFactory(new PropertyValueFactory<>("foodMultiplayer"));

        TableColumn<SessionInfo, Double> foodDropChanceColumn = new TableColumn<>("Drop chance");
        foodDropChanceColumn.setMaxWidth(75);
        foodDropChanceColumn.setCellValueFactory(new PropertyValueFactory<>("foodDropChance"));

        TableColumn<SessionInfo, Integer> numOfPlayersColumn = new TableColumn<>("Num of players");
        numOfPlayersColumn.setMaxWidth(75);
        numOfPlayersColumn.setCellValueFactory(new PropertyValueFactory<>("numOfPlayers"));

        TableColumn<SessionInfo, Boolean> canJoinColumn = new TableColumn<>("Can join");
        canJoinColumn.setMaxWidth(170);
        canJoinColumn.setCellValueFactory(new PropertyValueFactory<>("canJoin"));
        canJoinColumn.setSortType(TableColumn.SortType.DESCENDING);

        tableView = new TableView<>();
        tableView.setItems(getSessionsInfo());
        tableView.getColumns().addAll(ipColumn, portColumn, nameColumn, widthColumn, heightColumn, baseFoodColumn, foodMultiplyerColumn, foodDropChanceColumn, numOfPlayersColumn, canJoinColumn);

        return tableView;
    }

    private ObservableList<SessionInfo> getSessionsInfo()
    {
        ObservableList<SessionInfo> sessionsInfo = FXCollections.observableArrayList();

        for(Map.Entry<HostInfo, SnakesProto.GameMessage.AnnouncementMsg> entry : sessionInfoMap.entrySet())
        {
            for(SnakesProto.GamePlayer gamePlayer : entry.getValue().getPlayers().getPlayersList())
            {
                if(gamePlayer.getRole()== SnakesProto.NodeRole.MASTER)
                {
                    SnakesProto.GameConfig gameConfig = entry.getValue().getConfig();
                    sessionsInfo.add(new SessionInfo(
                            entry.getKey().getIp(), entry.getKey().getPort(), gamePlayer.getName(), gameConfig.getWidth(),
                            gameConfig.getHeight(), gameConfig.getFoodStatic(), gameConfig.getFoodPerPlayer(),
                            gameConfig.getDeadFoodProb(), entry.getValue().getPlayers().getPlayersCount(), entry.getValue().getCanJoin(),
                            entry.getValue().getConfig())
                    );
                    break;
                }
            }

        }

        return sessionsInfo;
    }

    private SnakesProto.GameConfig createGameConfig()
    {
        return SnakesProto.GameConfig.newBuilder()
                .setWidth(NewGameWindow.getWidth())
                .setHeight(NewGameWindow.getHeight())
                .setFoodStatic(NewGameWindow.getBaseFood())
                .setFoodPerPlayer(NewGameWindow.getFoodMultiplyer())
                .setStateDelayMs(NewGameWindow.getStateDelay())
                .setDeadFoodProb(NewGameWindow.getFoodDropChance())
                .setPingDelayMs(NewGameWindow.getPingDelay())
                .setNodeTimeoutMs(NewGameWindow.getNodeTimeout())
                .build();
    }
}
