package br.com.petgramapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import br.com.petgramapp.R;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.helper.UsuarioFirebase;

public class AddStoriesActivity extends AppCompatActivity {

    private Uri imagemUri;
    String storyUrl = "";
    private StorageTask storageTask;
    private StorageReference storageReference;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_stories);
        storageReference = ConfiguracaoFirebase.getStorageReference().child("Stories");
        reference = ConfiguracaoFirebase.getReferenciaDatabase();
        CropImage.activity().setAspectRatio(100,100).start(AddStoriesActivity.this);

    }

    private void publicarStory(){
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle("Postando PetStory");
        dialog.setMessage("Aguarde...");
        dialog.setIcon(R.drawable.ic_pets_black_24dp);
        dialog.show();
        if (imagemUri != null){
            StorageReference imageReference = storageReference.child(System.currentTimeMillis()+".jpeg");
            storageTask = imageReference.putFile(imagemUri);
            storageTask.continueWithTask((Continuation) task -> {
                if (!task.isSuccessful()){
                    throw task.getException();
                }
                return imageReference.getDownloadUrl();
            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {

                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    storyUrl = downloadUri.toString();

                    String idUsuario = UsuarioFirebase.getUsuarioAtual().getUid();
                    DatabaseReference storiesRef = reference.child("Stories").child(idUsuario);

                    String idStories = storiesRef.push().getKey();
                    long dataFim = System.currentTimeMillis() + 86400000;
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("urlStoriesFoto", storyUrl);
                    hashMap.put("dataInicio", ServerValue.TIMESTAMP);
                    hashMap.put("dataFim", dataFim);
                    hashMap.put("idStories", idStories);
                    hashMap.put("idUsuario", idUsuario);
                    storiesRef.child(idStories).setValue(hashMap);
                    dialog.dismiss();
                    finish();
                }else{
                    Toast.makeText(AddStoriesActivity.this, "Verifique sua conexão com a Internet!", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> Toast.makeText(AddStoriesActivity.this, "Verifique sua conexão com a Internet! "+e.getMessage(), Toast.LENGTH_SHORT).show());
        }else{
            Toast.makeText(AddStoriesActivity.this, "Nenhuma PetImagem foi selecionada!", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imagemUri = result.getUri();
            publicarStory();
        }else{
            Toast.makeText(AddStoriesActivity.this, "Verifique sua Internet para postar mais petStories!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AddStoriesActivity.this,StartActivity.class));
            finish();
        }
    }
}
