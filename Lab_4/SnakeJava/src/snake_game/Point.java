package snake_game;

public class Point
{
    private int x;
    private int y;

    public Point()
    {
        x = 0;
        y = 0;
    }

    public Point(int _x, int _y)
    {
        x = _x;
        y = _y;
    }

    public Point(Point p)
    {
        x = p.x;
        y = p.y;
    }

    public void setValues(Point p)
    {
        x = p.x;
        y = p.y;
    }

    public int getX()
    {
        return x;
    }
    public int getY()
    {
        return y;
    }
    public void setX(int _x)
    {
        x = _x;
    }
    public void setY(int _y)
    {
        y = _y;
    }

    @Override
    public boolean equals(Object obj)
    {

        if(this == obj)
            return true;

        if(obj == null)
            return false;

        if(getClass() != obj.getClass())
            return false;

        Point p = (Point) obj;

        return p.getX() == x && p.getY() == y;
    }
}