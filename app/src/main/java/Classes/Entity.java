package Classes;

import android.graphics.Bitmap;
import android.graphics.RectF;

public abstract class Entity {

    // Variables
    protected RectF rectF; // Mantiene coordenadas de 4 Floats (como las esquinas de un rectangulo)

    protected Bitmap bitmap[]; // Para representar la entidad
    protected Bitmap currentBitmap;
    protected int bitmapIndex = 1, bitmapSize = 1;

    protected float height;
    protected float width;
    protected float posX;
    protected float posY;

    protected int screenX;
    protected int screenY;

    protected boolean isVisible;

    protected byte currentMovement;
    protected int movementSpeed;

    protected abstract void borderCollision(byte border);

    protected void update(long fps){
        Movement.updateMovement(this, fps);
        updateRectF();
    }

    public int getScoreReward(){
        return 0;
    }

    private void resetPosition(){
        posX = 0;
        posY = 0;
        updateRectF();
    }

    private void updateRectF(){
        rectF.top = posY;
        rectF.bottom = posY + height;
        rectF.left = posX;
        rectF.right = posX + width;
    }
    public void changeState(){
        bitmapIndex = ++bitmapIndex % bitmapSize;
        currentBitmap = bitmap[bitmapIndex];
    }


    public RectF getRect() {
        return rectF;
    }

    public void setRect(RectF rectF) {
        this.rectF = rectF;
    }

    public Bitmap[] getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap[] bitmap) {
        this.bitmap = bitmap;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getPosX() {
        return posX;
    }

    public void setPosX(float posX) {
        this.posX = posX;
    }

    public float getPosY() {
        return posY;
    }

    public void setPosY(float posY) {
        this.posY = posY;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
        if(!isVisible){
            resetPosition();
        }
    }

    public byte getCurrentMovement() {
        return currentMovement;
    }

    public void setCurrentMovement(byte currentMovement) {
        this.currentMovement = currentMovement;
    }

    public Bitmap getCurrentBitmap() {
        return currentBitmap;
    }

    public void setCurrentBitmap(Bitmap currentBitmap) {
        this.currentBitmap = currentBitmap;
    }

    public int getMovementSpeed() {
        return movementSpeed;
    }

    public void setMovementSpeed(int movementSpeed) {
        this.movementSpeed = movementSpeed;
    }
}
