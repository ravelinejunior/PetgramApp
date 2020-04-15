package br.com.petgramapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import br.com.petgramapp.R;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.helper.UsuarioFirebase;
import br.com.petgramapp.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class ComentariosActivity extends AppCompatActivity {

    private EditText comentarioEditComentario;
    private Button botaoSalvarComentario;
    private CircleImageView fotoPerfilComentario;
    private String idPostagem;
    private String idUsuarioPostou;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comentarios);

        firebaseUser = UsuarioFirebase.getUsuarioAtual();
        carregarElementos();

        //recuperar valores da intent anterior
        Intent intent = getIntent();
        idPostagem = intent.getStringExtra("idPostagem");
        idUsuarioPostou = intent.getStringExtra("usuarioPostouId");

        databaseReference = ConfiguracaoFirebase.getReferenciaDatabase();

        recuperarImagem();




    }

    public void clicarBotaoSalvarComentario(View v){
        if (comentarioEditComentario.getText().toString().equals("")){
            Snackbar.make(v,"Você deveria fazer um PetComentario.",Snackbar.LENGTH_SHORT).show();
        }
        else{
            adicionarComentario();
        }


    }

    private void adicionarComentario() {
        DatabaseReference comentariosRef = databaseReference.child("Comentarios").child(idPostagem);

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("comentario",comentarioEditComentario.getText().toString());
        hashMap.put("idQuemPublicou",firebaseUser.getUid());

        comentariosRef.push().setValue(hashMap);
        comentarioEditComentario.setText("");
    }

    private void recuperarImagem(){
        DatabaseReference usuariosRef = databaseReference.child("usuarios").child(firebaseUser.getUid());

        usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);
                Picasso.get().load(usuario.getUriCaminhoFotoPetUsuario()).placeholder(R.drawable.ic_pets_black_24dp).into(fotoPerfilComentario);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void carregarElementos(){
        comentarioEditComentario = findViewById(R.id.comentario_EditText_Comentario_id);
        botaoSalvarComentario = findViewById(R.id.salvarComentario_button_Comentario_id);
        fotoPerfilComentario = findViewById(R.id.foto_PerfilUsuario_Comentario_id);

        Toolbar toolbarComentario = findViewById(R.id.toolbar_ComentariosActivity_id);
        setSupportActionBar(toolbarComentario);
        getSupportActionBar().setTitle("Comentarios");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbarComentario.setNavigationOnClickListener(v -> {
            finish();
        });

    }
}
