package br.com.petgramapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.adapter.AdapterFotoPostada;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.model.FotoPostada;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostagemUsuarioFragment extends Fragment {
    public AdapterFotoPostada adapterFotoPostada;
    private List<FotoPostada> fotoPostadaList;
    private RecyclerView recyclerViewFotoPostagemFragment;
    private String idPostagem;

    //FIREBASE
    private DatabaseReference databaseReference;

    public PostagemUsuarioFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_postagem_usuario, container, false);

        databaseReference = ConfiguracaoFirebase.getReferenciaDatabase();

        SharedPreferences preferences = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        idPostagem = preferences.getString("idPostagem","default");

        recyclerViewFotoPostagemFragment = view.findViewById(R.id.recyclerView_fotoPostagemFragment);
        recyclerViewFotoPostagemFragment.setHasFixedSize(true);
        //inverter ordem das postagem (a mais atual)
        LinearLayoutManager linearLayout = new LinearLayoutManager(getContext());
        linearLayout.setReverseLayout(true);
        linearLayout.setStackFromEnd(true);
        recyclerViewFotoPostagemFragment.setLayoutManager(linearLayout);

        fotoPostadaList = new ArrayList<>();
        adapterFotoPostada = new AdapterFotoPostada(fotoPostadaList, getContext());

        recyclerViewFotoPostagemFragment.setAdapter(adapterFotoPostada);
        receberPostagens();
        return view;
    }

    private void receberPostagens() {
        DatabaseReference postReference = databaseReference.child("Posts").child(idPostagem);
        postReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
              fotoPostadaList.clear();
              FotoPostada fotoPostada = dataSnapshot.getValue(FotoPostada.class);
              fotoPostadaList.add(fotoPostada);
              adapterFotoPostada.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
