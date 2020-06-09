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
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.activities.ChatJamActivity;
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
    private List<String> listaSeguidores = new ArrayList<>();
    private Toolbar toolbar;
    private RecyclerView recyclerViewStories;
    private AdapterStories adapterStories;
    private List<Stories> storiesList = new ArrayList<>();
    List<DocumentChange> documentChanges;
    FirebaseAuth firebaseAuth;
    LinearLayoutManager linearLayout;
    private FirebaseFirestore firebaseFirestore;
    private DocumentSnapshot lastResultado;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        progressBarHomeFragment = view.findViewById(R.id.progressBar_HomeFragment);
        toolbar = view.findViewById(R.id.toolbar_HomeFragment_id);
        firebaseFirestore = ConfiguracaoFirebase.getFirebaseFirestore();

        firebaseAuth = ConfiguracaoFirebase.getFirebaseAutenticacao();
        toolbar.setTitle("Bem vindo "+UsuarioFirebase.getUsuarioAtual().getDisplayName());
        toolbar.setLogo(R.drawable.ic_pets_white_24dp);
        toolbar.setPadding(15,0,0,0);
        toolbar.setTitleTextColor(ContextCompat.getColor(getContext(),R.color.branco));

        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);

        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        //FEED
        RecyclerView recyclerViewHomeFragment = view.findViewById(R.id.recyclerView_HomeFragment_id);
        recyclerViewHomeFragment.setHasFixedSize(true);
        recyclerViewHomeFragment.setNestedScrollingEnabled(false);

        //inverter ordem das postagem (a mais atual)
        linearLayout = new LinearLayoutManager(getContext());
        linearLayout.setReverseLayout(true);
        linearLayout.setStackFromEnd(true);
        recyclerViewHomeFragment.setLayoutManager(linearLayout);
        recyclerViewHomeFragment.setItemViewCacheSize(20);
        recyclerViewHomeFragment.setDrawingCacheEnabled(true);
        recyclerViewHomeFragment.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        fotoPostadaList = new ArrayList<>();
        adapterFotoPostada = new AdapterFotoPostada(fotoPostadaList, getContext());

 /*       recyclerViewHomeFragment.setAdapter(adapterFotoPostada);*/
        recyclerViewHomeFragment.postDelayed(new Runnable() {
            @Override
            public void run() {
                recyclerViewHomeFragment.setAdapter(adapterFotoPostada);
            }
        },50);


        //receberPostagens();

        checarUsuariosSeguidores();
        /*receberPostagensFireStore();*/

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

    private void receberPostagensFireStore() {

     CollectionReference collectionReference = firebaseFirestore.collection("Posts");

        Query query;
        if (lastResultado == null){
            query = collectionReference.
                    orderBy("dataPostada", Query.Direction.DESCENDING).
                    limit(10);
        }else{
            query = collectionReference.
                    orderBy("dataPostada",Query.Direction.DESCENDING).
                    startAfter(lastResultado).
                    limit(10);
        }

       // firebaseFirestore.collection("Posts").
       // query.orderBy("dataPostada",Query.Direction.DESCENDING).
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        fotoPostadaList.clear();
                        documentChanges = queryDocumentSnapshots.getDocumentChanges();

                        if (documentChanges != null){
                            for (DocumentChange doc: documentChanges) {
                                FotoPostada fotoPostada = doc.getDocument().toObject(FotoPostada.class);
                                fotoPostadaList.add(fotoPostada);
                            }
                        }

                        if (queryDocumentSnapshots.size() > 0){
                            lastResultado = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                        }

                        progressBarHomeFragment.setVisibility(View.GONE);
                    }

              });

            adapterFotoPostada.notifyDataSetChanged();

            }


    private void receberPostagensSeguidores() {


        DatabaseReference databaseReference = ConfiguracaoFirebase.getReferenciaDatabase().child("Posts");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                fotoPostadaList.clear();

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    FotoPostada fotoPostada = ds.getValue(FotoPostada.class);
                    for (String id:listaSeguidores){
                        if (fotoPostada.getIdUsuarioPostou().equals(id)){
                            fotoPostadaList.add(fotoPostada);
                        }
                    }
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
                .child(firebaseAuth.getCurrentUser().getUid())
                .child("seguindo");

        usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override

            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaSeguidores.clear();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    listaSeguidores.add(dataSnapshot1.getKey());
                }
                adapterFotoPostada.notifyDataSetChanged();
                receberPostagensSeguidores();
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

    private void deslogarUsuario() {
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
        }else if (item.getItemId() == R.id.chat_MenuSair){
            Intent intent = new Intent(getContext(), ChatJamActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }else if (item.getItemId() == R.id.atualizar_update_MenuSair){

            ((FragmentActivity)getContext()).getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).
                    replace(R.id.fragment_container_principal_StartAct,new FirestoreHomeFragment()).commit();

           /* Intent intent = new Intent(getContext(), FirestoreTestes.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);*/

        }
        return super.onOptionsItemSelected(item);

    }

}
