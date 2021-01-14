package view;

public class Score
{

    private String name;
    private int score;

    public Score(String _name, int _score)
    {
        name = _name;
        score = _score;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getScore()
    {
        return score;
    }

    public void setScore(int score)
    {
        this.score = score;
    }
}
