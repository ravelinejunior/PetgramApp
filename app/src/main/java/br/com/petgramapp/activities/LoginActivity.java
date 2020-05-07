package br.com.petgramapp.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import br.com.petgramapp.R;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.launcher.SlideHomeLauncher;
import br.com.petgramapp.model.Usuario;

import static android.view.View.GONE;

public class LoginActivity extends AppCompatActivity {
    //widgets
    private TextInputEditText emailLoginUsuario;
    private TextInputEditText senhaLoginUsuario;
    private Button botaoLogarLoginUsuario;
    private ProgressBar progressBarLoginUsuario;
    private TextView esqueciMinhaSenhaLoginUsuario;

    //firebase
    private FirebaseAuth firebaseAuth;
    private DatabaseReference reference;

    //Usuario
    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        carregarElementros();
        verificarUsuarioLogado();

        //CONFIGURAÇÃO INICIAL
        firebaseAuth = ConfiguracaoFirebase.getFirebaseAutenticacao();
        reference = ConfiguracaoFirebase.getReferenciaDatabase();

        botaoLogarAcionar();
        botaoEsqueciMinhaSenhaAcionar();
    }

    public void botaoLogarAcionar(){
        botaoLogarLoginUsuario.setOnClickListener(v -> {
            progressBarLoginUsuario.setVisibility(View.VISIBLE);

            String str_Email = emailLoginUsuario.getText().toString();
            String str_Senha = senhaLoginUsuario.getText().toString();

            if (!str_Email.isEmpty()){
                if (!str_Senha.isEmpty()){
                    usuario = new Usuario();
                    usuario.setEmailPetUsuario(str_Email);
                    usuario.setSenhaPetUsuario(str_Senha);
                    logarUsuario(usuario);
                }else{
                    Snackbar.make(v, R.string.digite_sua_senha,Snackbar.LENGTH_SHORT).show();
                    progressBarLoginUsuario.setVisibility(View.GONE);
                }
            }else{
                Snackbar.make(v,"Digite seu Pet email!",Snackbar.LENGTH_SHORT).show();
                progressBarLoginUsuario.setVisibility(View.GONE);
            }
        });
    }

    public Context getContext(){
        return LoginActivity.this;
    }

    //verificar se usuario está logado
    private void verificarUsuarioLogado(){
        firebaseAuth = ConfiguracaoFirebase.getFirebaseAutenticacao();
        if(firebaseAuth.getCurrentUser() != null){
            startActivity(new Intent(LoginActivity.this, StartActivity.class));
            finish();
        }
    }


    @Override
    public void onBackPressed() {
        Intent i = new Intent(getContext(), SlideHomeLauncher.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    public void logarUsuario(Usuario usuario){
        firebaseAuth = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String str_Emaillog = usuario.getEmailPetUsuario();
        String str_Senhalog = usuario.getSenhaPetUsuario();

        firebaseAuth.signInWithEmailAndPassword(str_Emaillog,str_Senhalog).addOnCompleteListener(
                LoginActivity.this,
                task -> {
                    if (task.isSuccessful()){
                        progressBarLoginUsuario.setVisibility(View.GONE);

                        DatabaseReference usuarioFirebase = reference.child("usuarios")
                                .child(firebaseAuth.getCurrentUser().getUid());
                        usuarioFirebase.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Intent i = new Intent(getContext(),StartActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(i);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }else{
                        progressBarLoginUsuario.setVisibility(View.GONE);
                        Toast.makeText(this, "Verifique se seu Pet Email ou sua Pet Senha estão digitados corretamente!", Toast.LENGTH_SHORT).show();
                    }
                }
        );

    }

    //METODO PARA Abrir o linear layout de recuperação de senha
    private void esqueceuSenha(){
        //Alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recuperar a senha");
        builder.setIcon(R.drawable.ic_email_alterar);
        //setar layout linear
        LinearLayout linearLayout = new LinearLayout(this);

        //views do alertdialog
        final EditText emailDigitadoNovo = new EditText(this);
        emailDigitadoNovo.setHint("Digite seu Pet Email");
        emailDigitadoNovo.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailDigitadoNovo.setMinEms(20);
        linearLayout.addView(emailDigitadoNovo);
        linearLayout.setPadding(10,10,10,10);
        builder.setView(linearLayout);

        //botoes para recuperar
        builder.setPositiveButton("Recuperar", (dialogInterface, i) -> {
            if (emailDigitadoNovo == null){
                Toast.makeText(this, "Não deixe o campo em branco!", Toast.LENGTH_SHORT).show();
            }
            else{
                String emailRecuperado = emailDigitadoNovo.getText().toString();
                progressBarLoginUsuario.setVisibility(View.VISIBLE);
                recuperarSenha(emailRecuperado);
            }

        });

        //botao para cancelar
        builder.setNegativeButton("Cancelar", (dialogInterface, i) -> {
        });

        builder.create().show();
    }

    private void recuperarSenha(String email){
        try {
            firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    progressBarLoginUsuario.setVisibility(GONE);
                    Toast.makeText(LoginActivity.this, "Email enviado.", Toast.LENGTH_SHORT).show();
                }
                else{
                    progressBarLoginUsuario.setVisibility(GONE);
                    Toast.makeText(LoginActivity.this, "Falha ao enviar.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                progressBarLoginUsuario.setVisibility(GONE);
                //mostrar o erro
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_LONG).show();
            });
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, "Verifique se o campo email está digitado corretamente.", Toast.LENGTH_SHORT).show();
            progressBarLoginUsuario.setVisibility(GONE);
        }
    }

    public void botaoEsqueciMinhaSenhaAcionar(){
        esqueciMinhaSenhaLoginUsuario.setOnClickListener(v -> esqueceuSenha());
    }


    public void carregarElementros(){
        emailLoginUsuario = findViewById(R.id.emailPet_Login_act);
        senhaLoginUsuario = findViewById(R.id.senhaPet_Login_act);
        botaoLogarLoginUsuario = findViewById(R.id.botaoLogarPet_LoginAct);
        progressBarLoginUsuario = findViewById(R.id.progressBar_Login_act);
        esqueciMinhaSenhaLoginUsuario = findViewById(R.id.recuperarSenha_Cadastro_act);

    }
}
