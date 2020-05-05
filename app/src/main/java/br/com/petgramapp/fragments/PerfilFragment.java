package br.com.petgramapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.activities.OpcoesActivity;
import br.com.petgramapp.activities.PerfilActivity;
import br.com.petgramapp.activities.SeguidoresActivity;
import br.com.petgramapp.adapter.AdapterMinhasFotos;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.helper.UsuarioFirebase;
import br.com.petgramapp.model.FotoPostada;
import br.com.petgramapp.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class PerfilFragment extends Fragment {

    //WIDGETS
    private Button botaoEditarPerfilFrament;
    private ImageButton imageButtonMinhasFotos;
    private ImageButton imageButtonSalvarFotos;
    private ImageButton imageButtonMenuFotos;
    private TextView nomePetUsuarioPerfilFragment;
    private TextView descricaoPetUsuarioPerfilFragment;
    private TextView nomeDonoPetUsuarioPerfilFragment;
    private TextView quantidadeCurtidasPetUsuarioPerfilFragment;
    private TextView quantidadeSeguidoresPerfilFragment;
    private TextView quantidadeSeguindoPerfilFragment;
    private TextView textoQuantidadeSeguindoPerfilFragment;
    private TextView textoQuantidadeSeguidoresPerfilFragment;
    private CircleImageView imagemFotoPerfilUsuarioFragment;
    private CircleImageView opcoesImagemView;

    //DADOS
    private String idPerfilUsuario;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;

    //VIEWS/ADAPTER
    private RecyclerView recyclerViewMinhasFotos;
    private AdapterMinhasFotos adapterMinhasFotos;
    private List<FotoPostada> fotoPostadaList = new ArrayList<>();

    private RecyclerView recyclerViewMenu;
    private AdapterMinhasFotos adapterMenuFotos;

    //fotos salvas
    private List<FotoPostada> fotosSalvasList = new ArrayList<>();
    private RecyclerView recyclerViewFotosSalvas;
    private AdapterMinhasFotos adapterMinhasFotos_fotosSalvas;
    private List<String> fotosSalvasStrings;



    public PerfilFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil,null);
        carregarElementos(view);

        firebaseUser = UsuarioFirebase.getUsuarioAtual();
        SharedPreferences preferences = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        idPerfilUsuario = preferences.getString("idUsuario","default");
        reference = ConfiguracaoFirebase.getReferenciaDatabase();

        botaoEditarPerfilFrament.setOnClickListener(v -> {

            String botaoEdit = botaoEditarPerfilFrament.getText().toString();

            if (botaoEdit.equalsIgnoreCase("Editar PetPerfil")){
                //ir para editar perfil
                startActivity(new Intent(getContext(),PerfilActivity.class));
            }else if(botaoEdit.equalsIgnoreCase("Seguir")){

                    seguirUsuario();

                    //NOTIFICAÇÃO
                addNovaNotificacao();

            }else if(botaoEdit.equalsIgnoreCase("Seguindo")){
                    unfollowUsuario();
            }
        });

        //SEGUINDO
        quantidadeSeguindoPerfilFragment.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SeguidoresActivity.class);
            intent.putExtra("idUsuario",idPerfilUsuario);
            intent.putExtra("titulo","Seguindo");
            startActivity(intent);
        });

        textoQuantidadeSeguindoPerfilFragment.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SeguidoresActivity.class);
            intent.putExtra("idUsuario",idPerfilUsuario);
            intent.putExtra("titulo","Seguindo");
            startActivity(intent);
        });

        //SEGUIDORES

        quantidadeSeguidoresPerfilFragment.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SeguidoresActivity.class);
            intent.putExtra("idUsuario",idPerfilUsuario);
            intent.putExtra("titulo","Seguidores");
            startActivity(intent);
        });

        textoQuantidadeSeguidoresPerfilFragment.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SeguidoresActivity.class);
            intent.putExtra("idUsuario",idPerfilUsuario);
            intent.putExtra("titulo","Seguidores");
            startActivity(intent);
        });

        //MENU OPÇÕES
        opcoesImagemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), OpcoesActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });




        usuarioInfo();
        getSeguidores();
        getSeguindo();
        getLikes();
        carregarMinhasFotos();
        minhasFotosSalvas();

        if (idPerfilUsuario.equals(firebaseUser.getUid())){
            botaoEditarPerfilFrament.setText(R.string.editar_petperfil);
        }else{
            verificarSegueUsuario();
            imageButtonSalvarFotos.setVisibility(View.GONE);
        }


        //RECYCLER VIEW DE MINHAS FOTOS
        recyclerViewMinhasFotos.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(getContext(),3);
        recyclerViewMinhasFotos.setLayoutManager(linearLayoutManager);
        adapterMinhasFotos = new AdapterMinhasFotos(getContext(),fotoPostadaList);
        recyclerViewMinhasFotos.setAdapter(adapterMinhasFotos);

        //RECYCLER VIEW DE FOTOS SALVAS

        recyclerViewFotosSalvas.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManagerFotosSalvas = new GridLayoutManager(getContext(),3);
        recyclerViewFotosSalvas.setLayoutManager(linearLayoutManagerFotosSalvas);
        adapterMinhasFotos_fotosSalvas = new AdapterMinhasFotos(getContext(),fotosSalvasList);
        recyclerViewFotosSalvas.setAdapter(adapterMinhasFotos_fotosSalvas);


        //RECYCLER VIEW DE MINHAS FOTOS MENU

        recyclerViewMenu.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManagerMenu = new LinearLayoutManager(getContext());
        recyclerViewMenu.setLayoutManager(linearLayoutManagerMenu);
        adapterMenuFotos = new AdapterMinhasFotos(getContext(),fotoPostadaList);
        recyclerViewMenu.setAdapter(adapterMenuFotos);



