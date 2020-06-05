package br.com.petgramapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

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
    private List<Conversas> conversasList = new ArrayList<>();
    private AdapterConversasJam adapterConversasJam;
    private RecyclerView recyclerViewConversasJam;
    private FirebaseFirestore firebaseFirestore;
    private String idUsuarioRemetente;
    private CollectionReference reference;
    private Task<QuerySnapshot> eventListener;
    private CollectionReference usuarioCollection;
    private List<Usuario> usuarioList = new ArrayList<>();

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

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setMeasurementCacheEnabled(true);
        recyclerViewConversasJam.setLayoutManager(linearLayoutManager);
        recyclerViewConversasJam.setHasFixedSize(true);

        adapterConversasJam = new AdapterConversasJam(getContext(),conversasList);
        recyclerViewConversasJam.setAdapter(adapterConversasJam);

        reference = firebaseFirestore.collection("Talks")
                                .document("Conversas")
        .collection(idUsuarioRemetente);

        recyclerViewConversasJam.addOnItemTouchListener(new RecyclerItemClickListener(
                getContext(), recyclerViewConversasJam, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Conversas conversas = conversasList.get(position);
                Intent i = new Intent(getActivity(), TalksJamActivity.class);
                Usuario usuarioLogado = usuarioList.get(0);
                i.putExtra("chatContato",conversas.getUsuario());
                i.putExtra("chatUsuarioLogado",usuarioLogado);
                startActivity(i);
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

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void readConversas(){

      conversasList.clear();
      eventListener = reference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
          @Override
          public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

              reference.orderBy("timeStamp", Query.Direction.DESCENDING)
                      .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                  @Override
                  public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                      for (DocumentSnapshot ds: queryDocumentSnapshots){
                          Conversas conversas = ds.toObject(Conversas.class);

                          if (conversas.getUsuario().getId().equals(UsuarioFirebase.getIdentificadorUsuario()))
                              continue;

                          conversasList.add(conversas);
                      }

                      adapterConversasJam.notifyDataSetChanged();
                  }
              });

          }

      });



    }

    public void recuperarContatoAtual(){
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
