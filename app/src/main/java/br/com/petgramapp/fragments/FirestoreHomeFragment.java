package br.com.petgramapp.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.shreyaspatil.firebase.recyclerpagination.DatabasePagingOptions;
import com.shreyaspatil.firebase.recyclerpagination.FirebaseRecyclerPagingAdapter;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.activities.ChatJamActivity;
import br.com.petgramapp.activities.ComentariosActivity;
import br.com.petgramapp.activities.LoginActivity;
import br.com.petgramapp.activities.SeguidoresActivity;
import br.com.petgramapp.activities.StartActivity;
import br.com.petgramapp.adapter.AdapterFirestore;
import br.com.petgramapp.adapter.AdapterStories;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.helper.UsuarioFirebase;
import br.com.petgramapp.model.FotoPostada;
import br.com.petgramapp.model.Stories;
import br.com.petgramapp.model.Usuario;
import br.com.petgramapp.testes.FirestoreTestes;
import de.hdodenhof.circleimageview.CircleImageView;

public class FirestoreHomeFragment extends Fragment implements AdapterFirestore.OnListItemClick {

    FirebaseAuth firebaseAuth;
    private ProgressBar progressBarHomeFragment;
    private List<String> listaIdUsuarios = new ArrayList<>();
    private Toolbar toolbar;
    private RecyclerView recyclerViewStories;
    private AdapterStories adapterStories;
    private List<Stories> storiesList = new ArrayList<>();
    private FirebaseFirestore firebaseFirestore;
    private static final int TOTAL_ITENS = 7;
    FirebaseRecyclerPagingAdapter<FotoPostada, ViewHolderFoto> adapter;

    private SwipeRefreshLayout swipeRefreshLayout;
    private int mCurrentPage = 1;
    private RecyclerView recyclerViewFirestore;


