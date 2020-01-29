package Classes;

// Esta clase contiene toda la lógica del juego
// Además, es quien responde a las entradas por pantalla

import android.content.Context;
import android.graphics.Bitmap;
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

import java.util.Random;

public class GameView extends SurfaceView implements Runnable {

    Context context;

    // Algunos parametros
    private final int BAR_PADDING_FACTOR = 10;
    private final int ROW_INVADERS = 4;
    private final int COLUMN_INVADERS = 6; // Se crean 1 menos que el numero
    private final int ROW_DEFENSE = 4;
    private final int COLUMN_DEFENSE = 8;
    private final int SHELTER_DEFENSE = 5; // Se crean 1 menos que el numero
    private final int STARTING_LIVES = 3;
    private final int MIN_MENACE_INTERVAL = 200;
    private final int MENACE_INTERVAL_FACTOR = 60; // Cantidad a la que se reduce el intervalo cada vez que se chocan los bordes
    private final int SCORE_FACTOR = 1; // Este factor multiplica el valor de puntos que otorga cada enemigo

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
    public static int screenX;
    public static int screenY;

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

    // otros
    private int currentScore;
    private int currentLives;
    private Random random = new Random();

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
            Log.e("error", "Error al cargar los sonidos.");
        }
        //prepareLevel();
    }

    private void prepareLevel() {

        // Crear la nave
        spaceship = new Spaceship(context);

        // Crear el proyectil del Spaceship y los proyectiles de los enemigos
        spaceshipProjectile = new Projectile();
        for (int i = 0; i < invaderProjectiles.length; i++) {
            invaderProjectiles[i] = new Projectile();
        }
        nextInvaderProjectile = 0;

        // Crear las filas de invasores
        int cant = 0, columns = COLUMN_INVADERS;
        for (int i = 0; i < ROW_INVADERS; i++) {
            for (int x = 1; x < columns; x++) {
                invaders[cant] = new Invader(context, i, x, screenY / BAR_PADDING_FACTOR, screenX / columns);

                cant++;
            }
        }
        // Crear bloques
        numDefenseBlocks = 0;
        int totalShelterNumber = SHELTER_DEFENSE; // Se crean 1 menos que el numero
        columns = COLUMN_DEFENSE;
        for (int shelterNumber = 1; shelterNumber < totalShelterNumber; shelterNumber++) {
            for (int i = 0; i < ROW_DEFENSE; i++) {
                for (int x = 0; x < columns; x++) {
                    defenseBlocks[numDefenseBlocks] = new DefenseBlock(i, x, shelterNumber, screenY - (screenY / 8 * 2), screenX / totalShelterNumber, columns);
                    numDefenseBlocks++;
                }
            }
        }
        currentScore = 0;
        currentLives = STARTING_LIVES;
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
            if (!gamePaused) {
                playSound(startFrameTime);
            }
        }
    }
    private void playSound(long startFrameTime) {
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

        // Vidas del jugador terminadas
        boolean lost = currentLives == 0;

        // Actualizar las entidades (invasores, spaceship y proyectiles) activas
        updateEntities();
        // Comprobar si hay colisiones
        updateCollisions();

        // TODO Condicion de perdida
        if (lost) {
            prepareLevel();
        }
        // Acutalizar el proyectil del Spaceship si ha sido disparado
        if (spaceshipProjectile.isVisible()) {
            spaceshipProjectile.update(fps);
        }
    }

    private void updateCollisions(){
        // Colision de invasor con spaceship
        for(Invader invader : invaders){
            if(invader != null && invader.isVisible()){
                if(RectF.intersects(invader.getRect(),spaceship.getRect())){
                    // TODO Perdida
                    if (currentLives == 0) {
                        gamePaused = true;
                        currentLives = STARTING_LIVES;
                        currentScore = 0;
                        spaceship.dead(context);
                        prepareLevel();
                    }
                }
            }
        }

        // Colision del proyectil con un invasor
        if (spaceshipProjectile.isVisible()) {
            for (Invader invader : invaders) {
                if (invader != null && invader.isVisible()) {
                    if (RectF.intersects(spaceshipProjectile.getRect(), invader.getRect())) {
                        destroyEntity(invader);
                        // TODO Victoria?
                    }
                }
            }
        }

        // Colision de proyectiles con los bloques
        for (Projectile projectile : invaderProjectiles) {
            if (projectile != null && projectile.isVisible()) {
                for (DefenseBlock block : defenseBlocks) {
                    if (block != null && block.isVisible()) {
                        if (RectF.intersects(projectile.getRect(), block.getRect())) {
                            projectile.setVisible(false);
                            block.setVisible(false);
                            // TODO Sound Projectile on DefenseBlock
                            soundPool.play(damageShelterID, 1, 1, 0, 0, 1);
                        }
                    }
                }
            }
        }
        if (spaceshipProjectile.isVisible()) {
            for (DefenseBlock block : defenseBlocks) {
                if (block != null && block.isVisible()) {
                    if (RectF.intersects(spaceshipProjectile.getRect(), block.getRect())) {
                        spaceshipProjectile.setVisible(false);
                        block.setVisible(false);
                        //  TODO Sound Projectile on DefenseBlock
                        soundPool.play(damageShelterID, 1, 1, 0, 0, 1);
                    }
                }
            }
        }

        // Colision de proyectil enemigo con jugador
        for (Projectile projectile : invaderProjectiles) {
            if (projectile != null && projectile.isVisible()) {
                if (RectF.intersects(spaceship.getRect(), projectile.getRect())) {
                    projectile.setVisible(false);
                    currentLives--;
                    // TODO Sound PlayerExplode
                    soundPool.play(playerExplodeID, 1, 1, 0, 0, 1);

                    // TODO Perdida? dxfdsgdggd
                    if (currentLives == 0) {
                        gamePaused = true;
                        currentLives = STARTING_LIVES;
                        currentScore = 0;
                        spaceship.dead(context);
                        prepareLevel();
                    }
                }
            }
        }
    }
    private void updateEntities() {
        // Mover el Spaceship
        spaceship.update(fps);

        // Colision con el borde la pantalla
        for (Invader invader : invaders) {
            if (invader != null && invader.isVisible()) {
                invader.update(fps);

                // Intentar disparar
                if (invader.takeAim(spaceship.getPosX(), spaceship.getWidth())) {
                    if (invaderProjectiles[nextInvaderProjectile] != null && invaderProjectiles[nextInvaderProjectile].shoot(invader.getPosX() + invader.getWidth() / 2, invader.getPosY(), Movement.DOWN)) {
                        nextInvaderProjectile++;
                        // Si se consumen todos los proyectiles, se comienza de 0
                        // TODO Quitar el limite de proyectiles
                        if (nextInvaderProjectile == MAX_INVADER_PROJECTILES) {
                            nextInvaderProjectile = 0;
                        }
                    }

                }
            }
        }
        // TODO Reducir el intervalo (velocidad del juego)
        /*
        if (menaceInterval - MENACE_INTERVAL_FACTOR >= MIN_MENACE_INTERVAL) {
            menaceInterval -= MENACE_INTERVAL_FACTOR;
        }*/

        // Actualizar los proyectiles activos de los Invasores
        for (Projectile projectile : invaderProjectiles) {
            if (projectile != null && projectile.isVisible()) {
                projectile.update(fps);
            }
        }
    }
    private void destroyEntity(Invader invader){
        currentScore += invader.SCORE_REWARD * SCORE_FACTOR;
        invader.setVisible(false);
        // TODO Sound InvaderExplode
        soundPool.play(invaderExplodeID, 1, 1, 0, 0, 1);
        spaceshipProjectile.setVisible(false);
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
            canvas.drawBitmap(spaceship.getCurrentBitmap(), spaceship.getPosX(), spaceship.getPosY(), paint);

            // Dibujar los invasores
            Bitmap[] bitmaps;
            Bitmap b;
            for (Invader invader : invaders) {
                if (invader != null && invader.isVisible()) {
                    // TODO Hacerlo menos feo
                    bitmaps = invader.getBitmap();
                    if (uhOrOh) {
                        b = bitmaps[0];
                    } else {
                        b = bitmaps[1];
                    }
                    canvas.drawBitmap(b, invader.getPosX(), invader.getPosY(), paint);
                }
            }

            // Dibujar bloques visibles
            for (DefenseBlock block : defenseBlocks) {
                if (block != null && block.isVisible()) {
                    canvas.drawRect(block.getRect(), paint);
                }
            }

            // Dibujar el proyectil del Spaceship y de los Invasores
            if (spaceshipProjectile.isVisible()) {
                canvas.drawRect(spaceshipProjectile.getRect(), paint);
            }
            for (Projectile projectile : invaderProjectiles) {
                if (projectile != null && projectile.isVisible()) {
                    canvas.drawRect(projectile.getRect(), paint);
                }
            }
            // Dinujar los stats
            paint.setColor(Color.argb(255, 249, 129, 0));
            paint.setTextSize(40);
            canvas.drawText("Score: " + currentScore + "   Lives: " + currentLives, 10, 50, paint);

            // Liberar el canvas
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public void pause() {
        gamePlaying = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Log.e("Error:", "Error al hacer .join()");
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
                if (motionEvent.getY() > screenY - (screenY / 8)) {
                    // Para mover la nave
                    if (motionEvent.getX() > screenX / 2) {
                        spaceship.setCurrentMovement(Movement.RIGHT);
                    } else {
                        spaceship.setCurrentMovement(Movement.LEFT);
                    }
                } else {
                    // Para disparar
                    if (spaceshipProjectile.shoot(spaceship.getPosX() + (spaceship.getWidth() / 2), spaceship.getPosY() - (spaceship.getHeight() / 2), Movement.UP)) {
                        // TODO Sound Shoot
                        soundPool.play(shootID, 1, 1, 0, 0, 1);
                    }
                }
                break;
            // Al quitar el dedo de la pantalla
            case MotionEvent.ACTION_UP:
                spaceship.setCurrentMovement(Movement.STOPPED);
                break;
        }
        return true;
    }


}
