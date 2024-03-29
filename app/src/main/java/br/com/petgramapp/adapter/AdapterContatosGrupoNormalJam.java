package br.com.petgramapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;

import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterContatosGrupoNormalJam extends RecyclerView.Adapter<AdapterContatosGrupoNormalJam.MyViewHolder> {

    private Context context;
    private List<Usuario> listUsuario;

    public AdapterContatosGrupoNormalJam(Context context, List<Usuario> listUsuario) {
        this.context = context;
        this.listUsuario = listUsuario;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_contatos_users,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Usuario usuario = listUsuario.get(position);
        boolean cabecalho = usuario.getEmailPetUsuario().isEmpty();

            holder.nomeUsuarioContato.setText(usuario.getNomePetUsuario());
            holder.emailUsuarioContato.setText(usuario.getEmailPetUsuario());

            if (usuario.getUriCaminhoFotoPetUsuario() != null) {
                Uri uriFoto = Uri.parse(usuario.getUriCaminhoFotoPetUsuario());
                Glide.with(context).load(uriFoto).priority(Priority.HIGH).frame(1000).into(holder.imagemPerfil);
            }

        if (cabecalho){
            holder.imagemPerfil.setImageResource(R.drawable.ic_grupos_pessoas_azul);
            holder.nomeUsuarioContato.setTextColor(Color.DKGRAY);
            holder.emailUsuarioContato.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return listUsuario.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        public TextView nomeUsuarioContato;
        public TextView emailUsuarioContato;
        public CircleImageView imagemPerfil;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            nomeUsuarioContato = itemView.findViewById(R.id.nome_perfil_ContatoAdapter);
            emailUsuarioContato = itemView.findViewById(R.id.emailContatos_ContatoAdapter);
            imagemPerfil = itemView.findViewById(R.id.imagem_perfil_ContatoAdapter);
        }
    }

}
