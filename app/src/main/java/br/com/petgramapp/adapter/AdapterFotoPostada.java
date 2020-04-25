package br.com.petgramapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.activities.ComentariosActivity;
import br.com.petgramapp.activities.SeguidoresActivity;
import br.com.petgramapp.fragments.PerfilFragment;
import br.com.petgramapp.fragments.PostagemUsuarioFragment;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.helper.UsuarioFirebase;
import br.com.petgramapp.model.FotoPostada;
import br.com.petgramapp.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterFotoPostada extends RecyclerView.Adapter<AdapterFotoPostada.ViewHolder> {
    public List<FotoPostada> fotoPostadaList;
    public Context context;
    private FirebaseUser firebaseUser;

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
    firebaseUser = UsuarioFirebase.getUsuarioAtual();


    //USUARIOS
    Uri fotoUri = Uri.parse(fotoPostada.getImagemPostada());

    Picasso.get().load(fotoUri).placeholder(R.drawable.ic_pets_black_24dp).into(holder.imagemPostadaHome);

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
                addNovaNotificacaoSalvar(fotoPostada.getIdUsuarioPostou(),fotoPostada.getIdPostagem());



            }else{
                ConfiguracaoFirebase.getReferenciaDatabase().child("SalvarFotos")
                        .child(firebaseUser.getUid())
                        .child(fotoPostada.getIdPostagem())
                        .removeValue();
            }
        });

        //SEGUIDORES
        holder.qtLikesHome.setOnClickListener(v -> {

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

            ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().
                    replace(R.id.fragment_container_principal_StartAct,new PostagemUsuarioFragment()).commit();
        });

        holder.nomeUsuarioHome.setOnClickListener(v -> {
            SharedPreferences.Editor editor = context.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
            editor.putString("idUsuario",fotoPostada.getIdUsuarioPostou());
            editor.apply();

            ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().
                    replace(R.id.fragment_container_principal_StartAct,new PerfilFragment()).commit();
        });

        holder.fotoPerfilHome.setOnClickListener(v -> {
            SharedPreferences.Editor editor = context.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
            editor.putString("idUsuario",fotoPostada.getIdUsuarioPostou());
            editor.apply();

            ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().
                    replace(R.id.fragment_container_principal_StartAct,new PerfilFragment()).commit();
        });

    }

    @Override
    public int getItemCount() {
        return fotoPostadaList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView descricaoHome;
        public TextView nomeUsuarioHome;
        public TextView qtLikesHome;
        public TextView comentariosTodosHome;
        public TextView postadaPorHome;

        public ImageView comentarioButtonHome;
        public LikeButton likeButtonHome;
        public ImageView salvarButtonHome;

        public ImageView imagemPostadaHome;
        public CircleImageView fotoPerfilHome;

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

            imagemPostadaHome = itemView.findViewById(R.id.imagemPostada_HomeAdapter_id);
            fotoPerfilHome = itemView.findViewById(R.id.imagem_perfilUsuario_HomeAdapter_id);
        }
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
                    imagemSalva.setImageResource(R.drawable.ic_pets_white_24dp);
                    imagemSalva.setTag("Salva");
                }else{
                    imagemSalva.setImageResource(R.drawable.ic_home_pet);
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

    private void informacoesPublicacao(CircleImageView imagemPerfil,TextView nomeUsuario, TextView publicadoPor, String userId){
        DatabaseReference databaseReference = ConfiguracaoFirebase.getReferenciaDatabase().child("usuarios").child(userId);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);

                if (usuario.getUriCaminhoFotoPetUsuario() != null){
                    Uri uriFotoPerfil = Uri.parse(usuario.getUriCaminhoFotoPetUsuario());
                    Picasso.get().load(uriFotoPerfil).placeholder(R.drawable.ic_pessoa_usuario).into(imagemPerfil);
                }else{
                    Picasso.get().load(R.drawable.ic_pessoa_usuario).into(imagemPerfil);
                }

                nomeUsuario.setText(usuario.getNomePetUsuario());
                publicadoPor.setText(usuario.getNomePetUsuario());


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addNovaNotificacao(String idUsuario,String idPostagem){
        DatabaseReference reference = ConfiguracaoFirebase.getReferenciaDatabase();
        DatabaseReference notificacaoReference =  reference.child("Notificacao").child(idUsuario);
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("idUsuario",firebaseUser.getUid());
        hashMap.put("comentarioFeito","Gostou da sua postagem");
        hashMap.put("idPostagem",idPostagem);
        hashMap.put("isPostado",true);

        notificacaoReference.push().setValue(hashMap);

    }

    private void addNovaNotificacaoSalvar(String idUsuario,String idPostagem){
        DatabaseReference reference = ConfiguracaoFirebase.getReferenciaDatabase();
        DatabaseReference notificacaoReference =  reference.child("Notificacao").child(idUsuario);
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("idUsuario",firebaseUser.getUid());
        hashMap.put("comentarioFeito","Salvou sua postagem!");
        hashMap.put("idPostagem",idPostagem);
        hashMap.put("isPostado",true);

        notificacaoReference.push().setValue(hashMap);

    }



}
