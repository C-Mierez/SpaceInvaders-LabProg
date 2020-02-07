package Classes;

// Esta clase contiene toda la lógica del juego
// Además, es quien responde a las entradas por pantalla

import android.content.Context;
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
import java.util.concurrent.LinkedBlockingQueue;

public class GameView extends SurfaceView implements Runnable {

    public static Context context;

    // Algunos parametros
    private final int INPUT_PLAYER_MOVEMENT_FACTOR = 8;
    private final int BAR_PADDING_FACTOR = 10;
    private final int ROW_INVADERS = 4;
    private final int COLUMN_INVADERS = 6; // Se crean 1 menos que el numero
    private final int ROW_DEFENSE = 4;
    private final int COLUMN_DEFENSE = 8;
    private final int SHELTER_DEFENSE_AMOUNT = 5; // Se crean 1 menos que el numero
    private final float SHELTER_DECREASE_FACTOR = 1.17f;
    private final float INVADER_INCREASE_FACTOR = 1.4f;
    private final int STARTING_INVADER_AMOUNT = 6;
    private final int MAX_INVADER_AMOUNT = 35;
    private float UFO_CHANCE_FACTOR = 1.2f;
    private int UFO_CHANCE = 10;
    private final byte INCREASE_SPEED_LIMIT = 10;
    private final int MIN_MENACE_INTERVAL = 240;
    private final int MENACE_INTERVAL_FACTOR = 60; // Cantidad a la que se reduce el intervalo cada vez que se chocan los bordes
    private final int SCORE_FACTOR = 1; // Este factor multiplica el valor de puntos que otorga cada enemigo
    private final int SCORE_TO_WIN = 30;
    private final int LEVELS_FOR_BOSS = 3; // TODO Cambiar
    private final int STARTING_LIVES = 3;
    private final int STARTING_MENACE_INTERVAL = 1000;

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
    public static Spaceship spaceship;
    // Proyectil del jugador
    private Projectile spaceshipProjectile;

    // TODO Proyectiles de los Invasores
    public static LinkedBlockingQueue<Projectile> invaderProjectiles;
    // Invasores
    public static LinkedBlockingQueue<Invader> invaders;
    // Bloques de defensa
    public static LinkedBlockingQueue<DefenseBlock> defenseBlocks;
    // Bosses
    public static LinkedBlockingQueue<Boss> bosses;

    // Sonidos ( incializados por default)
    private SoundPool soundPool;
    private int playerExplodeID = -1;
    private int invaderExplodeID = -1;
    private int shootID = -1;
    private int damageShelterID = -1;
    private int uhID = -1;
    private int ohID = -1;
    // Sonido en intervalo
    private long menaceInterval;
    private boolean uhOrOh;
    private long lastMenaceTime;
    private byte increaseSpeedCounter;

