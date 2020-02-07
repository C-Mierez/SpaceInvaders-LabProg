package Classes;

import android.graphics.Bitmap;

public abstract class Invader extends Entity{

    int currentLives;
    int animationIndex;
    private static Bitmap[] invaderDamage;

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
                GameView.destroySelfInvader(this);
                break;
            case Movement.TOP_BORDER:
                setVisible(false);
                break;
        }
    }

    public boolean dealDamage(){
        return --currentLives <= 0;
    }

    public abstract Bitmap getBitmap(int i);

}
