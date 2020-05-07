package br.com.petgramapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;

import br.com.petgramapp.R;
import br.com.petgramapp.helper.ConfiguracaoFirebase;

public class OpcoesActivity extends AppCompatActivity {

    private TextView sairOpcoes ;
    private TextView configuracaoOpcoes;
    private Toolbar toolbarOpcoes;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opcoes);
        carregarElementos();
        firebaseAuth = ConfiguracaoFirebase.getFirebaseAutenticacao();
        sairOpcoes.setOnClickListener(v -> deslogarUsuario());

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
}
