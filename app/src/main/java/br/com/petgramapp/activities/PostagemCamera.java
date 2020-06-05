package br.com.petgramapp.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import br.com.petgramapp.R;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.helper.UsuarioFirebase;
import br.com.petgramapp.model.FotoPostada;
import br.com.petgramapp.model.Usuario;

public class PostagemCamera extends AppCompatActivity {
    private Uri uriImagemPostagem;
    private String imagemUrlPostagem;
    private TextInputEditText descricaoInputTextPostagem;
    private ImageView imagemSelecionadaPostagem;

    // firebase
    StorageTask uploadTaskPostagem;
    StorageReference storageReferencePostagem;
    private DatabaseReference usuariosRef;
    private DatabaseReference usuarioLogadoRef;
    private DatabaseReference firebaseRef;
    ProgressDialog dialog;

    //usuarios
    private String idUsuarioLogado;
    private Usuario usuarioLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postagem_camera);
        carregarElementos();

        //CONFIGURAÇÕES INICIAIS
        firebaseRef = ConfiguracaoFirebase.getReferenciaDatabase();
        usuarioLogado = UsuarioFirebase.getUsuarioLogado();
        idUsuarioLogado = UsuarioFirebase.getIdentificadorUsuario();
        usuariosRef = ConfiguracaoFirebase.getReferenciaDatabase().child("usuarios");
        recuperarDadosPostagem();
        CropImage.activity().setAspectRatio(100,100).start(PostagemCamera.this);
    }

    public void carregarElementos(){
        //configurando toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_PostagemCamera);
        toolbar.setTitle("Postar PetFoto");
        toolbar.setTitleTextColor(ContextCompat.getColor(getApplicationContext(),R.color.branco));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_fechar);

        descricaoInputTextPostagem = findViewById(R.id.descricao_PostagemCamera);
        imagemSelecionadaPostagem = findViewById(R.id.imagemSelecionada_PostagemCamera);

    }

    @Override
    protected void onSaveInstanceState(Bundle oldInstanceState) {
        super.onSaveInstanceState(oldInstanceState);
        oldInstanceState.clear();
    }

    private void abrirDialogCarregamento(String descricao){
        dialog = new ProgressDialog(getContext());
        dialog.setTitle("Aguarde.");
        dialog.setMessage(descricao);
        dialog.setIcon(R.drawable.ic_pets_black_24dp);
        dialog.setCancelable(false);
        dialog.show();

    }

    private void recuperarDadosPostagem(){
        usuarioLogadoRef = usuariosRef.child(idUsuarioLogado);
        //assim que função é acionada, setar valor como true
        abrirDialogCarregamento("Carregando dados, aguarde!");
        usuarioLogadoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //recuperando valores de usuarios
                usuarioLogado = dataSnapshot.getValue(Usuario.class);
                dialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    public Context getContext(){
        return PostagemCamera.this;
    }

    private void uploadFotoPostagem(){
        abrirDialogCarregamento("Sua petFoto está sendo postada! Aguarde...");
        FotoPostada fotoPostada = new FotoPostada();

        fotoPostada.setIdUsuarioPostou(idUsuarioLogado);
        fotoPostada.setDescricaoImagemPostada(Objects.requireNonNull(descricaoInputTextPostagem.getText()).toString());

        //DATA POSTAGEM
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        String dataPost = "Publicado em ".concat(currentDate.concat(" às ").concat(currentTime));
        fotoPostada.setDataPostada(dataPost);

        if (uriImagemPostagem != null){
            StorageReference imagemRef = ConfiguracaoFirebase.getStorageReference().child("Imagens")
                    .child("fotoPostada")
                    .child(fotoPostada.getIdPostagem()+".jpeg");

            Bitmap bmp = null;
            try {
                bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), uriImagemPostagem);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.isMutable();
            bmp.compress(Bitmap.CompressFormat.WEBP, 20, baos);
            byte[] data = baos.toByteArray();

            //uploadTaskPostagem = imagemRef.putFile(uriImagemPostagem);

            uploadTaskPostagem = imagemRef.putBytes(data);

            uploadTaskPostagem.continueWithTask((Continuation) task -> {

                if (!task.isSuccessful()){
                    throw task.getException();
                }
                return imagemRef.getDownloadUrl();

            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {

                if (task.isSuccessful()) {
                    Uri downloadUriImagem = task.getResult();

                    imagemUrlPostagem = downloadUriImagem.toString();
                    fotoPostada.setImagemPostada(imagemUrlPostagem);
                    fotoPostada.setUsuario(usuarioLogado);

                    //salvando a foto no banco de dados
                    if (fotoPostada.salvarFotoPostada()){
                        //caso foto tenha sido postada com sucesso, atualizar numero de fotos postada
                        dialog.dismiss();
                        Toast.makeText(getContext(), "Foto postada com sucesso!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getContext(), StartActivity.class));
                        finish();

                    } else{
                        Toast.makeText(getContext(), "Erro ao postar foto. Verifique sua internet.", Toast.LENGTH_SHORT).show();
                    }

                    /*DatabaseReference postagemRef = firebaseRef.child("Posts");
                    String postId = postagemRef.push().getKey();

                    HashMap<String, Object> hashMap = new HashMap<>();

                    hashMap.put("idPostagem", postId);
                    hashMap.put("imagemPostada", imagemUrlPostagem);
                    hashMap.put("descricaoImagemPostada", descricaoInputTextPostagem.getText().toString());
                    hashMap.put("idUsuarioPostou", FirebaseAuth.getInstance().getCurrentUser().getUid());

                    postagemRef.child("idPostagem").setValue(hashMap);*/

                    //dialog.dismiss();

                  /*  startActivity(new Intent(getContext(), StartActivity.class));
                    finish();*/
                }else{
                    Toast.makeText(getContext(), "Algo deu errado. Verifique sua conexão com a internet!", Toast.LENGTH_LONG).show();
                }

            }).addOnFailureListener(e ->
                    Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show());
        }else{
            Toast.makeText(getContext(), "Nenhuma PetImagem selecionada", Toast.LENGTH_SHORT).show();
        }
    }

    //APOS CLICAR PARA SELECIONAR A FOTO

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            uriImagemPostagem = result.getUri();
            imagemSelecionadaPostagem.setImageURI(uriImagemPostagem);

        }else {
            Toast.makeText(this, "Algo deu errado. Verifique sua conexão com a Internet!", Toast.LENGTH_LONG).show();
        }

    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_filtros_postar,menu);
        return super.onCreateOptionsMenu(menu);
    }

    //definindo itens que foram selecionados

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.ic_salvar_postagem_menu_postar:
                uploadFotoPostagem();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }

}
