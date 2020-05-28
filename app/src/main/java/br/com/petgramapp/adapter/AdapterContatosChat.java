package br.com.petgramapp.adapter;

import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.squareup.picasso.Picasso;
import com.xwray.groupie.Item;
import com.xwray.groupie.ViewHolder;

import br.com.petgramapp.R;
import br.com.petgramapp.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterContatosChat extends Item<ViewHolder> {
    public Usuario usuario;
    Context context;
    AdapterContatosChat adapterContatosChat;

    public AdapterContatosChat(Usuario usuario,Context context) {
        this.context = context;
        this.usuario = usuario;
    }


    public AdapterContatosChat( Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public void bind(@NonNull ViewHolder viewHolder, int position) {
       TextView nomeUsuario =  viewHolder.itemView.findViewById(R.id.nome_perfil_ContatoAdapter);
       CircleImageView imagemPerfil = viewHolder.itemView.findViewById(R.id.imagem_perfil_ContatoAdapter);

        Picasso.get().load(usuario.getUriCaminhoFotoPetUsuario()).priority(Picasso.Priority.HIGH).into(imagemPerfil);
        nomeUsuario.setText(usuario.getNomePetUsuario());


    }


    @Override
    public int getLayout() {
        return R.layout.adapter_contatos_users;
    }
}
