package br.com.petgramapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import br.com.petgramapp.R;
import br.com.petgramapp.adapter.AdapterMensagensJam;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.helper.UsuarioFirebase;
import br.com.petgramapp.model.Conversas;
import br.com.petgramapp.model.GrupoJam;
import br.com.petgramapp.model.MensagemJam;
import br.com.petgramapp.model.NotificacoesJam;
import br.com.petgramapp.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class TalksJamActivity extends AppCompatActivity {

    public static String idUsuarioDestinatario;
    public Usuario usuarioLogado;
    List<DocumentChange> documentChangeList = new ArrayList<>();
    LinearLayoutManager linearLayoutManager;
    private TextView nomeUsuarioTalks;
    private CircleImageView imagemPerfilTalks;
    private EditText mensagemDigitadaTalks;
    private ImageView adicionarFotoTalks;
    private FloatingActionButton floatingActionButtonTalks;
    private Usuario usuarioSelecionado;
    private FirebaseFirestore firebaseFirestore;
    private RecyclerView recyclerViewContentTalks;
    private AdapterMensagensJam adapterMensagensJam;
    private List<MensagemJam> mensagemJamList = new ArrayList<>();
    private Query queryMensagens;
    private Uri imagemFotoUri;
    private ListenerRegistration eventListener;
    private StorageTask uploadFotoTask;
    private StorageReference storageReference;
    private String imagemUrlPostagem;
    //identificador de mensagens por usuario
    private String idUsuarioRemetente;
    private ProgressDialog dialog;
    //grupos
    private GrupoJam grupoJam;
    //teste com pagination firestore
    private DocumentSnapshot lastVisible;
    private boolean isScrolling;
    private boolean isLastItemReached;
    private int numConversas = 0;

    //teste com database RealTime
    private DatabaseReference reference;
    private DatabaseReference mensagemRef;
    private ChildEventListener childEventListener;
    private Conversas conversasGeral;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talks_jam);
        Toolbar toolbar = findViewById(R.id.toolbar_TalksJam);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        carregarElementos();
        reference = ConfiguracaoFirebase.getReferenciaDatabase();
        firebaseFirestore = ConfiguracaoFirebase.getFirebaseFirestore();

        storageReference = ConfiguracaoFirebase.getStorageReference();

        ChatApplication application = (ChatApplication) getApplication();
        getApplication().registerActivityLifecycleCallbacks(application);

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {

            //conversa normal
            if (bundle.containsKey("chatContato")) {
                usuarioSelecionado = bundle.getParcelable("chatContato");
                nomeUsuarioTalks.setText(usuarioSelecionado.getNomePetUsuario());
                conversasGeral = (Conversas) bundle.getSerializable("conversasFull");
               // Log.d("chatGeral", conversasGeral.getNumeroMensagens() + " conversas");
                idUsuarioDestinatario = usuarioSelecionado.getId();
                if (usuarioSelecionado.getUriCaminhoFotoPetUsuario() != null) {
                    Uri uriFotoPerfil = Uri.parse(usuarioSelecionado.getUriCaminhoFotoPetUsuario());
                    Picasso.get().load(uriFotoPerfil).
                            priority(Picasso.Priority.HIGH).noFade().into(imagemPerfilTalks);
                } else {
                    Picasso.get().load(R.drawable.ic_person_black_preto).
                            priority(Picasso.Priority.HIGH).noFade().into(imagemPerfilTalks);
                }
            } else if (bundle.containsKey("chatGrupo")) {
                grupoJam = (GrupoJam) bundle.getParcelable("chatGrupo");
                idUsuarioDestinatario = grupoJam.getIdGrupo();
                nomeUsuarioTalks.setText(grupoJam.getNomeGrupo());
                conversasGeral = (Conversas) bundle.getSerializable("conversasFull");
            //    Log.d("chatGeral", conversasGeral.getNumeroMensagens() + " conversas");
                if (grupoJam.getFotoGrupo() != null) {
                    Uri fotoGrupoUri = Uri.parse(grupoJam.getFotoGrupo());
                    Picasso.get().load(fotoGrupoUri).
                            priority(Picasso.Priority.HIGH).noFade().into(imagemPerfilTalks);
                } else {
                    imagemPerfilTalks.setImageResource(R.drawable.ic_person_black_preto);
                }

            }
        }

        Bundle bundle1 = getIntent().getExtras();
        if (bundle1 != null) {
            usuarioLogado = bundle1.getParcelable("chatUsuarioLogado");
            //Log.i("ChatUsuario","Nome usuario: "+usuarioLogado.getNomePetUsuario());
        }

        //configuração adapter conversas
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setMeasurementCacheEnabled(true);
        layoutManager.supportsPredictiveItemAnimations();

        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        linearLayoutManager.supportsPredictiveItemAnimations();

        recyclerViewContentTalks.setLayoutManager(linearLayoutManager);
        recyclerViewContentTalks.setHasFixedSize(true);

        adapterMensagensJam = new AdapterMensagensJam(this, mensagemJamList, recyclerViewContentTalks);
        adapterMensagensJam.setHasStableIds(false);

        recyclerViewContentTalks.setAdapter(adapterMensagensJam);

        idUsuarioRemetente = UsuarioFirebase.getIdentificadorUsuario();

        queryMensagens = firebaseFirestore.collection("Mensagens")
                .document(idUsuarioRemetente)
                .collection(idUsuarioDestinatario);

        mensagemRef = reference.child("Mensagens").child(idUsuarioRemetente)
                .child(idUsuarioDestinatario);

        if (conversasGeral != null) {
            if (conversasGeral.getNumeroMensagens() > 0) {
                firebaseFirestore.collection("Talks")
                        .document("Conversas")
                        .collection(idUsuarioRemetente)
                        .document(idUsuarioDestinatario).
                        update("numeroMensagens", 0);

                firebaseFirestore.collection("Talks")
                        .document("Conversas")
                        .collection(idUsuarioDestinatario)
                        .document(idUsuarioRemetente).
                        update("numeroMensagens", 0);

            }
        }


        //enviar foto
        adicionarFotoTalks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().setAspectRatio(100, 100)
                        .setCropShape(CropImageView.CropShape.RECTANGLE).start(TalksJamActivity.this);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        //readMensagensDatabase();
        readMensagens();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(TalksJamActivity.this, ChatJamActivity.class);
        startActivity(i);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        mensagemRef.removeEventListener(childEventListener);
        eventListener.remove();
    }

    void readMensagensDatabase() {

        mensagemJamList.clear();
        com.google.firebase.database.Query query = mensagemRef.orderByChild("timeStamp");

        childEventListener = query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                MensagemJam mensagemJam = dataSnapshot.getValue(MensagemJam.class);
                mensagemJamList.add(mensagemJam);
                adapterMensagensJam.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void readMensagens() {
        mensagemJamList.clear();
        eventListener = queryMensagens
                .orderBy("timeStamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) return;

                        List<DocumentChange> documentChanges = queryDocumentSnapshots.getDocumentChanges();
                        if (documentChanges != null) {
                            for (DocumentChange doc : documentChanges) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    MensagemJam mensagemJam = doc.getDocument().toObject(MensagemJam.class);
                                    mensagemJamList.add(mensagemJam);
                                    adapterMensagensJam.notifyDataSetChanged();

                                }
                            }
                        }
                    }
                });

