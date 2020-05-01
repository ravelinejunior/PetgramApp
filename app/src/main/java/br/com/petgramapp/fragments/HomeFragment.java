package br.com.petgramapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.activities.LoginActivity;
import br.com.petgramapp.adapter.AdapterFotoPostada;
import br.com.petgramapp.adapter.AdapterStories;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.helper.UsuarioFirebase;
import br.com.petgramapp.model.FotoPostada;
import br.com.petgramapp.model.Stories;

public class HomeFragment extends Fragment {

    private AdapterFotoPostada adapterFotoPostada;
    private List<FotoPostada> fotoPostadaList;
    private ProgressBar progressBarHomeFragment;
    private List<String> listaIdUsuarios = new ArrayList<>();
    private Toolbar toolbar;
    private RecyclerView recyclerViewStories;
    private AdapterStories adapterStories;
    private List<Stories> storiesList = new ArrayList<>();
    FirebaseAuth firebaseAuth;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        progressBarHomeFragment = view.findViewById(R.id.progressBar_HomeFragment);
        toolbar = view.findViewById(R.id.toolbar_HomeFragment_id);
        firebaseAuth = ConfiguracaoFirebase.getFirebaseAutenticacao();
        toolbar.setTitle("Bem vindo "+UsuarioFirebase.getUsuarioAtual().getDisplayName());
        toolbar.setLogo(R.drawable.ic_pets_white_24dp);
        toolbar.setPadding(15,0,0,0);
        toolbar.setTitleTextColor(ContextCompat.getColor(getContext(),R.color.branco));

        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Bem vindo "+UsuarioFirebase.getUsuarioAtual().getDisplayName());


        //FEED
        RecyclerView recyclerViewHomeFragment = view.findViewById(R.id.recyclerView_HomeFragment_id);
        recyclerViewHomeFragment.setHasFixedSize(true);

        //inverter ordem das postagem (a mais atual)
        LinearLayoutManager linearLayout = new LinearLayoutManager(getContext());
        linearLayout.setReverseLayout(true);
        linearLayout.setStackFromEnd(true);
        recyclerViewHomeFragment.setLayoutManager(linearLayout);

        fotoPostadaList = new ArrayList<>();
        adapterFotoPostada = new AdapterFotoPostada(fotoPostadaList, getContext());

        recyclerViewHomeFragment.setAdapter(adapterFotoPostada);
        receberPostagens();

        //STORIES
        recyclerViewStories = view.findViewById(R.id.recyclerView_Stories_HomeFragment_id);
        recyclerViewStories.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManagerStories = new LinearLayoutManager(getContext(),
                RecyclerView.HORIZONTAL, false);
        recyclerViewStories.setLayoutManager(linearLayoutManagerStories);
        adapterStories = new AdapterStories(getContext(), storiesList);
        recyclerViewStories.setAdapter(adapterStories);
        readStories();

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

    }

    private void receberPostagens() {
        DatabaseReference databaseReference = ConfiguracaoFirebase.getReferenciaDatabase().child("Posts");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                fotoPostadaList.clear();

                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    FotoPostada fotoPostada = ds.getValue(FotoPostada.class);

                    fotoPostadaList.add(fotoPostada);

                }

                adapterFotoPostada.notifyDataSetChanged();
                progressBarHomeFragment.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readStories() {
        DatabaseReference usuariosReferencia = ConfiguracaoFirebase.getReferenciaDatabase().child("usuarios");
        usuariosReferencia.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    listaIdUsuarios.add(ds.getKey());

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        readyStories();
    }

    private void readyStories() {
        DatabaseReference storiesRef = ConfiguracaoFirebase.getReferenciaDatabase()
                .child("Stories");

        storiesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long currentTime = System.currentTimeMillis();
                storiesList.clear();
                storiesList.add(new Stories(0, 0, UsuarioFirebase.getUsuarioAtual().getUid(), "", ""));

                for (String id : listaIdUsuarios) {
                    int countStories = 0;
                    Stories stories = null;
                    for (DataSnapshot ds1 : dataSnapshot.child(id).getChildren()) {
                        stories = ds1.getValue(Stories.class);
                        if (currentTime > stories.getDataInicio() && currentTime < stories.getDataFim()) {
                            countStories++;
                        }
                    }

                    if (countStories > 0) {
                        storiesList.add(stories);
                    }

                    adapterStories.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void checarUsuariosSeguidores() {

        DatabaseReference usuariosRef = ConfiguracaoFirebase.getReferenciaDatabase().child("Seguir")
                .child(UsuarioFirebase.getIdentificadorUsuario())
                .child("seguindo");

        usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override

            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaIdUsuarios.clear();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    listaIdUsuarios.add(dataSnapshot1.getKey());
                }
                adapterFotoPostada.notifyDataSetChanged();
                receberPostagens();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    } // para recuperar as postagens dos usuarios que estão sendo seguidos apenas

    public void checarUsuarios() {
        listaIdUsuarios = new ArrayList<>();
        DatabaseReference usuariosRef = ConfiguracaoFirebase.getReferenciaDatabase().child("usuarios");
        usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaIdUsuarios.clear();

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    listaIdUsuarios.add(dataSnapshot1.getKey());
                }
                receberPostagens();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    public void deslogarUsuario() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.deseja_sair_app);
        builder.setIcon(R.drawable.ic_pets_black_24dp);
        builder.setMessage(R.string.deseja_sair_app_message);
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.confirmar), (dialog, which) -> {
            try {
                firebaseAuth.signOut();
                startActivity(new Intent(getContext(), LoginActivity.class));
            } catch (Exception e) {
                Toast.makeText(getContext(), "Erro." + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(R.string.cancelar, (dialog, which) -> {
            Toast.makeText(getContext(), "Muito bem. Continue se divertindo com os pets do mundo todo!", Toast.LENGTH_SHORT).show();
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_sair_usuarios, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.item_sair_MenuSair) {
            deslogarUsuario();
        }
        return super.onOptionsItemSelected(item);

    }
}
