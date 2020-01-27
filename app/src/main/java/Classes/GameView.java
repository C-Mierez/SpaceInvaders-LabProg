package Classes;

// Esta clase contiene toda la lógica del juego
// Además, es quien responde a las entradas por pantalla

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.spaceinvaders_labprogramacion.R;

import java.io.IOException;

public class GameView extends SurfaceView implements Runnable {

    Context context;

    // Hilo del juego
    private Thread gameThread = null;
    // Para bloquear la vista antes de dibujar
    private SurfaceHolder surfaceHolder;
    // Para identificar cuando el juego esta corriendo o no
    private volatile boolean gamePlaying;
    private boolean gamePaused = true;
    // Para dibujar
    private Canvas canvas;
    private Paint paint;
    // Para tener los fps del juego
    private long fps;
    private long timeThisFrame;
    // Tamaño de la pantalla
    private int screenX;
    private int screenY;

    // Nave del jugador
    private Spaceship spaceship;
    // Proyectil del jugador
    private Projectile spaceshipProjectile;

    // TODO Proyectiles de los Invasores
    private Projectile[] invaderProjectiles = new Projectile[200];
    private int nextInvaderProjectile;
    private final int MAX_INVADER_PROJECTILES = 10;
    // Invasores
    Invader[] invaders = new Invader[60];
    int numInvaders = 0;
    // Bloques de defensa
    private DefenseBlock[] defenseBlocks = new DefenseBlock[400];
    private int numDefenseBlocks;

    // Sonidos ( incializados por default)
    private SoundPool soundPool;
    private int playerExplodeID = -1;
    private int invaderExplodeID = -1;
    private int shootID = -1;
    private int damageShelterID = -1;
    private int uhID = -1;
    private int ohID = -1;
    // Sonido en intervalo
    private long menaceInterval = 1000;
    private boolean uhOrOh;
    private long lastMenaceTime = System.currentTimeMillis();
    // Otros datos
    private int score = 0;
    private int lives = 3;

    // Constructor
    public GameView(Context context, int x, int y) {
        super(context);
        this.context = context;

        surfaceHolder = getHolder();
        paint = new Paint();

        screenX = x;
        screenY = y;

        soundPool = new SoundPool.Builder().build();
        //soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC,0); No se usa esto mas. Es un metodo viejo

        try {
            // Cargar los sonidos
            shootID = soundPool.load(context, R.raw.shoot, 1);
            invaderExplodeID = soundPool.load(context, R.raw.invaderexplode, 1);
            damageShelterID = soundPool.load(context, R.raw.damageshelter, 1);
            playerExplodeID = soundPool.load(context, R.raw.playerexplode, 1);
            uhID = soundPool.load(context, R.raw.uh, 1);
            ohID = soundPool.load(context, R.raw.oh, 1);

        } catch (Exception e) {
            // Print an error message to the console
            Log.e("error", "failed to load sound files");
        }
        //prepareLevel();
    }

    private void prepareLevel() {

        // Crear la nave
        spaceship = new Spaceship(context, screenX, screenY);

        // Crear el proyectil del Spaceship y los proyectiles de los enemigos
        spaceshipProjectile = new Projectile(screenY);
        for(int i = 0; i < invaderProjectiles.length; i++){
            invaderProjectiles[i] = new Projectile(screenY);
        }
        nextInvaderProjectile = 0;

        // Crear las filas de invasores
        int cant = 0;
        for(int i = 0; i < 6; i++){
            for(int x = 0; x < 5; x++){
                invaders[cant] = new Invader(context, i, x, screenX, screenY);
                cant++;
            }
        }

        // Build the shelters
        numDefenseBlocks = 0;
        for(int shelterNumber = 0; shelterNumber < 4; shelterNumber++){
            for(int i = 0; i < 5; i ++ ) {
                for (int x = 0; x < 10; x++) {
                    defenseBlocks[numDefenseBlocks] = new DefenseBlock(i, x, shelterNumber, screenX, screenY);
                    numDefenseBlocks++;
                }
            }
        }
    }

