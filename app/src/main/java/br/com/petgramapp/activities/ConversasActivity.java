package br.com.petgramapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.ViewHolder;

import java.util.ArrayList;
import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.helper.UsuarioFirebase;
import br.com.petgramapp.model.Contact;
import br.com.petgramapp.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class ConversasActivity extends AppCompatActivity {
    List<DocumentChange> documentChangeList = new ArrayList<>();
    private FirebaseFirestore firebaseFirestore;
    private GroupAdapter adapterConversas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversas);
        ChatApplication application = (ChatApplication) getApplication();

        getApplication().registerActivityLifecycleCallbacks(application);

        firebaseFirestore = ConfiguracaoFirebase.getFirebaseFirestore();
        RecyclerView recyclerViewConversas = findViewById(R.id.recyclerView_Conversas);
        recyclerViewConversas.setLayoutManager(new LinearLayoutManager(this));
        adapterConversas = new GroupAdapter();
        recyclerViewConversas.setAdapter(adapterConversas);
        
        updateToken();

        fetchLastMensagem();


    }

    private void updateToken() {
        String token = FirebaseInstanceId.getInstance().getToken();
        String idUsuario = UsuarioFirebase.getIdentificadorUsuario();
        Log.i("TokenUser",token);

        if (idUsuario != null){
            firebaseFirestore.collection("Usuarios")
                    .document(idUsuario)
                    .update("token",token);

        }
    }

    private void fetchLastMensagem() {
        FirebaseUser firebaseUser = UsuarioFirebase.getUsuarioAtual();
        firebaseFirestore.collection("LastMensagens")
                .document(firebaseUser.getUid())
                .collection("contatos")
                .addSnapshotListener(this,new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        documentChangeList = queryDocumentSnapshots.getDocumentChanges();

                        if (documentChangeList != null){
                            for (DocumentChange doc: documentChangeList){
                                if (doc.getType() == DocumentChange.Type.ADDED){
                                    Contact contact = doc.getDocument().toObject(Contact.class);
                                    if (contact.getIdUsuario().equals(UsuarioFirebase.getIdentificadorUsuario()))
                                        continue;
                                    adapterConversas.add(new AdapterContact(contact,ConversasActivity.this));
                                }
                            }

                            adapterConversas.notifyDataSetChanged();
                        }

                    }
                });

    }

    private static class AdapterContact extends Item<ViewHolder>{
        public Contact contact;
        public Context context;

        public AdapterContact(Contact contact) {
            this.contact = contact;
        }

        public AdapterContact(Contact contact, Context context) {
            this.contact = contact;
            this.context = context;
        }

        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {
            String token = FirebaseInstanceId.getInstance().getToken();
            Log.i("TokenAdapter",token);
            TextView nomeUsuario = viewHolder.itemView.findViewById(R.id.nome_perfil_ConversaAdapter);
            TextView ultimaMensagem = viewHolder.itemView.findViewById(R.id.ultimaMensagem_ConversaAdapter);
            CircleImageView fotoUsuario = viewHolder.itemView.findViewById(R.id.imagem_perfil_ConversaAdapter);

            nomeUsuario.setText(contact.getNomeUsuario());
            ultimaMensagem.setText(contact.getLastMensagem());

            Picasso.get().load(contact.getUriCaminhoFotoPetUsuario()).priority(Picasso.Priority.HIGH).into(fotoUsuario);

            nomeUsuario.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context,ChatActivity.class);
                    Usuario usuario = new Usuario();
                    usuario.setNomePetUsuario(contact.getNomeUsuario());
                    usuario.setId(contact.getIdUsuario());
                    usuario.setUriCaminhoFotoPetUsuario(contact.getUriCaminhoFotoPetUsuario());
                    usuario.setToken(token);
                    i.putExtra("users",usuario);
                    context.startActivity(i);
                }
            });

            ultimaMensagem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context,ChatActivity.class);
                    Usuario usuario = new Usuario();
                    usuario.setNomePetUsuario(contact.getNomeUsuario());
                    usuario.setId(contact.getIdUsuario());
                    usuario.setUriCaminhoFotoPetUsuario(contact.getUriCaminhoFotoPetUsuario());
                    usuario.setToken(token);

                    i.putExtra("users",usuario);
                    context.startActivity(i);
                }
            });


        }

        @Override
        public int getLayout() {
            return R.layout.adapter_conversas_users ;
        }
    }
}
