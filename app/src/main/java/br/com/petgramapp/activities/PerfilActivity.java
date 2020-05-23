package br.com.petgramapp.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    private String tokenId;

    FirebaseFirestore firebaseFirestore = ConfiguracaoFirebase.getFirebaseFirestore();



    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        //configurações iniciais
        carregarElementos();

    }

    @RequiresApi(api = Build.VERSION_CODES.P)
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

  @RequiresApi(api = Build.VERSION_CODES.P)
  public void  onClicks(){

      fecharEdicaoPerfil.setOnClickListener(v -> finish());

      salvarEdicaoPerfil.setOnClickListener(v ->{

          String nomePet = nomePetEdicaoPerfil.getText().toString();
          String nomeDonoPet = nomeDonoEdicaoPerfil.getText().toString();
          String descricaoPerfil = descricaoEdicaoPerfil.getText().toString();

              atualizarPerfil(nomePet,nomeDonoPet,descricaoPerfil,v);

              nomeDonoEdicaoPerfil.clearFocus();
              nomeDonoEdicaoPerfil.clearComposingText();

              nomePetEdicaoPerfil.clearFocus();
              nomePetEdicaoPerfil.clearComposingText();

              descricaoEdicaoPerfil.clearFocus();
              descricaoEdicaoPerfil.clearComposingText();

          InputMethodManager imm = (InputMethodManager) getSystemService(
                  Activity.INPUT_METHOD_SERVICE);
          imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

      });

      alterarEdicaoPerfil.setOnClickListener(v -> {
          CropImage.activity().setAspectRatio(100,100)
              .setCropShape(CropImageView.CropShape.OVAL).start(PerfilActivity.this);


      });

      fotoPerfilEdicaoPerfil.setOnClickListener(v ->{ CropImage.activity().setAspectRatio(100,100)
              .setCropShape(CropImageView.CropShape.OVAL).start(PerfilActivity.this);

      });
    }

    public String recuperarToken(){

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(instanceIdResult -> {
            String token = instanceIdResult.getToken();
            String id = instanceIdResult.getId();
            tokenId = token;
            Log.i("instanceIdResult","Token: "+token);
            Log.i("instanceIdResult","ID: "+id);


        });

        return tokenId;

    }

    private void atualizarPerfil(String nomePet, String nomeDonoPet, String descricaoPerfil, View view) {
        usuariosRef = reference.child("usuarios").child(usuarioFirebase.getUid());
        recuperarToken();
        HashMap<String,Object> hashMap = new HashMap<>();
        if ((nomeDonoPet != null || !nomeDonoPet.equals("")) ||
                (nomePet != null || !nomePet.equals("")) ||
                (descricaoPerfil != null || !descricaoPerfil.equals(""))){

            hashMap.put("nomePetUsuario", nomePet);
            hashMap.put("nomeDonoPet", nomeDonoPet);
            hashMap.put("descricaoPetUsuario", descricaoPerfil);
            hashMap.put("nomePetUsuarioUp",nomePet.toLowerCase());
            hashMap.put("tokenFoneMessage", tokenId);

            usuariosRef.updateChildren(hashMap);

            HashMap map = new HashMap();
            map.put("nomePetUsuario", nomePet);
            map.put("id", identificadorUsuario);
            map.put("nomeDonoPet", nomeDonoPet);
            map.put("descricaoPetUsuario", descricaoPerfil);
            map.put("nomePetUsuarioUp",nomePet.toLowerCase());
            map.put("emailPetUsuario",usuarioFirebase.getEmail());
            map.put("tokenFoneMessage", tokenId);

            firebaseFirestore.collection("Usuarios")
                    .document(identificadorUsuario)
                    .set(map);

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



            Bitmap bmp = null;
            try {
                bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), imagemFotoUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.WEBP, 10, baos);
            byte[] data = baos.toByteArray();

            uploadFotoTask = arquivoRef.putBytes(data);

            //uploadFotoTask = arquivoRef.putFile(imagemFotoUri);

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

                    firebaseFirestore.collection("Usuarios")
                            .document(identificadorUsuario)
                            .update("uriCaminhoFotoPetUsuario",urlResultado);



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
    public Context getContext(){
        return PerfilActivity.this;
    }

    public void informacoesUsuario() {
        DatabaseReference usuariosRef = reference.child("usuarios").child(usuarioFirebase.getUid());
        usuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);
                nomeDonoEdicaoPerfil.setText(usuario.getNomeDonoPet());
                nomePetEdicaoPerfil.setText(usuario.getNomePetUsuario());
                descricaoEdicaoPerfil.setText(usuario.getDescricaoPetUsuario());
                //Picasso.get().load(usuario.getUriCaminhoFotoPetUsuario()).into(fotoPerfilEdicaoPerfil);
                Glide.with(getContext()).load(usuario.getUriCaminhoFotoPetUsuario()).into(fotoPerfilEdicaoPerfil);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
}
















