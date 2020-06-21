package br.com.petgramapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;

import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterGrupoContatoSelecionadoJam extends FirestorePagingAdapter<Usuario,AdapterGrupoContatoSelecionadoJam.MyViewHolder> {

    Context context;
    List<Usuario> contatosSelecionadosList;

    public AdapterGrupoContatoSelecionadoJam(@NonNull FirestorePagingOptions<Usuario> options) {
        super(options);
    }

    public AdapterGrupoContatoSelecionadoJam(@NonNull FirestorePagingOptions<Usuario> options, Context context, List<Usuario> contatosSelecionadosList) {
        super(options);
        this.context = context;
        this.contatosSelecionadosList = contatosSelecionadosList;
    }


    @Override
    protected void onLoadingStateChanged(@NonNull LoadingState state) {
        super.onLoadingStateChanged(state);
        switch (state){

            case FINISHED:
                Log.d("PAGING_LOG","CARREGADO");
                break;

            case LOADED:
                Log.d("PAGING_LOG","Total de itens: "+getItemCount());
                break;

            case LOADING_INITIAL:
                Log.d("PAGING_LOG","Carregando inicial");
                break;

            case LOADING_MORE:
                Log.d("PAGING_LOG","Carregando mais...");
                break;

            case ERROR:
                Log.d("PAGING_LOG","Erro. ");
                break;
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Usuario model) {

        if (contatosSelecionadosList.size() > 0) {
            Usuario usuario = contatosSelecionadosList.get(position);

            boolean cabecalho = usuario.getEmailPetUsuario().isEmpty();

            holder.nomeUsuario.setText(usuario.getNomePetUsuario());
            if (usuario.getUriCaminhoFotoPetUsuario() != null) {
                Uri uriFoto = Uri.parse(usuario.getUriCaminhoFotoPetUsuario());
                Glide.with(context).load(uriFoto).priority(Priority.HIGH).frame(1000).into(holder.imagemPerfil);
            }

            if (cabecalho) {
                holder.imagemPerfil.setImageResource(R.drawable.ic_grupos_pessoas_azul);
                holder.nomeUsuario.setTextColor(Color.DKGRAY);
            }

        } else{
            holder.nomeUsuario.setVisibility(View.GONE);
            holder.imagemPerfil.setVisibility(View.GONE);
        }

        }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_grupo_contatos_selecionados,parent,false);
        return new AdapterGrupoContatoSelecionadoJam.MyViewHolder(view);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView nomeUsuario;
        public CircleImageView imagemPerfil;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            nomeUsuario = itemView.findViewById(R.id.nomeUsuario_AdapterGrupoContatosSelecionado);
            imagemPerfil = itemView.findViewById(R.id.imagemPerfil_AdapterGrupoContatosSelecionado);

        }
    }
}
