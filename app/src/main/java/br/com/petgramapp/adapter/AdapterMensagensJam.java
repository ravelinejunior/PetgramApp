package br.com.petgramapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    RecyclerView recyclerView;


    public AdapterMensagensJam(Context context, List<MensagemJam> mensagemJamList, RecyclerView recyclerView) {
        this.context = context;
        this.mensagemJamList = mensagemJamList;
        this.recyclerView = recyclerView;
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

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        MensagemJam mensagemJam = mensagemJamList.get(position);
try {
    holder.setIsRecyclable(false);
    if (mensagemJam.getImagemEnviada() != null
            && mensagemJam.getMensagem().equalsIgnoreCase("imagem.jpeg")) {

        Uri uriImagem = Uri.parse(mensagemJam.getImagemEnviada());
        Glide.with(context).load(uriImagem).into(holder.imagemTalks);
        holder.nomeUsuarioTalks.setText("Enviada por " + mensagemJam.getNomeUsuarioEnviou());
        holder.dataEnvioRecebido.setText(mensagemJam.getDataEnvio());
        //holder.mensagemTalks.setText(mensagemJam.getMensagem());
        holder.mensagemTalks.setVisibility(View.GONE);

    } else if (mensagemJam.getImagemEnviada().equals("")) {
        //Uri uriImagem = Uri.parse(mensagemJam.getImagemEnviada());
        //Glide.with(context).load(R.drawable.ic_pets_white_24dp).into(holder.imagemTalks);
        holder.nomeUsuarioTalks.setText("Enviada por " + mensagemJam.getNomeUsuarioEnviou());
        holder.dataEnvioRecebido.setText(mensagemJam.getDataEnvio());
        holder.mensagemTalks.setText(mensagemJam.getMensagem());
        holder.imagemTalks.setVisibility(View.GONE);
    } else {
        holder.nomeUsuarioTalks.setText("Enviada por " + mensagemJam.getNomeUsuarioEnviou());
        holder.dataEnvioRecebido.setText(mensagemJam.getDataEnvio());
        holder.mensagemTalks.setText(mensagemJam.getMensagem());
        holder.imagemTalks.setVisibility(View.GONE);
    }

    Log.d("LogsAdapter","mensagem - "+mensagemJam.getMensagem());
    Log.d("LogsAdapter","foto - "+mensagemJam.getImagemEnviada());
    Log.d("LogsAdapter","usuario - "+mensagemJam.getNomeUsuarioEnviou());
}catch (Exception e){
    e.printStackTrace();
    Toast.makeText(context, "Erro ao carregar mensagens.", Toast.LENGTH_SHORT).show();
}
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
        public TextView nomeUsuarioTalks;
        public TextView dataEnvioRecebido;
        public ImageView imagemTalks;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            nomeUsuarioTalks = itemView.findViewById(R.id.nome_id_usuario_AdapterMensagem);
            mensagemTalks = itemView.findViewById(R.id.mensagemDigitada_AdapterMensagem);
            dataEnvioRecebido = itemView.findViewById(R.id.horaMensagem_id_Mensagem);
            imagemTalks = itemView.findViewById(R.id.imagem_MensagemTalks);

        }
    }

}
