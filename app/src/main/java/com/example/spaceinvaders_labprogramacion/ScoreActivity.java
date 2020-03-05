package com.example.spaceinvaders_labprogramacion;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class ScoreActivity extends Activity {

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //Obtengo valor del score
        Bundle bundle = getIntent().getExtras();
        int score = bundle.getInt("score");
        TextView textView = (TextView)findViewById(R.id.scoreDerrota);
        textView.setText("" + score);

        //Creacion de activity del menu
        final Intent inicio = new Intent(this, MenuActivity.class);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                startActivity(inicio);
                finish();
            }
        }, 3000);
    }
}
