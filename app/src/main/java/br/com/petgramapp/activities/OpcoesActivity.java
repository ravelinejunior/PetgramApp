package br.com.petgramapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import br.com.petgramapp.R;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.model.FirebaseNotification;
import br.com.petgramapp.model.NotificacaoDadosFirebase;
import br.com.petgramapp.model.Usuario;
import br.com.petgramapp.utils.MessageUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OpcoesActivity extends AppCompatActivity {

    private TextView sairOpcoes ;
    private TextView configuracaoOpcoes;
    private Toolbar toolbarOpcoes;
    private FirebaseAuth firebaseAuth;
    private Usuario usuario;
    private String idUsuario;
    private String tokenId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opcoes);
        carregarElementos();
        firebaseAuth = ConfiguracaoFirebase.getFirebaseAutenticacao();
        sairOpcoes.setOnClickListener(v -> deslogarUsuario());
        FirebaseMessaging.getInstance().subscribeToTopic("teste");


       Bundle bundle = getIntent().getExtras();

        if (bundle != null){
            idUsuario = bundle.getString("idUsuario");
        }

        Log.i("idUsuario",idUsuario);

    }

    public void recuperarToken(){
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(instanceIdResult -> {
            String token = instanceIdResult.getToken();
            String id = instanceIdResult.getId();

            Log.i("instanceIdResult","Token: "+token);
            Log.i("instanceIdResult","ID: "+id);
        });
    }

    public void enviarNotificacao(){

        usuarioInfo(idUsuario);

        String token;
        String titulo;
        String body;

    if (idUsuario != null && tokenId != null){

        token = usuarioInfo(idUsuario);
        titulo = "Função em desenvolvimento.";
        body = "Desculpe. Estamos trabalhando para disponibilizar essa funcionalidade o mais rapido possivel.";

    }else{

        token = "cdunBWO_Z20:APA91bF86geZDKW9PwtiUI-Zo6z_Qho-AcBJbO8bdG6adWfAYoRQNWW9xkkiAOGG5iFhR4MiMER343AVI37QVUkY-JnMACINCAgq8PFlEYCFLQ2zDE8Kl9w_P3Gr1ClOOjQNTKL6eh9-";
        titulo = "Função ainda em produção.";
        body = "Desculpe. Estamos trabalhando para disponibilizar essa funcionalidade o mais rapido possivel.";

    }

        //Montar objeto notificação
        NotificacaoDadosFirebase notificacaoDadosFirebase = new NotificacaoDadosFirebase(titulo,body);
        FirebaseNotification firebaseNotification = new FirebaseNotification(token,notificacaoDadosFirebase);

        //enviar
        Call<FirebaseNotification> callNotification = MessageUtils.getNotificacao().salvarNotificacao(firebaseNotification);
        callNotification.enqueue(new Callback<FirebaseNotification>() {
            @Override
            public void onResponse(Call<FirebaseNotification> call, Response<FirebaseNotification> response) {

                if (response.isSuccessful()){
                    Toast.makeText(OpcoesActivity.this, "Codigo de status: "+response.code(), Toast.LENGTH_SHORT).show();
                    Log.i("onResponse","Url: "+response.raw()+ " call"+call.request());
                }
            }

            @Override
            public void onFailure(Call<FirebaseNotification> call, Throwable t) {

            }
        });


    }

    private String usuarioInfo(String idUser){
        DatabaseReference reference = ConfiguracaoFirebase.getReferenciaDatabase()
                .child("usuarios").child(idUser);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Usuario usuario = dataSnapshot.getValue(Usuario.class);
                tokenId = usuario.getTokenFoneMessage();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Log.i("tokenId",""+tokenId);
        return tokenId;
    }

    public void carregarElementos(){
        toolbarOpcoes = findViewById(R.id.toolbar_Opcoes);
        sairOpcoes = findViewById(R.id.sair_Opcoes_id);
        configuracaoOpcoes = findViewById(R.id.configuracoes_Opcoes_id);

        toolbarOpcoes.setTitleTextColor(getResources().getColor(R.color.branco));

        setSupportActionBar(toolbarOpcoes);
        getSupportActionBar().setTitle(R.string.opcoes);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbarOpcoes.setNavigationOnClickListener(v -> finish());

    }

    private void deslogarUsuario(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.deseja_sair_app);
        builder.setIcon(R.drawable.ic_pets_black_24dp);
        builder.setMessage(R.string.deseja_sair_app_message);
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.confirmar), (dialog, which) -> {
            try {
                firebaseAuth.signOut();
                startActivity(new Intent(this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            } catch (Exception e) {
                Toast.makeText(this, "Erro." + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(R.string.cancelar, (dialog, which) -> {
            Toast.makeText(this, "Muito bem. Continue se divertindo com os pets do mundo todo!", Toast.LENGTH_SHORT).show();
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void selecionarOpcoes(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("O que fazer.");
        builder.setPositiveButton("Recuperar Token", (dialog, which) -> {
            recuperarToken();
            dialog.dismiss();
        });

        builder.setNegativeButton("Enviar Notificação", (dialog, which) -> {
            enviarNotificacao();
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


}
