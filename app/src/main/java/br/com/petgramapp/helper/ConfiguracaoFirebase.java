package br.com.petgramapp.helper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ConfiguracaoFirebase {
    private static DatabaseReference referenciaDatabase;
    private static FirebaseFirestore firebaseFirestore;
    private static FirebaseAuth referenciaAuth;
    private static StorageReference storageReference;

    public ConfiguracaoFirebase() {
    }

    public static FirebaseFirestore getFirebaseFirestore(){
        if (firebaseFirestore == null){
            firebaseFirestore = FirebaseFirestore.getInstance();
        }

        return firebaseFirestore;
    }

    public static FirebaseAuth getFirebaseAutenticacao(){
        //caso a autenticação esteja nula
        if (referenciaAuth == null){
            referenciaAuth = FirebaseAuth.getInstance();
        }
        return referenciaAuth;
    }

    public static DatabaseReference getReferenciaDatabase(){
        if (referenciaDatabase == null){
            referenciaDatabase = FirebaseDatabase.getInstance().getReference();
        }

        return referenciaDatabase;
    }

    public static StorageReference getStorageReference(){
        if (storageReference == null){
            storageReference = FirebaseStorage.getInstance().getReference();
        }
        return storageReference;
    }
}
