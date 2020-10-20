package br.com.petgramapp.testes;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import br.com.petgramapp.R;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.helper.UsuarioFirebase;
import br.com.petgramapp.model.Usuario;
import br.com.petgramapp.model.Video;

public class VideoPostActivity extends AppCompatActivity {

    public static final int VIDEO_REQUEST_CODE = 2000;
    ProgressDialog dialog;
    StorageTask uploadTaskPostagem;
    private Button selectFileButton;
    private Button uploadVideoButton;
    private TextView nameFile;
    private VideoView videoView;
    //URL do arquivo no armazenamento local
    private Uri fileUri;
    //URL do arquivo no Storage do Firebase
    private Uri videoUri;
    //Usado para subir os arquivos
    private FirebaseStorage storage;
    //Usado para armazenar a URL dos arquivos
    private FirebaseDatabase database;
    private String idVideo;
    //usuarios
    private String idUsuarioLogado;
    private Usuario usuarioLogado;
    private DatabaseReference usuariosRef;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_post);

        initComponents();

        selectFileButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(VideoPostActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                selecionarArquivo();
            } else {
                ActivityCompat.requestPermissions(VideoPostActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        VIDEO_REQUEST_CODE);
            }
        });

        uploadVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fileUri != null){
                    uploadFileToStorage(fileUri);
                }else{
                    Toast.makeText(VideoPostActivity.this, "Selecione ", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    void initComponents() {

        //Retorna um objeto do Firebase Storage
        storage = FirebaseStorage.getInstance();

        //Retorna um objeto do Firebase Database
        database = FirebaseDatabase.getInstance();

        selectFileButton = findViewById(R.id.file_select_button);
        uploadVideoButton = findViewById(R.id.upload_selected_video_button);
        nameFile = findViewById(R.id.file_selected_text);
        videoView = findViewById(R.id.videoViewId);

        usuarioLogado = UsuarioFirebase.getUsuarioLogado();
        idUsuarioLogado = UsuarioFirebase.getIdentificadorUsuario();
        usuariosRef = ConfiguracaoFirebase.getReferenciaDatabase().child("usuarios");
    }

    public void selecionarArquivo() {

        //Oferecer ao usuário a possibilidade de escolher um arquivo usando o gerenciador de arquivos
        Intent intent = new Intent();

        //Especificando o tipo de arquivo que será selecionado
        intent.setType("video/mp4");

        //Buscar arquivos
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, VIDEO_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == VIDEO_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selecionarArquivo();
        } else {
            Toast.makeText(this, "Por favor, dê permissão para acessar arquivos", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        //Verificando se o usuário selecionou um arquivo ou não
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VIDEO_REQUEST_CODE &&
                resultCode == RESULT_OK &&
                data != null) {

            //Retorna a uri do arquivo selecionado
            fileUri = data.getData();
            if (fileUri != null) {
                nameFile.setText("Um arquivo selecionado para fazer upload");
            }
        } else {
            Toast.makeText(this, "Selecione um arquivo", Toast.LENGTH_LONG).show();
        }
    }

    private void abrirDialogCarregamento(String descricao){
        dialog = new ProgressDialog(this);
        dialog.setTitle("Aguarde.");
        dialog.setMessage(descricao);
        dialog.setIcon(R.drawable.ic_pets_black_24dp);
        dialog.setCancelable(false);
        dialog.show();

    }


    public void uploadFileToStorage(Uri fileUri) {
//Retorna pasta raiz do Storage

        abrirDialogCarregamento("Carregando dados, aguarde!");

        Video video = new Video();
        video.setIdUsuarioPostou(idUsuarioLogado);
        //DATA POSTAGEM
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        String dataPost = "Publicado em ".concat(currentDate.concat(" às ").concat(currentTime));
        video.setDataPostada(dataPost);

        StorageReference storageReference = ConfiguracaoFirebase.getStorageReference().child("Video")
                .child("videoPostado")
                .child(video.getIdVideo()+".mp4");

        uploadTaskPostagem = storageReference.putFile(fileUri);

        uploadTaskPostagem.continueWithTask(new Continuation() {
            @Override
            public Object then(@NonNull Task task) throws Exception {
                if (!task.isSuccessful()){
                    throw task.getException();
                }
                return storageReference.getDownloadUrl();
            }
        }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {

            if(task.isSuccessful()){
                videoUri = task.getResult();
                String url = videoUri.toString();

                video.setVideoPostado(url);
                video.setUsuario(usuarioLogado);

                //salvando a foto no banco de dados
                if (video.salvarVideoPostada()){

                    video.salvarVideoFireStore();
                    idVideo = video.getIdVideo();

                    //caso foto tenha sido postada com sucesso, atualizar numero de fotos postada
                    dialog.dismiss();
                    Toast.makeText(VideoPostActivity.this, "Video postado com sucesso!", Toast.LENGTH_SHORT).show();
                    //startActivity(new Intent(VideoPostActivity.this, StartActivity.class));
                    //finish();
                }else{
                    Toast.makeText(this, "Erro ao postar video", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });

    }

    public void playVideo(View view) {
// Recupera a referência do database diretamente no nó em que se encontra o link do vídeo
        if (idVideo != null) {
            DatabaseReference dbRef = database.getReference().child("Video").child(idVideo);
            dbRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
//Recupera o valor do nó no RealtimeDatabase e converte em String
                    //   String url = dataSnapshot.getValue().toString();
                    Video video = dataSnapshot.getValue(Video.class);
                    videoUri = Uri.parse(video.getVideoPostado());
                    videoView.setVideoURI(videoUri);
                    videoView.setMediaController(new MediaController(
                            VideoPostActivity.this
                    ));
                    videoView.requestFocus();
                    videoView.start();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }

            });
        }else{
            Snackbar.make(view,"Selecione um video.",Snackbar.LENGTH_LONG).setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE).show();
        }
    }
}