package br.com.petgramapp.fragments;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.activities.GrupoContatosJam;
import br.com.petgramapp.activities.TalksJamActivity;
import br.com.petgramapp.adapter.AdapterContatosJam;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.helper.RecyclerItemClickListener;
import br.com.petgramapp.helper.UsuarioFirebase;
import br.com.petgramapp.model.Usuario;

public class ContatosFragmentJam extends Fragment {

    private RecyclerView recyclerViewListaContatos;
    private AdapterContatosJam adapterContatosJam;
    private List<Usuario> usuarioList = new ArrayList<>();
    private List<Usuario> usuarioUnicoList = new ArrayList<>();
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference usuarioCollection;
    private ListenerRegistration eventListener;
    private ProgressBar progressBarContatosJam;
    private  List<Usuario> contatosLista = new ArrayList<>();

    public ContatosFragmentJam() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contatos_jam, container, false);
        firebaseFirestore = ConfiguracaoFirebase.getFirebaseFirestore();
        usuarioCollection = firebaseFirestore.collection("Usuarios");
        progressBarContatosJam = view.findViewById(R.id.progressBar_FragmentContatosJam);

        recyclerViewListaContatos = view.findViewById(R.id.recyclerView_Contatos_Jam);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerViewListaContatos.setLayoutManager(layoutManager);
        recyclerViewListaContatos.setHasFixedSize(true);

        adapterContatosJam = new AdapterContatosJam(getContext(),usuarioList);
        recyclerViewListaContatos.setAdapter(adapterContatosJam);

        //configuração do evento de clique da lista de contatos
        recyclerViewListaContatos.addOnItemTouchListener(new RecyclerItemClickListener(
                getContext(), recyclerViewListaContatos, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
               if (contatosLista.size() > 0){

                   Intent intent = new Intent(getContext(), TalksJamActivity.class);
                   Usuario usuarioSelecionado = contatosLista.get(position);
                   Usuario usuarioLogado = usuarioUnicoList.get(0);
                   boolean cabecalho = usuarioSelecionado.getEmailPetUsuario().isEmpty();

                   if (cabecalho){
                    Intent i = new Intent(getActivity(), GrupoContatosJam.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                   }else{

                       intent.putExtra("chatContato",usuarioSelecionado);
                       intent.putExtra("chatUsuarioLogado",usuarioLogado);
                       startActivity(intent);
                       startActivity(intent);
                       contatosLista.clear();
                   }


               }else{
                   Intent intent = new Intent(getContext(), TalksJamActivity.class);
                   Usuario usuarioSelecionado = usuarioList.get(position);
                   Usuario usuarioLogado = usuarioUnicoList.get(0);
                   boolean cabecalho = usuarioSelecionado.getEmailPetUsuario().isEmpty();

                   if (cabecalho){
                       Intent i = new Intent(getActivity(), GrupoContatosJam.class);
                       i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                       startActivity(i);
                   }else{
                       intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                       intent.putExtra("chatContato",usuarioSelecionado);
                       intent.putExtra("chatUsuarioLogado",usuarioLogado);
                       startActivity(intent);
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


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarContatoAtual();
        recuperarContatos();
    }

    @Override
    public void onStop() {
        super.onStop();
        eventListener.remove();
    }

    public void adicionaHeader(){
        Usuario itemGrupo = new Usuario();
        itemGrupo.setNomePetUsuario("Novo Grupo");
        itemGrupo.setEmailPetUsuario("");
        usuarioList.add(itemGrupo);
    }

    public void getContatosJam(String contato){
        contatosLista = new ArrayList<>();
        for (Usuario usu: usuarioList){
            String nomeUsuario = usu.getNomePetUsuario().toLowerCase();
            String emailUsuario = usu.getEmailPetUsuario().toLowerCase();

            if (emailUsuario.contains(contato) || nomeUsuario.contains(contato)){
                contatosLista.add(usu);
            }
        }

        adapterContatosJam = new AdapterContatosJam(getContext(),contatosLista);
        recyclerViewListaContatos.setAdapter(adapterContatosJam);
        adapterContatosJam.notifyDataSetChanged();



    }

    public void reloadContatosJam(){
        adapterContatosJam = new AdapterContatosJam(getContext(),usuarioList);
        recyclerViewListaContatos.setAdapter(adapterContatosJam);
        adapterContatosJam.notifyDataSetChanged();
    }

    public void recuperarContatos(){
        Query query = usuarioCollection;

        eventListener = query.orderBy("nomePetUsuario").addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) return;

                    usuarioList.clear();
                    adicionaHeader();

                    for (DocumentSnapshot ds: queryDocumentSnapshots){
                        Usuario usuario = ds.toObject(Usuario.class);

                        if (usuario.getId().equals(UsuarioFirebase.getIdentificadorUsuario()))
                            continue;
                        usuarioList.add(usuario);
                    }

                    adapterContatosJam.notifyDataSetChanged();
                    progressBarContatosJam.setVisibility(View.GONE);

            }
        });
    }

    public void recuperarContatoAtual(){

       usuarioCollection.document(UsuarioFirebase.getIdentificadorUsuario())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        usuarioUnicoList.clear();

                            Usuario usuario = documentSnapshot.toObject(Usuario.class);
                            usuarioUnicoList.add(usuario);
                        }

                });
    }

}
