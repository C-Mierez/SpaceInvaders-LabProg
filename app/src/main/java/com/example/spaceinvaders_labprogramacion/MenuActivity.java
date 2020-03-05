package com.example.spaceinvaders_labprogramacion;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;


public class MenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        AudioManager amanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        amanager.setStreamMute(AudioManager.STREAM_MUSIC,false);
    }

    public void startGame (View view){
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
        finish();
    }

    public void closeGame (View view){
        finish();
        System.exit(0);
    }

    //TODO Hacer funcionar lo del audio
    public void mute (View view) {
        AudioManager amanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        amanager.setStreamMute(AudioManager.STREAM_MUSIC,true);
        ImageButton btnUnmute = (ImageButton) findViewById(R.id.unmuteButton), btnMute = (ImageButton) findViewById(R.id.muteButton);
        btnUnmute.setVisibility(View.INVISIBLE);
        btnMute.setVisibility(View.VISIBLE);
    }

    public void unmute (View view) {
        AudioManager amanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        amanager.setStreamMute(AudioManager.STREAM_MUSIC,false);
        ImageButton btnUnmute = (ImageButton) findViewById(R.id.unmuteButton), btnMute = (ImageButton) findViewById(R.id.muteButton);
        btnMute.setVisibility(View.INVISIBLE);
        btnUnmute.setVisibility(View.VISIBLE);
    }
}
