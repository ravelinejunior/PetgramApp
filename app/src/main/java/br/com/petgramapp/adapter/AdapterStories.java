package br.com.petgramapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.activities.AddStoriesActivity;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.helper.UsuarioFirebase;
import br.com.petgramapp.model.Stories;
import br.com.petgramapp.model.Usuario;

public class AdapterStories extends RecyclerView.Adapter<AdapterStories.MyViewHolder> {
    public Context contexto;
    public List<Stories> listStories;
    DatabaseReference reference = ConfiguracaoFirebase.getReferenciaDatabase();

    public AdapterStories(Context contexto, List<Stories> listStories) {
        this.contexto = contexto;
        this.listStories = listStories;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0){
            View view = LayoutInflater.from(contexto).inflate(R.layout.adapter_add_stories,parent,false);
            return new MyViewHolder(view);
        }else{
            View view = LayoutInflater.from(contexto).inflate(R.layout.adapter_story,parent,false);
            return new MyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Stories stories = listStories.get(position);
        usuarioInfo(holder,stories.getIdUsuario(),position);

        if (holder.getAdapterPosition() != 0){
            visualizarStories(holder,stories.getIdUsuario());
        }

        if (holder.getAdapterPosition() == 0){
            myStories(holder.storyTextoStories,holder.imagemPlusStories,false);
        }

        holder.itemView.setOnClickListener(v -> myStories(holder.storyTextoStories,holder.imagemPlusStories,true));
    }

    @Override
    public int getItemCount() {
        return listStories.size();
    }



    public static class MyViewHolder extends RecyclerView.ViewHolder{

        public ImageView imagemPerfilStories;
        public ImageView imagemVisualizadaStories;
        public ImageView imagemPlusStories;
        public TextView nomeUsuarioStories;
        public TextView storyTextoStories;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imagemPerfilStories = itemView.findViewById(R.id.foto_StoriesAdapter);
            imagemVisualizadaStories = itemView.findViewById(R.id.fotoVista_StoriesAdapter);
            imagemPlusStories = itemView.findViewById(R.id.fotoStory_icPlus_AddStoriesAdapter);
            nomeUsuarioStories = itemView.findViewById(R.id.nomeUsuario_StoriesAdapter);
            storyTextoStories = itemView.findViewById(R.id.addFotoStories_AdapterAddStories);

        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0){
            return 0;
        }
        return 1;
    }

    private void usuarioInfo(MyViewHolder viewHolder,String idUsuario, int posicao){
        DatabaseReference usuarioRef = reference.child("usuarios").child(idUsuario);
        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               Usuario usuario = dataSnapshot.getValue(Usuario.class);
               Picasso.get().load(usuario.getUriCaminhoFotoPetUsuario()).into(viewHolder.imagemPerfilStories);

                if(posicao != 0){
                    Picasso.get().load(usuario.getUriCaminhoFotoPetUsuario()).into(viewHolder.imagemVisualizadaStories);
                    viewHolder.nomeUsuarioStories.setText(usuario.getNomePetUsuario());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void myStories(TextView storyTextoStory, ImageView circleImage, Boolean clicado ){
        DatabaseReference storiesRef = reference.child("Stories").child(UsuarioFirebase.getUsuarioAtual().getUid());
        storiesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = 0;
                long timeCurrent = System.currentTimeMillis();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Stories stories = ds.getValue(Stories.class);

                    if (timeCurrent > stories.getDataInicio() && timeCurrent < stories.getDataFim()){
                        count ++;
                    }
                }
                if (clicado){

                    if (count>0){
                      AlertDialog dialog = new AlertDialog.Builder(contexto).create();
                      dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Visualizar Story", (dialog12, which) -> {

                      });

                      dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Adicionar Story", (dialog1, which) -> {
                          Intent intent = new Intent(contexto, AddStoriesActivity.class);
                          contexto.startActivity(intent);
                          dialog.dismiss();
                      });
                      dialog.show();


                    }else{
                        Intent intent = new Intent(contexto, AddStoriesActivity.class);
                        contexto.startActivity(intent);
                    }

                }else{
                    if (count > 0){
                        storyTextoStory.setText("PetStory");
                        circleImage.setVisibility(View.GONE);
                    }else {
                        storyTextoStory.setText("Add Stories");
                        circleImage.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void visualizarStories(MyViewHolder viewHolder, String idUsuario){
        DatabaseReference storiesRef = ConfiguracaoFirebase.getReferenciaDatabase().child("Stories").child(idUsuario);
        storiesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i  = 0;
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    if (!ds.child("views").child(UsuarioFirebase.getUsuarioAtual().getUid())
                            .exists() && System.currentTimeMillis() < ds.getValue(Stories.class).getDataFim())
                    {
                        i++;
                    }
                }

                if (i > 0){
                    viewHolder.imagemPerfilStories.setVisibility(View.VISIBLE);
                    viewHolder.imagemVisualizadaStories.setVisibility(View.GONE);
                }else{
                    viewHolder.imagemPerfilStories.setVisibility(View.GONE);
                    viewHolder.imagemVisualizadaStories.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}



















