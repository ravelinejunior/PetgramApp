package br.com.petgramapp.launcher;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide;

import br.com.petgramapp.R;
import br.com.petgramapp.activities.CadastrarActivity;
import br.com.petgramapp.activities.LoginActivity;
import br.com.petgramapp.activities.StartActivity;
import br.com.petgramapp.helper.ConfiguracaoFirebase;

public class SlideHomeLauncher extends IntroActivity {

    private FirebaseAuth firebaseAuth;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_slide_home_launcher);
      verificarUsuarioLogado();

        addSlide(new FragmentSlide.Builder()
                .background(R.color.branco)
                .fragment(R.layout.launcher_item_1)
                .canGoBackward(false)
                .build()
        );

        addSlide(new FragmentSlide.Builder()
                .background(R.color.branco)
                .fragment(R.layout.launcher_item_2)
                .build()
        );

        addSlide(new FragmentSlide.Builder()
                .background(R.color.branco)
                .fragment(R.layout.launcher_item_3)
                .build()
        );

        addSlide(new FragmentSlide.Builder()
                .background(R.color.branco)
                .fragment(R.layout.launcher_item_4)
                .canGoForward(false)
                .build()
        );



    }

    public void botaoCadastrarLauncher(View view) {
        Intent intent = new Intent(getContext(), CadastrarActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public Context getContext() {
        return SlideHomeLauncher.this;
    }

    public void botaoLogarLauncher(View view) {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void verificarUsuarioLogado(){
        firebaseAuth = ConfiguracaoFirebase.getFirebaseAutenticacao();
        if(firebaseAuth.getCurrentUser() != null){
            startActivity(new Intent(this, StartActivity.class));
            finish();
        }
    }


}
