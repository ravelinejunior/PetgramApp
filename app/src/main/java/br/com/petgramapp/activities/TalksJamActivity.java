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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
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
import br.com.petgramapp.model.MensagemJam;
import br.com.petgramapp.model.NotificacoesJam;
import br.com.petgramapp.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class TalksJamActivity extends AppCompatActivity {

    public static String idUsuarioDestinatario;
    public  Usuario usuarioLogado;
    List<DocumentChange> documentChangeList = new ArrayList<>();
    private TextView nomeUsuarioTalks;
    private CircleImageView imagemPerfilTalks;
    private EditText mensagemDigitadaTalks;
    private ImageView adicionarFotoTalks;
    private FloatingActionButton floatingActionButtonTalks;
    private Usuario usuarioSelecionado;
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference mensagemRef;
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


        storageReference = ConfiguracaoFirebase.getStorageReference();

        ChatApplication application = (ChatApplication) getApplication();
        getApplication().registerActivityLifecycleCallbacks(application);

       Bundle bundle = getIntent().getExtras();

       if (bundle != null){
           usuarioSelecionado = bundle.getParcelable("chatContato");
           nomeUsuarioTalks.setText(usuarioSelecionado.getNomePetUsuario());
           if (usuarioSelecionado.getUriCaminhoFotoPetUsuario() != null){
               Uri uriFotoPerfil = Uri.parse(usuarioSelecionado.getUriCaminhoFotoPetUsuario());
               Picasso.get().load(uriFotoPerfil).
                       priority(Picasso.Priority.HIGH).noFade().into(imagemPerfilTalks);
           }else{
               Picasso.get().load(R.drawable.ic_person_black_preto).
                       priority(Picasso.Priority.HIGH).noFade().into(imagemPerfilTalks);
           }

       }

       Bundle bundle1 = getIntent().getExtras();
        if (bundle1 != null){
            usuarioLogado = bundle1.getParcelable("chatUsuarioLogado");
            //Log.i("ChatUsuario","Nome usuario: "+usuarioLogado.getNomePetUsuario());
        }

       //configuração adapter conversas
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setMeasurementCacheEnabled(true);
        layoutManager.supportsPredictiveItemAnimations();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        linearLayoutManager.supportsPredictiveItemAnimations();

        recyclerViewContentTalks.setLayoutManager(linearLayoutManager);
        recyclerViewContentTalks.setHasFixedSize(true);

        adapterMensagensJam = new AdapterMensagensJam(this,mensagemJamList);
        recyclerViewContentTalks.setAdapter(adapterMensagensJam);

        idUsuarioRemetente = UsuarioFirebase.getIdentificadorUsuario();
        idUsuarioDestinatario = usuarioSelecionado.getId();
        firebaseFirestore = ConfiguracaoFirebase.getFirebaseFirestore();

        queryMensagens =  firebaseFirestore.collection("Mensagens")
               .document(idUsuarioRemetente)
               .collection(idUsuarioDestinatario);


        //enviar foto
        adicionarFotoTalks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().setAspectRatio(100,100)
                        .setCropShape(CropImageView.CropShape.RECTANGLE).start(TalksJamActivity.this);

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        readMensagens();
    }

    @Override
    public void onBackPressed() {
     Intent i = new Intent(TalksJamActivity.this,ChatJamActivity.class);
     startActivity(i);
    }

    @Override
    protected void onStop() {
        super.onStop();
        eventListener.remove();
    }

    private void readMensagens(){

        mensagemJamList.clear();

        eventListener = queryMensagens
                .orderBy("timeStamp", Query.Direction.ASCENDING)
                .addSnapshotListener(this,new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) return;

                List<DocumentChange> documentChanges = queryDocumentSnapshots.getDocumentChanges();
                if (documentChanges != null){
                    for (DocumentChange doc: documentChanges){
                        if (doc.getType() == DocumentChange.Type.ADDED || doc.getType() == DocumentChange.Type.MODIFIED ||
                            doc.getType() == DocumentChange.Type.REMOVED){
                            MensagemJam mensagemJam = doc.getDocument().toObject(MensagemJam.class);
                            mensagemJamList.add(mensagemJam);
                            adapterMensagensJam.notifyDataSetChanged();

                        }
                    }
                }
            }

        });

    }

    private void abrirDialogCarregamento(String descricao){
        dialog = new ProgressDialog(this);
        dialog.setTitle("Aguarde.");
        dialog.setMessage(descricao);
        dialog.setIcon(R.drawable.ic_pets_black_24dp);
        dialog.setCancelable(false);
        dialog.create();
        dialog.show();

    }

    public void enviarMensagem(View view){

        //DATA POSTAGEM
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        String dataPost = "Enviado em ".concat(currentDate.concat(" às ").concat(currentTime));
        String dataGet = "Rebido em ".concat(currentDate.concat(" às ").concat(currentTime));

        String mensagemDigitada = mensagemDigitadaTalks.getText().toString();

        if (!mensagemDigitada.isEmpty()){

            MensagemJam mensagemJam = new MensagemJam();
            mensagemJam.setId(idUsuarioRemetente);
            mensagemJam.setMensagem(mensagemDigitada);
            mensagemJam.setDataEnvio(dataPost);
            mensagemJam.setDataRecebido(dataGet);
            mensagemJam.setImagemEnviada("");
            mensagemJam.setTimeStamp(System.currentTimeMillis());

          //  if (!usuarioSelecionado.isOnline()){

                NotificacoesJam notificacoesJam = new NotificacoesJam();

                notificacoesJam.setFromName(usuarioLogado.getNomePetUsuario());
                notificacoesJam.setId(idUsuarioRemetente);
                notificacoesJam.setDataEnvio(dataPost);
                notificacoesJam.setDataRecebido(dataPost);
                notificacoesJam.setTimeStamp(System.currentTimeMillis());
                notificacoesJam.setMensagem(mensagemDigitada);

                firebaseFirestore.collection("Notificacoes")
                        .document(usuarioSelecionado.getToken())
                        .set(notificacoesJam);

          //  }

            //para o remetente
            salvarMensagem(idUsuarioRemetente,idUsuarioDestinatario,mensagemJam);

            //para o destinatario
            salvarMensagem(idUsuarioDestinatario,idUsuarioRemetente,mensagemJam);

            //para o remetente
            salvarConversa(mensagemJam);

        }else{
            Snackbar.make(view,"Por gentileza, digitar uma mensagem.",Snackbar.LENGTH_SHORT).show();
        }
    }

    public void salvarMensagem(String idUsuRemetente,String idUsuDesti,MensagemJam mensagem){

            firebaseFirestore.collection("Mensagens");

                   firebaseFirestore.collection("Mensagens")
                           .document(idUsuRemetente)
                            .collection(idUsuDesti).
                            add(mensagem);

              mensagemDigitadaTalks.setText("");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult activityResult = CropImage.getActivityResult(data);
            imagemFotoUri = activityResult.getUri();
            uploadImagemEnviada();
        }else{
            Toast.makeText(this, "Eita", Toast.LENGTH_LONG).show();
        }
    }

    private void uploadImagemEnviada(){

        abrirDialogCarregamento("Sua petFoto está sendo postada! Aguarde...");
        MensagemJam mensagemJam = new MensagemJam();

        if (imagemFotoUri != null){
            StorageReference imagemRef = ConfiguracaoFirebase.getStorageReference().child("Imagens")
                    .child("fotoEnviada")
                    .child(idUsuarioRemetente)
                    .child(UUID.randomUUID()+".jpeg")
                    ;

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

                if (!task.isSuccessful()){
                    throw task.getException();
                }
                return imagemRef.getDownloadUrl();

            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {

                if (task.isSuccessful()) {
                    Uri downloadUriImagem = task.getResult();

                    imagemUrlPostagem = downloadUriImagem.toString();

                    //DATA POSTAGEM
                    String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
                    String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                    String dataPost = "Enviado em ".concat(currentDate.concat(" às ").concat(currentTime));
                    mensagemJam.setDataEnvio(dataPost);
                    mensagemJam.setDataRecebido(dataPost);
                    mensagemJam.setImagemEnviada(imagemUrlPostagem);
                    mensagemJam.setId(idUsuarioRemetente);
                    mensagemJam.setMensagem("imagem.jpeg");
                    //mensagemJam.setMensagem(null);
                    mensagemJam.setTimeStamp(System.currentTimeMillis());

                    //para remetente
                    salvarMensagem(idUsuarioRemetente,idUsuarioDestinatario,mensagemJam);

                    //para destinatario
                    salvarMensagem(idUsuarioDestinatario,idUsuarioRemetente,mensagemJam);

                    //SALVAR CONVERSA
                    salvarConversa(mensagemJam);

                   // if (!usuarioSelecionado.isOnline()){

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

                   // }

                    Toast.makeText(this, "Imagem enviada com sucesso.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();



                }else{
                    Toast.makeText(this, "Verifique sua conexão com a internet!", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }

            }).addOnFailureListener(e ->
                    Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show());
            dialog.dismiss();
        }else{
            Toast.makeText(this, "Nenhuma PetImagem selecionada", Toast.LENGTH_SHORT).show();
        }
    }

    private void salvarConversa(MensagemJam mensagem) {
        //DATA POSTAGEM
        String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        String dataPost = "Enviado em ".concat(currentDate.concat(" às ").concat(currentTime));
        Conversas conversasRemetente = new Conversas();

        conversasRemetente.setIdRemetente(idUsuarioRemetente);
        conversasRemetente.setIdDestinatario(idUsuarioDestinatario);
        conversasRemetente.setTimeStamp(System.currentTimeMillis());
        conversasRemetente.setUltimaMensagem(mensagem.getMensagem());
        conversasRemetente.setDataEnvio(dataPost);
        conversasRemetente.setUsuario(usuarioSelecionado);

        conversasRemetente.salvarConversa();
        conversasRemetente.salvarConversaOutroUsuario(usuarioLogado);


    }

    public void carregarElementos(){
        nomeUsuarioTalks = findViewById(R.id.nomeUsuario_Toolbar_TalksJam);
        imagemPerfilTalks = findViewById(R.id.imagemPerfil_Toolbar_TalksJam);
        mensagemDigitadaTalks = findViewById(R.id.mensagemDigitada_Content_TalksJam);
        adicionarFotoTalks = findViewById(R.id.camera_Content_TalksJam);
        floatingActionButtonTalks = findViewById(R.id.floatingActionButton_ContentTalks);
        recyclerViewContentTalks = findViewById(R.id.recyclerView_Content_ChatTalksJam);
    }

}
