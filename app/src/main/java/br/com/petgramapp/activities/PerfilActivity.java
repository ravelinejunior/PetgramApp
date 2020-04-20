package br.com.petgramapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import br.com.petgramapp.R;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.helper.UsuarioFirebase;
import br.com.petgramapp.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class PerfilActivity extends AppCompatActivity {

    //WIDGETS
    private Button salvarEdicaoPerfil;
    private TextView alterarEdicaoPerfil;
    private MaterialEditText nomePetEdicaoPerfil;
    private MaterialEditText nomeDonoEdicaoPerfil;
    private MaterialEditText descricaoEdicaoPerfil;
    private CircleImageView fotoPerfilEdicaoPerfil;
    private CircleImageView fecharEdicaoPerfil;

    //FIREBASE
    FirebaseUser usuarioFirebase;
    Uri imagemFotoUri;
    private StorageTask uploadFotoTask;
    StorageReference storageReference;
    DatabaseReference reference;
    DatabaseReference usuariosRef;
    private String identificadorUsuario;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        //configurações iniciais
        carregarElementos();

    }

    public void carregarElementos(){
        fecharEdicaoPerfil = findViewById(R.id.fechar_PerfilActivity_id);
        salvarEdicaoPerfil = findViewById(R.id.salvar_PerfilActivity_id);
        alterarEdicaoPerfil = findViewById(R.id.alterarFoto_UsuarioPerfil_Activity);
        nomeDonoEdicaoPerfil = findViewById(R.id.nomeDonoPet_Perfil_Activity);
        nomePetEdicaoPerfil = findViewById(R.id.nomePet_Perfil_Activity);
        descricaoEdicaoPerfil = findViewById(R.id.descricaoPet_Perfil_Activity);
        fotoPerfilEdicaoPerfil = findViewById(R.id.imagem_fotoPerfil_Activity);

        //firebase
        usuarioFirebase = UsuarioFirebase.getUsuarioAtual();
        identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();
        reference = ConfiguracaoFirebase.getReferenciaDatabase();
        storageReference = ConfiguracaoFirebase.getStorageReference().child("Imagens").child("FotoPerfil").child(usuarioFirebase.getUid());
        informacoesUsuario();
        onClicks();

    }

  public void  onClicks(){

      fecharEdicaoPerfil.setOnClickListener(v -> finish());

      salvarEdicaoPerfil.setOnClickListener(v ->{

          String nomePet = nomePetEdicaoPerfil.getText().toString();
          String nomeDonoPet = nomeDonoEdicaoPerfil.getText().toString();
          String descricaoPerfil = descricaoEdicaoPerfil.getText().toString();
              atualizarPerfil(nomePet,nomeDonoPet,descricaoPerfil,v);

              salvarEdicaoPerfil.clearFocus();

      });

      alterarEdicaoPerfil.setOnClickListener(v -> {
          CropImage.activity().setAspectRatio(100,100)
              .setCropShape(CropImageView.CropShape.OVAL).start(PerfilActivity.this);


      });

      fotoPerfilEdicaoPerfil.setOnClickListener(v ->{ CropImage.activity().setAspectRatio(100,100)
              .setCropShape(CropImageView.CropShape.OVAL).start(PerfilActivity.this);

      });
    }


    private void atualizarPerfil(String nomePet, String nomeDonoPet, String descricaoPerfil,View view) {
        usuariosRef = reference.child("usuarios").child(usuarioFirebase.getUid());

        HashMap<String,Object> hashMap = new HashMap<>();
        if ((nomeDonoPet != null || !nomeDonoPet.equals("")) ||
                ((nomePet != null || !nomePet.equals(""))) ||
                (descricaoPerfil != null || !descricaoPerfil.equals(""))) {
            //#TODO 1: CRIAR UM NÓ DE NOME DONO DO PET NO BANCO DE DADOS
            //hashMap.put("nomeDonoPet",nomeDonoPet);
            hashMap.put("nomePetUsuario", nomePet);
            hashMap.put("nomePetUsuarioUp", nomeDonoPet); // alterar esse aqui
            hashMap.put("descricaoPetUsuario", descricaoPerfil);

            usuariosRef.updateChildren(hashMap);
            Snackbar.make(view,"Dados atualizados com sucesso!",Snackbar.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Nada a atualizar.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }

    private void uploadImagemPerfil(){
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle("Alterando");
        dialog.setMessage("Alterando sua PetFoto");
        dialog.setIcon(R.drawable.ic_pets_black_24dp);
        dialog.setCancelable(false);
        dialog.show();

        if (imagemFotoUri != null){
           // StorageReference arquivoRef = storageReference.child(System.currentTimeMillis()+"."+getFileExtension(imagemFotoUri));

            StorageReference arquivoRef = storageReference.
                    child( identificadorUsuario+"."+getFileExtension(imagemFotoUri));
            uploadFotoTask = arquivoRef.putFile(imagemFotoUri);
            uploadFotoTask.continueWithTask((Continuation) task -> {
                if (task.isSuccessful()){

                }else{
                    throw task.getException();
                }
                return arquivoRef.getDownloadUrl();
            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {


                if (task.isSuccessful()){
                    Uri downloadUri =  task.getResult();
                    String urlResultado = downloadUri.toString();
                    DatabaseReference usuariosRef = reference.child("usuarios").child(usuarioFirebase.getUid());
                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("uriCaminhoFotoPetUsuario",""+urlResultado);
                    usuariosRef.updateChildren(hashMap);
                    dialog.dismiss();
                    startActivity(new Intent(this,PerfilActivity.class));
                    finish();
                }else{
                    dialog.dismiss();
                    Toast.makeText(this, "Erro ao alterar sua petFoto. Verifique sua conexão com a internet!", Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, ""+e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            });
        }else{
            Toast.makeText(this, "Nenhuma imagem selecionada!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult activityResult = CropImage.getActivityResult(data);
            imagemFotoUri = activityResult.getUri();
            uploadImagemPerfil();
        }else{
            Toast.makeText(this, "Algo deu errado! Tente mais tarde ou verifique sua internet;", Toast.LENGTH_LONG).show();
        }
    }

    public void informacoesUsuario() {
        DatabaseReference usuariosRef = reference.child("usuarios").child(usuarioFirebase.getUid());
        usuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);
                nomeDonoEdicaoPerfil.setText(usuario.getNomePetUsuarioUp());
                nomePetEdicaoPerfil.setText(usuario.getNomePetUsuario());
                descricaoEdicaoPerfil.setText(usuario.getDescricaoPetUsuario());
                Picasso.get().load(usuario.getUriCaminhoFotoPetUsuario()).into(fotoPerfilEdicaoPerfil);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
}
















