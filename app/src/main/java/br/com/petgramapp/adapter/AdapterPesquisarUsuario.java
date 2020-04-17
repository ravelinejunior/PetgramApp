package br.com.petgramapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.fragments.PerfilFragment;
import br.com.petgramapp.fragments.PesquisarFragment;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.helper.UsuarioFirebase;
import br.com.petgramapp.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterPesquisarUsuario extends RecyclerView.Adapter<AdapterPesquisarUsuario.ViewHolder> {

    public Context c;
    private List<Usuario> usuarioList;

    FirebaseUser firebaseUser;
    public static Integer posicaoSelecionada;
    DatabaseReference database = ConfiguracaoFirebase.getReferenciaDatabase();

    public AdapterPesquisarUsuario(Context c, List<Usuario> usuarioList) {
        this.c = c;
        this.usuarioList = usuarioList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(c).inflate(R.layout.adapter_usuario_pesquisar,parent,false);
        return new AdapterPesquisarUsuario.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        firebaseUser = UsuarioFirebase.getUsuarioAtual();
        Usuario usuario = usuarioList.get(position);
        posicaoSelecionada = position;

        holder.botaoSeguirUsuarioPet.setVisibility(View.VISIBLE);
        holder.nomeUsuarioPet.setText(usuario.getNomePetUsuario());
        holder.sexoUsuarioPet.setText(usuario.getSexoPetUsuario());

        Uri uriImagem = Uri.parse(usuario.getUriCaminhoFotoPetUsuario());
        Picasso.get().load(uriImagem).placeholder(R.drawable.ic_person_alterar).into(holder.imagemPerfilUsuarioPet);

        segueUsuario(usuario.getId(),holder.botaoSeguirUsuarioPet);

        if (usuario.getId().equalsIgnoreCase(firebaseUser.getUid())){
            holder.botaoSeguirUsuarioPet.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            SharedPreferences.Editor editor = c.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
            editor.putString("idUsuario",usuario.getId());
            editor.apply();

            ((FragmentActivity)c).getSupportFragmentManager().
                    beginTransaction().
                    replace(R.id.fragment_container_principal_StartAct,new PerfilFragment()).
                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).
                    commit();

        });

        //VERIFICAR SE BOTAO ESTÁ SETADO COMO SEGUIDO PARA "DES SEGUIR"
        holder.botaoSeguirUsuarioPet.setOnClickListener(v -> {
            if (holder.botaoSeguirUsuarioPet.getText().toString().equalsIgnoreCase("seguir")){

                //CASO USUARIO AINDA NÃO SEGUIU, USUARIO LOGADO SEGUE USUARIO CLICADO
                database.child("Seguir").
                            child(firebaseUser.getUid()).
                             child("seguindo").
                                child(usuario.getId()).
                                    setValue(true);

                //CASO USUARIO CLIQUE EM SEGUIR, ADICIONA USUARIO LOGADO AO NÓ CITANDO QUEM ELE ESTÁ SEGUINDO
                database.child("Seguir").
                             child(usuario.getId()).
                                 child("seguidores").
                                     child(firebaseUser.getUid()).
                                        setValue(true);


            }else{

                //CASO USUARIO SIGA E QUEIRA DEIXAR DE SEGUIR
                database.child("Seguir").
                            child(firebaseUser.getUid()).
                                child("seguindo").
                                     child(usuario.getId()).
                                        removeValue();

                database.child("Seguir").
                             child(usuario.getId()).
                                 child("seguidores").
                                    child(firebaseUser.getUid()).
                                          removeValue();

            }

        });

    }

    @Override
    public int getItemCount() {
        return usuarioList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView nomeUsuarioPet;
        TextView sexoUsuarioPet;
        CircleImageView imagemPerfilUsuarioPet;
        Button botaoSeguirUsuarioPet;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nomeUsuarioPet = itemView.findViewById(R.id.nomeUsuario_pesquisarUsuarioFragment_id);
            sexoUsuarioPet = itemView.findViewById(R.id.sexoPet_pesquisarUsuarioFragment_id);
            imagemPerfilUsuarioPet = itemView.findViewById(R.id.fotoUsuario_pesquisarUsuarioFragment_id);
            botaoSeguirUsuarioPet = itemView.findViewById(R.id.botaoSeguirPet_pesquisarUsuarioFragment_id);
        }
    }

    public void segueUsuario(String usuarioId,Button botaoSeguir){

        DatabaseReference reference = ConfiguracaoFirebase.getReferenciaDatabase();
        DatabaseReference seguidoresReference = reference.child("Seguir")
                .child(firebaseUser.getUid())
                .child("seguindo");
        seguidoresReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(usuarioId).exists()){
                    botaoSeguir.setText(R.string.seguindo);
                }else{
                    botaoSeguir.setText(R.string.seguir);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
