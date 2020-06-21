package br.com.petgramapp.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.adapter.AdapterGrupoContatosSelecionadosNormalJam;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.helper.UsuarioFirebase;
import br.com.petgramapp.model.GrupoJam;
import br.com.petgramapp.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class CadastroGrupoConversasJam extends AppCompatActivity {

    //FIREBASE
    FirebaseUser usuarioFirebase;
    StorageReference storageReference;
    DatabaseReference reference;
    FirebaseFirestore firebaseFirestore = ConfiguracaoFirebase.getFirebaseFirestore();
    private List<Usuario> membrosSelecionadosList = new ArrayList<>();
    private FloatingActionButton fabCadastroGrupo;
    private TextView participantesCadastroGrupo;
    private CircleImageView imagemPerfilCadastroGrupo;
    private TextInputEditText nomeGrupoCadastroGrupo;
    private RecyclerView recyclerViewCadastroGrupo;
    private AdapterGrupoContatosSelecionadosNormalJam adapterGrupoContatosSelecionadosNormalJam;
    private StorageTask uploadFotoTask;
    private String identificadorUsuario;
    private String tokenId;
    private Uri imagemFotoUri;
    private GrupoJam grupoJam;
    private List<Usuario> membrosRecebidos = new ArrayList<>();
    private List<Usuario> usuarioUnicoList = new ArrayList<>();


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_grupo_conversas_jam);
        Toolbar toolbarCadastroGrupoConversas = findViewById(R.id.toolbar_CadastroGrupoConversasJam);
        toolbarCadastroGrupoConversas.setTitle("Novo Grupo");
        toolbarCadastroGrupoConversas.setSubtitle("Defina o nome do seu grupo");
        setSupportActionBar(toolbarCadastroGrupoConversas);
        loadElementos();

        //recuperar lista com usuarios selecionados
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            membrosRecebidos = (List<Usuario>) bundle.getSerializable("membros");
            membrosSelecionadosList.addAll(membrosRecebidos);
            participantesCadastroGrupo.setText("Número de integrantes: " + membrosSelecionadosList.size());
        }

        imagemPerfilCadastroGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().setAspectRatio(200, 200)
                        .setCropShape(CropImageView.CropShape.OVAL).start(CadastroGrupoConversasJam.this);

            }
        });

        //SALVAR GRUPO
        fabCadastroGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (nomeGrupoCadastroGrupo.getText() != null
                        && !nomeGrupoCadastroGrupo.getText().toString().equalsIgnoreCase("")) {

                    String nomeGrupo = nomeGrupoCadastroGrupo.getText().toString();
                    grupoJam.setNomeGrupo(nomeGrupo);

                    //para adicionar a lista, o membro admin
                    if (usuarioUnicoList != null) {
                        membrosSelecionadosList.add(usuarioUnicoList.get(0));
                    } else {
                        membrosSelecionadosList.add(UsuarioFirebase.getUsuarioLogado());
                    }

                    grupoJam.setIdAdminGrupo(identificadorUsuario);

                    if (membrosSelecionadosList != null && membrosSelecionadosList.size() > 0) {
                        grupoJam.setMembrosGrupo(membrosSelecionadosList);
                    }

                    try {
                        grupoJam.salvarGrupoFirebase();
                    } catch (Exception e) {
                        Log.e("ExceptionSalvarGrupo", e.getMessage());
                    }

                    try {
                        grupoJam.salvarGrupoFirestore();
                    } catch (Exception e) {
                        Log.e("ExceptionSalvarGrupo", e.getMessage());
                    }

                    nomeGrupoCadastroGrupo.setText("");
                    Snackbar.make(view, "Grupo " + nomeGrupo + " salvo com sucesso.", Snackbar.LENGTH_SHORT).show();
                   // finish();

                    //enviar usuario para tela de conversa em grupo
                    Intent i = new Intent(CadastroGrupoConversasJam.this, TalksJamActivity.class);
                    i.putExtra("chatGrupo", (Parcelable) grupoJam);
                    i.putExtra("chatUsuarioLogado", usuarioUnicoList.get(0));
                    startActivity(i);
                } else {
                    Snackbar.make(view, "Digite o nome do seu grupo", Snackbar.LENGTH_SHORT).show();
                }

            }
        });

        //recycler
        loadRecycler(recyclerViewCadastroGrupo);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    public void loadRecycler(RecyclerView recyclerView) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CadastroGrupoConversasJam.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapterGrupoContatosSelecionadosNormalJam = new AdapterGrupoContatosSelecionadosNormalJam(CadastroGrupoConversasJam.this, membrosSelecionadosList);

        Collections.sort(membrosSelecionadosList, new Comparator<Usuario>() {
            @Override
            public int compare(Usuario o1, Usuario o2) {
                return o1.getNomePetUsuario().compareToIgnoreCase(o2.getNomePetUsuario());
            }
        });

        recyclerView.setAdapter(adapterGrupoContatosSelecionadosNormalJam);

    }

    @Override
    protected void onStart() {
        super.onStart();
        //recuperar o usuario logado
        recuperarContatoAtual();
    }

    private void loadElementos() {

        fabCadastroGrupo = findViewById(R.id.fab_CadastroGrupoJam);
        participantesCadastroGrupo = findViewById(R.id.numeroParticipantes_CadastroGrupoJam);
        imagemPerfilCadastroGrupo = findViewById(R.id.imagemPerfil_CadastroGrupoJam);
        nomeGrupoCadastroGrupo = findViewById(R.id.nomeGrupo_TextInputEditText_CadastroGrupoJam);
        recyclerViewCadastroGrupo = findViewById(R.id.recyclerView_MembrosCadastroGrupoJam);
        usuarioFirebase = UsuarioFirebase.getUsuarioAtual();
        identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();
        reference = ConfiguracaoFirebase.getReferenciaDatabase();
        grupoJam = new GrupoJam();
        storageReference = ConfiguracaoFirebase.getStorageReference().
                child("Imagens").
                child("FotoPerfilGrupo").
                child(grupoJam.getIdGrupo());


    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, GrupoContatosJam.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    //METODO PARA REALIZAR UPLOAD DE IMAGEM DE GRUPO
    private void uploadImagemPerfil() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle("Alterando");
        dialog.setMessage("Alterando sua PetFoto");
        dialog.setIcon(R.drawable.ic_pets_black_24dp);
        dialog.setCancelable(false);
        dialog.show();

        if (imagemFotoUri != null) {

            StorageReference arquivoRef = storageReference.
                    child(grupoJam.getIdGrupo() + ".jpg");

            Bitmap bmp = null;
            try {
                bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), imagemFotoUri);
                imagemPerfilCadastroGrupo.setImageBitmap(bmp);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.WEBP, 30, baos);
            byte[] data = baos.toByteArray();

            uploadFotoTask = arquivoRef.putBytes(data);

            uploadFotoTask.continueWithTask((Continuation) task -> {
                if (task.isSuccessful()) {

                } else {
                    throw task.getException();
                }
                return arquivoRef.getDownloadUrl();
            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {


                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    String urlResultado = downloadUri.toString();

                    grupoJam.setFotoGrupo(urlResultado);

                    dialog.dismiss();

                } else {
                    dialog.dismiss();
                    Toast.makeText(this, "Nenhuma imagem selecionada!", Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "" + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            });
        } else {
            Toast.makeText(this, "Nenhuma imagem selecionada!", Toast.LENGTH_LONG).show();
        }
    }

    //METODO PARA RECUPERAR O RESULT DA OPÇÃO SELECIONADA NA FOTO
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult activityResult = CropImage.getActivityResult(data);
            imagemFotoUri = activityResult.getUri();
            uploadImagemPerfil();
        } else {
            Toast.makeText(this, "Eita", Toast.LENGTH_LONG).show();
        }
    }

    //para recuperar o usuario logado
    public void recuperarContatoAtual() {

        FirebaseFirestore firebaseFirestore = ConfiguracaoFirebase.getFirebaseFirestore();
        CollectionReference usuarioCollection = firebaseFirestore.collection("Usuarios");
        usuarioCollection.document(UsuarioFirebase.getIdentificadorUsuario())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                usuarioUnicoList.clear();
                Usuario usuario = documentSnapshot.toObject(Usuario.class);
                usuarioUnicoList.add(usuario);
            }

        });
    }

}
