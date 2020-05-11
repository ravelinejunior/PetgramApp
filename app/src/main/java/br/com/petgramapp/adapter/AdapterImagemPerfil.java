package br.com.petgramapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.model.Usuario;

public class AdapterImagemPerfil extends RecyclerView.Adapter<AdapterImagemPerfil.MyViewHolder> {
    public Context context;
    public List<Usuario> usuarioList;

    public AdapterImagemPerfil(Context context, List<Usuario> fotoPostadaList) {
        this.context = context;
        this.usuarioList = fotoPostadaList;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_imagem_perfil,parent,false);
        return new AdapterImagemPerfil.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Usuario usuario = usuarioList.get(position);

        userInfo(usuario.getId(),holder.fotoPerfil,holder.nomePerfil);

       /* holder.fecharPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences.Editor editor = context.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
                editor.putString("idUsuario",usuario.getId());
                editor.apply();


                ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).
                        replace(R.id.fragment_container_principal_StartAct,new PerfilFragment()).commit();



            }
        });
*/
        holder.fotoPerfil.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Snackbar.make(v,usuario.getNomePetUsuario(),Snackbar.LENGTH_LONG).show();
                return true;
            }
        });

        holder.nomePerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v,"Dê um olá para "+usuario.getNomePetUsuario(),Snackbar.LENGTH_LONG).show();
            }
        });

        holder.fotoPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v,"Dê um olá para "+usuario.getNomePetUsuario(),Snackbar.LENGTH_LONG).show();
            }
        });


    }



    @Override
    public int getItemCount() {
        return usuarioList.size();
    }

    public void userInfo(String idUsuario,ImageView fotoPerfil,TextView nomePerfil){
        DatabaseReference usuarioRef = ConfiguracaoFirebase.getReferenciaDatabase()
                .child("usuarios").child(idUsuario);
        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);
               // Picasso.get().load(usuario.getUriCaminhoFotoPetUsuario()).into(fotoPerfil);
                 Glide.with(context).load(usuario.getUriCaminhoFotoPetUsuario()).into(fotoPerfil);

                nomePerfil.setText(usuario.getNomePetUsuario());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        public ImageView fotoPerfil;
        public TextView nomePerfil;
        public ImageView fecharPerfil;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            fotoPerfil = itemView.findViewById(R.id.fotoPerfil_AdapterImagemPerfil);
            nomePerfil = itemView.findViewById(R.id.nomeUsuario_AdapterImagemPerfil);
            fecharPerfil = itemView.findViewById(R.id.fechar_adapterImagemPerfil);

        }
    }


}
