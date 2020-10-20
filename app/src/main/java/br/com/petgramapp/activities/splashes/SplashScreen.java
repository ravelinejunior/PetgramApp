package br.com.petgramapp.activities.splashes;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import br.com.petgramapp.R;
import br.com.petgramapp.launcher.SlideHomeLauncher;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(SplashScreen.this, SlideHomeLauncher.class);
                startActivity(i);
            }
        },2000);
    }
}
