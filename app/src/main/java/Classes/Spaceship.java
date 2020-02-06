package Classes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

import com.example.spaceinvaders_labprogramacion.R;

public class Spaceship extends Entity {

    // Algunos paramentros ajustables
    private final int SIZE_FACTOR = 22; // Tamaño de la nave
    private final int MOVEMENT_SPEED = 450;
    public final int STARTING_X_FACTOR = 2;
    public final int STARTING_Y_FACTOR = 12;


    public Spaceship(Context context){
        rectF = new RectF();

        this.screenX = GameView.screenX;
        this.screenY = GameView.screenY;

        width = screenX / SIZE_FACTOR;
        height = screenY / SIZE_FACTOR;

        // Posicion del Spaceship (aproximadamente en la mitad de la pantalla)
        posX = (screenX / STARTING_X_FACTOR) - (width / 2);
        posY = screenY - (screenY / STARTING_Y_FACTOR);

        // Inicializar el Bitmap y ajustarlo a la pantalla
        bitmapSize = 2;
        bitmap = new Bitmap[bitmapSize]; // Cantidad de imagenes posibles

        // Nave normal
        bitmap[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.playership);
        bitmap[0] = Bitmap.createScaledBitmap(bitmap[0], (int)width, (int)height, false);
        // Nave destruida
        bitmap[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.perdedor);
        bitmap[1] = Bitmap.createScaledBitmap(bitmap[1], (int)width, (int)height, false);
        // Bitmap inicial
        bitmapIndex = 0;
        currentBitmap = bitmap[bitmapIndex];

        movementSpeed = MOVEMENT_SPEED;
        currentMovement = Movement.STOPPED;

        isVisible = true;
    }

    @Override
    protected void borderCollision(byte border) {
        if(border == Movement.LEFT_BORDER){
            posX = 0;
        }
        if(border == Movement.RIGHT_BORDER){
            posX = screenX - width;
        }
        currentMovement = Movement.STOPPED;
    }
}
