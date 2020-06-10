package br.com.petgramapp.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.adapter.AdapterGrupoJam;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.helper.UsuarioFirebase;
import br.com.petgramapp.model.Usuario;

public class GrupoContatosJam extends AppCompatActivity {
    private RecyclerView recyclerViewContatosGrupo;
    private RecyclerView recyclerViewContatosSelecionadosGrupo;
    private AdapterGrupoJam adapterGrupoJam;
    private FirebaseFirestore firebaseFirestore;
    private String idUsuarioLogado;
    private List<Usuario> usuarioList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grupo_contatos_jam);
        Toolbar toolbarGrupo = findViewById(R.id.toolbar_GrupoContatosJam);
        setSupportActionBar(toolbarGrupo);
        carregarElementos();

        //config init
        firebaseFirestore = ConfiguracaoFirebase.getFirebaseFirestore();
        idUsuarioLogado = UsuarioFirebase.getIdentificadorUsuario();
        recuperarContatos();

        //CONFIGURAÇÃO INICIAL DO RECYCLER CONTATOS GERAL
        //query
        Query query = firebaseFirestore.collection("Usuarios")
                .orderBy("nomePetUsuario", Query.Direction.ASCENDING);

        //PAGINATION
        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(10)
                .build();

        FirestorePagingOptions<Usuario> options = new FirestorePagingOptions.Builder<Usuario>()
                .setLifecycleOwner(this)
                .setQuery(query, config, Usuario.class)
                .build();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerViewContatosGrupo.setLayoutManager(linearLayoutManager);
       // recyclerViewContatosGrupo.setHasFixedSize(true);
        adapterGrupoJam = new AdapterGrupoJam(options,this);
        recyclerViewContatosGrupo.setAdapter(adapterGrupoJam);



        FloatingActionButton fabGrupoContatos = findViewById(R.id.fab_grupoContatosJam);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void carregarElementos(){
        recyclerViewContatosGrupo = findViewById(R.id.recyclerView_Contatos_GrupoJam);
        recyclerViewContatosSelecionadosGrupo = findViewById(R.id.recyclerView_ContatosSelecionados_GrupoJam);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapterGrupoJam.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapterGrupoJam.stopListening();
    }

    public void recuperarContatos(){
        Query query = firebaseFirestore.collection("Usuarios");

        ListenerRegistration eventListener = query.orderBy("nomePetUsuario")
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) return;

                usuarioList.clear();

                for (DocumentSnapshot ds : queryDocumentSnapshots) {
                    Usuario usuario = ds.toObject(Usuario.class);

                    if (usuario.getId().equals(UsuarioFirebase.getIdentificadorUsuario()))
                        continue;
                    usuarioList.add(usuario);
                }

                adapterGrupoJam.notifyDataSetChanged();


            }
        });
    }
}
