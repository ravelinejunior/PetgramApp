package br.com.petgramapp.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.fragments.PerfilFragment;
import br.com.petgramapp.fragments.PostagemUsuarioFragment;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.model.FotoPostada;
import br.com.petgramapp.model.Notificacao;
import br.com.petgramapp.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterNotificacao extends RecyclerView.Adapter<AdapterNotificacao.MyViewHolder> {
    private Context contexto;
    private List<Notificacao> notificacaoList;
    private List<FotoPostada> fotoPostadaList;
    DatabaseReference reference;

    public AdapterNotificacao(Context contexto, List<Notificacao> notificacaoList) {
        this.contexto = contexto;
        this.notificacaoList = notificacaoList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(contexto).inflate(R.layout.adapter_notificacao,parent,false);
        reference = ConfiguracaoFirebase.getReferenciaDatabase();
        return new AdapterNotificacao.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Notificacao notificacao = notificacaoList.get(position);
try {
    if (notificacao.getIdPostagem() != null) {

        holder.comentarioNotificacao.setText(notificacao.getComentarioFeito());

        getInfoUsuario(holder.imagemPerfilNotificacao, holder.nomeUsuarioNotificacao, notificacao.getIdUsuario());

        if (notificacao.getIsPostado()) {
            holder.imagemPostada.setVisibility(View.VISIBLE);
            getPostagem(holder.imagemPostada, notificacao.getIdPostagem());
        } else {
            holder.imagemPostada.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (notificacao.getIsPostado()) {
                SharedPreferences.Editor editor = contexto.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("idPostagem", notificacao.getIdPostagem());
                editor.apply();
                ((FragmentActivity) contexto).getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE).
                        replace(R.id.fragment_container_principal_StartAct,
                                new PostagemUsuarioFragment()).commit();
            } else {
                SharedPreferences.Editor editor = contexto.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("idUsuario", notificacao.getIdUsuario());
                editor.apply();
                ((FragmentActivity) contexto).getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE).
                        replace(R.id.fragment_container_principal_StartAct,
                                new PerfilFragment()).commit();
            }
        });

    } else {
        holder.itemView.setVisibility(View.GONE);
    }
}catch (Exception e){
    Toast.makeText(contexto, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
}
    }

    @Override
    public int getItemCount() {
        return notificacaoList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        public CircleImageView imagemPerfilNotificacao;
        public TextView nomeUsuarioNotificacao;
        public TextView comentarioNotificacao;
        public ImageView imagemPostada;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imagemPerfilNotificacao = itemView.findViewById(R.id.imagemPerfil_notificacaoAdapter);
            nomeUsuarioNotificacao = itemView.findViewById(R.id.nomeUsuario_NotificacaoAdapter);
            imagemPostada = itemView.findViewById(R.id.imagemPostada_notificacaoAdapter);
            comentarioNotificacao = itemView.findViewById(R.id.comentario_NotificacaoAdapter);
        }
    }

    private void getInfoUsuario(CircleImageView imagemPerfil,TextView nomeUsuarioD, String idUsuarioPublicou){
        DatabaseReference usuariosRef = reference.child("usuarios").child(idUsuarioPublicou);
        usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);
            //    Picasso.get().load(usuario.getUriCaminhoFotoPetUsuario()).into(imagemPerfil);
                Glide.with(contexto).load(usuario.getUriCaminhoFotoPetUsuario()).into(imagemPerfil);
                nomeUsuarioD.setText(usuario.getNomePetUsuario());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getPostagem(ImageView imagemPostada,String idPostagemD){
        DatabaseReference postagemRef = reference.child("Posts").child(idPostagemD);
        postagemRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                FotoPostada fotoPostada = dataSnapshot.getValue(FotoPostada.class);
                //Picasso.get().load(fotoPostada.getImagemPostada()).into(imagemPostada);
                if (fotoPostada != null && fotoPostada.getImagemPostada() != null) {
                    Glide.with(contexto).load(fotoPostada.getImagemPostada()).into(imagemPostada);
                }else{
                    Picasso.get().load(R.drawable.ic_add_foto).into(imagemPostada);
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



}
