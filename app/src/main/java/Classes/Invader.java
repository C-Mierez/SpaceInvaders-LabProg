package Classes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

import com.example.spaceinvaders_labprogramacion.R;

import java.util.Random;

public class Invader {

    // Parametros configurables
    private final int SPEED_FACTOR = 80;
    private final int SIZE_FACTOR = 26; // Tamaño de los invasores
    public static final int PADDING = 12;
    public final long TOP_PADDING;


    // Para alterar la frecuencia de los disparos
    private final int CHANCE_NEAR = 150;
    private final int CHANCE_FAR = 2000;

    // Igual que los otros
    RectF rectF;

    Random randomGenerator = new Random();

    // Para ir alternando entre dos imagenes (simulamos una animacion)
    private Bitmap bitmap1;
    private Bitmap bitmap2;

    // Parametros de posicion
    private float posX;
    private float posY;
    public static float length;
    public static float height;




    public final int LEFT = 1;
    public final int RIGHT = 2;
    private float invaderSpeed;
    private int invaderCurrentMovement = RIGHT;

    boolean isVisible;

    public Invader(Context context, int row, int column, int screenX, int screenY, long top_padding, long left_padding) {

        rectF = new RectF();

        length  = screenX / SIZE_FACTOR;
        height = screenY / SIZE_FACTOR;

        isVisible = true;

        TOP_PADDING = top_padding;

        long padding = screenX / PADDING;

        posX = (column * left_padding) - (length / 2);
        posY = TOP_PADDING + row * (length + padding / 4);

        // Incializar bitmaps y escalarlos
        bitmap1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.invader1);
        bitmap2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.invader2);
        bitmap1 = Bitmap.createScaledBitmap(bitmap1, (int) (length), (int) (height),false);
        bitmap2 = Bitmap.createScaledBitmap(bitmap2, (int) (length), (int) (height),false);

        // How fast is the invader in pixels per second
        invaderSpeed = SPEED_FACTOR;
    }

    public void update(long fps){
        if(invaderCurrentMovement == LEFT){
            posX -= invaderSpeed / fps;
        }
        if(invaderCurrentMovement == RIGHT){
            posX += invaderSpeed / fps;
        }
        // Update rect which is used to detect hits
        rectF.top = posY;
        rectF.bottom = posY + height;
        rectF.left = posX;
        rectF.right = posX + length;
    }
    public void dropDownAndReverse(){
        if(invaderCurrentMovement == LEFT){
            invaderCurrentMovement = RIGHT;
        }else{
            invaderCurrentMovement = LEFT;
        }
        posY += height;

        invaderSpeed *= 1.18f;
    }

    public boolean takeAim(float playerShipX, float playerShipLength){
        int randomNumber;
        boolean shoot = false;
        // Si se esta cerca del jugador
        if((playerShipX + playerShipLength > posX && playerShipX + playerShipLength < posX + length) || (playerShipX > posX && playerShipX < posX + length)) {
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


    public void setInvisible(){
        isVisible = false;
    }

    public boolean isVisible(){
        return isVisible;
    }

    public RectF getRect(){
        return rectF;
    }

    public Bitmap getBitmap(){
        return bitmap1;
    }

    public Bitmap getBitmap2(){
        return bitmap2;
    }

    public float getX(){
        return posX;
    }

    public float getY(){
        return posY;
    }

    public float getLength(){
        return length;
    }
}
