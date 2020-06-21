package br.com.petgramapp.adapter;

import android.content.Context;
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
import br.com.petgramapp.model.Conversas;
import br.com.petgramapp.model.GrupoJam;
import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterConversasJam extends RecyclerView.Adapter<AdapterConversasJam.MyViewHolder> {
    private Context context;
    private List<Conversas> listConversas;

    public AdapterConversasJam(Context context, List<Conversas> listConversas) {
        this.context = context;
        this.listConversas = listConversas;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_conversas_jam, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Conversas conversas = listConversas.get(position);
        holder.ultimaMensagem.setText(conversas.getUltimaMensagem());
        holder.dataEnvio.setText(conversas.getDataEnvio());

        //validar os grupos
        if (conversas.getIsGroup().equals("true")) {
            GrupoJam grupoJam = conversas.getGrupoJam();
            holder.nomeUsuario.setText(grupoJam.getNomeGrupo());

            if (grupoJam.getFotoGrupo() != null) {
                Uri uriImagem = Uri.parse(grupoJam.getFotoGrupo());
                Glide.with(context).load(uriImagem).dontAnimate().priority(Priority.IMMEDIATE).into(holder.imagemPerfil);
            } else {
                Glide.with(context).load(R.drawable.ic_person_black_preto).dontAnimate()
                        .priority(Priority.IMMEDIATE).into(holder.imagemPerfil);
            }

        } else {

            //verificar se usuario é diferente de nulo para exibir mensagem de grupo
            if (conversas.getUsuario() != null) {

                holder.nomeUsuario.setText(conversas.getUsuario().getNomePetUsuario());
                if (conversas.getUsuario() != null) {
                    if (conversas.getUsuario().getUriCaminhoFotoPetUsuario() != null) {
                        Uri uriImagem = Uri.parse(conversas.getUsuario().getUriCaminhoFotoPetUsuario());
                        Glide.with(context).load(uriImagem).dontAnimate().priority(Priority.IMMEDIATE).into(holder.imagemPerfil);
                    }

                } else {
                    Glide.with(context).load(R.drawable.ic_person_black_preto).dontAnimate()
                            .priority(Priority.IMMEDIATE).into(holder.imagemPerfil);
                }
            }else{

            }

        }

    }

    @Override
    public int getItemCount() {
        return listConversas.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView nomeUsuario;
        public TextView dataEnvio;
        public TextView ultimaMensagem;
        public CircleImageView imagemPerfil;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            nomeUsuario = itemView.findViewById(R.id.nomeUsario_AdapterConversasJam);
            dataEnvio = itemView.findViewById(R.id.dataEnvio_AdapterConversasJam);
            ultimaMensagem = itemView.findViewById(R.id.ultimaMensagem_AdapterConversasJam);
            imagemPerfil = itemView.findViewById(R.id.imagem_fotoPerfil_AdapterConversasJam);
        }
    }
}