/*
        Query query = firebaseFirestore.collection("Mensagens")
                .document(idUsuarioRemetente).collection(idUsuarioDestinatario)
                .orderBy("timeStamp").limit(10);

        query.get().addOnCompleteListener(this, new OnCompleteListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot dc : task.getResult()) {
                        MensagemJam mensagemJam = dc.toObject(MensagemJam.class);
                        mensagemJamList.add(mensagemJam);
                    }
                    recyclerViewContentTalks.setAdapter(adapterMensagensJam);
                    adapterMensagensJam.notifyDataSetChanged();
                    lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);
                    Toast.makeText(TalksJamActivity.this, "Primeira pagina carregada.", Toast.LENGTH_SHORT).show();

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

                                Query nextQuery = firebaseFirestore.collection("Mensagens")
                                        .document(idUsuarioRemetente).collection(idUsuarioDestinatario)
                                        .orderBy("timeStamp")
                                        .startAfter(lastVisible)
                                        .limit(10);

                                nextQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        for (DocumentSnapshot dc : task.getResult()) {
                                            MensagemJam mensagemJam = dc.toObject(MensagemJam.class);
                                            mensagemJamList.add(mensagemJam);
                                        }

                                        adapterMensagensJam.notifyDataSetChanged();

                                        lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);
                                        Toast.makeText(TalksJamActivity.this, "Primeira pagina carregada.", Toast.LENGTH_SHORT).show();

                                        if (task.getResult().size() < 10){
                                            isLastItemReached = true;
                                        }
                                    }
                                });

                            }
                        }
                    };
                    recyclerViewContentTalks.addOnScrollListener(scrollListener);
                }
            }
        });*/


    }

    private void abrirDialogCarregamento(String descricao) {
        dialog = new ProgressDialog(this);
        dialog.setTitle("Aguarde.");
        dialog.setMessage(descricao);
        dialog.setIcon(R.drawable.ic_pets_black_24dp);
        dialog.setCancelable(false);
        dialog.create();
        dialog.show();

    }

    public void enviarMensagem(View view) {

        //DATA POSTAGEM
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        String dataPost = "Enviado em ".concat(currentDate.concat(" às ").concat(currentTime));
        String dataGet = "Rebido em ".concat(currentDate.concat(" às ").concat(currentTime));

        String mensagemDigitada = mensagemDigitadaTalks.getText().toString();

        if (!mensagemDigitada.isEmpty()) {

            //verificar se mensagem é mensagem de um grupo
            if (usuarioSelecionado != null) {
                MensagemJam mensagemJam = new MensagemJam();
                mensagemJam.setId(idUsuarioRemetente);
                mensagemJam.setMensagem(mensagemDigitada);
                mensagemJam.setDataEnvio(dataPost);
                mensagemJam.setDataRecebido(dataGet);
                mensagemJam.setImagemEnviada("");
                mensagemJam.setNomeUsuarioEnviou(usuarioLogado.getNomePetUsuario());
                mensagemJam.setTimeStamp(System.currentTimeMillis());

                NotificacoesJam notificacoesJam = new NotificacoesJam();

                notificacoesJam.setFromName(usuarioLogado.getNomePetUsuario());
                notificacoesJam.setId(idUsuarioRemetente);
                notificacoesJam.setDataEnvio(dataPost);
                notificacoesJam.setDataRecebido(dataPost);
                notificacoesJam.setTimeStamp(System.currentTimeMillis());
                notificacoesJam.setMensagem(mensagemDigitada);
                notificacoesJam.setNomeUsuarioEnviou(usuarioLogado.getNomePetUsuario());

                firebaseFirestore.collection("Notificacoes")
                        .document(usuarioSelecionado.getToken())
                        .set(notificacoesJam);

                //para o remetente
                salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagemJam);

                //para o destinatario
                salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagemJam);

                //para o remetente
                salvarConversa(idUsuarioRemetente, idUsuarioDestinatario, usuarioSelecionado, mensagemJam, false);
                salvarConversaUsuario(mensagemJam, false);
                //para o remetente
                //salvarConversa(idUsuarioDestinatario,idUsuarioRemetente,usuarioLogado,mensagemJam, false);
            } else {
                for (Usuario membros : grupoJam.getMembrosGrupo()) {
                    String idRemetenteGrupo = membros.getId();
                    String idUsuarioLogadoGrupo = UsuarioFirebase.getIdentificadorUsuario();

                    MensagemJam mensagemJam = new MensagemJam();
                    mensagemJam.setId(idUsuarioLogadoGrupo);
                    mensagemJam.setMensagem(mensagemDigitada);
                    mensagemJam.setDataEnvio(dataPost);
                    mensagemJam.setDataRecebido(dataGet);
                    mensagemJam.setImagemEnviada(" ");
                    mensagemJam.setNomeUsuarioEnviou(usuarioLogado.getNomePetUsuario());
                    mensagemJam.setTimeStamp(System.currentTimeMillis());

                    NotificacoesJam notificacoesJam = new NotificacoesJam();

                    notificacoesJam.setFromName(grupoJam.getNomeGrupo());
                    notificacoesJam.setId(idUsuarioLogadoGrupo);
                    notificacoesJam.setDataEnvio(dataPost);
                    notificacoesJam.setDataRecebido(dataPost);
                    notificacoesJam.setTimeStamp(System.currentTimeMillis());
                    notificacoesJam.setMensagem(mensagemDigitada);
                    notificacoesJam.setNomeUsuarioEnviou(membros.getNomePetUsuario());

                    //para o remetente
                    salvarMensagem(idRemetenteGrupo, idUsuarioDestinatario, mensagemJam);

                    //para o destinatario
                    salvarMensagem(idUsuarioDestinatario, idRemetenteGrupo, mensagemJam);

                    //para o remetente //para o destinario que é o grupo
                    salvarConversa(idRemetenteGrupo, idUsuarioDestinatario, usuarioSelecionado, mensagemJam, true);

                    recyclerViewContentTalks.scrollToPosition(View.SCROLL_AXIS_VERTICAL);


                }
            }


        } else {
            Snackbar.make(view, "Por gentileza, digitar uma mensagem.", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void salvarMensagem(String idUsuRemetente, String idUsuDesti, MensagemJam mensagem) {

        firebaseFirestore.collection("Mensagens")
                .document(idUsuRemetente)
                .collection(idUsuDesti).
                add(mensagem);

        mensagemDigitadaTalks.setText("");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult activityResult = CropImage.getActivityResult(data);
            imagemFotoUri = activityResult.getUri();
            if (usuarioSelecionado != null) {
                uploadImagemEnviada();
            } else {
                uploadImagemEnviada();
            }

        } else {
            Toast.makeText(this, "Eita", Toast.LENGTH_LONG).show();
        }
    }

    private void uploadImagemEnviada() {

        abrirDialogCarregamento("Sua petFoto está sendo postada! Aguarde...");
        MensagemJam mensagemJam = new MensagemJam();

        if (imagemFotoUri != null) {
            StorageReference imagemRef = ConfiguracaoFirebase.getStorageReference().child("Imagens")
                    .child("fotoEnviada")
                    .child(idUsuarioRemetente)
                    .child(UUID.randomUUID() + ".jpeg");

            Bitmap bmp = null;
            try {
                bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), imagemFotoUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 40, baos);
            byte[] data = baos.toByteArray();

            uploadFotoTask = imagemRef.putBytes(data);

            uploadFotoTask.continueWithTask((Continuation) task -> {

                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return imagemRef.getDownloadUrl();

            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {

                if (task.isSuccessful()) {
                    Uri downloadUriImagem = task.getResult();

                    imagemUrlPostagem = downloadUriImagem.toString();

                    if (usuarioSelecionado != null) {
                        //DATA POSTAGEM
                        String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
                        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                        String dataPost = "Enviado em ".concat(currentDate.concat(" às ").concat(currentTime));
                        mensagemJam.setDataEnvio(dataPost);
                        mensagemJam.setDataRecebido(dataPost);
                        mensagemJam.setImagemEnviada(imagemUrlPostagem);
                        mensagemJam.setId(idUsuarioRemetente);
                        mensagemJam.setNomeUsuarioEnviou(usuarioLogado.getNomePetUsuario());
                        mensagemJam.setMensagem("imagem.jpeg");
                        //mensagemJam.setMensagem(null);
                        mensagemJam.setTimeStamp(System.currentTimeMillis());
                        //para remetente
                        salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagemJam);
                        //para destinatario
                        salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagemJam);
                        //SALVAR CONVERSA
                        salvarConversa(idUsuarioRemetente, idUsuarioDestinatario, usuarioSelecionado, mensagemJam, false);
                        //salvarConversaUsuario(mensagemJam, false);
                        NotificacoesJam notificacoesJam = new NotificacoesJam();

                        notificacoesJam.setFromName(usuarioLogado.getNomePetUsuario());
                        notificacoesJam.setId(idUsuarioRemetente);
                        notificacoesJam.setDataEnvio(dataPost);
                        notificacoesJam.setDataRecebido(dataPost);
                        notificacoesJam.setTimeStamp(System.currentTimeMillis());
                        notificacoesJam.setMensagem(mensagemJam.getMensagem());

                        firebaseFirestore.collection("Notificacoes")
                                .document(usuarioSelecionado.getToken())
                                .set(notificacoesJam);

                    } else {
                        for (Usuario membros : grupoJam.getMembrosGrupo()) {
                            String idRemetenteGrupo = membros.getId();
                            String idUsuarioLogadoGrupo = UsuarioFirebase.getIdentificadorUsuario();

                            String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
                            String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                            String dataPost = "Enviado em ".concat(currentDate.concat(" às ").concat(currentTime));
                            mensagemJam.setDataEnvio(dataPost);
                            mensagemJam.setDataRecebido(dataPost);
                            mensagemJam.setImagemEnviada(imagemUrlPostagem);
                            mensagemJam.setId(idUsuarioRemetente);
                            mensagemJam.setNomeUsuarioEnviou(usuarioLogado.getNomePetUsuario());
                            mensagemJam.setMensagem("imagem.jpeg");
                            //mensagemJam.setMensagem(null);
                            mensagemJam.setTimeStamp(System.currentTimeMillis());

                            NotificacoesJam notificacoesJam = new NotificacoesJam();

                            notificacoesJam.setFromName(grupoJam.getNomeGrupo());
                            notificacoesJam.setId(idUsuarioLogadoGrupo);
                            notificacoesJam.setDataEnvio(dataPost);
                            notificacoesJam.setDataRecebido(dataPost);
                            notificacoesJam.setTimeStamp(System.currentTimeMillis());
                            notificacoesJam.setMensagem(mensagemJam.getMensagem());
                            notificacoesJam.setNomeUsuarioEnviou(membros.getNomePetUsuario());

                            //para o remetente
                            salvarMensagem(idRemetenteGrupo, idUsuarioDestinatario, mensagemJam);

                            //para o destinatario
                            salvarMensagem(idUsuarioDestinatario, idRemetenteGrupo, mensagemJam);

                            //para o remetente //para o destinario que é o grupo
                            salvarConversa(idRemetenteGrupo, idUsuarioDestinatario, usuarioSelecionado, mensagemJam, true);

                        }

                    }


                    Toast.makeText(this, "Imagem enviada com sucesso.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();


                } else {
                    Toast.makeText(this, "Verifique sua conexão com a internet!", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }

            }).addOnFailureListener(e ->
                    Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show());
            dialog.dismiss();
        } else {
            Toast.makeText(this, "Nenhuma PetImagem selecionada", Toast.LENGTH_SHORT).show();
        }
    }

    private void salvarConversa(String idUsuRemetente, String idUsuDesti, Usuario usuarioExibicao, MensagemJam mensagem, boolean isGroup) {

        //DATA POSTAGEM
        String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        String dataPost = "Enviado em ".concat(currentDate.concat(" às ").concat(currentTime));
        Conversas conversasRemetente = new Conversas();
        numConversas++;
        conversasRemetente.setIdRemetente(idUsuRemetente);
        conversasRemetente.setIdDestinatario(idUsuDesti);
        conversasRemetente.setTimeStamp(System.currentTimeMillis());
        conversasRemetente.setUltimaMensagem(mensagem.getMensagem());
        conversasRemetente.setDataEnvio(dataPost);

        //verificar se é conversa de grupo
        if (isGroup) {
            conversasRemetente.setNumeroMensagens(numConversas);
            conversasRemetente.setGrupoJam(grupoJam);
            conversasRemetente.setIsGroup("true");
            conversasRemetente.salvarConversa();
            conversasRemetente.salvarConversaOutroUsuario(usuarioLogado);

        } else {

            conversasRemetente.setNumeroMensagens(numConversas);
            conversasRemetente.setUsuario(usuarioExibicao);
            conversasRemetente.setIsGroup("false");
            conversasRemetente.salvarConversa();
            conversasRemetente.salvarConversaOutroUsuario(usuarioLogado);

        }


    }

    private void salvarConversaUsuario(MensagemJam mensagem, boolean isGroup) {

        //DATA POSTAGEM
        String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        String dataPost = "Enviado em ".concat(currentDate.concat(" às ").concat(currentTime));
        Conversas conversasRemetente = new Conversas();

        numConversas++;
        conversasRemetente.setIdRemetente(idUsuarioRemetente);
        conversasRemetente.setIdDestinatario(idUsuarioDestinatario);
        conversasRemetente.setTimeStamp(System.currentTimeMillis());
        conversasRemetente.setUltimaMensagem(mensagem.getMensagem());
        conversasRemetente.setDataEnvio(dataPost);

        //verificar se é conversa de grupo
        if (isGroup) {
            // conversasRemetente.setUsuario(usuarioSelecionado);
            conversasRemetente.setNumeroMensagens(numConversas);
            conversasRemetente.setGrupoJam(grupoJam);
            conversasRemetente.setIsGroup("true");
            conversasRemetente.salvarConversa();
            conversasRemetente.salvarConversaOutroUsuario(usuarioLogado);

        } else {
            conversasRemetente.setNumeroMensagens(numConversas);
            conversasRemetente.setUsuario(usuarioSelecionado);
            conversasRemetente.setIsGroup("false");
            conversasRemetente.salvarConversa();
            conversasRemetente.salvarConversaOutroUsuario(usuarioLogado);

        }


    }

    public void carregarElementos() {
        nomeUsuarioTalks = findViewById(R.id.nomeUsuario_Toolbar_TalksJam);
        imagemPerfilTalks = findViewById(R.id.imagemPerfil_Toolbar_TalksJam);
        mensagemDigitadaTalks = findViewById(R.id.mensagemDigitada_Content_TalksJam);
        adicionarFotoTalks = findViewById(R.id.camera_Content_TalksJam);
        floatingActionButtonTalks = findViewById(R.id.floatingActionButton_ContentTalks);
        recyclerViewContentTalks = findViewById(R.id.recyclerView_Content_ChatTalksJam);
    }

}
