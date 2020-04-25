package br.com.petgramapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.adapter.AdapterFotoPostada;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.helper.UsuarioFirebase;
import br.com.petgramapp.model.FotoPostada;

public class HomeFragment extends Fragment {

    private AdapterFotoPostada adapterFotoPostada;
    private List<FotoPostada> fotoPostadaList;
    private ProgressBar progressBarHomeFragment;
    private List<String> listaIdUsuarios = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        progressBarHomeFragment = view.findViewById(R.id.progressBar_HomeFragment);

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
        return view;
    }

    private void receberPostagens(){
        DatabaseReference databaseReference = ConfiguracaoFirebase.getReferenciaDatabase().child("Posts");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                fotoPostadaList.clear();

                for (DataSnapshot ds:dataSnapshot.getChildren()) {

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

    public void checarUsuariosSeguidores(){

        DatabaseReference usuariosRef = ConfiguracaoFirebase.getReferenciaDatabase().child("Seguir")
                .child(UsuarioFirebase.getIdentificadorUsuario())
                .child("seguindo");

        usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override

            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaIdUsuarios.clear();
                for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){
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

    public void checarUsuarios(){
        listaIdUsuarios = new ArrayList<>();
        DatabaseReference usuariosRef = ConfiguracaoFirebase.getReferenciaDatabase().child("usuarios");
        usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaIdUsuarios.clear();

                for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){
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
}
