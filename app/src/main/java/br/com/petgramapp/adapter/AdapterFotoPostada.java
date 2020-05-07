package br.com.petgramapp.adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.activities.ComentariosActivity;
import br.com.petgramapp.activities.SeguidoresActivity;
import br.com.petgramapp.activities.StartActivity;
import br.com.petgramapp.fragments.PerfilFragment;
import br.com.petgramapp.fragments.PostagemUsuarioFragment;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.helper.UsuarioFirebase;
import br.com.petgramapp.model.FotoPostada;
import br.com.petgramapp.model.Notificacao;
import br.com.petgramapp.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterFotoPostada extends RecyclerView.Adapter<AdapterFotoPostada.ViewHolder> {
    public List<FotoPostada> fotoPostadaList;
    public static Integer posicao = 0;
    public Context context;
    private FirebaseUser firebaseUser;
    public List<Notificacao> notificacaoList = new ArrayList<>();
    public List<String> idPostagensList = new ArrayList<>();

    public AdapterFotoPostada(List<FotoPostada> fotoPostadaList, Context c) {
        this.fotoPostadaList = fotoPostadaList;
        this.context = c;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_homefragment_post,parent,false);
        return new AdapterFotoPostada.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    FotoPostada fotoPostada = fotoPostadaList.get(position);
    posicao = position;
    firebaseUser = UsuarioFirebase.getUsuarioAtual();


    //USUARIOS
    Uri fotoUri = Uri.parse(fotoPostada.getImagemPostada());

        Picasso.get().load(fotoUri).into(holder.imagemPostadaHome);
        //Glide.with(context).load(fotoUri).into(holder.imagemPostadaHome);

    if (fotoPostada.getDescricaoImagemPostada().equalsIgnoreCase("") || fotoPostada.getDescricaoImagemPostada() == null) {
        holder.descricaoHome.setVisibility(View.GONE);
    } else {
        holder.descricaoHome.setVisibility(View.VISIBLE);
        holder.descricaoHome.setText(fotoPostada.getDescricaoImagemPostada());
    }

    informacoesPublicacao(holder.fotoPerfilHome, holder.nomeUsuarioHome, holder.postadaPorHome, fotoPostada.getIdUsuarioPostou());

    //CURTIDAS
    estaCurtido(fotoPostada.getIdPostagem(),holder.likeButtonHome);
    quantidadeLikes(holder.qtLikesHome,fotoPostada.getIdPostagem());

        //LIKES
        holder.likeButtonHome.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {

                ConfiguracaoFirebase.getReferenciaDatabase().
                        child("Likes").
                        child(fotoPostada.getIdPostagem()).
                        child(firebaseUser.getUid()).setValue(true);

                //NOTIFICACAO
                addNovaNotificacao(fotoPostada.getIdUsuarioPostou(),fotoPostada.getIdPostagem());
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                ConfiguracaoFirebase.getReferenciaDatabase().
                        child("Likes").
                        child(fotoPostada.getIdPostagem()).
                        child(firebaseUser.getUid()).removeValue();
            }
        });

        //SALVAR POSTS
        fotoSalvar(fotoPostada.getIdPostagem(),holder.salvarButtonHome);

        holder.salvarButtonHome.setOnClickListener(v -> {

            if(holder.salvarButtonHome.getTag().equals("Salvar")){

                ConfiguracaoFirebase.getReferenciaDatabase().child("SalvarFotos")
                        .child(firebaseUser.getUid())
                        .child(fotoPostada.getIdPostagem())
                        .setValue(true);
                Toast.makeText(context, "Fofo né? Agora está salvo com você.", Toast.LENGTH_LONG).show();

                addNovaNotificacaoSalvar(fotoPostada.getIdUsuarioPostou(),fotoPostada.getIdPostagem());



            }else{
                ConfiguracaoFirebase.getReferenciaDatabase().child("SalvarFotos")
                        .child(firebaseUser.getUid())
                        .child(fotoPostada.getIdPostagem())
                        .removeValue();

                Toast.makeText(context, "Você pode me favoritar depois se quiser. Sentirei sua falta.", Toast.LENGTH_SHORT).show();

            }
        });

        //SEGUIDORES
        holder.qtLikesHome.setOnClickListener(v -> {

                    Intent intent = new Intent(context, SeguidoresActivity.class);
                    intent.putExtra("idPostagem",fotoPostada.getIdPostagem());
                    intent.putExtra("titulo","Curtir");
                    context.startActivity(intent);

        });

        holder.imagemLikeHome.setOnClickListener(v -> {

            Intent intent = new Intent(context, SeguidoresActivity.class);
            intent.putExtra("idPostagem",fotoPostada.getIdPostagem());
            intent.putExtra("titulo","Curtir");
            context.startActivity(intent);

        });


        //COMENTARIOS
        holder.comentarioButtonHome.setOnClickListener(v -> {
            Intent intent = new Intent(context, ComentariosActivity.class);
            intent.putExtra("idPostagem",fotoPostada.getIdPostagem());
            intent.putExtra("usuarioPostouId",fotoPostada.getIdUsuarioPostou());
            context.startActivity(intent);
        });

        holder.comentariosTodosHome.setOnClickListener(v -> {
            Intent intent = new Intent(context, ComentariosActivity.class);
            intent.putExtra("idPostagem",fotoPostada.getIdPostagem());
            intent.putExtra("usuarioPostouId",fotoPostada.getIdUsuarioPostou());
            context.startActivity(intent);
        });

        getQuantidadeComentarios(fotoPostada.getIdPostagem(),holder.comentariosTodosHome);

        //POSTAGEM DETALHES
        holder.imagemPostadaHome.setOnClickListener(v -> {
            SharedPreferences.Editor editor = context.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
            editor.putString("idPostagem",fotoPostada.getIdPostagem());
            editor.apply();

           ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).
                    replace(R.id.fragment_container_principal_StartAct,new PostagemUsuarioFragment()).commit();


        });

        holder.nomeUsuarioHome.setOnClickListener(v -> {
            SharedPreferences.Editor editor = context.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
            editor.putString("idUsuario",fotoPostada.getIdUsuarioPostou());
            editor.apply();

            ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).
                    replace(R.id.fragment_container_principal_StartAct,new PerfilFragment()).commit();
        });

        holder.fotoPerfilHome.setOnClickListener(v -> {
            SharedPreferences.Editor editor = context.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
            editor.putString("idUsuario",fotoPostada.getIdUsuarioPostou());
            editor.apply();

            ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE).
                    replace(R.id.fragment_container_principal_StartAct,new PerfilFragment()).commit();

        });

        //DELETAR POSTAGEM

        if (!firebaseUser.getUid().equalsIgnoreCase(fotoPostada.getIdUsuarioPostou())){
            holder.deletarPostagemHome.setVisibility(View.GONE);
        }else{
            holder.deletarPostagemHome.setVisibility(View.VISIBLE);
        }

        holder.deletarPostagemHome.setOnClickListener(v -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Deletar PetPostagem");
            builder.setMessage("Deseja deletar essa linda PetPostagem?\n\n\n *OBS: Apagando a postagem, você tambem exclui TODAS suas notificações.");
            builder.setIcon(R.drawable.ic_pets_black_24dp);
            builder.setPositiveButton(R.string.confirmar, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deletarPostagem(firebaseUser.getUid(),fotoPostada.getIdPostagem(),notificacaoList.get(position).getIdPostagem());
                    dialog.dismiss();
                    Intent intent = new Intent(context, StartActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(intent);
                }
            });
            builder.setNegativeButton(R.string.cancelar, (dialog, which) -> {
                    Snackbar.make(v,"Ufa, uma postagem fofa dessas deveria ser suuuper vista, não acha?",Snackbar.LENGTH_LONG).
                    show();
                    dialog.dismiss();
            });

            Dialog dialog = builder.create();
            dialog.show();

        });

    }

    @Override
    public int getItemCount() {
        return fotoPostadaList.size();
    }

    private void informacoesPublicacao(CircleImageView imagemPerfil,TextView nomeUsuario, TextView publicadoPor, String userId){
        DatabaseReference databaseReference = ConfiguracaoFirebase.getReferenciaDatabase().child("usuarios").child(userId);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);

                if (usuario.getUriCaminhoFotoPetUsuario() != null){
                    Uri uriFotoPerfil = Uri.parse(usuario.getUriCaminhoFotoPetUsuario());
                   // Picasso.get().load(uriFotoPerfil).placeholder(R.drawable.ic_pessoa_usuario).into(imagemPerfil);
                    Glide.with(context).load(uriFotoPerfil).into(imagemPerfil);
                }else{
                    //Picasso.get().load(R.drawable.ic_pessoa_usuario).into(imagemPerfil);
                    Glide.with(context).load(R.drawable.ic_pessoa_usuario).into(imagemPerfil);
                }

                nomeUsuario.setText(usuario.getNomePetUsuario());
                publicadoPor.setText(String.format("@%s", usuario.getNomePetUsuario().replace(" ", "")));
                publicadoPor.setTextColor(context.getResources().getColor(R.color.escuroAzul));


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getQuantidadeComentarios(String idPostagem,TextView comentariosView){
        DatabaseReference referenceComentario = ConfiguracaoFirebase.getReferenciaDatabase()
                .child("Comentarios")
                .child(idPostagem);

        referenceComentario.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                comentariosView.setText(MessageFormat.format("Visualizar todos os {0} Pet Comentários", dataSnapshot.getChildrenCount()));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void quantidadeLikes(TextView quantidadeLikesText, String idPostagem){
        DatabaseReference reference = ConfiguracaoFirebase.getReferenciaDatabase();
        DatabaseReference likesRef = reference.child("Likes").child(idPostagem);

        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                quantidadeLikesText.setText(dataSnapshot.getChildrenCount()+" likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void fotoSalvar(String idPostagem, ImageView imagemSalva){
        firebaseUser = UsuarioFirebase.getUsuarioAtual();
        DatabaseReference reference = ConfiguracaoFirebase.getReferenciaDatabase();
        DatabaseReference salvarReference = reference.child("SalvarFotos").child(firebaseUser.getUid());

        salvarReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(idPostagem).exists()){
                    imagemSalva.setImageResource(R.drawable.ic_foto_salva_completo);
                    imagemSalva.setTag("Salva");

                }else{

                    imagemSalva.setImageResource(R.drawable.ic_foto_salva_oco);
                    imagemSalva.setTag("Salvar");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void estaCurtido(String idPostagem, LikeButton likeButtonImage){
        FirebaseUser user = UsuarioFirebase.getUsuarioAtual();
        DatabaseReference reference = ConfiguracaoFirebase.getReferenciaDatabase();
        DatabaseReference likesRef = reference.child("Likes").child(idPostagem);

        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(user.getUid()).exists()){
                       // likeButtonImage.setImageResource(R.drawable.ic_likebutton_colorido);
                        likeButtonImage.setTag("curtido");
                        likeButtonImage.setLiked(true);
                }else{
                   // likeButtonImage.setImageResource(R.drawable.ic_like_branco);
                    likeButtonImage.setTag("curtir");
                    likeButtonImage.setLiked(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void deletarPostagem(String idUsuario,String idPostagem, String idNotificacao){
           DatabaseReference postsRef = ConfiguracaoFirebase.getReferenciaDatabase()
                   .child("Posts").child(idPostagem);

           postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                   if (idUsuario.equals(firebaseUser.getUid())){
                       postsRef.removeValue().addOnCompleteListener(task -> {
                           if (task.isSuccessful()){
                               //deletaNotificacao(idUsuario,idPostagem);
                               alteraNotificacao(idUsuario,idNotificacao);
                               Toast.makeText(context, "Deletado com sucesso.", Toast.LENGTH_SHORT).show();
                           }
                       });
                   }

               }

               @Override
               public void onCancelled(@NonNull DatabaseError databaseError) {

               }
           });
    }

    private void addNovaNotificacao(String idUsuario,String idPostagem){
        DatabaseReference reference = ConfiguracaoFirebase.getReferenciaDatabase();
        DatabaseReference notificacaoReference =  reference.child("Notificacao").
                child(idUsuario);
        // child(idPostagem).child(idUsuario);
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("idUsuario",firebaseUser.getUid());
        hashMap.put("comentarioFeito","Gostou da sua postagem");
        hashMap.put("idPostagem",idPostagem);
        hashMap.put("isPostado",true);

        notificacaoReference.push().setValue(hashMap);

    }

    private void alteraNotificacao(String idUsuario,String idPostagem){
        DatabaseReference reference = ConfiguracaoFirebase.getReferenciaDatabase();
        DatabaseReference notificacaoReference =  reference.child("Notificacao").
                child(idUsuario).child(idPostagem);
        //    child(idPostagem).child(idUsuario);
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("idUsuario",firebaseUser.getUid());
        hashMap.put("idPostagem",null);
        hashMap.put("isPostado",true);

        notificacaoReference.updateChildren(hashMap);

    }

    private void deletaNotificacao(String idUsuario,String idPostagem){
        DatabaseReference reference = ConfiguracaoFirebase.getReferenciaDatabase();
        DatabaseReference notificacaoReference =  reference.child("Notificacao").child(idUsuario).child(idPostagem);
        notificacaoReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                notificacaoReference.removeValue().addOnCompleteListener(task -> {

                    if (task.isSuccessful()){
                        Log.i("notificacaoReference","Notificação excluida. Id Postagem = "+idPostagem);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addNovaNotificacaoSalvar(String idUsuario,String idPostagem){
        DatabaseReference reference = ConfiguracaoFirebase.getReferenciaDatabase();
        DatabaseReference notificacaoReference =  reference.child("Notificacao").
                child(idUsuario);
            //    child(idPostagem).child(idUsuario);
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("idUsuario",firebaseUser.getUid());
        hashMap.put("comentarioFeito","Salvou sua postagem!");
        hashMap.put("idPostagem",idPostagem);
        hashMap.put("isPostado",true);

        notificacaoReference.push().setValue(hashMap);

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView descricaoHome;
        public TextView nomeUsuarioHome;
        public TextView qtLikesHome;
        public TextView comentariosTodosHome;
        public TextView postadaPorHome;

        public ImageView comentarioButtonHome;
        public ImageView imagemLikeHome;
        public LikeButton likeButtonHome;
        public ImageView salvarButtonHome;

        public ImageView imagemPostadaHome;
        public CircleImageView fotoPerfilHome;
        public CircleImageView deletarPostagemHome;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            descricaoHome = itemView.findViewById(R.id.descricao_HomeAdapter_id);
            nomeUsuarioHome = itemView.findViewById(R.id.nome_perfilUsuario_HomeAdapter_id);
            qtLikesHome = itemView.findViewById(R.id.quantidadeLikes_HomeAdapter_id);
            comentariosTodosHome = itemView.findViewById(R.id.verTodosComentarios_HomeAdapter_id);
            postadaPorHome = itemView.findViewById(R.id.publicadaPor_HomeAdapter_id);

            comentarioButtonHome = itemView.findViewById(R.id.mensagemButton_HomeAdapter_id);
            likeButtonHome = itemView.findViewById(R.id.likeButton_HomeAdapter_id);
            salvarButtonHome = itemView.findViewById(R.id.salvarButton_HomeAdapter_id);
            imagemLikeHome = itemView.findViewById(R.id.likeImagem_visualizarLikes);

            imagemPostadaHome = itemView.findViewById(R.id.imagemPostada_HomeAdapter_id);
            fotoPerfilHome = itemView.findViewById(R.id.imagem_perfilUsuario_HomeAdapter_id);
            deletarPostagemHome = itemView.findViewById(R.id.deletarPostagem_HomeAdapter);
        }
    }



}
