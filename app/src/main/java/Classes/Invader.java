package Classes;

public interface Invader{

    public boolean tryShooting(float playershipX, float playershipWdith);

    public void borderCollision();

    public void shoot();

    public int getScoreReward();

}
