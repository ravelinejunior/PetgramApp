package br.com.petgramapp.activities;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.helper.UsuarioFirebase;

public class ChatApplication extends Application implements Application.ActivityLifecycleCallbacks {
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firebaseFirestore;

    private void setOnline(boolean enabled){
        firebaseFirestore =ConfiguracaoFirebase.getFirebaseFirestore();
        firebaseUser = UsuarioFirebase.getUsuarioAtual();
        String idUsuario = firebaseUser.getUid();
        if (idUsuario != null){
            firebaseFirestore.collection("Usuarios")
                    .document(idUsuario)
                    .update("online",enabled);
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        setOnline(true);
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        setOnline(false);
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }
}
