package br.com.petgramapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.adapter.AdapterPesquisarUsuario;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.helper.UsuarioFirebase;
import br.com.petgramapp.model.Usuario;

public class SeguidoresActivity extends AppCompatActivity {

    private String idUsuario;
    private String idPostagem;
    private String titulo;
     List<String> idLista;
    private RecyclerView recyclerViewSeguidores;
    private AdapterPesquisarUsuario adapterPesquisarUsuario;
     List<Usuario> usuarioList;

    //FIREBASE
    DatabaseReference reference;
    FirebaseUser userFirebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seguidores);

        Intent intent = getIntent();
        idUsuario = intent.getStringExtra("idUsuario");
        idPostagem = intent.getStringExtra("idPostagem");
        titulo = intent.getStringExtra("titulo");

        Toolbar toolbarSeguidores = findViewById(R.id.toobar_Seguidores);
        setSupportActionBar(toolbarSeguidores);
        getSupportActionBar().setTitle(titulo);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarSeguidores.setTitleTextColor(getResources().getColor(R.color.branco));

        toolbarSeguidores.setNavigationOnClickListener(v -> finish());

        recyclerViewSeguidores = findViewById(R.id.recyclerView_Seguidores);
        recyclerViewSeguidores.setHasFixedSize(true);
        reference = ConfiguracaoFirebase.getReferenciaDatabase();
        userFirebase = UsuarioFirebase.getUsuarioAtual();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerViewSeguidores.setLayoutManager(linearLayoutManager);

        usuarioList = new ArrayList<>();
        adapterPesquisarUsuario = new AdapterPesquisarUsuario(this,usuarioList,false);
        recyclerViewSeguidores.setAdapter(adapterPesquisarUsuario);

        idLista = new ArrayList<>();

        switch (titulo){
            case "Curtir":
                getLikes();
                break;
            case "Seguindo":
                getSeguindo();
                break;
            case "Seguidores":
                getSeguidores();
                break;
            case "Views":
                getVisualizacoes();
                break;
        }


    }

    private void getVisualizacoes(){
        DatabaseReference visualizacoesRef = ConfiguracaoFirebase.getReferenciaDatabase()
                .child("Stories").child(idUsuario).child(getIntent().getStringExtra("idStories")).child("Views");

        visualizacoesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                idLista.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    idLista.add(ds.getKey());
                }
                exibirUsuarios();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getSeguidores() {
        DatabaseReference seguidoresRef = ConfiguracaoFirebase.getReferenciaDatabase().child("Seguir").
                child(idUsuario).
                child("seguidores");

        seguidoresRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                idLista.clear();

                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    idLista.add(ds.getKey());
                }
                exibirUsuarios();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
    }

    private void getSeguindo() {
        DatabaseReference seguindoRef = ConfiguracaoFirebase.getReferenciaDatabase().child("Seguir").
                child(idUsuario).
                child("seguindo");
        seguindoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                idLista.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    idLista.add(ds.getKey());
                }
                exibirUsuarios();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getLikes() {
        DatabaseReference likesRef = ConfiguracaoFirebase.getReferenciaDatabase().child("Likes").
                child(idPostagem);

        likesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                idLista.clear();

                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    idLista.add(ds.getKey());
                }

                exibirUsuarios();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void exibirUsuarios(){
        DatabaseReference usuariosRef = ConfiguracaoFirebase.getReferenciaDatabase().child("usuarios");
        usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usuarioList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    Usuario usuario = ds.getValue(Usuario.class);
                    for (String id:idLista){
                        if (usuario.getId().equals(id)){
                            usuarioList.add(usuario);
                            Log.i("usuario",usuario.getNomePetUsuario());
                        }
                    }
                }
                adapterPesquisarUsuario.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
