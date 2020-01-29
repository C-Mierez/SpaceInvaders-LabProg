package Classes;

import android.graphics.Bitmap;
import android.graphics.RectF;

public class Projectile extends Entity{

    // Algunos parametros
    private final int HEIGHT_FACTOR = 30;
    public final int SPEED_FACTOR = 550;
    public final int WIDTH = 8;

    public Projectile() {
        screenX = GameView.screenX;
        screenY = GameView.screenY;

        height = screenY / HEIGHT_FACTOR;
        width = WIDTH;
        isVisible = false;

        rectF = new RectF();

        // TODO El proyectil no usa imagen por ahora
        bitmap = null;
        currentBitmap = null;

        currentMovement = Movement.STOPPED;
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
    protected void borderCollision() {
        isVisible = false;
    }
}
