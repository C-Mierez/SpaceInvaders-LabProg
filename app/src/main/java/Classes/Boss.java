package Classes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

import com.example.spaceinvaders_labprogramacion.R;

import java.util.Random;

public class Boss extends Invader {

    // Parametros configurables
    private final int SPEED_FACTOR = 350;
    private final int STARTING_LIVES = 5;
    private int MAX_SPEED = 400;
    private float SPEED_INCREASE_FACTOR = 1.19f;
    private int SIZE_FACTOR = 8; // Tamaño de los invasores
    public int SCORE_REWARD = 100;
    // Para alterar la frecuencia de los disparos
    private int CHANCE_NEAR = 50;
    private int CHANCE_FAR = 150;



    Random randomGenerator = new Random();
    private static Bitmap[] invaderDamage =
            {       BitmapFactory.decodeResource(GameView.context.getResources(), R.drawable.boss),
                    BitmapFactory.decodeResource(GameView.context.getResources(), R.drawable.boss_damage1),
                    BitmapFactory.decodeResource(GameView.context.getResources(), R.drawable.boss_damage2),
                    BitmapFactory.decodeResource(GameView.context.getResources(), R.drawable.boss_damage3),
                    BitmapFactory.decodeResource(GameView.context.getResources(), R.drawable.boss_damage4),
                    BitmapFactory.decodeResource(GameView.context.getResources(), R.drawable.boss2),
                    BitmapFactory.decodeResource(GameView.context.getResources(), R.drawable.boss2_damage1),
                    BitmapFactory.decodeResource(GameView.context.getResources(), R.drawable.boss2_damage2),
                    BitmapFactory.decodeResource(GameView.context.getResources(), R.drawable.boss2_damage3),
                    BitmapFactory.decodeResource(GameView.context.getResources(), R.drawable.boss2_damage4)};
    int animationIndex = 5; // Cuantos estados hay por cada tipo (Boss / Boss2 en este caso ^)


    public Boss(int y) {

        rectF = new RectF();

        screenX = GameView.screenX;
        screenY = GameView.screenY;

        width  = screenX / SIZE_FACTOR;
        height = screenY / SIZE_FACTOR;

        isVisible = true;
        currentLives = STARTING_LIVES;
        movementSpeed = SPEED_FACTOR + (int)(SPEED_FACTOR * randomGenerator.nextDouble());
        currentMovement = Movement.RIGHT;

        posX = (screenX / 2) - (width / 2);
        posY = y;

        // Incializar bitmaps y escalarlos
        bitmapSize = 1;
        bitmap = new Bitmap[bitmapSize];
        bitmap[0] = Bitmap.createScaledBitmap(invaderDamage[0], (int) (width), (int) (height),false);

        bitmapIndex = 0;
        currentBitmap = bitmap[bitmapIndex];
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

    @Override
    public boolean dealDamage(){
        boolean dead = super.dealDamage();
        if(!dead){
            currentBitmap = Bitmap.createScaledBitmap( invaderDamage[++bitmapIndex], (int) (width), (int) (height),false);
        }
        return dead;
    }


    public void shoot(){
        if(randomGenerator.nextInt(10) < 8){
            GameView.invaderProjectiles.add(new Projectile(posX + (width / 3), posY + (height/2), Movement.DOWN_LEFT));
            GameView.invaderProjectiles.add(new Projectile(posX + (width / 3) * 2, posY + (height/2), Movement.DOWN_RIGHT));
        }else{
            GameView.invaderProjectiles.add(new Projectile(posX + (width / 4), posY + (height/3), Movement.DOWN));
            GameView.invaderProjectiles.add(new Projectile(posX + (width / 2), posY + (height/2), Movement.DOWN));
            GameView.invaderProjectiles.add(new Projectile(posX + (width / 4) * 3, posY + (height/3), Movement.DOWN));
        }
        if(randomGenerator.nextInt(10) < 1){
            if(randomGenerator.nextInt(2) == 0){
                GameView.invaders.add(new UFO(posX + width/2, posY - height));
            } else{
                GameView.invaders.add(new Crab(posX + width/2, posY - height));
            }
            GameView.soundPool.play(GameView.evilLaughID, 1,1,1,0,1);
        }
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

        if (posY + height >= GameView.spaceship.getPosY() - GameView.spaceship.getHeight()) {
            bottomBorderMovement();
        }else if(posY - height <= screenY / 10){
            topBorderMovement();
        }else{
            kamikazeMovement(spaceshipX);
        }
        // Realizar el resto de acciones
        super.update(fps);
    }

    @Override
    public void borderCollision(byte border) {
        super.borderCollision(border);
        switch (randomGenerator.nextInt(3)){
            case 0:
                currentMovement |= Movement.UP;
                break;
            case 1:
                currentMovement|= Movement.DOWN;
                break;
            default:
                break;
        }
    }

    private void kamikazeMovement(float spaceshipX){
        // Modo kamikaze (Agrega el bit de movimiento hacia abajo)
        if((currentMovement & Movement.DOWN) == 0 && randomGenerator.nextInt(5000) < 10){
            currentMovement |= Movement.DOWN;
        }
        // Si se mueve hacia abajo (diagonal)
        if((currentMovement & Movement.DOWN) > 0){
            // Modificamos la direccion de movimiento horizontal segun el x de la nave con respecto al nuestro
            if(spaceshipX < posX){
                if(posX - spaceshipX < 3 * width){
                    currentMovement = Movement.DOWN_LEFT;
                }else{
                    currentMovement = Movement.LEFT;
                }
            }else if (spaceshipX > posX){
                if(spaceshipX - posX < 3 * width){
                    currentMovement = Movement.DOWN_RIGHT;
                }else{
                    currentMovement = Movement.RIGHT;
                }
            }else{
                currentMovement = Movement.DOWN;
            }
        }
    }

    private void bottomBorderMovement(){
        switch (randomGenerator.nextInt(5)){
            case 0:
                currentMovement = Movement.LEFT;
                break;
            case 1:
                currentMovement = Movement.UP_LEFT;
                break;
            case 2:
                currentMovement = Movement.UP;
                break;
            case 3:
                currentMovement = Movement.UP_RIGHT;
                break;
            case 4:
                currentMovement = Movement.RIGHT;
                break;
        }
    }

    private void topBorderMovement(){
        switch (randomGenerator.nextInt(5)){
            case 0:
                currentMovement = Movement.LEFT;
                break;
            case 1:
                currentMovement = Movement.DOWN_LEFT;
                break;
            case 2:
                currentMovement = Movement.DOWN;
                break;
            case 3:
                currentMovement = Movement.DOWN_RIGHT;
                break;
            case 4:
                currentMovement = Movement.RIGHT;
                break;
        }
    }

    public Bitmap getBitmap(int i){
        return Bitmap.createScaledBitmap( invaderDamage[i*animationIndex + bitmapIndex], (int) (width), (int) (height),false);
    }
}
