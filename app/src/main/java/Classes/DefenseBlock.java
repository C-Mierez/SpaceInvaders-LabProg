package Classes;

import android.graphics.RectF;

public class DefenseBlock extends Entity {

    private final int WIDTH_FACTOR = 90;
    private final int HEIGHT_FACTOR = 40;


    public DefenseBlock(int row, int column, int shelterNumber, int startHeight, long left_padding, int totalColumns){

        screenX = GameView.screenX;
        screenY = GameView.screenY;

        // TODO Pasar todos los parametros de abajo a constantes para mas facil configuracion
        int width = screenX / WIDTH_FACTOR;
        int height = screenY / HEIGHT_FACTOR;

        isVisible = true;
        int brickPadding = 1;
        // TODO Comentar

        float x = (shelterNumber * left_padding ) + (column * (width + brickPadding) - (totalColumns / 2 * (width + brickPadding)));
        float y = startHeight + (row * (height + brickPadding));
        rectF = new RectF(x , y,x + width , y + height);

        bitmap = null;
        currentBitmap = null;
        movementSpeed = 0;
        currentMovement = Movement.STOPPED;
        /*rectF = new RectF(column * width + brickPadding + (left_padding * shelterNumber) + left_padding + left_padding * shelterNumber,
                row * height + brickPadding + startHeight,
                column * width + width - brickPadding + (left_padding * shelterNumber) + left_padding + left_padding * shelterNumber,
                row * height + height - brickPadding + startHeight);*/
    }

    @Override
    protected void borderCollision() {
        // :)
    }

}
