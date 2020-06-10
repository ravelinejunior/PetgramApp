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

import br.com.petgramapp.R;
import br.com.petgramapp.helper.UsuarioFirebase;
import br.com.petgramapp.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterGrupoJam extends FirestorePagingAdapter<Usuario,AdapterGrupoJam.MyViewHolder> {
    private Context context;

    public AdapterGrupoJam(@NonNull FirestorePagingOptions<Usuario> options) {
        super(options);
    }

    public AdapterGrupoJam(@NonNull FirestorePagingOptions<Usuario> options, Context context) {
        super(options);
        this.context = context;
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
    protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Usuario usuario) {

        String idUsuarioLogado = UsuarioFirebase.getIdentificadorUsuario();

        boolean cabecalho = usuario.getEmailPetUsuario().isEmpty();

        if (idUsuarioLogado.equalsIgnoreCase(usuario.getId())){
            holder.emailUsuario.setVisibility(View.GONE);
            holder.nomeUsuario.setVisibility(View.GONE);
            holder.imagemPerfil.setVisibility(View.GONE);
            holder.divider.setVisibility(View.GONE);
        }else{
            holder.nomeUsuario.setText(usuario.getNomePetUsuario());
            holder.emailUsuario.setText(usuario.getEmailPetUsuario());


            if (usuario.getUriCaminhoFotoPetUsuario() != null) {
                Uri uriFoto = Uri.parse(usuario.getUriCaminhoFotoPetUsuario());
                Glide.with(context).load(uriFoto).priority(Priority.HIGH).frame(1000).into(holder.imagemPerfil);
            }

        }

        if (cabecalho){
            holder.imagemPerfil.setImageResource(R.drawable.ic_grupos_pessoas_azul);
            holder.nomeUsuario.setTextColor(Color.DKGRAY);
            holder.emailUsuario.setVisibility(View.GONE);
        }


    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_contatos_users,parent,false);
        return new AdapterGrupoJam.MyViewHolder(view);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView nomeUsuario;
        public TextView emailUsuario;
        public CircleImageView imagemPerfil;
        public View divider;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            nomeUsuario = itemView.findViewById(R.id.nome_perfil_ContatoAdapter);
            emailUsuario = itemView.findViewById(R.id.emailContatos_ContatoAdapter);
            imagemPerfil = itemView.findViewById(R.id.imagem_perfil_ContatoAdapter);
            divider = itemView.findViewById(R.id.divider);
        }
    }
}
