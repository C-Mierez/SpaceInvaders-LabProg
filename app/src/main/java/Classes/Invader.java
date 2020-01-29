package Classes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

import com.example.spaceinvaders_labprogramacion.R;

import java.util.Random;

public class Invader extends Entity {

    // Parametros configurables
    private final int SPEED_FACTOR = 80;
    private final float SPEED_INCREASE_FACTOR = 1.19f;
    private final int SIZE_FACTOR = 26; // Tamaño de los invasores
    public static final int PADDING = 12;
    public final long TOP_PADDING;
    public final int SCORE_REWARD = 10;
    // Para alterar la frecuencia de los disparos
    private final int CHANCE_NEAR = 150;
    private final int CHANCE_FAR = 2000;


    Random randomGenerator = new Random();

    public Invader(Context context, int row, int column, long top_padding, long left_padding) {

        rectF = new RectF();

        screenX = GameView.screenX;
        screenY = GameView.screenY;

        width  = screenX / SIZE_FACTOR;
        height = screenY / SIZE_FACTOR;

        isVisible = true;
        movementSpeed = SPEED_FACTOR;
        currentMovement = Movement.LEFT;

        TOP_PADDING = top_padding;
        long padding = screenX / PADDING;

        posX = (column * left_padding) - (width / 2);
        posY = TOP_PADDING + row * (width + padding / 4);

        // Incializar bitmaps y escalarlos
        bitmap = new Bitmap[3]; // TODO Agregar explosion
        bitmap[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.invader1);
        bitmap[0] = Bitmap.createScaledBitmap(bitmap[0], (int) (width), (int) (height),false);
        bitmap[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.invader2);
        bitmap[1] = Bitmap.createScaledBitmap(bitmap[1], (int) (width), (int) (height),false);
        currentBitmap = bitmap[0];
    }

    public boolean takeAim(float playerShipX, float playerShipLength){
        int randomNumber;
        boolean shoot = false;
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
    protected void borderCollision() {
        if((currentMovement & Movement.LEFT) > 0){
            currentMovement = Movement.RIGHT;
        }else{ // TODO Esto no es valido si hay movimiento en diagonal
            currentMovement = Movement.LEFT;
        }

        // TODO Velocidad de decenso de los invasores
        posY += height;
        movementSpeed *= SPEED_INCREASE_FACTOR;
    }
}
