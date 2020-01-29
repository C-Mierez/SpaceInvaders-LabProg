package com.example.spaceinvaders_labprogramacion;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;

import Classes.GameView;

// Esta es la actividad principal del juego, que se encarga del juego interactuando directamente
// con la clase GameView

public class MainActivity extends Activity {

    GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        // Obtener los detalles de la pantalla
        Display display = getWindowManager().getDefaultDisplay();
        // Guardar resolucion en una variable de tipo Point
        Point screenResolution = new Point();
        display.getSize(screenResolution);

        // Inicializar gameView con los datos obtenidos
        gameView = new GameView(this, screenResolution.x, screenResolution.y);
        setContentView(gameView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Avisar al gameView que resuma
        gameView.resume();
    }

    @Override
    protected void onPause(){
        super.onPause();
        // Avisar al gameView que pare
        gameView.pause();
    }

}