    // otros
    private int currentLevel;
    private int currentScore;
    private int currentLives;
    private int invaderAmount;
    private int shelterAmount;
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
        invaderAmount = STARTING_INVADER_AMOUNT;
        shelterAmount = SHELTER_DEFENSE_AMOUNT;
        currentLevel = 0;
        currentScore = 0;
        currentLives = STARTING_LIVES;
        spaceship = new Spaceship(context);
        spaceshipProjectile = new Projectile();
        invaderProjectiles = new LinkedBlockingQueue<>();
        invaders = new LinkedBlockingQueue<>();
        bosses = new LinkedBlockingQueue<>();
        //prepareLevel();
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
                increaseSpeed(startFrameTime);
                if (currentLives == 0) {
                    lose();
                }
                if (invaders.isEmpty() && bosses.isEmpty()) {
                    win();
                }
            }
        }
    }

    private void increaseSpeed(long startFrameTime) {
        // Hacer sonar sonido cuando se cumple el intervalo
        if ((startFrameTime - lastMenaceTime) > menaceInterval) {
            if (uhOrOh) {
                soundPool.play(uhID, 1, 1, 0, 0, 1);
            } else {
                soundPool.play(ohID, 1, 1, 0, 0, 1);
            }
            lastMenaceTime = System.currentTimeMillis();
            uhOrOh = !uhOrOh; // Intercambio de sonido
            // Aumentar la velocidad de los enemigos tras pasar
            if (menaceInterval - MENACE_INTERVAL_FACTOR >= MIN_MENACE_INTERVAL && increaseSpeedCounter++ >= INCREASE_SPEED_LIMIT) {
                menaceInterval -= MENACE_INTERVAL_FACTOR;
                increaseSpeedCounter = 0;
            }
        }

    }

    private void update() {
        // Actualizar las entidades (invasores, spaceship y proyectiles) activas
        updateEntities();
        // Comprobar si hay colisiones
        updateCollisions();
        // Acutalizar el proyectil del Spaceship si ha sido disparado
        if (spaceshipProjectile.isVisible()) {
            spaceshipProjectile.update(fps);
        }
    }

    private void updateCollisions() {
        // Colision de invasor con spaceship
        for (Entity invader : invaders) {
            if (invader.isVisible()) {
                if (RectF.intersects(invader.getRect(), spaceship.getRect())) {
                    // TODO Perdida
                    currentLives = 0;
                }
            }
        }
        for (Entity boss : bosses) {
            if (boss.isVisible()) {
                if (RectF.intersects(boss.getRect(), spaceship.getRect())) {
                    // TODO Perdida
                    currentLives = 0;
                }
            }
        }
        // TODO Colision de invasor con bloques
        for (Entity invader : invaders) {
            if (invader.isVisible()) {
                for (DefenseBlock block : defenseBlocks) {
                    if (block != null && block.isVisible()) {
                        if (RectF.intersects(invader.getRect(), block.getRect())) {
                            block.setVisible(false);
                            // soundPool.play(damageShelterID, 1,1,0,0,1);
                        }
                    }
                }
            }
        }
        for (Entity boss : bosses) {
            if (boss.isVisible()) {
                for (DefenseBlock block : defenseBlocks) {
                    if (block != null && block.isVisible()) {
                        if (RectF.intersects(boss.getRect(), block.getRect())) {
                            block.setVisible(false);
                            // soundPool.play(damageShelterID, 1,1,0,0,1);
                        }
                    }
                }
            }
        }
        // Colision del proyectil con un invasor
        if (spaceshipProjectile.isVisible()) {
            for (Invader invader : invaders) {
                if (invader.isVisible()) {
                    if (RectF.intersects(spaceshipProjectile.getRect(), invader.getRect())) {
                        if(invader.dealDamage()){
                            // Si muere
                            destroyEntities(invader, spaceshipProjectile, true, invaderExplodeID);
                        }else{
                            soundPool.play(invaderExplodeID, 1, 1, 0, 0, 1);
                            spaceshipProjectile.setVisible(false);
                        }
                    }
                }
            }
        }
        if (spaceshipProjectile.isVisible()) {
            for (Boss boss : bosses) {
                if (boss.isVisible()) {
                    if (RectF.intersects(spaceshipProjectile.getRect(), boss.getRect())) {
                        if(boss.dealDamage()){
                            destroyEntities(boss, spaceshipProjectile, true, invaderExplodeID);
                        }else{ // TODO Cambiar sonido
                            soundPool.play(invaderExplodeID, 1, 1, 0, 0, 1);
                            spaceshipProjectile.setVisible(false);
                        }
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
                            destroyEntities(projectile, block, false, damageShelterID);
                        }
                    }
                }
            }
        }
        if (spaceshipProjectile.isVisible()) {
            for (DefenseBlock block : defenseBlocks) {
                if (block != null && block.isVisible()) {
                    if (RectF.intersects(spaceshipProjectile.getRect(), block.getRect())) {
                        destroyEntities(spaceshipProjectile, block, false, damageShelterID);
                    }
                }
            }
        }
        // Colision de proyectil jugador con proyectil enemigo
        for(Projectile projectile : invaderProjectiles){
            if(projectile.isVisible() && RectF.intersects(spaceshipProjectile.getRect(), projectile.getRect())){
                destroyEntities(projectile,spaceshipProjectile,false, damageShelterID);
            }
        }

        // Colision de proyectil enemigo con jugador
        for (Projectile projectile : invaderProjectiles) {
            if (projectile != null && projectile.isVisible()) {
                if (RectF.intersects(spaceship.getRect(), projectile.getRect())) {
                    damagePlayer(projectile);
                }
            }
        }
    }

    private void updateEntities() {
        // Mover el Spaceship
        spaceship.update(fps);

        // Actualizar invasores
        for (Entity invader : invaders) {
            if (invader.isVisible()) {
                invader.update(fps);
                // Intentar disparar
                if (((Invader)invader).tryShooting(spaceship.getPosX(), spaceship.getWidth())) {
                    ((Invader)invader).shoot();
                }
            }
        }

        // Actualizar bosses
        for(Boss boss : bosses){
            if(boss.isVisible()){
                boss.update(fps);
            }
            if (boss.tryShooting(spaceship.getPosX(), spaceship.getWidth())) {
                boss.shoot();
            }
        }

        // Actualizar los proyectiles activos de los Invasores
        for (Projectile projectile : invaderProjectiles) {
            if (projectile != null && projectile.isVisible()) {
                projectile.update(fps);
            }
        }
    }

    private void damagePlayer(Entity entity) {
        entity.setVisible(false);
        currentLives--;
        // TODO Sound PlayerExplode
        soundPool.play(playerExplodeID, 1, 1, 0, 0, 1);
    }

    private void destroyEntities(Entity entity, Entity entity2, boolean gainScore, int soundID) {
        entity.setVisible(false);
        destroySelfInvader(entity);
        entity2.setVisible(false);
        destroySelfInvader(entity2);
        if (gainScore) {
            currentScore += (entity.getScoreReward() + entity2.getScoreReward()) * SCORE_FACTOR;
        }
        // TODO Sound InvaderExplode
        soundPool.play(soundID, 1, 1, 0, 0, 1);
    }

    public static void destroySelfInvader(Entity invader){
        invaders.remove(invader);
        bosses.remove(invader);
    }

    private void draw() {
        // Asegurar que la superficie a dibujar sea valida
        if (surfaceHolder.getSurface().isValid()) {
            // Bloquear el canvas
            canvas = surfaceHolder.lockCanvas();
            // Dibujar color de fondo
            // canvas.drawColor(Color.argb(255, 26, 128, 182));
            canvas.drawColor(Color.argb(255, 10, 10, 10));
            paint.setColor(Color.argb(255, 255, 255, 255));

            // Dibujar el spaceship
            canvas.drawBitmap(spaceship.getCurrentBitmap(), spaceship.getPosX(), spaceship.getPosY(), paint);

            // Dibujar los invasores
            for (Invader invader : invaders) {
                if (invader != null && invader.isVisible()) {
                        if (uhOrOh) {
                            canvas.drawBitmap(invader.getBitmap(0), invader.getPosX(), invader.getPosY(), paint);
                        } else {
                            canvas.drawBitmap(invader.getBitmap(1), invader.getPosX(), invader.getPosY(), paint);
                        }
                }
            }
            // Dibujar los bosses
            for(Boss boss : bosses){
                if (boss != null && boss.isVisible()) {
                    if(uhOrOh){
                        canvas.drawBitmap(boss.getBitmap(0), boss.getPosX(), boss.getPosY(), paint);
                    }else{
                        canvas.drawBitmap(boss.getBitmap(1), boss.getPosX(), boss.getPosY(), paint);
                    }
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
                //canvas.drawRect(spaceshipProjectile.getRect(), paint);
                canvas.drawBitmap(spaceshipProjectile.getCurrentBitmap(), spaceshipProjectile.getPosX(), spaceshipProjectile.getPosY(), paint);
            }
            for (Projectile projectile : invaderProjectiles) {
                if (projectile != null && projectile.isVisible()) {
                    //canvas.drawRect(projectile.getRect(), paint);
                    //canvas.drawCircle(projectile.getPosX(), projectile.getPosY(), (int)(projectile.getWidth() * 1.5), paint);
                    canvas.drawBitmap(projectile.getCurrentBitmap(), projectile.getPosX(), projectile.getPosY(), paint);
                }
            }
            // Dinujar los stats
            paint.setColor(Color.argb(255, 255, 255, 255));
            paint.setTextSize(40);
            canvas.drawText("Score: " + currentScore + "   Lives: " + currentLives + "   Level: " + currentLevel, 10, 50, paint);

            // Liberar el canvas
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void win() {
        gamePaused = true;
        draw();
        if(currentLevel % LEVELS_FOR_BOSS == 0){
            prepareBossLevel();
        }else{
            if((currentLevel - 1) % LEVELS_FOR_BOSS == 0){
                currentLives = STARTING_LIVES + (currentLevel / LEVELS_FOR_BOSS);
            }
            prepareLevel();
        }
    }

    private void lose() {
        gamePaused = true;
        spaceship.setCurrentBitmap(spaceship.getBitmap()[1]);
        draw();
    }

    private void prepareLevel() {

        // Posicionar la nave en el lugar adecuado
        spaceship.setPosX((screenX / spaceship.STARTING_X_FACTOR) - (spaceship.width / 2));
        spaceship.setPosY(screenY - (screenY / spaceship.STARTING_Y_FACTOR));

        // Limpiar todos los proyectiles creados anteriormente
        invaderProjectiles.clear();

        // Crear las filas de invasores al azar
        invaders = new LinkedBlockingQueue<>();
        for (int i = 0; i < invaderAmount; i++) {
            if(random.nextInt(100) > UFO_CHANCE * UFO_CHANCE_FACTOR){ // Crear Crabs
                invaders.add(new Crab(context,  screenY / BAR_PADDING_FACTOR + (random.nextInt(screenY / BAR_PADDING_FACTOR * 5))));
            }else{ // Crear UFO
                invaders.add(new UFO(context,  screenY / BAR_PADDING_FACTOR + (random.nextInt(screenY / BAR_PADDING_FACTOR * 5))));
            }
        }
        // Crear bloques
        defenseBlocks = new LinkedBlockingQueue<>();
        shelterAmount = random.nextInt(SHELTER_DEFENSE_AMOUNT)+2;
        for (int shelterNumber = 1; shelterNumber < shelterAmount; shelterNumber++) {
            for (int i = 0; i < ROW_DEFENSE; i++) {
                for (int x = 0; x < COLUMN_DEFENSE; x++) {
                    if(random.nextInt(1000) > 300 * SHELTER_DECREASE_FACTOR){
                        defenseBlocks.add(new DefenseBlock(i, x, shelterNumber, screenY - (screenY / 8 * 2), screenX / shelterAmount, COLUMN_DEFENSE));
                    }
                }
            }
        }
        // TODO Guardar score de rondas anteriores
        /*
        currentScore = 0;
        currentLives = STARTING_LIVES;
        */
        // Aumentar cantidad siempre y cuando se respete el limite
        if (invaderAmount * INVADER_INCREASE_FACTOR < MAX_INVADER_AMOUNT) {
            invaderAmount *= INVADER_INCREASE_FACTOR;
        }else{
            invaderAmount = MAX_INVADER_AMOUNT;
        }
        // Aumentar chance de UFOs
        UFO_CHANCE *= UFO_CHANCE_FACTOR;
        resetValues();
    }

    private void prepareBossLevel(){
        // Posicionar la nave en el lugar adecuado
        spaceship.setPosX((screenX / spaceship.STARTING_X_FACTOR) - (spaceship.width / 2));
        spaceship.setPosY(screenY - (screenY / spaceship.STARTING_Y_FACTOR));

        // Limpiar todos los proyectiles creados anteriormente
        invaderProjectiles.clear();

        // Crear las filas de invasores al azar
        bosses = new LinkedBlockingQueue<>();
        for (int i = 0; i < (currentLevel / LEVELS_FOR_BOSS); i++) {
            bosses.add(new Boss(context, screenY / BAR_PADDING_FACTOR + (random.nextInt(screenY / BAR_PADDING_FACTOR * 5))));
        }
        // Crear bloques
        defenseBlocks = new LinkedBlockingQueue<>();
        shelterAmount = random.nextInt(SHELTER_DEFENSE_AMOUNT-2)+4;
        for (int shelterNumber = 1; shelterNumber < shelterAmount; shelterNumber++) {
            for (int i = 0; i < ROW_DEFENSE; i++) {
                for (int x = 0; x < COLUMN_DEFENSE; x++) {
                    if(random.nextInt(1000) > 300 * SHELTER_DECREASE_FACTOR){
                        defenseBlocks.add(new DefenseBlock(i, x, shelterNumber, screenY - (screenY / 8 * 2), screenX / shelterAmount, COLUMN_DEFENSE));
                    }
                }
            }
        }
        // TODO Guardar score de rondas anteriores
        /*
        currentScore = 0;
        */
        resetValues();
    }

    private void resetValues(){
        menaceInterval = STARTING_MENACE_INTERVAL;
        lastMenaceTime = System.currentTimeMillis();
        increaseSpeedCounter = 0;
        currentLevel++;
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
                if (motionEvent.getY() > screenY - (screenY / INPUT_PLAYER_MOVEMENT_FACTOR)) {
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
