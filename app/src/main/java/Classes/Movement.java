package Classes;

public class Movement {

    public static final byte STOPPED = 0x0;
    public static final byte UP = 0x1;
    public static final byte RIGHT = 0x2;
    public static final byte DOWN = 0x4;
    public static final byte LEFT = 0x8;
    public static final byte DOWN_RIGHT = 0x6;
    public static final byte DOWN_LEFT = 0xC;

    public static final byte TOP_BORDER = 1;
    public static final byte RIGHT_BORDER = 2;
    public static final byte BOTTOM_BORDER = 3;
    public static final byte LEFT_BORDER = 4;

    public static void updateMovement(Entity entity, long fps){

        if((entity.getCurrentMovement() & UP) > 0 && entity.getPosY() >= 0){
            entity.setPosY(entity.getPosY() - entity.getMovementSpeed() / fps);
            if(entity.getPosY() < 0){
                entity.borderCollision(TOP_BORDER);
            }
        }
        if((entity.getCurrentMovement() & DOWN) > 0 && entity.getPosY() - entity.getHeight() <= GameView.screenY){
            entity.setPosY(entity.getPosY() + entity.getMovementSpeed() / fps);
            if(entity.getPosY() - entity.getHeight() > GameView.screenY){
                entity.borderCollision(BOTTOM_BORDER);
            }
        }
        if((entity.getCurrentMovement() & RIGHT) > 0 && entity.getPosX() + entity.getWidth() < GameView.screenX){
            entity.setPosX(entity.getPosX() + entity.getMovementSpeed() / fps);
            if(entity.getPosX() + entity.getWidth() >= GameView.screenX){
                entity.borderCollision(RIGHT_BORDER);
            }
        }
        if((entity.getCurrentMovement() & LEFT) > 0 && entity.getPosX() > 0){
            entity.setPosX(entity.getPosX() - entity.getMovementSpeed() / fps);
            if(entity.getPosX() <= 0){
                entity.borderCollision(LEFT_BORDER);
            }
        }
    }
}
