package br.com.petgramapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.activities.TalksJamActivity;
import br.com.petgramapp.adapter.AdapterConversasJam;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.helper.RecyclerItemClickListener;
import br.com.petgramapp.helper.UsuarioFirebase;
import br.com.petgramapp.model.Conversas;
import br.com.petgramapp.model.Usuario;

public class ConversasFragmentJam extends Fragment {
    Query query;
    List<Conversas> conversasLista = new ArrayList<>();
    private List<Conversas> conversasList = new ArrayList<>();
    private AdapterConversasJam adapterConversasJam;
    private RecyclerView recyclerViewConversasJam;
    private FirebaseFirestore firebaseFirestore;
    private String idUsuarioRemetente;
    private String idUsuarioDestinatario;
    private CollectionReference reference;
    private ListenerRegistration eventListener;
    private CollectionReference usuarioCollection;
    private List<Usuario> usuarioList = new ArrayList<>();
    private ProgressBar progressBarConversasJam;


    public ConversasFragmentJam() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_conversas_jam, container, false);
        recyclerViewConversasJam = view.findViewById(R.id.recyclerView_ConversasJam);
        firebaseFirestore = ConfiguracaoFirebase.getFirebaseFirestore();
        idUsuarioRemetente = UsuarioFirebase.getIdentificadorUsuario();
        progressBarConversasJam = view.findViewById(R.id.progressBar_FragmentConversasJam);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setMeasurementCacheEnabled(true);
        recyclerViewConversasJam.setLayoutManager(linearLayoutManager);
        recyclerViewConversasJam.setHasFixedSize(true);

        adapterConversasJam = new AdapterConversasJam(getContext(), conversasList);
        recyclerViewConversasJam.setAdapter(adapterConversasJam);

        reference = firebaseFirestore.collection("Talks")
                .document("Conversas")
                .collection(idUsuarioRemetente);


        recyclerViewConversasJam.addOnItemTouchListener(new RecyclerItemClickListener(
                getContext(), recyclerViewConversasJam, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (conversasLista.size() > 0) {
                    Conversas conversas = conversasLista.get(position);

                    //verificar se conversa clicada é de um grupo
                    if (conversas.getIsGroup().equals("true")) {
                        Intent i = new Intent(getActivity(), TalksJamActivity.class);
                        Usuario usuarioLogado = usuarioList.get(0);
                        i.putExtra("chatGrupo", (Parcelable) conversas.getGrupoJam());
                        i.putExtra("chatUsuarioLogado", usuarioLogado);
                        i.putExtra("conversasFull", (Serializable) conversas);
                        startActivity(i);
                        conversasLista.clear();

                    } else {
                        Intent i = new Intent(getActivity(), TalksJamActivity.class);
                        Usuario usuarioLogado = usuarioList.get(0);
                        i.putExtra("chatContato", conversas.getUsuario());
                        i.putExtra("chatUsuarioLogado", usuarioLogado);
                        i.putExtra("conversasFull", (Serializable) conversas);
                        startActivity(i);
                        conversasLista.clear();
                    }


                } else {
                    Conversas conversas = conversasList.get(position);
                    //verificar se conversa clicada é de um grupo
                    if (conversas.getIsGroup().equals("true")) {
                        Intent i = new Intent(getActivity(), TalksJamActivity.class);
                        Usuario usuarioLogado = usuarioList.get(0);
                        i.putExtra("chatGrupo", (Parcelable) conversas.getGrupoJam());
                        i.putExtra("chatUsuarioLogado", usuarioLogado);
                        i.putExtra("conversasFull", (Serializable) conversas);
                        startActivity(i);
                    } else {
                        Intent i = new Intent(getActivity(), TalksJamActivity.class);
                        Usuario usuarioLogado = usuarioList.get(0);
                        i.putExtra("chatContato", conversas.getUsuario());
                        i.putExtra("chatUsuarioLogado", usuarioLogado);
                        i.putExtra("conversasFull", (Serializable) conversas);
                        startActivity(i);
                    }

                }

            }

            @Override
            public void onLongItemClick() {

            }

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        }
        ));

        query = reference;


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        readConversas();
        recuperarContatoAtual();
    }

    @Override
    public void onStop() {
        super.onStop();
        eventListener.remove();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void pesquisarConversas(String texto) {
        //  Log.d("queryTextPesquisa",texto.toString());
        conversasLista = new ArrayList<>();
        for (Conversas conversas : conversasList) {

            //verificar se existe um usuario, caso usuario exista, é uma conversa normal
            if (conversas.getUsuario() != null) {
                String nomeUsuario = conversas.getUsuario().getNomePetUsuario().toLowerCase();
                String ultimaMensagem = conversas.getUltimaMensagem().toLowerCase();

                if (nomeUsuario.contains(texto) || ultimaMensagem.contains(texto)) {
                    conversasLista.add(conversas);
                }
            } else {

                String nomeUsuario = conversas.getGrupoJam().getNomeGrupo().toLowerCase();
                String ultimaMensagem = conversas.getUltimaMensagem().toLowerCase();

                if (nomeUsuario.contains(texto) || ultimaMensagem.contains(texto)) {
                    conversasLista.add(conversas);
                }
            }

        }

        adapterConversasJam = new AdapterConversasJam(getContext(), conversasLista);
        recyclerViewConversasJam.setAdapter(adapterConversasJam);
        adapterConversasJam.notifyDataSetChanged();


    }

    public void readConversas() {


        eventListener = reference.orderBy("timeStamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        conversasList.clear();
                        for (DocumentSnapshot ds : queryDocumentSnapshots) {
                            Conversas conversas = ds.toObject(Conversas.class);

                            if (conversas.getIsGroup().equals("false")) {

                                if (conversas.getUsuario() != null) {
                                    if (conversas.getUsuario().getId().equals(UsuarioFirebase.getIdentificadorUsuario()))
                                        continue;
                                }

                            }

                            conversasList.add(conversas);

                        }

                        adapterConversasJam.notifyDataSetChanged();
                        progressBarConversasJam.setVisibility(View.GONE);
                    }
                });



    }

    public void reloadConversas() {
        conversasLista.clear();
        adapterConversasJam = new AdapterConversasJam(getContext(), conversasList);
        recyclerViewConversasJam.setAdapter(adapterConversasJam);
        adapterConversasJam.notifyDataSetChanged();
    }

    public void recuperarContatoAtual() {
        usuarioCollection = firebaseFirestore.collection("Usuarios");
        usuarioCollection.document(UsuarioFirebase.getIdentificadorUsuario())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                usuarioList.clear();

                Usuario usuario = documentSnapshot.toObject(Usuario.class);
                usuarioList.add(usuario);
            }

        });
    }


}
