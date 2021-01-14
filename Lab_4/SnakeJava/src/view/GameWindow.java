package view;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import game_search.GameSearcher;
import message_decoder.SnakesProto;
import message_manager.HostInfo;
import message_manager.MessageManager;
import snake_game.Point;
import snake_game.Snake;
import snake_game.SnakeGame;

import java.util.*;

public class GameWindow
{
    private Stage window;

    private final SnakeGame snakeGame;

    private SnakesProto.GameConfig gameConfig;

    private int pi;
    private GraphicsContext context;

    private TableView<Score> scores;

    private final int scoresWidth = 150;
    private final int windowWidth = 1000;
    private final int windowHeight = 700;

    private Color backGroundColor = Color.rgb(0,0,0);
    private Color foodColor = Color.rgb(255,153,0);
    private Color enemyBodyColor = Color.rgb(255, 0,0);
    private Color enemyHeadColor = Color.rgb(255,95, 168);
    private Color myBodyColor = Color.rgb(75,204,46);
    private Color myHeadColor = Color.rgb(164,243,76);

    private Timer timer = new Timer();
    private MessageManager messageManager;

    private SnakesProto.NodeRole nodeRole;
    private GameSearcher gameSearcher;

    private double cellWidth = 30;


    public GameWindow(SnakesProto.GameConfig _gameConfig, GameSearcher _gameSearcher, String name, SnakesProto.NodeRole _nodeRole, HostInfo hi)
    {
        gameSearcher = _gameSearcher;
        nodeRole = _nodeRole;

        gameConfig = _gameConfig;

        snakeGame = new SnakeGame(gameConfig, this, nodeRole);

        messageManager = snakeGame.getMessageManager();

        messageManager.sendJoin(hi, name);

        createWindow(name);
    }

    public GameWindow(SnakesProto.GameConfig _gameConfig, GameSearcher _gameSearcher, String name, SnakesProto.NodeRole _nodeRole)
    {
        gameSearcher = _gameSearcher;
        nodeRole = _nodeRole;

        gameConfig = _gameConfig;

        snakeGame = new SnakeGame(gameConfig, this, nodeRole);

        messageManager = snakeGame.getMessageManager();

        if((pi = messageManager.addMe(name, SnakesProto.NodeRole.MASTER,
                SnakesProto.PlayerType.HUMAN)) == -1)
        {
            ErrorBox.display("Unable to create Snake");
        }

        gameSearcher.sendAnnouncementMsg(snakeGame, gameConfig);

        createWindow(name);

    }

    private void createWindow(String name)
    {

        cellWidth = (double)(windowWidth - scoresWidth)/snakeGame.getWidth();
        if(cellWidth > (double)windowHeight/snakeGame.getHeight())
        {
            cellWidth = (double)windowHeight/snakeGame.getHeight();
        }

        Canvas c = new Canvas(snakeGame.getWidth() * cellWidth, snakeGame.getHeight() * cellWidth);
        context = c.getGraphicsContext2D();

        window = new Stage();

        window.setTitle(name);

        VBox vbox = new VBox();
        Button becameViewer = new Button("Became Viewer");
        becameViewer.setFocusTraversable(false);
        becameViewer.setOnAction(actionEvent ->
        {
            messageManager.becameViewer();
        });

        Button exitButton = new Button("Exit");
        exitButton.setFocusTraversable(false);
        exitButton.setOnAction(actionEvent ->
        {
            messageManager.safeExit();
        });

        draw();
        createScores();

        vbox.getChildren().addAll(scores, becameViewer, exitButton);

        Pane p = new Pane(c);

        HBox hbox = new HBox(p, vbox);

        Scene scene = new Scene(hbox, windowWidth, cellWidth * snakeGame.getHeight());
        scene.setOnKeyPressed(keyEvent ->
        {
            if (keyEvent.getCode() == KeyCode.LEFT)
            {
                if(nodeRole == SnakesProto.NodeRole.MASTER)
                {
                    snakeGame.changeSnakeDir(pi, SnakesProto.Direction.LEFT);
                }
                else if(nodeRole != SnakesProto.NodeRole.VIEWER)
                {
                    messageManager.sendSteer(pi, SnakesProto.Direction.LEFT);
                }
            }
            else if (keyEvent.getCode() == KeyCode.RIGHT)
            {
                if(nodeRole == SnakesProto.NodeRole.MASTER)
                {
                    snakeGame.changeSnakeDir(pi, SnakesProto.Direction.RIGHT);
                }
                else if(nodeRole != SnakesProto.NodeRole.VIEWER)
                {
                    messageManager.sendSteer(pi, SnakesProto.Direction.RIGHT);
                }
            }
            else if (keyEvent.getCode() == KeyCode.UP)
            {
                if(nodeRole == SnakesProto.NodeRole.MASTER)
                {
                    snakeGame.changeSnakeDir(pi, SnakesProto.Direction.UP);
                }
                else if(nodeRole != SnakesProto.NodeRole.VIEWER)
                {
                    messageManager.sendSteer(pi, SnakesProto.Direction.UP);
                }
            }
            else if (keyEvent.getCode() == KeyCode.DOWN)
            {
                if(nodeRole == SnakesProto.NodeRole.MASTER)
                {
                    snakeGame.changeSnakeDir(pi, SnakesProto.Direction.DOWN);
                }
                else if(nodeRole != SnakesProto.NodeRole.VIEWER)
                {
                    messageManager.sendSteer(pi, SnakesProto.Direction.DOWN);
                }
            }
        });


        timer = new Timer();
        timer.scheduleAtFixedRate(
        new TimerTask() {
            @Override
            public void run()
            {
                Platform.runLater( () ->
                {
                    synchronized (snakeGame)
                    {
                        if(nodeRole == SnakesProto.NodeRole.MASTER)
                        {
                            snakeGame.moveSnakes();
                            messageManager.sendState();

                            repaint();
                        }
                    }
                });
            }
        },
        0,
        gameConfig.getStateDelayMs());

        window.setOnCloseRequest(windowEvent -> terminate());


        window.setScene(scene);
        window.show();
    }


