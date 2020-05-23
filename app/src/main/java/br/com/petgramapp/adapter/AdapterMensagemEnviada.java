package br.com.petgramapp.adapter;

import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.xwray.groupie.Item;
import com.xwray.groupie.ViewHolder;

import br.com.petgramapp.R;
import br.com.petgramapp.helper.UsuarioFirebase;
import br.com.petgramapp.model.Mensagem;
import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterMensagemEnviada extends Item {

    FirebaseUser usuarioAtual = UsuarioFirebase.getUsuarioAtual();
    private Mensagem mensagem;

    public AdapterMensagemEnviada(Mensagem mensagem) {
        this.mensagem = mensagem;
    }

    @Override
    public void bind(@NonNull ViewHolder viewHolder, int position) {
        TextView mensagemEnviada = viewHolder.itemView.findViewById(R.id.mensagemEnviada_Mensagem);
        CircleImageView fotoUsuarioMensagemEnviado = viewHolder.itemView.findViewById(R.id.fotoUsuario_enviado_Chat);

        mensagemEnviada.setText(mensagem.getMensagem());
    }

    @Override
    public int getLayout() {
        return mensagem.getIdEnviadoDe() == usuarioAtual.getUid() ? R.layout.adapter_mensagem_enviada : R.layout.adapter_mensagem_recebida;
    }

}
