package Classes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

import com.example.spaceinvaders_labprogramacion.R;

import java.util.Random;

public class UFO extends Invader {

    // Parametros configurables
    private int SPEED_FACTOR = 100;
    private int MAX_SPEED = 400;
    private float SPEED_INCREASE_FACTOR = 1.19f;
    private int SIZE_FACTOR = 18; // Tamaño de los invasores
    public int SCORE_REWARD = 10;
    // Para alterar la frecuencia de los disparos
    private int CHANCE_NEAR = 150;
    private int CHANCE_FAR = 2000;


    Random randomGenerator = new Random();

    boolean kamizazeMode;

    public UFO(Context context, int y) {

        rectF = new RectF();

        screenX = GameView.screenX;
        screenY = GameView.screenY;

        width  = screenX / SIZE_FACTOR;
        height = screenY / SIZE_FACTOR;

        isVisible = true;
        movementSpeed = SPEED_FACTOR + (int)(SPEED_FACTOR * randomGenerator.nextDouble() * (randomGenerator.nextInt(2) + 1));
        currentMovement = Movement.RIGHT;

        posX = width + randomGenerator.nextInt( (int)(screenX - (width * 2)));
        posY = y;

        kamizazeMode = false;

        // Incializar bitmaps y escalarlos
        bitmap = new Bitmap[3]; // TODO Agregar explosion
        bitmap[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.ufo);
        bitmap[0] = Bitmap.createScaledBitmap(bitmap[0], (int) (width), (int) (height),false);
        bitmap[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.ufo);
        bitmap[1] = Bitmap.createScaledBitmap(bitmap[0], (int) (width), (int) (height),false);
        currentBitmap = bitmap[0];
    }

    public boolean tryShooting(float playerShipX, float playerShipLength){
        int randomNumber;
        boolean shoot;
        // Si se esta cerca del jugador
        if((playerShipX + playerShipLength > posX && playerShipX + playerShipLength < posX + width) || (playerShipX > posX && playerShipX < posX + width)) {
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


    public void shoot(){
        GameView.invaderProjectiles.add(new Projectile(posX + width / 2, posY, Movement.DOWN));
    }

    @Override
    public int getScoreReward(){
        return SCORE_REWARD;
    }

    @Override
    public void update(long fps){
        float spaceshipX;
        // Logica para cambiar el movimiento
        spaceshipX = GameView.spaceship.getPosX();

        // Modo kamikaze
        if((currentMovement & Movement.DOWN) == 0 && randomGenerator.nextInt(1000) < 10){
            currentMovement |= Movement.DOWN;
        }
        // Si no se mueve hacia abajo (diagonal)
        if((currentMovement & Movement.DOWN) > 0){
            // Modificamos la direccion de movimiento horizontal segun el x de la nave con respecto al nuestro
            if(spaceshipX < posX){
                if(posX - spaceshipX < 3 * width){
                    currentMovement = Movement.DOWN_LEFT;
                }else{
                    currentMovement = Movement.LEFT;
                    kamizazeMode = false;
                }
            }else if (spaceshipX > posX){
                if(spaceshipX - posX < 3 * width){
                    currentMovement = Movement.DOWN_RIGHT;
                }else{
                    currentMovement = Movement.RIGHT;
                    kamizazeMode = false;
                }
            }else{
                currentMovement = Movement.DOWN;
            }
        }

        // Realizar el resto de acciones
        super.update(fps);
    }
}
