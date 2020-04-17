package br.com.petgramapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.activities.StartActivity;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.helper.UsuarioFirebase;
import br.com.petgramapp.model.Comentarios;
import br.com.petgramapp.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterComentarios extends RecyclerView.Adapter<AdapterComentarios.ViewHolder> {
    private List<Comentarios> comentariosList;
    private Context context;
    FirebaseUser firebaseUser;
    DatabaseReference reference;

    public AdapterComentarios(List<Comentarios> comentariosList, Context context) {
        this.comentariosList = comentariosList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_comentarios,parent,false);
        return new AdapterComentarios.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        reference = ConfiguracaoFirebase.getReferenciaDatabase();
        firebaseUser = UsuarioFirebase.getUsuarioAtual();
        Comentarios comentarios = comentariosList.get(position);

        holder.comentarioFeitoComentario.setText(comentarios.getComentario());

        informacoesUsuario(holder.imagemPerfilUsuarioComentario,holder.nomeUsuarioComentario,comentarios.getIdQuemPublicou());

        holder.comentarioFeitoComentario.setOnClickListener(v -> {
            Intent intent = new Intent(context, StartActivity.class);
            intent.putExtra("idAutorComentario",comentarios.getIdQuemPublicou());
            context.startActivity(intent);
        });

        holder.imagemPerfilUsuarioComentario.setOnClickListener(v -> {
            Intent intent = new Intent(context, StartActivity.class);
            intent.putExtra("idAutorComentario",comentarios.getIdQuemPublicou());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return comentariosList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public CircleImageView imagemPerfilUsuarioComentario;
        public TextView nomeUsuarioComentario;
        public TextView comentarioFeitoComentario;
        public View dividerView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imagemPerfilUsuarioComentario = itemView.findViewById(R.id.imagem_fotoUsuarioComentou_AdapterComentario);
            nomeUsuarioComentario = itemView.findViewById(R.id.nomeUsuarioComentou_AdapterComentario);
            comentarioFeitoComentario = itemView.findViewById(R.id.texto_comentarioFeito_AdapterComentario);
            dividerView = itemView.findViewById(R.id.divider_AdapterComentario);

        }
    }

    public void informacoesUsuario(CircleImageView circleView, TextView nomeUsuario, String autorComentarioId){
        reference = ConfiguracaoFirebase.getReferenciaDatabase();
        DatabaseReference usuariosRef = reference.child("usuarios").child(autorComentarioId);

        usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);
                nomeUsuario.setText(usuario.getNomePetUsuario());
                Picasso.get().load(usuario.getUriCaminhoFotoPetUsuario()).into(circleView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
