package br.com.petgramapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.adapter.AdapterContatosChat;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.helper.UsuarioFirebase;
import br.com.petgramapp.model.Usuario;

public class ContatosActivity extends AppCompatActivity {
    List<DocumentSnapshot> doc = new ArrayList<>();
    private FirebaseFirestore firebaseFirestore;
    private GroupAdapter adapterContatos;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contatos);
        firebaseFirestore = ConfiguracaoFirebase.getFirebaseFirestore();

        RecyclerView recyclerViewContatos = findViewById(R.id.recyclerView_Contatos);
        adapterContatos = new GroupAdapter<>();
        recyclerViewContatos.setAdapter(adapterContatos);
        recyclerViewContatos.setLayoutManager(new LinearLayoutManager(this));

        adapterContatos.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull Item item, @NonNull View view) {
               Intent intent = new Intent(ContatosActivity.this,ChatActivity.class);
                AdapterContatosChat userItem = (AdapterContatosChat) item;
                intent.putExtra("users",userItem.usuario);
                startActivity(intent);
            }
        });

        fetchUsers();
    }

    private void fetchUsers() {
        Query query = firebaseFirestore.collection("Usuarios");
   //   firebaseFirestore.collection("Usuarios")
        query.orderBy("nomePetUsuario", Query.Direction.ASCENDING)
              .addSnapshotListener(this,new EventListener<QuerySnapshot>() {
                  @Override
                  public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                      if (e != null){
                          Log.e("ErrodocumentSnapshot",e.getMessage(),e);
                          return;
                      }

                      doc.clear();

                      doc = queryDocumentSnapshots.getDocuments();
                      adapterContatos.clear();
                      for (DocumentSnapshot ds: doc){
                          Usuario usuario = ds.toObject(Usuario.class);

                          if (usuario.getId().equals(UsuarioFirebase.getIdentificadorUsuario()))
                              continue;

                          adapterContatos.add(new AdapterContatosChat(usuario));
                          adapterContatos.notifyDataSetChanged();
                      }

                      adapterContatos.notifyDataSetChanged();
                  }

              });

    }


}