//PADRAO RECYCLER
        recyclerViewMinhasFotos.setVisibility(View.VISIBLE);
        recyclerViewFotosSalvas.setVisibility(View.GONE);
        recyclerViewMenu.setVisibility(View.GONE);


        imageButtonMinhasFotos.setOnClickListener(v -> {
            recyclerViewMinhasFotos.setVisibility(View.VISIBLE);
            recyclerViewFotosSalvas.setVisibility(View.GONE);
            recyclerViewMenu.setVisibility(View.GONE);
        });

        imageButtonSalvarFotos.setOnClickListener(v -> {

            recyclerViewMinhasFotos.setVisibility(View.GONE);
            recyclerViewMenu.setVisibility(View.GONE);
            recyclerViewFotosSalvas.setVisibility(View.VISIBLE);

        });

        imageButtonMenuFotos.setOnClickListener(v -> {
            recyclerViewMinhasFotos.setVisibility(View.GONE);
            recyclerViewMenu.setVisibility(View.VISIBLE);
            recyclerViewFotosSalvas.setVisibility(View.GONE);

        });


        return view;


    }

    private void carregarMinhasFotos(){
        DatabaseReference fotosRef = reference.child("Posts");
        fotosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fotoPostadaList.clear();

                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    FotoPostada fotoPostada = ds.getValue(FotoPostada.class);
                    if (fotoPostada.getIdUsuarioPostou().equals(idPerfilUsuario)){
                        fotoPostadaList.add(fotoPostada);
                    }
                }

                Collections.reverse(fotoPostadaList);
                adapterMinhasFotos.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void seguirUsuario(){
        //CASO USUARIO AINDA NÃO SEGUIU, USUARIO LOGADO SEGUE USUARIO CLICADO
        reference.child("Seguir").
                child(firebaseUser.getUid()).
                child("seguindo").
                child(idPerfilUsuario).
                setValue(true);

        //CASO USUARIO CLIQUE EM SEGUIR, ADICIONA USUARIO LOGADO AO NÓ CITANDO QUEM ELE ESTÁ SEGUINDO
        reference.child("Seguir").
                child(idPerfilUsuario).
                child("seguidores").
                child(firebaseUser.getUid()).
                setValue(true);
    }

    private void unfollowUsuario(){
        //CASO USUARIO SIGA E QUEIRA DEIXAR DE SEGUIR
        reference.child("Seguir").
                child(firebaseUser.getUid()).
                child("seguindo").
                child(idPerfilUsuario).
                removeValue();

        reference.child("Seguir").
                child(idPerfilUsuario).
                child("seguidores").
                child(firebaseUser.getUid()).
                removeValue();
    }

    private void carregarElementos(View view){
        imageButtonMinhasFotos = view.findViewById(R.id.minhasFotos_PerfilFragment_id);
        imageButtonSalvarFotos = view.findViewById(R.id.salvarMinhasFotos_PerfilFragment_id);
        botaoEditarPerfilFrament = view.findViewById(R.id.botao_editarPetPerfil_PerfilFragment_id);
        nomePetUsuarioPerfilFragment = view.findViewById(R.id.nomeUsuario_perfilFragment_id);
        nomeDonoPetUsuarioPerfilFragment = view.findViewById(R.id.nomeDono_petUsuario_PerfilFragment);
        descricaoPetUsuarioPerfilFragment = view.findViewById(R.id.descricao_petUsuario_PerfilFragment);
        quantidadeCurtidasPetUsuarioPerfilFragment = view.findViewById(R.id.quantidade_PostagensPet_PerfilFragment_id);
        quantidadeSeguidoresPerfilFragment = view.findViewById(R.id.quantidade_SeguidoresPet_PerfilFragment_id);
        quantidadeSeguindoPerfilFragment = view.findViewById(R.id.quantidade_SeguindoPet_PerfilFragment_id);
        imagemFotoPerfilUsuarioFragment = view.findViewById(R.id.imagem_PerfilUsuario_PerfilFragment_id);
        textoQuantidadeSeguidoresPerfilFragment = view.findViewById(R.id.texto_quantidadeSeguidores_PerfilFragment);
        textoQuantidadeSeguindoPerfilFragment = view.findViewById(R.id.texto_quantidadeSeguindo_PerfilFragment);
        opcoesImagemView = view.findViewById(R.id.opcoes_PerfilFramgent_id);
        imageButtonMenuFotos = view.findViewById(R.id.menuMinhasFotos_PerfilFragment_id);
        recyclerViewMinhasFotos = view.findViewById(R.id.recyclerView_minhasFotos_PerfilFragment);
        recyclerViewFotosSalvas = view.findViewById(R.id.recyclerView_fotosSalvas_PerfilFragment);
        recyclerViewMenu = view.findViewById(R.id.recyclerView_menuFotosSalvas_PerfilFragment);
    }

    private void usuarioInfo(){
        DatabaseReference usuariosRef = reference.child("usuarios").child(idPerfilUsuario);

        usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (getContext() == null){
                    return;
                }

                Usuario usuario = dataSnapshot.getValue(Usuario.class);
                Picasso.get().load(usuario.getUriCaminhoFotoPetUsuario()).into(imagemFotoPerfilUsuarioFragment);
                nomePetUsuarioPerfilFragment.setText(usuario.getNomePetUsuario());
                nomeDonoPetUsuarioPerfilFragment.setText(usuario.getNomePetUsuario());
                descricaoPetUsuarioPerfilFragment.setText(usuario.getDescricaoPetUsuario());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void verificarSegueUsuario(){
        DatabaseReference seguidorRef = reference.child("Seguir").
                child(firebaseUser.getUid()).
                child("seguindo");
        seguidorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(idPerfilUsuario).exists()){
                    botaoEditarPerfilFrament.setText(R.string.seguindo);
                }else{
                    botaoEditarPerfilFrament.setText(R.string.seguir);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getSeguidores(){

        DatabaseReference seguidorRef = reference.child("Seguir").
                child(idPerfilUsuario).
                child("seguidores");

        seguidorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                quantidadeSeguidoresPerfilFragment.setText(""+dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getSeguindo(){

        DatabaseReference seguidorRef = reference.child("Seguir").
                child(idPerfilUsuario).
                child("seguindo");

        seguidorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                quantidadeSeguindoPerfilFragment.setText(""+dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getLikes(){
        DatabaseReference likesRef = reference.child("Posts");

        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    FotoPostada fotoPostada = ds.getValue(FotoPostada.class);
                    if (fotoPostada.getIdUsuarioPostou().equals(idPerfilUsuario)){
                        i++;
                    }
                }

                quantidadeCurtidasPetUsuarioPerfilFragment.setText(""+i);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void minhasFotosSalvas(){
        fotosSalvasStrings = new ArrayList<>();
        DatabaseReference salvarReferencia = reference.child("SalvarFotos").child(firebaseUser.getUid());
        salvarReferencia.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    fotosSalvasStrings.add(ds.getKey());

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        readMinhasFotosSalvas();
    }

    private void readMinhasFotosSalvas(){
        DatabaseReference postsReferencia = reference.child("Posts");
        postsReferencia.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fotosSalvasList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    FotoPostada fotoPostada = ds.getValue(FotoPostada.class);
                    for (String idSalvo:fotosSalvasStrings){
                        if (fotoPostada.getIdPostagem().equals(idSalvo))
                        fotosSalvasList.add(fotoPostada);
                    }

                }
                Collections.reverse(fotosSalvasList);
                adapterMinhasFotos_fotosSalvas.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addNovaNotificacao(){
        DatabaseReference reference = ConfiguracaoFirebase.getReferenciaDatabase();
        DatabaseReference notificacaoReference =  reference.child("Notificacao").child(idPerfilUsuario);
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("idUsuario",firebaseUser.getUid());
        hashMap.put("comentarioFeito","Começou a seguir você!");
        hashMap.put("idPostagem","");
        hashMap.put("isPostado",false);

        notificacaoReference.push().setValue(hashMap);

    }




}
