package br.com.petgramapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.activities.ChatJamActivity;
import br.com.petgramapp.activities.LoginActivity;
import br.com.petgramapp.adapter.AdapterFirestore;
import br.com.petgramapp.adapter.AdapterStories;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.helper.UsuarioFirebase;
import br.com.petgramapp.model.FotoPostada;
import br.com.petgramapp.model.Stories;
import br.com.petgramapp.testes.FirestoreTestes;

public class FirestoreHomeFragment extends Fragment implements AdapterFirestore.OnListItemClick {

    FirebaseAuth firebaseAuth;
    private ProgressBar progressBarHomeFragment;
    private List<String> listaIdUsuarios = new ArrayList<>();
    private Toolbar toolbar;
    private RecyclerView recyclerViewStories;
    private AdapterStories adapterStories;
    private List<Stories> storiesList = new ArrayList<>();
    private FirebaseFirestore firebaseFirestore;
    private FirestorePagingAdapter adapter;


    private AdapterFirestore adapterFirestore;

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.fragment_home_firestore_adapter, container, false);


        progressBarHomeFragment = view.findViewById(R.id.progressBar_HomeFragment);
        toolbar = view.findViewById(R.id.toolbar_HomeFragment_id);

        firebaseFirestore = ConfiguracaoFirebase.getFirebaseFirestore();

        firebaseAuth = ConfiguracaoFirebase.getFirebaseAutenticacao();

        toolbar.setTitle("Bem vindo "+ UsuarioFirebase.getUsuarioAtual().getDisplayName());
        toolbar.setLogo(R.drawable.ic_pets_white_24dp);
        toolbar.setPadding(15,0,0,0);
        toolbar.setTitleTextColor(ContextCompat.getColor(getContext(),R.color.branco));

        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);

        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        //query
        Query query = firebaseFirestore.collection("Posts").orderBy("dataPostada", Query.Direction.DESCENDING);

        //PAGINATION
        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(1)
                .setPageSize(5)
                .build();


        FirestorePagingOptions<FotoPostada> options = new FirestorePagingOptions.Builder<FotoPostada>()
                .setQuery(query, config, FotoPostada.class)
                .setLifecycleOwner(this)
                .build();

        //ADAPTER
        RecyclerView recyclerViewFirestore = view.findViewById(R.id.recyclerViewTestes_Firestore_Fragment);
        //recyclerViewFirestore.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewFirestore.setLayoutManager(linearLayoutManager);
        adapterFirestore = new AdapterFirestore(options,getContext(), this);
        recyclerViewFirestore.setAdapter(adapterFirestore);


        //STORIES
        recyclerViewStories = view.findViewById(R.id.recyclerView_Stories_HomeFragment_id);
        recyclerViewStories.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManagerStories = new LinearLayoutManager(getContext(),
                RecyclerView.HORIZONTAL, false);
        recyclerViewStories.setLayoutManager(linearLayoutManagerStories);
        adapterStories = new AdapterStories(getContext(), storiesList);
        recyclerViewStories.setAdapter(adapterStories);
        readStories();
        // Inflate the layout for this fragment

        return view;
    }



    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
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
                    progressBarHomeFragment.setVisibility(View.GONE);
                    adapterStories.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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

            Intent intent = new Intent(getContext(), FirestoreTestes.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        }
        return super.onOptionsItemSelected(item);

    }


    @Override
    public void OnItemClick(DocumentSnapshot snapshot, int position) {
        Log.i("onClickAdapterFirestore","Item clicado: "+position);
        Log.i("onClickAdapterFirestore","ID clicado: "+snapshot.getId());
    }


    @Override
    public void onStart() {
        super.onStart();
        adapterFirestore.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapterFirestore.stopListening();
    }
}



