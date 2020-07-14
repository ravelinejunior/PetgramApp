package br.com.petgramapp.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.adapter.AdapterPesquisarUsuario;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.helper.UsuarioFirebase;
import br.com.petgramapp.model.Usuario;

public class PesquisarFragment extends Fragment {

    RecyclerView recyclerViewPesquisarFragment;
    private AdapterPesquisarUsuario adapterPesquisarUsuario;
    private List<Usuario> listaUsuario;
    private TextInputEditText textoBuscaPesquisarFragment;
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference usuarioCollection;
    private ListenerRegistration eventListener;
    private ProgressBar progressBarContatosJam;
    private List<Usuario> contatosLista = new ArrayList<>();


    public PesquisarFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pesquisar, container, false);
        recyclerViewPesquisarFragment = view.findViewById(R.id.recyclerView_pesquisarFragment);
        recyclerViewPesquisarFragment.setHasFixedSize(true);
        recyclerViewPesquisarFragment.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewPesquisarFragment.setItemViewCacheSize(20);
        recyclerViewPesquisarFragment.setHasTransientState(true);
        recyclerViewPesquisarFragment.setNestedScrollingEnabled(true);
        firebaseFirestore = ConfiguracaoFirebase.getFirebaseFirestore();
        usuarioCollection = firebaseFirestore.collection("Usuarios");

        textoBuscaPesquisarFragment = view.findViewById(R.id.pesquisarUsuario_pesquisarFragment_Edittext);
        listaUsuario = new ArrayList<>();

        adapterPesquisarUsuario = new AdapterPesquisarUsuario(getActivity(), listaUsuario, true);
        recyclerViewPesquisarFragment.setAdapter(adapterPesquisarUsuario);

        readUsuarios();
        //recuperarContatos();
        textoBuscaPesquisarFragment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //recuperarContatos();
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

    private void pesquisarUsuarios(String s) {

        Query queryPesquisa = ConfiguracaoFirebase.getReferenciaDatabase().child("usuarios")
                .orderByChild("nomePetUsuarioUp")
                .startAt(s)
                .endAt(s + "\uf8ff");


        com.google.firebase.firestore.Query refQuery;
        refQuery = usuarioCollection.
                orderBy("nomePetUsuario",
                        com.google.firebase.firestore.Query.Direction.ASCENDING).
                startAt(s).
                endAt(s + "\uf8ff");


        /*eventListener = refQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                List<DocumentChange> documentChanges = queryDocumentSnapshots.getDocumentChanges();

                if (documentChanges != null) {
                    for (DocumentChange doc : documentChanges) {
                        Usuario usuario = doc.getDocument().toObject(Usuario.class);
                        listaUsuario.add(usuario);
                    }
                    adapterPesquisarUsuario.notifyDataSetChanged();
                }

            }
        });

*/
        queryPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                listaUsuario.clear();

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
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

    private void readUsuarios() {
        Query reference = ConfiguracaoFirebase.getReferenciaDatabase().child("usuarios").orderByChild("nomePetUsuario");

        com.google.firebase.firestore.Query refQuery;
        refQuery = usuarioCollection.orderBy("nomePetUsuario", com.google.firebase.firestore.Query.Direction.ASCENDING);

     /*   eventListener = refQuery.addSnapshotListener( new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                List<DocumentChange> documentChanges = queryDocumentSnapshots.getDocumentChanges();
                if (documentChanges != null) {
                    for (DocumentChange doc : documentChanges) {
                        Usuario usuario = doc.getDocument().toObject(Usuario.class);
                        listaUsuario.add(usuario);
                    }
                }

            }
        });*/


        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (textoBuscaPesquisarFragment.getText().toString().equalsIgnoreCase("")) {

                    listaUsuario.clear();

                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
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

    public void recuperarContatos() {
        com.google.firebase.firestore.Query query = usuarioCollection;
        eventListener =  query.orderBy("nomePetUsuario").addSnapshotListener( new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) return;
                listaUsuario.clear();
                for (DocumentSnapshot ds : queryDocumentSnapshots) {
                    Usuario usuario = ds.toObject(Usuario.class);
                    if (usuario.getId().equals(UsuarioFirebase.getIdentificadorUsuario()))
                        continue;
                    listaUsuario.add(usuario);
                }
                adapterPesquisarUsuario.notifyDataSetChanged();

            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        //eventListener.remove();

    }


}
