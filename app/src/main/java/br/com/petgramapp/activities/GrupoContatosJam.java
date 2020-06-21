package br.com.petgramapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.adapter.AdapterContatosGrupoNormalJam;
import br.com.petgramapp.adapter.AdapterGrupoContatosSelecionadosNormalJam;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.helper.RecyclerItemClickListener;
import br.com.petgramapp.helper.UsuarioFirebase;
import br.com.petgramapp.model.Usuario;

public class GrupoContatosJam extends AppCompatActivity {
    private RecyclerView recyclerViewContatosGrupo;
    private RecyclerView recyclerViewContatosSelecionadosGrupo;
    private AdapterContatosGrupoNormalJam adapterContatosGrupoNormalJam;
    private FloatingActionButton fabGrupoContatos;
    private AdapterGrupoContatosSelecionadosNormalJam adapterGrupoContatosSelecionadosNormalJam;
    private FirebaseFirestore firebaseFirestore;
    private String idUsuarioLogado;
    private List<Usuario> listaMembros = new ArrayList<>();
    private List<Usuario> listaMembrosSelecionados = new ArrayList<>();
    private ListenerRegistration eventListener;
    private Toolbar toolbarGrupo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grupo_contatos_jam);
        toolbarGrupo = findViewById(R.id.toolbar_GrupoContatosJam);
        toolbarGrupo.setTitle("Novo PetGrupo");
        setSupportActionBar(toolbarGrupo);
        carregarElementos();

        //config init
        firebaseFirestore = ConfiguracaoFirebase.getFirebaseFirestore();
        idUsuarioLogado = UsuarioFirebase.getIdentificadorUsuario();


        //CONFIGURAÇÃO INICIAL DO RECYCLER CONTATOS GERAL

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerViewContatosGrupo.setLayoutManager(linearLayoutManager);
        adapterContatosGrupoNormalJam = new AdapterContatosGrupoNormalJam(this, listaMembros);
        recyclerViewContatosGrupo.setAdapter(adapterContatosGrupoNormalJam);

        recyclerViewContatosGrupo.addOnItemTouchListener(new RecyclerItemClickListener(
                this, recyclerViewContatosGrupo, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Usuario usuarioSelecionado = listaMembros.get(position);
                Toast.makeText(GrupoContatosJam.this,
                        usuarioSelecionado.getNomePetUsuario() + " adicionado a seu novo grupo.", Toast.LENGTH_SHORT).show();
                //REMOVER USUARIO DA LISTA
                listaMembros.remove(usuarioSelecionado);
                adapterContatosGrupoNormalJam.notifyDataSetChanged();

                //adicionando usuario na nova lista
                listaMembrosSelecionados.add(usuarioSelecionado);
                recyclerViewContatosSelecionadosGrupo.setVisibility(View.VISIBLE);


                Collections.sort(listaMembrosSelecionados, new Comparator<Usuario>() {
                    @Override
                    public int compare(Usuario o1, Usuario o2) {
                        return o1.getNomePetUsuario().compareToIgnoreCase(o2.getNomePetUsuario());
                    }
                });
                adapterGrupoContatosSelecionadosNormalJam.notifyDataSetChanged();
                atualizarMembrosToolbar();

            }

            @Override
            public void onLongItemClick() {

            }

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        }
        ));


        LinearLayoutManager linearLayoutManagerSelecionados =
                new LinearLayoutManager(GrupoContatosJam.this, LinearLayoutManager.HORIZONTAL, false);

        recyclerViewContatosSelecionadosGrupo.setLayoutManager(linearLayoutManagerSelecionados);

        adapterGrupoContatosSelecionadosNormalJam =
                new AdapterGrupoContatosSelecionadosNormalJam(this, listaMembrosSelecionados);

        recyclerViewContatosSelecionadosGrupo.setAdapter(adapterGrupoContatosSelecionadosNormalJam);

        recyclerViewContatosSelecionadosGrupo.addOnItemTouchListener(new RecyclerItemClickListener(this,
                recyclerViewContatosSelecionadosGrupo, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Usuario usuarioSelecionado = listaMembrosSelecionados.get(position);
                Toast.makeText(GrupoContatosJam.this,
                        usuarioSelecionado.getNomePetUsuario() + " removido. Tadinho(a).", Toast.LENGTH_LONG).show();
                //REMOVER USUARIO DA LISTA
                listaMembrosSelecionados.remove(usuarioSelecionado);
                adapterContatosGrupoNormalJam.notifyDataSetChanged();
                if (listaMembrosSelecionados.size() == 0 || listaMembrosSelecionados == null) {
                    recyclerViewContatosSelecionadosGrupo.setVisibility(View.GONE);
                }


                //adicionando usuario na nova lista
                listaMembros.add(usuarioSelecionado);
                Collections.sort(listaMembros, new Comparator<Usuario>() {
                    @Override
                    public int compare(Usuario o1, Usuario o2) {
                        return o1.getNomePetUsuario().compareToIgnoreCase(o2.getNomePetUsuario());
                    }
                });
                atualizarMembrosToolbar();
                adapterContatosGrupoNormalJam.notifyDataSetChanged();
            }

            @Override
            public void onLongItemClick() {

            }

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        }));

        fabGrupoContatos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (listaMembrosSelecionados != null && listaMembrosSelecionados.size() > 0) {
                    Intent intent = new Intent(GrupoContatosJam.this, CadastroGrupoConversasJam.class);
                    intent.putExtra("membros", (Serializable) listaMembrosSelecionados);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    Snackbar.make(v, "Sua lista está vazia. Tem que ter amiguinhos selecionados.", Snackbar.LENGTH_LONG)
                            .show();
                }

            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }


    public void atualizarMembrosToolbar() {
        int totalSelecionados = listaMembrosSelecionados.size();
        int totalContatos = listaMembros.size() + totalSelecionados;
        toolbarGrupo.setSubtitle("Selecionados " + totalSelecionados + " de " + totalContatos);
    }

    private void carregarElementos() {
        recyclerViewContatosGrupo = findViewById(R.id.recyclerView_Contatos_GrupoJam);
        recyclerViewContatosSelecionadosGrupo = findViewById(R.id.recyclerView_ContatosSelecionados_GrupoJam);
        fabGrupoContatos = findViewById(R.id.fab_grupoContatosJam);
    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarContatos();
    }

    @Override
    protected void onStop() {
        super.onStop();
        eventListener.remove();
    }

    public void recuperarContatos() {
        Query query = firebaseFirestore.collection("Usuarios");

        eventListener = query.orderBy("nomePetUsuario")
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) return;

                        listaMembros.clear();

                        for (DocumentSnapshot ds : queryDocumentSnapshots) {
                            Usuario usuario = ds.toObject(Usuario.class);

                            if (usuario.getId().equals(UsuarioFirebase.getIdentificadorUsuario()))
                                continue;
                            listaMembros.add(usuario);
                        }

                        atualizarMembrosToolbar();
                        adapterContatosGrupoNormalJam.notifyDataSetChanged();

                    }
                });
    }
}
