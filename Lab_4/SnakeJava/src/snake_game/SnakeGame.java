package snake_game;

import message_decoder.SnakesProto;
import message_manager.HostInfo;
import message_manager.MessageManager;
import view.GameWindow;

import java.util.*;

public class SnakeGame
{
    private int height;
    private int width;

    private ArrayList<Point> food = new ArrayList<>();
    private HashMap<Integer, Snake> snakes = new HashMap<>();

    private HashMap<Integer, SnakesProto.GamePlayer> players = new HashMap<>();

    private HashMap<Integer, SnakesProto.Direction> movements = new HashMap<>();

    private Random rand = new Random(System.currentTimeMillis());

    private FoodFunc foodFunc;
    private int foodCount;
    private float deadFoodProb;

    private int spawnArea = 5;

    private int gameStateCounter = 0;

    private int playerIdCounter = 1;

    private List<Integer> deadSnakes = new ArrayList<>();


    private SnakesProto.GameConfig gameConfig;

    private MessageManager messageManager;

    private SnakesProto.NodeRole nodeRole;

    private GameWindow gameWindow;

    public SnakeGame(SnakesProto.GameConfig _gameConfig, GameWindow _gameWindow, SnakesProto.NodeRole _nodeRole)
    {

        gameConfig = _gameConfig;
        gameWindow = _gameWindow;
        messageManager = new MessageManager(this, _gameConfig, _nodeRole);

        nodeRole = _nodeRole;
        height = gameConfig.getHeight();
        width = gameConfig.getWidth();

        foodFunc = numOfPlayers -> gameConfig.getFoodStatic() + (int)(gameConfig.getFoodPerPlayer() * numOfPlayers);
        deadFoodProb = gameConfig.getDeadFoodProb();

        foodCount = foodFunc.getFoodCount(0);

        generateFood(foodCount);
    }

    public int addPlayer(String snakeName, SnakesProto.NodeRole _nodeRole,
                         SnakesProto.PlayerType _playerType, String ip, int port)
    {
        System.out.println("Add player:" + snakeName);

        synchronized (this)
        {
            if(_nodeRole == SnakesProto.NodeRole.VIEWER)
            {
                while(players.containsKey(playerIdCounter) && snakes.containsKey(playerIdCounter))
                    playerIdCounter++;

                SnakesProto.GamePlayer newPlayer = SnakesProto.GamePlayer.newBuilder()
                        .setName(snakeName)
                        .setId(playerIdCounter)
                        .setRole(_nodeRole)
                        .setType(_playerType)
                        .setScore(0)
                        .setIpAddress(ip)
                        .setPort(port)
                        .build();

                players.put(playerIdCounter, newPlayer);

                return playerIdCounter;
            }

            ArrayList<Point> newSnakeBody = createNewSnakeBody();

            if(newSnakeBody.size() == 0) return -1;


            while(players.containsKey(playerIdCounter) && snakes.containsKey(playerIdCounter))
                playerIdCounter++;

            Snake newSnake = new Snake(newSnakeBody, height, width, playerIdCounter, SnakesProto.GameState.Snake.SnakeState.ALIVE);

            snakes.put(playerIdCounter, newSnake);

            movements.put(playerIdCounter, newSnake.getPrevMovement());

            SnakesProto.GamePlayer newPlayer = SnakesProto.GamePlayer.newBuilder()
                    .setName(snakeName)
                    .setId(playerIdCounter)
                    .setRole(_nodeRole)
                    .setType(_playerType)
                    .setScore(0)
                    .setIpAddress(ip)
                    .setPort(port)
                    .build();

            players.put(playerIdCounter, newPlayer);

            int prevFoodCount = foodCount;
            foodCount = foodFunc.getFoodCount(snakes.size());
            generateFood(foodCount - prevFoodCount);

            return playerIdCounter;
        }
    }

    private Point getShiftByMovement(SnakesProto.Direction dir)
    {
        switch (dir)
        {
            case UP: return new Point(0, -1);
            case RIGHT: return new Point(1, 0);
            case DOWN: return new Point(0, 1);
            case LEFT: return new Point(-1, 0);
            default: return new Point(0 ,0);
        }
    }

    private void increasePlayerScore(int pi)
    {
        SnakesProto.GamePlayer prevPlayer = players.get(pi);

        if(prevPlayer == null) return;

        int newScore = prevPlayer.getScore() + 1;

        players.put(pi, prevPlayer.toBuilder().setScore(newScore).build());
    }

