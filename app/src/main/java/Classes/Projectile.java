package Classes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

import com.example.spaceinvaders_labprogramacion.R;

import java.util.Random;

public class Projectile extends Entity{

    // Algunos parametros
    public final int SPEED_FACTOR = 550;
    public final int WIDTH = 24;

    Random randomGenerator = new Random();

    private final static Bitmap[] projectiles =
            {       BitmapFactory.decodeResource(GameView.context.getResources(), R.drawable.projectile_1),
                    BitmapFactory.decodeResource(GameView.context.getResources(), R.drawable.projectile_2),
                    BitmapFactory.decodeResource(GameView.context.getResources(), R.drawable.projectile_3),
                    BitmapFactory.decodeResource(GameView.context.getResources(), R.drawable.projectile_4)};


    public Projectile() {
        screenX = GameView.screenX;
        screenY = GameView.screenY;

        height = WIDTH*2;
        width = WIDTH;
        isVisible = false;

        rectF = new RectF();

        // TODO El proyectil no usa imagen por ahora
        bitmapSize = 3;
        bitmap = new Bitmap[bitmapSize];

        bitmapIndex = 0;
        currentBitmap = Bitmap.createScaledBitmap(projectiles[3], (int) (width), (int) (height),false);

        currentMovement = Movement.STOPPED;
        movementSpeed = SPEED_FACTOR;
    }

    public Projectile(float startX, float startY, byte direction) {
        screenX = GameView.screenX;
        screenY = GameView.screenY;

        height = WIDTH;
        width = WIDTH;

        posX = startX;
        posY = startY;
        currentMovement = direction;
        isVisible = true;

        rectF = new RectF();

        // TODO El proyectil no usa imagen por ahora
        bitmapSize = 3;
        bitmap = new Bitmap[bitmapSize];
        bitmapIndex = 0;
        if(currentMovement == Movement.UP){
            currentBitmap = Bitmap.createScaledBitmap(projectiles[3], (int) (width), (int) (height*2),false);
        }else if(currentMovement == Movement.DOWN){
            currentBitmap = Bitmap.createScaledBitmap(projectiles[0], (int) (width), (int) (height*2),false);
        } else if((currentMovement & Movement.LEFT) > 0 || (currentMovement & Movement.RIGHT) > 0){
            currentBitmap = randomGenerator.nextInt(2) == 0 ?
                    Bitmap.createScaledBitmap(projectiles[1], (int) (width*2), (int) (height*2),false) :
                    Bitmap.createScaledBitmap(projectiles[2], (int) (width*2), (int) (height*2),false); // Si. Esto lo hicimos nosotros.
        }

        movementSpeed = SPEED_FACTOR;
    }


    public boolean shoot(float startX, float startY, byte direction) {
        boolean succesfulShooting = false;
        // Verificamos que ya no haya sido disparada. Se retorna un bool para indicar si se pudo o no
        if (!isVisible) {
            posX = startX;
            posY = startY;
            currentMovement = direction;
            isVisible = true;
            succesfulShooting = true;
        }
        return succesfulShooting;
    }

    @Override
    protected void borderCollision(byte border) {
        setVisible(false);
        GameView.invaderProjectiles.remove(this);
    }
}
