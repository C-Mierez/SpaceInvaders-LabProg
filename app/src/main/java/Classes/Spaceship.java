package Classes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

import com.example.spaceinvaders_labprogramacion.R;

public class Spaceship {

    // Algunos paramentros ajustables
    private final int SIZE_FACTOR = 22; // Tamaño de la nave
    private final int SPEED_FACTOR = 450; // Velocidad de la nave

    RectF rectF; // Mantiene coordenadas de 4 Floats (como las esquinas de un rectangulo)

    private Bitmap bitmap; // Para representar la Spaceship
    private float height;
    private float length;
    private float posX;
    private float posY;

    private int screenX;
    private int screenY;


    private float shipSpeed; // Velocidad (pixeles por segundo)

    // Direcciones de movimiento (Pues solo hay pocas opciones las volvemos constantes)
    public final int STOPPED = 0;
    public final int LEFT = 1;
    public final int RIGHT = 2;

    public int shipCurrentMovement = STOPPED; // Movimiento actual del Spaceship

    public Spaceship(Context context, int screenX, int screenY){

        rectF = new RectF();

        this.screenX = screenX;
        this.screenY = screenY;

        length = screenX / SIZE_FACTOR;
        height = screenY / SIZE_FACTOR;

        // Posicion del Spaceship (aproximadamente en la mitad de la pantalla)
        posX = screenX / 2;
        posY = screenY - 20;

        // Inicializar el Bitmap y ajustarlo a la pantalla
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.playership);
        bitmap = Bitmap.createScaledBitmap(bitmap, (int)length, (int)height, false);

        // Velocidad
        shipSpeed = SPEED_FACTOR;
    }

    public void update(long fps){

            if(shipCurrentMovement == LEFT && posX >= 0){
                posX -= shipSpeed / fps;
            }if(shipCurrentMovement == RIGHT && posX + length <= screenX){
                posX += shipSpeed / fps;
            }
            // Actualizar rectF con los nuevos valores
            rectF.top = posY;
            rectF.bottom = posY + height;
            rectF.left = posX;
            rectF.right = posX + length;
    }

    public RectF getRect(){
        return rectF;
    }
    public Bitmap getBitmap(){
        return bitmap;
    }
    public float getPosX(){
        return posX;
    }
    public float getPosY(){
        return posY;
    }
    public float getLength(){
        return length;
    }
    public void setMovementState(int movement){
        shipCurrentMovement = movement;
    }



}
