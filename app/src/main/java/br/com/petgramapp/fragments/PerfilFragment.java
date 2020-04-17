package br.com.petgramapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import br.com.petgramapp.R;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.helper.UsuarioFirebase;
import br.com.petgramapp.model.FotoPostada;
import br.com.petgramapp.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class PerfilFragment extends Fragment {

    private Button botaoEditarPerfilFrament;
    private ImageButton imageButtonMinhasFotos;
    private ImageButton imageButtonSalvarFotos;
    private TextView nomePetUsuarioPerfilFragment;
    private TextView descricaoPetUsuarioPerfilFragment;
    private TextView nomeDonoPetUsuarioPerfilFragment;
    private TextView quantidadeCurtidasPetUsuarioPerfilFragment;
    private TextView quantidadeSeguidoresPerfilFragment;
    private TextView quantidadeSeguindoPerfilFragment;
    private CircleImageView imagemFotoPerfilUsuarioFragment;

    String idPerfilUsuario;
    FirebaseUser firebaseUser;
    DatabaseReference reference;


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
            }else if(botaoEdit.equalsIgnoreCase("Seguir")){
                    seguirUsuario();
            }else if(botaoEdit.equalsIgnoreCase("Seguindo")){
                    unfollowUsuario();
            }
        });

        usuarioInfo();
        getSeguidores();
        getSeguindo();
        getLikes();

        if (idPerfilUsuario.equals(firebaseUser.getUid())){
            botaoEditarPerfilFrament.setText(R.string.editar_petperfil);
        }else{
            verificarSegueUsuario();
            imageButtonSalvarFotos.setVisibility(View.GONE);
        }


        return view;
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
}
