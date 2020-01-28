package Classes;

import android.graphics.RectF;

public class DefenseBlock {

    private RectF rectF; // :)

    private boolean isVisible;

    public DefenseBlock(int row, int column, int shelterNumber, int screenX, int screenY){

        // TODO Pasar todos los parametros de abajo a constantes para mas facil configuracion
        int width = screenX / 90;
        int height = screenY / 40;

        isVisible = true;

        int brickPadding = 1;

        int shelterPadding = screenX / 9;
        int startHeight = screenY - (screenY / 8 * 2);


        // TODO Comentar
        rectF = new RectF(column * width + brickPadding +
                (shelterPadding * shelterNumber) +
                shelterPadding + shelterPadding * shelterNumber,
                row * height + brickPadding + startHeight,
                column * width + width - brickPadding +
                        (shelterPadding * shelterNumber) +
                        shelterPadding + shelterPadding * shelterNumber,
                row * height + height - brickPadding + startHeight);
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
