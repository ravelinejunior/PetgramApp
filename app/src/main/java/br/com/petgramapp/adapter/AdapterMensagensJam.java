package br.com.petgramapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.helper.UsuarioFirebase;
import br.com.petgramapp.model.MensagemJam;

public class AdapterMensagensJam extends RecyclerView.Adapter<AdapterMensagensJam.MyViewHolder> {

    private static final int TIPO_REMETENTE = 0;
    private static final int TIPO_DESTINATARIO = 1;
    private Context context;
    private List<MensagemJam> mensagemJamList;


    public AdapterMensagensJam(Context context, List<MensagemJam> mensagemJamList) {
        this.context = context;
        this.mensagemJamList = mensagemJamList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = null;

      if (viewType == TIPO_REMETENTE){
           view = LayoutInflater.from(parent.getContext()).
                   inflate(R.layout.adapter_mensagemjam_remetente,parent,false);
      }else if (viewType == TIPO_DESTINATARIO){
           view = LayoutInflater.from(parent.getContext()).
                   inflate(R.layout.adapter_mensagemjam_destinatario,parent,false);
      }

      return new MyViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        MensagemJam mensagemJam = mensagemJamList.get(position);

        if (mensagemJam.getImagemEnviada() != null){
            Uri uriImagem = Uri.parse(mensagemJam.getImagemEnviada());
            Glide.with(context).load(uriImagem).into(holder.imagemTalks);
            holder.dataEnvioRecebido.setText(mensagemJam.getDataEnvio());
            holder.mensagemTalks.setText(mensagemJam.getMensagem());
        }else{
           // Uri uriImagem = Uri.parse(mensagemJam.getImagemEnviada());
            Glide.with(context).load(R.drawable.ic_pets_white_24dp).into(holder.imagemTalks);
            holder.dataEnvioRecebido.setText(mensagemJam.getDataEnvio());
            holder.mensagemTalks.setText(mensagemJam.getMensagem());
            holder.imagemTalks.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdapterMensagensJam adapterMensagensJam = new AdapterMensagensJam(context,mensagemJamList);
                adapterMensagensJam.notifyDataSetChanged();

            }
        });


      /*  if (mensagemJam.getImagemEnviada() != null){

            Uri uriImagem = Uri.parse(mensagemJam.getImagemEnviada());
            Glide.with(context).load(uriImagem).priority(Priority.HIGH).dontAnimate().into(holder.imagemTalks);
            holder.dataEnvioRecebido.setText(mensagemJam.getDataEnvio());
            holder.mensagemTalks.setText(mensagemJam.getMensagem());

        }else if(mensagemJam.getMensagem().equalsIgnoreCase("imagem.jpeg")){
            Uri uriImagem = Uri.parse(mensagemJam.getImagemEnviada());
            Glide.with(context).load(uriImagem).priority(Priority.HIGH).dontAnimate().into(holder.imagemTalks);
            holder.dataEnvioRecebido.setText(mensagemJam.getDataEnvio());
        }else{

            holder.imagemTalks.setVisibility(View.GONE);
            holder.mensagemTalks.setText(mensagemJam.getMensagem());
            holder.dataEnvioRecebido.setText(mensagemJam.getDataEnvio());
        }*/

    }

    @Override
    public int getItemViewType(int position) {
    MensagemJam mensagemJam = mensagemJamList.get(position);
    String idUsuarioLogado = UsuarioFirebase.getIdentificadorUsuario();
    if (idUsuarioLogado.equals(mensagemJam.getId())){
        return TIPO_REMETENTE;
    }

    return TIPO_DESTINATARIO;
    }

    @Override
    public int getItemCount() {
        return mensagemJamList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        public TextView mensagemTalks;
        public TextView dataEnvioRecebido;
        public ImageView imagemTalks;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            mensagemTalks = itemView.findViewById(R.id.mensagemDigitada_AdapterMensagem);
            dataEnvioRecebido = itemView.findViewById(R.id.horaMensagem_id_Mensagem);
            imagemTalks = itemView.findViewById(R.id.imagem_MensagemTalks);

        }
    }

}