    @Override
    public void run() {
        prepareLevel();
        while (gamePlaying) {
            // Tiempo en milisegundos actuales para startFrameTime
            long startFrameTime = System.currentTimeMillis();
            // Actualizar el frame
            if (!gamePaused) {
                update();
            }
            // Dibujar el frame
            draw();
            // Calcular el fps del frame
            timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame >= 1) {
                fps = 1000 / timeThisFrame;
            }
            // Sonidito uUrUURuoogogo UWU
            if(!gamePaused) {
                playSound(startFrameTime);
            }
        }
    }
    private void playSound(long startFrameTime){
        if ((startFrameTime - lastMenaceTime) > menaceInterval) {
            if (uhOrOh) {
                soundPool.play(uhID, 1, 1, 0, 0, 1);
            } else {
                soundPool.play(ohID, 1, 1, 0, 0, 1);
            }
            lastMenaceTime = System.currentTimeMillis();
            uhOrOh = !uhOrOh; // Intercambio
        }
    }

    private void update() {
        // Colision con el borde la pantalla
        boolean bumpedScreenBorder = false;
        // Vidas del juegador terminadas
        boolean lost = false;

        // Mover el Spaceship
        spaceship.update(fps);

        // Actualizar los invasores visibles
        for(Invader invader : invaders){
            if(invader != null && invader.isVisible()) {
                invader.update(fps);

                // Intentar disparar
                if(invader.takeAim(spaceship.getPosX(), spaceship.getLength())){
                    if(invaderProjectiles[nextInvaderProjectile] != null && invaderProjectiles[nextInvaderProjectile].shoot(invader.getX() + invader.getLength() / 2, invader.getY(), spaceshipProjectile.DOWN)) {
                        nextInvaderProjectile++;
                        // Si se consumen todos los proyectiles, se comienza de 0
                        // TODO Quitar el limite de proyectiles
                        if (nextInvaderProjectile == MAX_INVADER_PROJECTILES) {
                            nextInvaderProjectile = 0;
                        }
                    }

                }
                // Si toca el borde, se setea como que toco
                if (invader.getX() > screenX - invader.getLength() || invader.getX() < 0){
                    bumpedScreenBorder = true;
                }
            }
        }

        // Actualizar los proyectiles activos de los Invasores
        for(Projectile projectile : invaderProjectiles){
            if(projectile != null && projectile.isActive()){
                projectile.update(fps);
            }
        }

        // TODO Si el invasor ha golpeado el borde. Solo los visibles?
        if(bumpedScreenBorder){
            for(Invader invader : invaders){
                if(invader != null && invader.isVisible()){
                    invader.dropDownAndReverse();
                    // Si tocan el suelo, se pierde el juego
                    if(invader.getY() > screenY - screenY / 10){
                        lost = true;
                    }
                }
            }
            menaceInterval -= 80;
        }
        // TODO Condicion de perdida
        if (lost) {
            prepareLevel();
        }
        // Acutalizar el proyectil del Spaceship si ha sido disparado
        if(spaceshipProjectile.isActive()){
            spaceshipProjectile.update(fps);
        }

        // Verificar proyectil no ha tocado los bordes superiores o inferiores, respectivamente
        if(spaceshipProjectile.getImpactPointY() < 0){
            spaceshipProjectile.setInactive();
        }
        for(Projectile projectile : invaderProjectiles){
            if(projectile != null && projectile.getImpactPointY() > screenY){
                projectile.setInactive();
            }
        }

        // Colision del proyectil con un invasor
        if(spaceshipProjectile.isActive()) {
            for (Invader invader : invaders) {
                if (invader != null && invader.isVisible()) {
                    if (RectF.intersects(spaceshipProjectile.getRect(), invader.getRect())) {
                        invader.setInvisible();
                        // TODO Sound InvaderExplode
                        soundPool.play(invaderExplodeID, 1, 1, 0, 0, 1);
                        spaceshipProjectile.setInactive();
                        score = score + 10;
                        // TODO Victoria?
                        if(score == numInvaders * 10){
                            gamePaused = true;
                            score = 0;
                            lives = 3;
                            prepareLevel();
                        }
                    }
                }
            }
        }

        // Colision de proyectiles con los bloques
        for(Projectile projectile : invaderProjectiles){
            if(projectile != null && projectile.isActive()){
                for(DefenseBlock block : defenseBlocks){
                    if(block != null && block.getVisibility()){
                        if(RectF.intersects(projectile.getRect(), block.getRect())){
                            projectile.setInactive();
                            block.setInvisible();
                            // TODO Sound Projectile on DefenseBlock
                            soundPool.play(damageShelterID, 1, 1, 0, 0, 1);
                        }
                    }
                }
            }
        }
        if(spaceshipProjectile.isActive()){
            for(DefenseBlock block : defenseBlocks){
                if(block != null && block.getVisibility()){
                    if(RectF.intersects(spaceshipProjectile.getRect(), block.getRect())){
                        spaceshipProjectile.setInactive();
                        block.setInvisible();
                        //  TODO Sound Projectile on DefenseBlock
                        soundPool.play(damageShelterID, 1, 1, 0, 0, 1);
                    }
                }
            }
        }

        // Colision de proyectil enemigo con jugador
        for(Projectile projectile : invaderProjectiles){
            if(projectile != null && projectile.isActive()){
                if(RectF.intersects(spaceship.getRect(), projectile.getRect())){
                    projectile.setInactive();
                    lives--;
                    // TODO Sound PlayerExplode
                    soundPool.play(playerExplodeID, 1, 1, 0, 0, 1);

                    // TODO Perdida? dxfdsgdggd
                    if(lives == 0){
                        gamePaused = true;
                        lives = 3;
                        score = 0;
                        prepareLevel();
                    }
                }
            }
        }
    }

    private void draw() {
        // Asegurar que la superficie a dibujar sea valida
        if (surfaceHolder.getSurface().isValid()) {
            // Bloquear el canvas
            canvas = surfaceHolder.lockCanvas();
            // Dibujar color de fondo
            canvas.drawColor(Color.argb(255, 26, 128, 182));
            paint.setColor(Color.argb(255, 255, 255, 255));

            // Dibujar el spaceship
            canvas.drawBitmap(spaceship.getBitmap(), spaceship.getPosX(), spaceship.getPosY(), paint);

            // Dibujar los invasores
            for(Invader invader: invaders){
                if(invader != null && invader.isVisible()) {
                    if(uhOrOh) {
                        canvas.drawBitmap(invader.getBitmap(), invader.getX(), invader.getY(), paint);
                    }else{
                        canvas.drawBitmap(invader.getBitmap2(), invader.getX(), invader.getY(), paint);
                    }
                }
            }

            // Dibujar bloques visibles
            for(DefenseBlock block : defenseBlocks){
                if(block != null && block.getVisibility()) {
                    canvas.drawRect(block.getRect(), paint);
                }
            }

            // Dibujar el proyectil del Spaceship y de los Invasores
            if(spaceshipProjectile.isActive()){
                canvas.drawRect(spaceshipProjectile.getRect(), paint);
            }
            for(Projectile projectile : invaderProjectiles){
                if(projectile != null && projectile.isActive()){
                    canvas.drawRect(projectile.getRect(), paint);
                }
            }
            // Dinujar los stats
            paint.setColor(Color.argb(255, 249, 129, 0));
            paint.setTextSize(40);
            canvas.drawText("Score: " + score + "   Lives: " + lives, 10, 50, paint);

            // Liberar el canvas
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public void pause() {
        gamePlaying = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Log.e("Error:", "joining thread");
        }
    }
    public void resume() {
        gamePlaying = true;
        gameThread = new Thread(this);
        gameThread.start();
    }


    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        // Listener para cuando se toca la pantalla
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            // Al tocar la pantalla
            case MotionEvent.ACTION_DOWN:
                gamePaused = false;
                if(motionEvent.getY() > screenY - (screenY / 8)){
                    // Para mover la nave
                    if(motionEvent.getX() > screenX / 2){
                        spaceship.setMovementState(spaceship.RIGHT);
                    }else{
                        spaceship.setMovementState(spaceship.LEFT);
                    }
                }else{
                    // Para disparar
                    if(spaceshipProjectile.shoot(spaceship.getPosX() + (spaceship.getLength() / 2), spaceship.getPosY() - (spaceship.getHeight() / 2), spaceshipProjectile.UP)){
                        // TODO Sound Shoot
                        soundPool.play(shootID, 1,1, 0, 0, 1);
                    }
                }
                break;
            // Al quitar el dedo de la pantalla
            case MotionEvent.ACTION_UP:
                spaceship.setMovementState(spaceship.STOPPED);
                break;
        }
        return true;
    }


}
