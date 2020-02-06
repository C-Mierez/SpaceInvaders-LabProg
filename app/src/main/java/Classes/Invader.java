package Classes;

public abstract class Invader extends Entity{

    public abstract boolean tryShooting(float playershipX, float playershipWdith);

    public abstract void shoot();

    public abstract int getScoreReward();

    public void borderCollision(byte border) {
        // Colision con los bordes
        switch (border){
            case Movement.LEFT_BORDER:
                currentMovement = Movement.RIGHT;
                break;
            case Movement.RIGHT_BORDER:
                currentMovement = Movement.LEFT;
                break;
            case Movement.BOTTOM_BORDER:
                setVisible(false);
                break;
            case Movement.TOP_BORDER:
                setVisible(false);
                break;
        }
    }

}
