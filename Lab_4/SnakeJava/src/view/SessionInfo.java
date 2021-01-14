package view;

import message_decoder.SnakesProto;

import java.net.InetAddress;

public class SessionInfo
{
    private InetAddress ip;
    private int port;

    private String name;

    private int width;
    private int height;
    private int baseFood;
    private double foodMultiplayer;
    private double foodDropChance;
    private int numOfPlayers;
    private boolean canJoin;

    private SnakesProto.GameConfig gameConfig;

    public SessionInfo(InetAddress _ip, int _port, String _name,
                int _width, int _height,
                int _baseFood, double _foodMultiplayer, double _foodDropChance,
                int _numOfPlayers, boolean _canJoin, SnakesProto.GameConfig _gameConfig)
    {

        ip = _ip;
        port = _port;
        name = _name;
        width = _width;
        height = _height;
        baseFood = _baseFood;
        foodMultiplayer = _foodMultiplayer;
        foodDropChance = _foodDropChance;
        numOfPlayers = _numOfPlayers;
        canJoin = _canJoin;
        gameConfig = _gameConfig;
    }

    public InetAddress getIp()
    {
        return ip;
    }

    public void setIp(InetAddress ip)
    {
        this.ip = ip;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }


    public int getWidth()
    {
        return width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public int getHeight()
    {
        return height;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }

    public int getBaseFood()
    {
        return baseFood;
    }

    public void setBaseFood(int baseFood)
    {
        this.baseFood = baseFood;
    }

    public double getFoodMultiplayer()
    {
        return foodMultiplayer;
    }

    public void setFoodMultiplayer(double foodMultiplayer)
    {
        this.foodMultiplayer = foodMultiplayer;
    }

    public double getFoodDropChance()
    {
        return foodDropChance;
    }

    public void setFoodDropChance(double foodDropChance)
    {
        this.foodDropChance = foodDropChance;
    }

    public int getNumOfPlayers()
    {
        return numOfPlayers;
    }

    public void setNumOfPlayers(int numOfPlayers)
    {
        this.numOfPlayers = numOfPlayers;
    }

    public boolean isCanJoin()
    {
        return canJoin;
    }

    public void setCanJoin(boolean canJoin)
    {
        this.canJoin = canJoin;
    }

    public SnakesProto.GameConfig getGameConfig()
    {
        return gameConfig;
    }

    public void setGameConfig(SnakesProto.GameConfig gameConfig)
    {
        this.gameConfig = gameConfig;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}