    private boolean checkCrash(Integer pi, Snake snake)
    {
        Point head = snake.getHead();

        for(Map.Entry<Integer, Snake> entry : snakes.entrySet())
        {
            Snake s = entry.getValue();
            int sPi = entry.getKey();
            ArrayList<Point> sBody  = s.getSnakeBody();
            for(int i = (sPi == pi) ? 1 : 0; i < sBody.size(); ++i)
            {
                if(head.equals(sBody.get(i)))
                {
                    if(i != 0)
                    {
                        increasePlayerScore(sPi);
                    }
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isFood(int x, int y)
    {
        for(Point p : food)
        {
            if(p.getX() == x && p.getY() == y)
                return true;
        }
        return false;
    }

    public void changeSnakeDir(Integer pi, SnakesProto.Direction dir)
    {
        synchronized (this)
        {
            if(!snakes.containsKey(pi)) return;

            Snake snake = snakes.get(pi);

            if(!snake.canMove(dir)) return;

            movements.put(pi, dir);
        }
    }

    public void moveSnakes()
    {
        synchronized (this)
        {
            for(Map.Entry<Integer, Snake> entry : snakes.entrySet())
            {
                moveSnake(entry.getKey(), entry.getValue(), movements.get(entry.getKey()));
            }

            deadSnakes.clear();

            for (Map.Entry<Integer, Snake> entry : snakes.entrySet())
            {
                if (checkCrash(entry.getKey(), entry.getValue()))
                {
                    deadSnakes.add(entry.getKey());
                }
            }

            for(int i : deadSnakes)
            {
                killPlayer(i);
            }

            checkFood();

        }
    }

    private void checkFood()
    {

        int createFood = 0;
        boolean eaten = false;
        for(Map.Entry<Integer, Snake> entry : snakes.entrySet())
        {
            Point head = entry.getValue().getHead();

            for(Point p : food)
            {
                if(head.equals(p))
                {
                    eaten = true;
                    break;
                }
            }
            if(eaten)
            {
                food.remove(head);
                ++createFood;
                eaten = false;
            }
        }

        generateFood(createFood);
    }

    public boolean isGameOver()
    {
        for(SnakesProto.GamePlayer gp : players.values())
        {
            if(gp.getRole() != SnakesProto.NodeRole.VIEWER) return false;
        }
        return true;
    }

    public boolean isDead(int playerId)
    {
        return !snakes.containsKey(playerId);
    }

    private void moveSnake(int pi, Snake snake, SnakesProto.Direction move)
    {
        Point head = snake.getHead();

        Point shift = getShiftByMovement(move);

        int newHeadPosX = (head.getX() + shift.getX() + width) % width;
        int newHeadPosY = (head.getY() + shift.getY() + height) % height;


        if (isFood(newHeadPosX, newHeadPosY))
        {
            snake.increaseSnake();
            increasePlayerScore(pi);
        }

        snake.moveSnake(move);
    }

    public ArrayList<Point> getFood()
    {
        return food;
    }

    public HashMap<Integer, Snake> getSnakes()
    {
        return snakes;
    }

    public int getHeight()
    {
        return height;
    }

    public int getWidth()
    {
        return width;
    }

    private void killPlayer(Integer pi)
    {
        ArrayList<Point> body = snakes.get(pi).getSnakeBody();
        for (int i = 0; i < body.size(); ++i)
        {
            if(i == 0) continue;
            if(foodDropped())
            {
                Point p = body.get(i);
                food.add(new Point(p.getX(), p.getY()));
            }
        }

        snakes.remove(pi);
        movements.remove(pi);
    }

    private void generateFood(int num)
    {
        int freeCells = getNumOfFreeCells();
        int availableNum = (num - freeCells > 0) ? freeCells : num;
        if(availableNum == 0) return;

        for(int i = 0; i < availableNum; ++i)
        {
            while(true)
            {
                int foodX = rand.nextInt(width);
                int foodY = rand.nextInt(height);
                if(sellEmpty(foodX, foodY))
                {
                    food.add(new Point(foodX, foodY));
                    break;
                }
            }
        }
    }

    private boolean sellEmpty(int x, int y)
    {
        for(Point p : food)
        {
            if(p.getX() == x && p.getY() == y)
            {
                return false;
            }
        }

        for(Map.Entry<Integer, Snake> entry : snakes.entrySet())
        {
            for(Point p : entry.getValue().getSnakeBody())
            {
                if(p.getX() == x && p.getY() == y)
                    return false;
            }
        }

        return true;
    }

    private int getNumOfFreeCells()
    {
        int result = 0;
        for(Map.Entry<Integer, Snake> entry : snakes.entrySet())
        {
            result += entry.getValue().getSnakeSize();
        }

        result += food.size();

        return height * width - result;
    }

    private boolean foodDropped()
    {
        return (float)rand.nextInt(10)/10.0f < deadFoodProb;
    }

    private Point findSpawnAreaRect()
    {
        if(getNumOfFreeCells() < spawnArea * spawnArea) return new Point(-1, -1);

        boolean[][] freePlace = new boolean[width][height];
        for(int i = 0; i < width; ++i)
            for(int j = 0; j < height; ++j)
                freePlace[i][j] = true;


        for (Map.Entry<Integer, Snake> entry : snakes.entrySet())
        {
            for (Point p: entry.getValue().getSnakeBody())
            {
                freePlace[p.getX()][p.getY()] = false;
            }
        }

        for(Point p : food)
        {
            freePlace[p.getX()][p.getY()] = false;
        }

        int halfOfSpawn = spawnArea / 2;

        for(int i = halfOfSpawn; i < width - halfOfSpawn; ++i)
        {
            for(int j = halfOfSpawn; j < height - halfOfSpawn; ++j)
            {
                boolean good = true;
                for(int k = 0; k < spawnArea; ++k)
                {
                    for(int l = 0; l < spawnArea; ++l)
                    {
                        if(!freePlace[k + i - halfOfSpawn][l + j - halfOfSpawn])
                        {
                            good = false;
                            break;
                        }
                    }
                    if(!good) break;
                }
                if(good)
                {
                    return new Point(i, j);
                }
            }
        }

        return new Point(-1, -1);
    }

    private Point getRandDir()
    {
        switch(rand.nextInt(4))
        {
            case 0: return new Point(0, -1);
            case 1: return new Point(0,  1);
            case 2: return new Point(-1,  0);
            case 3: return new Point(1,  0);
        }

        return new Point(0,  1);
    }

    private ArrayList<Point> createNewSnakeBody()
    {
        Point p = findSpawnAreaRect();

        if(p.getX() == -1)
        {
            return new ArrayList<>();
        }

        Point tailShift = getRandDir();
        Point tail = new Point(p.getX() + tailShift.getX(), p.getY() + tailShift.getY());


        ArrayList<Point> result = new ArrayList<>();
        result.add(p);
        result.add(tail);

        return result;
    }


    public void loadState(SnakesProto.GameState newGameState, HostInfo sender)
    {
        synchronized (this)
        {
            gameStateCounter = newGameState.getStateOrder();

            food.clear();
            for(int i = 0; i < newGameState.getFoodsCount(); ++i)
            {
                SnakesProto.GameState.Coord coord = newGameState.getFoods(i);
                food.add(i, new Point(coord.getX(), coord.getY()));
            }

            SnakesProto.GamePlayers gamePl = newGameState.getPlayers();

            snakes.clear();
            players.clear();
            for(int i = 0; i < newGameState.getSnakesCount(); ++i)
            {
                SnakesProto.GameState.Snake snake = newGameState.getSnakes(i);

                snakes.put(snake.getPlayerId(), new Snake(snake, newGameState.getConfig()));
                movements.put(snake.getPlayerId(), snake.getHeadDirection());
            }

            for(int i = 0; i < gamePl.getPlayersCount(); ++i)
            {
                SnakesProto.GamePlayer gamePlayer = gamePl.getPlayers(i);

                if(gamePlayer.getIpAddress().equals(""))
                {
                    gamePlayer = gamePlayer.toBuilder().setIpAddress(sender.getIp().toString()).build();
                }
                players.put(gamePlayer.getId(), gamePlayer);

            }

            gameStateCounter = newGameState.getStateOrder();

        }
    }

    public int getGameStateCounter()
    {
        return gameStateCounter;
    }

    public SnakesProto.GameState generateNewState()
    {
        synchronized (this)
        {
            SnakesProto.GameState.Builder gameStateBuilder = SnakesProto.GameState.newBuilder();

            gameStateBuilder.setStateOrder(gameStateCounter)
                    .setConfig(gameConfig);

            for(Point p : food)
            {
                gameStateBuilder.addFoods(SnakesProto.GameState.Coord.newBuilder()
                        .setX(p.getX())
                        .setY(p.getY()));
            }

            SnakesProto.GamePlayers.Builder gamePlayersBuilder = SnakesProto.GamePlayers.newBuilder();


            for(Map.Entry<Integer, SnakesProto.GamePlayer> entry : players.entrySet())
            {
                gamePlayersBuilder.addPlayers(entry.getValue());
            }


            for(Map.Entry<Integer, Snake> entry : snakes.entrySet())
            {
                gameStateBuilder.addSnakes(entry.getValue().getBufferedSnake());
            }

            gameStateBuilder.setPlayers(gamePlayersBuilder);

            ++gameStateCounter;

            return gameStateBuilder.build();
        }
    }

    public SnakesProto.GameConfig getGameConfig()
    {
        return gameConfig;
    }

    public MessageManager getMessageManager()
    {
        return messageManager;
    }

    public HashMap<Integer, SnakesProto.GamePlayer> getPlayers()
    {
        synchronized (this)
        {
            return players;
        }
    }

    public GameWindow getGameWindow()
    {
        return gameWindow;
    }

    public List<Integer> getDeadSnakes()
    {
        return deadSnakes;
    }
}

