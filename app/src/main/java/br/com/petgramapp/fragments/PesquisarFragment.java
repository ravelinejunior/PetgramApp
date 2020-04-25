package br.com.petgramapp.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.adapter.AdapterPesquisarUsuario;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.model.Usuario;

public class PesquisarFragment extends Fragment {

    RecyclerView recyclerViewPesquisarFragment;
    private AdapterPesquisarUsuario adapterPesquisarUsuario;
    private List<Usuario> listaUsuario;
    private TextInputEditText textoBuscaPesquisarFragment;


    public PesquisarFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_pesquisar, container, false);
        recyclerViewPesquisarFragment = view.findViewById(R.id.recyclerView_pesquisarFragment);
        recyclerViewPesquisarFragment.setHasFixedSize(true);
        recyclerViewPesquisarFragment.setLayoutManager(new LinearLayoutManager(getContext()));

        textoBuscaPesquisarFragment = view.findViewById(R.id.pesquisarUsuario_pesquisarFragment_Edittext);
        listaUsuario = new ArrayList<>();

        adapterPesquisarUsuario = new AdapterPesquisarUsuario(getActivity(),listaUsuario,true);
        recyclerViewPesquisarFragment.setAdapter(adapterPesquisarUsuario);

        readUsuarios();
        textoBuscaPesquisarFragment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                readUsuarios();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                pesquisarUsuarios(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return view;
    }

    private void pesquisarUsuarios(String s){

        Query queryPesquisa = ConfiguracaoFirebase.getReferenciaDatabase().child("usuarios")
                .orderByChild("nomePetUsuarioUp")
                .startAt(s)
                .endAt(s+"\uf8ff");

        queryPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                listaUsuario.clear();

                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Usuario usuario = ds.getValue(Usuario.class);
                    listaUsuario.add(usuario);
                }
                adapterPesquisarUsuario.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void readUsuarios(){
        Query reference = ConfiguracaoFirebase.getReferenciaDatabase().child("usuarios").orderByChild("nomePetUsuarioUp");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (textoBuscaPesquisarFragment.getText().toString().equalsIgnoreCase("")){

                    listaUsuario.clear();

                    for (DataSnapshot ds: dataSnapshot.getChildren()){
                        Usuario usuario = ds.getValue(Usuario.class);
                        listaUsuario.add(usuario);
                    }
                    adapterPesquisarUsuario.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