    public void repaint()
    {
        synchronized (snakeGame)
        {
            draw();
            updateScores();
            if(snakeGame.isGameOver())
            {
                System.out.println("Game over!");
                terminate();
                Platform.runLater(() -> GameOverWindow.display(scores));
            }
        }
    }

    public void terminate()
    {
        messageManager.disableMessageManager();
        timer.cancel();
        gameSearcher.stopSendAnnouncementMsg();
        Platform.runLater(()-> window.close());
    }


    private  void draw()
    {
        HashMap<Integer, Snake> snakes  = snakeGame.getSnakes();

        context.setFill(backGroundColor);
        context.fillRect(0, 0, cellWidth * snakeGame.getWidth(), cellWidth * snakeGame.getHeight());


        for(Map.Entry<Integer, Snake> entry : snakes.entrySet())
        {
            Color bodyColor = enemyBodyColor;
            Color headColor = enemyHeadColor;
            if(entry.getKey() == pi)
            {
                bodyColor = myBodyColor;
                headColor = myHeadColor;
            }

            ArrayList<Point> snakeBody = entry.getValue().getSnakeBody();
            for(int i = 0; i < snakeBody.size(); ++i)
            {

                int x = snakeBody.get(i).getX();
                int y = snakeBody.get(i).getY();

                if(i == 0)
                {
                    context.setFill(headColor);
                    context.fillRect(cellWidth * x, cellWidth * y, cellWidth , cellWidth);
                    continue;
                }
                context.setFill(bodyColor);
                context.fillRect(cellWidth * x, cellWidth * y, cellWidth , cellWidth);
            }
        }

        context.setFill(foodColor);
        for(Point p : snakeGame.getFood())
        {
            context.fillRect(cellWidth * p.getX(), cellWidth * p.getY(), cellWidth , cellWidth);
        }

    }

    private  void createScores()
    {
        TableColumn<Score, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setMinWidth(100);

        TableColumn<Score, Integer> scoreColumn = new TableColumn<>("Score");
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
        scoreColumn.setSortType(TableColumn.SortType.DESCENDING);
        scoreColumn.setMinWidth(50);


        scores = new TableView<>();
        scores.getColumns().addAll(nameColumn, scoreColumn);
        scores.setMaxWidth(windowWidth - snakeGame.getWidth() * cellWidth);
        scores.setMinWidth(windowWidth - snakeGame.getWidth() * cellWidth);
        scores.setEditable(false);
        scores.setFocusTraversable(false);
        scores.sort();
    }

    private  void updateScores()
    {
        ObservableList<Score> scoresNew = FXCollections.observableArrayList();
        for(Map.Entry<Integer, SnakesProto.GamePlayer> entry : snakeGame.getPlayers().entrySet())
        {
            scoresNew.add(new Score(entry.getValue().getName(), entry.getValue().getScore()));
        }

        scores.setItems(scoresNew);

    }

    public void setPi(int pi)
    {
        this.pi = pi;
    }


    public void setNodeRole(SnakesProto.NodeRole _nodeRole)
    {
        if(nodeRole == _nodeRole) return;

        nodeRole = _nodeRole;

        if(nodeRole == SnakesProto.NodeRole.MASTER)
        {
            gameSearcher.stopSendAnnouncementMsg();
            gameSearcher.sendAnnouncementMsg(snakeGame, gameConfig);
        }
        else
        {
            gameSearcher.stopSendAnnouncementMsg();
        }
    }
}
