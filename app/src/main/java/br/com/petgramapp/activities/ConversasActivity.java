package br.com.petgramapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.OnItemClickListener;
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
    private Contact contactGeral;

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

        adapterConversas.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull Item item, @NonNull View view) {
                Intent intent = new Intent(ConversasActivity.this,ChatActivity.class);
                AdapterContact adapterContact = (AdapterContact) item;
                intent.putExtra("users",adapterContact.usuario);
                startActivity(intent);
            }
        });


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
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                       // adapterConversas.clear();
                        documentChangeList = queryDocumentSnapshots.getDocumentChanges();

                        if (documentChangeList != null){
                            for (DocumentChange doc: documentChangeList){
                                if (doc.getType() == DocumentChange.Type.ADDED){
                                    Contact contact = doc.getDocument().toObject(Contact.class);
                                    if (contact.getIdUsuario().equals(UsuarioFirebase.getIdentificadorUsuario()))
                                        continue;
                                    adapterConversas.add(new AdapterContact(contact,ConversasActivity.this));
                                }
                                adapterConversas.notifyDataSetChanged();
                            }

                            adapterConversas.notifyDataSetChanged();
                        }

                    }
                });

    }

    private void fetchLastMensagemContatos() {
        FirebaseUser firebaseUser = UsuarioFirebase.getUsuarioAtual();
        firebaseFirestore.collection("LastMensagens")
                .document(firebaseUser.getUid())
                .collection("contatos")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        // adapterConversas.clear();
                        documentChangeList = queryDocumentSnapshots.getDocumentChanges();

                        if (documentChangeList != null){
                            for (DocumentChange doc: documentChangeList){
                                if (doc.getType() == DocumentChange.Type.ADDED){
                                    Contact contact = doc.getDocument().toObject(Contact.class);
                                    if (contact.getIdUsuario().equals(UsuarioFirebase.getIdentificadorUsuario()))
                                        continue;
                                    adapterConversas.add(new AdapterContact(contact,ConversasActivity.this));
                                }
                                adapterConversas.notifyDataSetChanged();
                            }

                            adapterConversas.notifyDataSetChanged();
                        }

                    }
                });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal_conversas,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.contatos_menuContatoPrincipal){
            Intent intent = new Intent(this,ContatosActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private static class AdapterContact extends Item<ViewHolder>{
        public Contact contact;
        public Context context;
        public Usuario usuario;
        public FirebaseFirestore firebaseFirestore;

        public AdapterContact(Contact contact) {
            this.contact = contact;
        }

        public AdapterContact(Contact contact, Context context,Usuario usuario) {
            this.contact = contact;
            this.context = context;
            this.usuario = usuario;
        }

        public AdapterContact(Contact contact, Context context) {
            this.contact = contact;
            this.context = context;

        }

        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {
            TextView nomeUsuario = viewHolder.itemView.findViewById(R.id.nome_perfil_ConversaAdapter);
            TextView ultimaMensagem = viewHolder.itemView.findViewById(R.id.ultimaMensagem_ConversaAdapter);
            CircleImageView fotoUsuario = viewHolder.itemView.findViewById(R.id.imagem_perfil_ConversaAdapter);
            firebaseFirestore = ConfiguracaoFirebase.getFirebaseFirestore();

            nomeUsuario.setText(contact.getNomeUsuario());
            ultimaMensagem.setText(contact.getLastMensagem());

            Picasso.get().load(contact.getUriCaminhoFotoPetUsuario()).priority(Picasso.Priority.HIGH).into(fotoUsuario);

         nomeUsuario.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context,ChatActivity.class);
                   /* Usuario usuario = new Usuario();
                    usuario.setNomePetUsuario(contact.getNomeUsuario());
                    usuario.setId(contact.getIdUsuario());
                    usuario.setUriCaminhoFotoPetUsuario(contact.getUriCaminhoFotoPetUsuario());*/
                    firebaseFirestore.collection("Usuarios")
                            .document(contact.getIdUsuario())
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    Usuario usuario = documentSnapshot.toObject(Usuario.class);
                                    Log.i("TokenCollection",usuario.getToken());
                                    i.putExtra("users",usuario);
                                    context.startActivity(i);
                                }
                            });

                }
            });


            ultimaMensagem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context,ChatActivity.class);
                     Usuario usuario = new Usuario();
                   /* usuario.setNomePetUsuario(contact.getNomeUsuario());
                    usuario.setId(contact.getIdUsuario());
                    usuario.setUriCaminhoFotoPetUsuario(contact.getUriCaminhoFotoPetUsuario());
                    String tokenCollection = "";*/
                    firebaseFirestore.collection("Usuarios")
                            .document(contact.getIdUsuario())
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    Usuario usuario = documentSnapshot.toObject(Usuario.class);
                                    Log.i("TokenCollection",usuario.getToken());
                                    i.putExtra("users",usuario);
                                    context.startActivity(i);
                                }
                            });

                }
            });

            fotoUsuario.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context,ChatActivity.class);
                    firebaseFirestore.collection("Usuarios")
                            .document(contact.getIdUsuario())
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    Usuario usuario = documentSnapshot.toObject(Usuario.class);
                                    Log.i("TokenCollection",usuario.getToken());
                                    i.putExtra("users",usuario);
                                    context.startActivity(i);
                                }
                            });
                }
            });


        }

        @Override
        public int getLayout() {
            return R.layout.adapter_conversas_users ;
        }
    }

}
