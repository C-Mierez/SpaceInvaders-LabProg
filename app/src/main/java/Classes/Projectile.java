package Classes;

import android.graphics.RectF;

public class Projectile {

    // Algunos parametros
    public final int SPEED_FACTOR = 550;
    public final int WIDTH = 8;

    // Posicion del proyectil en la pantalla
    private float posX;
    private float posY;
    private int width;
    private int height;


    private RectF rectF; // Uso similar al del Spaceship

    // Direccion del proyectil
    public final int NONE = 0;
    public final int UP = 1;
    public final int DOWN = 2;

    int projectileCurrentMovement = NONE;
    float projectileSpeed =  SPEED_FACTOR;

    private boolean isActive;

    public Projectile(int screenY) {
        height = screenY / 30;
        width = WIDTH;
        isActive = false;

        rectF = new RectF();
    }

    public void update(long fps){
            if(projectileCurrentMovement == UP){
                posY -= projectileSpeed / fps;
            }if(projectileCurrentMovement == DOWN){
                posY += projectileSpeed / fps;
            }

            // Actualizar rectF
            rectF.left = posX;
            rectF.right = posX + width;
            rectF.top = posY;
            rectF.bottom = posY + height;

    }

    public boolean shoot(float startX, float startY, int direction) {
        boolean succesfulShooting = false;

        // Verificamos que ya no haya sido disparada. Se retorna un bool para indicar si se pudo o no
        if (!isActive) {
            posX = startX;
            posY = startY;
            projectileCurrentMovement = direction;
            isActive = true;
            succesfulShooting = true;

        }
        return succesfulShooting;
    }

    public RectF getRect(){
        return  rectF;
    }
    public boolean isActive(){
        return isActive;
    }
    public void setInactive(){
        isActive = false;
    }
    public float getImpactPointY(){
        if (projectileCurrentMovement == DOWN){
            return posY + height;
        }else{
            return posY;
        }

    }

}
