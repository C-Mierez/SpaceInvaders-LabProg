package Classes;

import android.graphics.RectF;

public class DefenseBlock {

    private RectF rectF; // :)

    private boolean isVisible;

    public DefenseBlock(int row, int column, int shelterNumber, int screenX, int screenY, int startHeight, long left_padding, int totalColumns){

        // TODO Pasar todos los parametros de abajo a constantes para mas facil configuracion
        int width = screenX / 90;
        int height = screenY / 40;

        isVisible = true;
        int brickPadding = 1;
        // TODO Comentar

        float x = (shelterNumber * left_padding ) + (column * (width + brickPadding) - (totalColumns / 2 * (width + brickPadding)));
        float y = startHeight + (row * (height + brickPadding));
        rectF = new RectF(x , y,x + width , y + height);

        /*rectF = new RectF(column * width + brickPadding + (left_padding * shelterNumber) + left_padding + left_padding * shelterNumber,
                row * height + brickPadding + startHeight,
                column * width + width - brickPadding + (left_padding * shelterNumber) + left_padding + left_padding * shelterNumber,
                row * height + height - brickPadding + startHeight);*/
    }

    public RectF getRect(){
        return this.rectF;
    }
    public void setInvisible(){
        isVisible = false;
    }
    public boolean getVisibility(){
        return isVisible;
    }

}
