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

public class AdapterGrupoContatosSelecionadosNormalJam
        extends RecyclerView.Adapter<AdapterGrupoContatosSelecionadosNormalJam.MyViewHolder> {

    private Context context;
    private List<Usuario> contatosSelecionadosList;

    public AdapterGrupoContatosSelecionadosNormalJam(Context context, List<Usuario> contatosSelecionadosList) {
        this.context = context;
        this.contatosSelecionadosList = contatosSelecionadosList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_grupo_contatos_selecionados,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Usuario usuario = contatosSelecionadosList.get(position);
        boolean cabecalho = usuario.getEmailPetUsuario().isEmpty();

        holder.nomeUsuario.setText(usuario.getNomePetUsuario());

        if (usuario.getUriCaminhoFotoPetUsuario() != null) {
            Uri uriFoto = Uri.parse(usuario.getUriCaminhoFotoPetUsuario());
            Glide.with(context).load(uriFoto).priority(Priority.HIGH).frame(1000).into(holder.imagemPerfil);
        }

        if (cabecalho){
            holder.imagemPerfil.setImageResource(R.drawable.ic_grupos_pessoas_azul);
            holder.nomeUsuario.setTextColor(Color.DKGRAY);
        }
    }

    @Override
    public int getItemCount() {
        return contatosSelecionadosList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        public TextView nomeUsuario;
        public CircleImageView imagemPerfil;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            nomeUsuario = itemView.findViewById(R.id.nomeUsuario_AdapterGrupoContatosSelecionado);
            imagemPerfil = itemView.findViewById(R.id.imagemPerfil_AdapterGrupoContatosSelecionado);

        }
    }

}