    private AdapterFirestore adapterFirestore;
    private DocumentSnapshot lastVisible;
    private boolean isScrolling;
    private boolean isLastItemReached;

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.fragment_home_firestore_adapter, container, false);

        progressBarHomeFragment = view.findViewById(R.id.progressBar_HomeFragment);
        toolbar = view.findViewById(R.id.toolbar_HomeFragment_id);
        recyclerViewFirestore = view.findViewById(R.id.recyclerViewTestes_Firestore_Fragment);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh_FirestoreHome);
        //swipeRefreshLayout.setEnabled(true);

        firebaseFirestore = ConfiguracaoFirebase.getFirebaseFirestore();

        firebaseAuth = ConfiguracaoFirebase.getFirebaseAutenticacao();

        toolbar.setTitle(" Bem vindo");
        toolbar.setLogo(R.drawable.ic_pets_white_24dp);
        toolbar.setPadding(15,0,0,0);
        toolbar.setTitleTextColor(ContextCompat.getColor(getContext(),R.color.branco));


        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);



        //query
        Query query = firebaseFirestore.collection("Posts")
                .orderBy("dataPostada", Query.Direction.DESCENDING);
               // .limitToLast(25);


        com.google.firebase.database.Query query1 = ConfiguracaoFirebase.getReferenciaDatabase().child("Posts");

       //PAGINATION
        PagedList.Config config = new PagedList.Config.Builder()
                .setPageSize(10)
                .setMaxSize(200)
                .build();


        FirestorePagingOptions<FotoPostada> options1 = new FirestorePagingOptions.Builder<FotoPostada>()
                .setLifecycleOwner(this)
                .setQuery(query, config, FotoPostada.class)
                .build();


        DatabasePagingOptions<FotoPostada> options = new DatabasePagingOptions.Builder<FotoPostada>()
                .setLifecycleOwner(this)
                .setQuery(query1, config, new SnapshotParser<FotoPostada>() {
                    @NonNull
                    @Override
                    public FotoPostada parseSnapshot(@NonNull DataSnapshot snapshot) {
                        FotoPostada fotoPostada = snapshot.getValue(FotoPostada.class);
                        swipeRefreshLayout.setRefreshing(false);
                        if (fotoPostada.getIdUsuarioPostou().equals(UsuarioFirebase.getIdentificadorUsuario())){
                            return null;
                        }else{
                            return fotoPostada;
                        }
                    }
                })
                .build();


        adapter = new FirebaseRecyclerPagingAdapter<FotoPostada, ViewHolderFoto>(options) {
            @NonNull
            @Override
            public ViewHolderFoto onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_homefragment_post,parent,false);
                return new ViewHolderFoto(view);
            }

            @Override
            public long getItemId(int position) {
                return super.getItemId(position);
            }


            @Override
            protected void onBindViewHolder(@NonNull ViewHolderFoto holder, int position, @NonNull FotoPostada fotoPostada) {
                FirebaseUser firebaseUser = UsuarioFirebase.getUsuarioAtual();

                RequestOptions reqOpt = RequestOptions
                        .fitCenterTransform()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .disallowHardwareConfig()
                        .override(holder.imagemPostadaHome.getWidth(),holder.imagemPostadaHome.getHeight()); // Overrides size of downloaded image and converts it's bitmaps to your desired image size;

                if (fotoPostada != null) {
                    //USUARIOS
                    Uri fotoUri = Uri.parse(fotoPostada.getImagemPostada());

                    Glide.with(getContext()).load(fotoUri).thumbnail(0.1f).apply(reqOpt).priority(Priority.IMMEDIATE).into(holder.imagemPostadaHome);

                    if (fotoPostada.getDescricaoImagemPostada().equalsIgnoreCase("") || fotoPostada.getDescricaoImagemPostada() == null) {
                        holder.descricaoHome.setVisibility(View.GONE);
                    } else {
                        holder.descricaoHome.setVisibility(View.VISIBLE);
                        holder.descricaoHome.setText(fotoPostada.getDescricaoImagemPostada());
                    }

                    if (fotoPostada.getDataPostada() != null) {
                        if (fotoPostada.getDataPostada().equalsIgnoreCase("") || fotoPostada.getDataPostada() == null) {
                            holder.dataPostada.setVisibility(View.GONE);
                        } else {
                            holder.dataPostada.setVisibility(View.VISIBLE);
                            holder.dataPostada.setText(fotoPostada.getDataPostada());
                        }
                    }


                    informacoesPublicacao(holder.fotoPerfilHome, holder.nomeUsuarioHome, holder.postadaPorHome, fotoPostada.getIdUsuarioPostou());


                    //CURTIDAS
                    estaCurtido(fotoPostada.getIdPostagem(), holder.likeButtonHome);
                    quantidadeLikes(holder.qtLikesHome, fotoPostada.getIdPostagem());

                    //LIKES
                    holder.likeButtonHome.setOnLikeListener(new OnLikeListener() {
                        @Override
                        public void liked(LikeButton likeButton) {

                            ConfiguracaoFirebase.getReferenciaDatabase().
                                    child("Likes").
                                    child(fotoPostada.getIdPostagem()).
                                    child(firebaseUser.getUid()).setValue(true);

                            //NOTIFICACAO
                            addNovaNotificacao(fotoPostada.getIdUsuarioPostou(), fotoPostada.getIdPostagem());
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
                    fotoSalvar(fotoPostada.getIdPostagem(), holder.salvarButtonHome);

                    holder.salvarButtonHome.setOnClickListener(v -> {

                        if (holder.salvarButtonHome.getTag().equals("Salvar")) {

                            ConfiguracaoFirebase.getReferenciaDatabase().child("SalvarFotos")
                                    .child(firebaseUser.getUid())
                                    .child(fotoPostada.getIdPostagem())
                                    .setValue(true);
                            Toast.makeText(getContext(), "Fofo né? Agora está salvo com você.", Toast.LENGTH_LONG).show();

                            addNovaNotificacaoSalvar(fotoPostada.getIdUsuarioPostou(), fotoPostada.getIdPostagem());


                        } else {
                            ConfiguracaoFirebase.getReferenciaDatabase().child("SalvarFotos")
                                    .child(firebaseUser.getUid())
                                    .child(fotoPostada.getIdPostagem())
                                    .removeValue();

                            Toast.makeText(getContext(), "Você pode me favoritar depois se quiser. Sentirei sua falta.", Toast.LENGTH_SHORT).show();

                        }
                    });

                    //SEGUIDORES
                    holder.qtLikesHome.setOnClickListener(v -> {

                        Intent intent = new Intent(getContext(), SeguidoresActivity.class);
                        intent.putExtra("idPostagem", fotoPostada.getIdPostagem());
                        intent.putExtra("titulo", "Curtir");
                        startActivity(intent);

                    });

                    holder.imagemLikeHome.setOnClickListener(v -> {

                        Intent intent = new Intent(getContext(), SeguidoresActivity.class);
                        intent.putExtra("idPostagem", fotoPostada.getIdPostagem());
                        intent.putExtra("titulo", "Curtir");
                        startActivity(intent);

                    });


                    //COMENTARIOS
                    holder.comentarioButtonHome.setOnClickListener(v -> {
                        Intent intent = new Intent(getContext(), ComentariosActivity.class);
                        intent.putExtra("idPostagem", fotoPostada.getIdPostagem());
                        intent.putExtra("usuarioPostouId", fotoPostada.getIdUsuarioPostou());
                        startActivity(intent);
                    });

                    holder.comentariosTodosHome.setOnClickListener(v -> {
                        Intent intent = new Intent(getContext(), ComentariosActivity.class);
                        intent.putExtra("idPostagem", fotoPostada.getIdPostagem());
                        intent.putExtra("usuarioPostouId", fotoPostada.getIdUsuarioPostou());
                        startActivity(intent);
                    });

                    getQuantidadeComentarios(fotoPostada.getIdPostagem(), holder.comentariosTodosHome);

                    //POSTAGEM DETALHES
                    holder.imagemPostadaHome.setOnClickListener(v -> {
                        SharedPreferences.Editor editor = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                        editor.putString("idPostagem", fotoPostada.getIdPostagem());
                        editor.apply();

                        ((FragmentActivity) getContext()).getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).
                                replace(R.id.fragment_container_principal_StartAct, new PostagemUsuarioFragment()).commit();


                    });

                    holder.nomeUsuarioHome.setOnClickListener(v -> {
                        SharedPreferences.Editor editor = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                        editor.putString("idUsuario", fotoPostada.getIdUsuarioPostou());
                        editor.apply();

                        ((FragmentActivity) getContext()).getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).
                                replace(R.id.fragment_container_principal_StartAct, new PerfilFragment()).commit();
                    });

                    holder.fotoPerfilHome.setOnClickListener(v -> {
                        SharedPreferences.Editor editor = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                        editor.putString("idUsuario", fotoPostada.getIdUsuarioPostou());
                        editor.apply();

                        ((FragmentActivity) getContext()).getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE).
                                replace(R.id.fragment_container_principal_StartAct, new PerfilFragment()).commit();

                    });

                    //DELETAR POSTAGEM

                    if (!firebaseUser.getUid().equalsIgnoreCase(fotoPostada.getIdUsuarioPostou())) {
                        holder.deletarPostagemHome.setVisibility(View.GONE);
                    } else {
                        holder.deletarPostagemHome.setVisibility(View.VISIBLE);
                    }

                    holder.deletarPostagemHome.setOnClickListener(v -> {

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Deletar PetPostagem");
                        builder.setMessage("Deseja deletar essa linda PetPostagem?\n\n\n *OBS: Apagando a postagem, você tambem exclui TODAS suas notificações.");
                        builder.setIcon(R.drawable.ic_pets_black_24dp);
                        builder.setPositiveButton(R.string.confirmar, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deletarPostagem(firebaseUser.getUid(), fotoPostada.getIdPostagem());
                                dialog.dismiss();
                                Intent intent = new Intent(getContext(), StartActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        });
                        builder.setNegativeButton(R.string.cancelar, (dialog, which) -> {
                            Snackbar.make(v, "Ufa, uma postagem fofa dessas deveria ser suuuper vista, não acha?", Snackbar.LENGTH_LONG).
                                    show();
                            dialog.dismiss();
                        });

                        Dialog dialog = builder.create();
                        dialog.show();

                    });
                }else{
                    holder.itemView.setVisibility(View.GONE);
                }
            }

            @Override
            protected void onLoadingStateChanged(@NonNull com.shreyaspatil.firebase.recyclerpagination.LoadingState state) {
                switch (state) {
                    case LOADING_INITIAL:
                    case LOADING_MORE:
                        // Do your loading animation
                        swipeRefreshLayout.setRefreshing(false);
                        break;

                    case LOADED:
                        // Stop Animation
                        swipeRefreshLayout.setRefreshing(false);
                        break;

                    case FINISHED:
                        //Reached end of Data set
                        swipeRefreshLayout.setRefreshing(false);
                        break;

                    case ERROR:
                        retry();
                        break;
                }
            }
        };


        //ADAPTER
        RecyclerView recyclerViewFirestore = view.findViewById(R.id.recyclerViewTestes_Firestore_Fragment);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewFirestore.setLayoutManager(linearLayoutManager);
        adapterFirestore = new AdapterFirestore(options1,getContext());
        adapterFirestore.setHasStableIds(true);

        recyclerViewFirestore.setItemViewCacheSize(20);
        adapter.setHasStableIds(true);


        recyclerViewFirestore.setAdapter(adapterFirestore);


        Query queryCop = firebaseFirestore.collection("Posts")
                .orderBy("timeStamp").limit(10);

        queryCop.get().addOnCompleteListener(getActivity(), new OnCompleteListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot dc : task.getResult()) {
                       FotoPostada fotoPostada = dc.toObject(FotoPostada.class);

                    }
                    recyclerViewFirestore.setAdapter(adapter);
                    adapterFirestore.notifyDataSetChanged();
                    lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);
                    Toast.makeText(getContext(), "Primeira pagina carregada.", Toast.LENGTH_SHORT).show();

                    RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                            super.onScrollStateChanged(recyclerView, newState);

                            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                                isScrolling = true;

                            }
                        }

                        @Override
                        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);

                            int firstItemVisible = linearLayoutManager.findFirstVisibleItemPosition();
                            int visibleItemCount = linearLayoutManager.getChildCount();
                            int totalItemCount = linearLayoutManager.getItemCount();

                            if (isScrolling && (firstItemVisible + visibleItemCount == totalItemCount) && !isLastItemReached) {
                                isScrolling = false;

                                Query nextQuery = firebaseFirestore.collection("Posts")
                                        .orderBy("timeStamp")
                                        .startAfter(lastVisible)
                                        .limit(10);

                                nextQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        for (DocumentSnapshot dc : task.getResult()) {

                                        }

                                        adapterFirestore.notifyDataSetChanged();

                                        lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);
                                        Toast.makeText(getContext(), "Primeira pagina carregada.", Toast.LENGTH_SHORT).show();

                                        if (task.getResult().size() < 10){
                                            isLastItemReached = true;
                                        }
                                    }
                                });

                            }
                        }
                    };
                    recyclerViewFirestore.addOnScrollListener(scrollListener);
                }
            }
        });


        //STORIES
        recyclerViewStories = view.findViewById(R.id.recyclerView_Stories_HomeFragment_id);
        recyclerViewStories.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManagerStories = new LinearLayoutManager(getContext(),
                RecyclerView.HORIZONTAL, false);
        recyclerViewStories.setLayoutManager(linearLayoutManagerStories);
        adapterStories = new AdapterStories(getContext(), storiesList);
        recyclerViewStories.setAdapter(adapterStories);
        readStories();
        // Inflate the layout for this fragment

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                adapter.notifyDataSetChanged();
            }
        });

        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }
    private void readStories() {
        DatabaseReference usuariosReferencia = ConfiguracaoFirebase.getReferenciaDatabase().child("usuarios");
        usuariosReferencia.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    listaIdUsuarios.add(ds.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        readyStories();
    }

    private void readyStories() {
        DatabaseReference storiesRef = ConfiguracaoFirebase.getReferenciaDatabase()
                .child("Stories");

        storiesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long currentTime = System.currentTimeMillis();
                storiesList.clear();
                storiesList.add(new Stories(0, 0, UsuarioFirebase.getUsuarioAtual().getUid(), "", ""));

                for (String id : listaIdUsuarios) {
                    int countStories = 0;
                    Stories stories = null;
                    for (DataSnapshot ds1 : dataSnapshot.child(id).getChildren()) {
                        stories = ds1.getValue(Stories.class);
                        if (currentTime > stories.getDataInicio() && currentTime < stories.getDataFim()) {
                            countStories++;
                        }
                    }

                    if (countStories > 0) {
                        storiesList.add(stories);
                    }
                    adapterStories.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void deslogarUsuario() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.deseja_sair_app);
        builder.setIcon(R.drawable.ic_pets_black_24dp);
        builder.setMessage(R.string.deseja_sair_app_message);
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.confirmar), (dialog, which) -> {
            try {
                firebaseAuth.signOut();
                startActivity(new Intent(getContext(), LoginActivity.class));
            } catch (Exception e) {
                Toast.makeText(getContext(), "Erro." + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(R.string.cancelar, (dialog, which) -> {
            Toast.makeText(getContext(), "Muito bem. Continue se divertindo com os pets do mundo todo!", Toast.LENGTH_SHORT).show();
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_sair_usuarios, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.item_sair_MenuSair) {
            deslogarUsuario();
        }else if (item.getItemId() == R.id.chat_MenuSair){
            Intent intent = new Intent(getContext(), ChatJamActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }else if (item.getItemId() == R.id.atualizar_update_MenuSair){

            Intent intent = new Intent(getContext(), FirestoreTestes.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        }
        return super.onOptionsItemSelected(item);

    }


    @Override
    public void OnItemClick(DocumentSnapshot snapshot, int position) {
        Log.i("onClickAdapterFirestore","Item clicado: "+position);
        Log.i("onClickAdapterFirestore","ID clicado: "+snapshot.getId());
    }




    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        progressBarHomeFragment.setVisibility(View.GONE);
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
                    Glide.with(getContext()).load(uriFotoPerfil).priority(Priority.IMMEDIATE).into(imagemPerfil);
                }else{
                    //Picasso.get().load(R.drawable.ic_pessoa_usuario).into(imagemPerfil);
                    Glide.with(getContext()).load(R.drawable.ic_pessoa_usuario).into(imagemPerfil);
                }

                nomeUsuario.setText(usuario.getNomePetUsuario());
                publicadoPor.setText(String.format("@%s", usuario.getNomePetUsuario().replace(" ", "")));
                publicadoPor.setTextColor(getContext().getResources().getColor(R.color.escuroAzul));


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
        FirebaseUser firebaseUser = UsuarioFirebase.getUsuarioAtual();
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

    private void deletarPostagem(String idUsuario,String idPostagem){

        FirebaseUser firebaseUser = UsuarioFirebase.getUsuarioAtual();
        DatabaseReference postsRef = ConfiguracaoFirebase.getReferenciaDatabase()
                .child("Posts").child(idPostagem);

        postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (idUsuario.equals(firebaseUser.getUid())){
                    postsRef.removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            deletaNotificacao(idUsuario,idPostagem);
                            alteraNotificacao(idUsuario,idPostagem);
                            Toast.makeText(getContext(), "Deletado com sucesso.", Toast.LENGTH_SHORT).show();
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
        FirebaseUser firebaseUser = UsuarioFirebase.getUsuarioAtual();
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
        FirebaseUser firebaseUser = UsuarioFirebase.getUsuarioAtual();
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
        DatabaseReference notificacaoReference =  reference.child("Notificacao").child(idUsuario);
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
        FirebaseUser firebaseUser = UsuarioFirebase.getUsuarioAtual();
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

    private class ViewHolderFoto extends RecyclerView.ViewHolder {
        public TextView descricaoHome;
        public TextView nomeUsuarioHome;
        public TextView qtLikesHome;
        public TextView comentariosTodosHome;
        public TextView postadaPorHome;
        public TextView dataPostada;

        public ImageView comentarioButtonHome;
        public ImageView imagemLikeHome;
        public LikeButton likeButtonHome;
        public ImageView salvarButtonHome;

        public ImageView imagemPostadaHome;
        public CircleImageView fotoPerfilHome;
        public CircleImageView deletarPostagemHome;


        public ViewHolderFoto(@NonNull View itemView) {
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
            dataPostada = itemView.findViewById(R.id.dataPostagem_HomeAdapter);

        }
    }


}



