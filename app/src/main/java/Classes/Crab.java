package Classes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

import com.example.spaceinvaders_labprogramacion.R;

import java.util.Random;

public class Crab extends Invader {

    // Parametros configurables
    private int SPEED_FACTOR = 100;
    private final int STARTING_LIVES = 1;
    private int MAX_SPEED = 300;
    private float SPEED_INCREASE_FACTOR = 1.19f;
    private int SIZE_FACTOR = 26; // Tamaño de los invasores
    public int SCORE_REWARD = 10;
    // Para alterar la frecuencia de los disparos
    private int CHANCE_NEAR = 100;
    private int CHANCE_FAR = 2000;


    Random randomGenerator = new Random();

    private static Bitmap[] invaderDamage =
            {       BitmapFactory.decodeResource(GameView.context.getResources(), R.drawable.invader1),
                    BitmapFactory.decodeResource(GameView.context.getResources(), R.drawable.invader2),};
    int animationIndex = 1;

    public Crab(float x, float y) {

        rectF = new RectF();

        screenX = GameView.screenX;
        screenY = GameView.screenY;

        width  = screenX / SIZE_FACTOR;
        height = screenY / SIZE_FACTOR;

        isVisible = true;
        movementSpeed = SPEED_FACTOR + (int)(SPEED_FACTOR * randomGenerator.nextDouble() * (randomGenerator.nextInt(2) + 1));
        currentMovement = Movement.LEFT;
        currentLives = STARTING_LIVES;

        posX = x;
        posY = y;

        // Incializar bitmaps y escalarlos
        bitmapSize = 2;
        bitmap = new Bitmap[bitmapSize];
        bitmapIndex = 0;
        currentBitmap = Bitmap.createScaledBitmap(invaderDamage[bitmapIndex], (int) (width), (int) (height),false);
    }

    public Crab(int y) {

        rectF = new RectF();

        screenX = GameView.screenX;
        screenY = GameView.screenY;

        width  = screenX / SIZE_FACTOR;
        height = screenY / SIZE_FACTOR;

        isVisible = true;
        movementSpeed = SPEED_FACTOR + (int)(SPEED_FACTOR * randomGenerator.nextDouble() * (randomGenerator.nextInt(2) + 1));
        currentMovement = Movement.LEFT;
        currentLives = STARTING_LIVES;

        posX = width + randomGenerator.nextInt( (int)(screenX - (width * 2)));
        posY = y;

        // Incializar bitmaps y escalarlos
        bitmapSize = 2;
        bitmap = new Bitmap[bitmapSize];
        bitmapIndex = 0;
        currentBitmap = Bitmap.createScaledBitmap(invaderDamage[bitmapIndex], (int) (width), (int) (height),false);
    }

    public boolean tryShooting(float playerShipX, float playerShipWidth){
        int randomNumber;
        boolean shoot;
        // Si se esta cerca del jugador
        if((playerShipX + playerShipWidth > posX && playerShipX + playerShipWidth < posX + width) || (playerShipX > posX && playerShipX < posX + width)) {
            // Posibilidad de disparar  cuando esta cerca
            randomNumber = randomGenerator.nextInt(CHANCE_NEAR);
            shoot = randomNumber == 1;
        }else{
            // Posibilidad de disparar cuando no se esta cerca
            randomNumber = randomGenerator.nextInt(CHANCE_FAR);
            shoot = randomNumber == 1;
        }
        return shoot;
    }

    @Override
    public void borderCollision(byte border) {
        super.borderCollision(border);
        posY += height;
        increaseMovementSpeed();
    }

    private void increaseMovementSpeed(){
        if(movementSpeed * SPEED_INCREASE_FACTOR <= MAX_SPEED){
            movementSpeed *= SPEED_INCREASE_FACTOR;
        }else{
            movementSpeed = MAX_SPEED;
        }
    }

    public void shoot(){
        GameView.invaderProjectiles.add(new Projectile(posX + width / 2, posY, Movement.DOWN));
    }

    @Override
    public int getScoreReward(){
        return SCORE_REWARD;
    }

    @Override
    public Bitmap getBitmap(int i){
        return Bitmap.createScaledBitmap( invaderDamage[i*animationIndex + bitmapIndex], (int) (width), (int) (height),false);
    }
}
