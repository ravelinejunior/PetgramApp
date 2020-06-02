package br.com.petgramapp.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Objects;

import br.com.petgramapp.R;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.launcher.SlideHomeLauncher;
import br.com.petgramapp.model.Usuario;

public class CadastrarActivity extends AppCompatActivity {

    //widgets
    private TextInputEditText nomePet;
    private TextInputEditText emailPet;
    private TextInputEditText idadePet;
    private TextInputEditText descricaoPet;
    private TextInputEditText senhaPet;
    private TextInputEditText confirmaSenhaPet;
    private Button botaoCadastrarPet;
    private ProgressBar progressBarCadastrarPet;

    //USUARIO
    private Usuario usuario;

    //firebase
    private FirebaseAuth auth;
    private String tokenId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar);
        carregarElementos();

        //configurações iniciais
        auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
        DatabaseReference reference = ConfiguracaoFirebase.getReferenciaDatabase();

        botaoCadastrarUsuario();


    }

    @Override
    public boolean onSupportNavigateUp() {
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigateUp() {
        return super.onNavigateUp();
    }

    public Context getContext(){
      return CadastrarActivity.this;
    }


    public String recuperarToken(){

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(instanceIdResult -> {
            String token = instanceIdResult.getToken();
            String id = instanceIdResult.getId();
            tokenId = token;
            Log.i("instanceIdResult","Token: "+token);
            Log.i("instanceIdResult","ID: "+id);


        });

        return tokenId;

    }

    public void botaoCadastrarUsuario(){
        botaoCadastrarPet.setOnClickListener(v -> {
            progressBarCadastrarPet.setVisibility(View.VISIBLE);
            progressBarCadastrarPet.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            InputMethodManager imm = (InputMethodManager) getSystemService(
                    Activity.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            //receber as strings de cadastro
            String str_nome = nomePet.getText().toString();
            String str_email = emailPet.getText().toString();
            String str_idade = idadePet.getText().toString();
            String str_descricao = descricaoPet.getText().toString();
            String str_senha = senhaPet.getText().toString();
            String str_confirmaSenha = confirmaSenhaPet.getText().toString();

            //verificar se campos foram digitados
            if (!str_nome.isEmpty()){
                if (!str_email.isEmpty()){
                    if (!str_senha.isEmpty()){
                        if (!str_confirmaSenha.isEmpty() && str_confirmaSenha.equalsIgnoreCase(str_senha)){
                            String token = FirebaseInstanceId.getInstance().getToken();
                            usuario = new Usuario();
                            usuario.setNomePetUsuario(str_nome);
                            usuario.setEmailPetUsuario(str_email);
                            usuario.setSenhaPetUsuario(str_senha);
                            usuario.setConfirmacaoSenhaPetUsuario(str_confirmaSenha);
                            usuario.setIdadePetUsuario(str_idade);
                            usuario.setDescricaoPetUsuario(str_descricao);
                            usuario.setTokenFoneMessage(token);
                            cadastrarNovoUsuario(usuario);


                        }else{
                            Snackbar.make(v,"Favor, digitar sua Pet senha como a senha superior digitada!",Snackbar.LENGTH_SHORT).show();
                            progressBarCadastrarPet.setVisibility(View.GONE);
                        }
                    }else{
                        Snackbar.make(v,"Favor, digitar sua Pet senha!",Snackbar.LENGTH_SHORT).show();
                        progressBarCadastrarPet.setVisibility(View.GONE);
                    }
                }else{
                    Snackbar.make(v,"Favor, digitar seu Pet email!",Snackbar.LENGTH_SHORT).show();
                    progressBarCadastrarPet.setVisibility(View.GONE);
                }
            }else{
                Snackbar.make(v,"Favor, digitar o nome do seu Pet!",Snackbar.LENGTH_SHORT).show();
                progressBarCadastrarPet.setVisibility(View.GONE);
            }

        });
    }

    private void cadastrarNovoUsuario(Usuario usuario) {
        auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String str_emailcad = emailPet.getText().toString();
        String str_senhacad = senhaPet.getText().toString();
        try {
            //AUTENTICANDO USUARIO
            auth.createUserWithEmailAndPassword(str_emailcad,str_senhacad).addOnCompleteListener(
                    CadastrarActivity.this, task -> {
                        if (task.isSuccessful()){
                            String idUsuario = task.getResult().getUser().getUid();
                            usuario.setId(idUsuario);
                            usuario.salvarUsuario();
                            usuario.salvarUsuarioFirestore();
                            progressBarCadastrarPet.setVisibility(View.GONE);
                            Intent intent = new Intent(getContext(),LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }else{
                            //tratando os erros
                            String erro = "";
                            try {
                                throw Objects.requireNonNull(task.getException());

                            } catch (FirebaseAuthEmailException e) {
                                erro = "Autenticação falhou.";

                            } catch (FirebaseAuthWeakPasswordException e) {
                                erro = "Senha fraca.";

                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                erro = "Digite um email válido.";

                            } catch (FirebaseAuthUserCollisionException e) {
                                erro = "Email já cadastrado.";

                            } catch (Exception e) {
                                erro = "Erro de autenticação: " + e.getMessage();
                                e.printStackTrace();
                            }
                            Toast.makeText(this, erro, Toast.LENGTH_SHORT).show();
                            progressBarCadastrarPet.setVisibility(View.GONE);
                        }
                    }
            );
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(getContext(), SlideHomeLauncher.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    public void carregarElementos(){
        nomePet = findViewById(R.id.nomePet_Cadastro_act);
        emailPet = findViewById(R.id.emailUsuarioPet_Cadastro_act);
        idadePet = findViewById(R.id.idade_Cadastro_act);
        descricaoPet = findViewById(R.id.descricaoPet_Cadastro_act);
        senhaPet = findViewById(R.id.senhaPet_Cadastro_act);
        confirmaSenhaPet = findViewById(R.id.confirmacaoSenhaPet_Cadastro_act);
        botaoCadastrarPet = findViewById(R.id.botaoCadastrar_Cadastro_act);
        progressBarCadastrarPet = findViewById(R.id.progressBar_Cadastrar_act);

    }
}


















