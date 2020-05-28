package br.com.petgramapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.ViewHolder;

import java.util.ArrayList;
import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.helper.UsuarioFirebase;
import br.com.petgramapp.model.Mensagem;
import br.com.petgramapp.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    List<DocumentChange> documentChanges = new ArrayList<>();
    private GroupAdapter adapterMensagemEnviada;
    private Usuario usuario;
    private EditText mensagemDigitada;
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference usuarioCollection;
    private Usuario usuarioLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        RecyclerView recyclerViewMensagemEnviada = findViewById(R.id.recyclerView_mensagem_Chat);
        ImageButton botaoEnviarMensagem = findViewById(R.id.enviarBotao_Mensagem_Chat);
        mensagemDigitada = findViewById(R.id.mensagem_Digitada_Chat);
        firebaseFirestore = ConfiguracaoFirebase.getFirebaseFirestore();


        usuario = getIntent().getExtras().getParcelable("users");
        if (usuario != null){
            getSupportActionBar().setTitle(usuario.getNomePetUsuario());
        }


        adapterMensagemEnviada = new GroupAdapter();
        recyclerViewMensagemEnviada.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMensagemEnviada.setAdapter(adapterMensagemEnviada);

        //recuperar dados usuario logado
        FirebaseUser firebaseUser = UsuarioFirebase.getUsuarioAtual();
        firebaseFirestore.collection("Usuarios").document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        usuarioLogado = documentSnapshot.toObject(Usuario.class);
                        fetchMensagens();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });


        botaoEnviarMensagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarMensagem();
            }
        });


    }

    private void fetchMensagens() {
        if (usuarioLogado != null){
            String enviadoDeId = usuarioLogado.getId();
            String enviadoParaId = usuario.getId();

            //Lista de mensagens para quem eu estou enviando as mensagens
            firebaseFirestore.collection("Chat")
                    .document(enviadoDeId)
                    .collection(enviadoParaId)
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            //RECUPERA AS MUDANÇAS NOS DOCUMENTOS

                            documentChanges = queryDocumentSnapshots.getDocumentChanges();

                            if (documentChanges != null){
                                for (DocumentChange doc: documentChanges){
                                    //pra cada mudança no objeto, transformar o documento num objeto tipo mensagem
                                    if (doc.getType() == DocumentChange.Type.ADDED){
                                        // verifica o tipo dele se foi ADD OU MODIFIED OU REMOVED
                                         Mensagem mensagem = doc.getDocument().toObject(Mensagem.class);
                                         adapterMensagemEnviada.add(new AdapterMensagemEnviada(mensagem));
                                    }
                                }

                                adapterMensagemEnviada.notifyDataSetChanged();
                            }
                        }
                    });
        }
    }

    private void enviarMensagem() {
        String mensagem = mensagemDigitada.getText().toString();
        mensagemDigitada.setText("");

        String enviadoDeId = UsuarioFirebase.getIdentificadorUsuario();
        String enviadoParaId = usuario.getId();
        long timeStamp = System.currentTimeMillis();

        Mensagem mensagemObj = new Mensagem();
        mensagemObj.setIdEnviadoDe(enviadoDeId);
        mensagemObj.setIdEnviadoPara(enviadoParaId);
        mensagemObj.setTimestamp(timeStamp);
        mensagemObj.setMensagem(mensagem);

        if (!mensagemObj.getMensagem().isEmpty()){
            mensagemObj.enviarMensagem(enviadoDeId,enviadoParaId,usuario,mensagemObj,usuarioLogado);

        }
    }


    public class AdapterMensagemEnviada extends Item {

        FirebaseUser usuarioAtual = UsuarioFirebase.getUsuarioAtual();
        private Mensagem mensagem;

        public AdapterMensagemEnviada(Mensagem mensagem) {
            this.mensagem = mensagem;
        }

        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {
            TextView mensagemEnviada = viewHolder.itemView.findViewById(R.id.mensagemEnviada_Mensagem);
            CircleImageView fotoUsuarioMensagemEnviado = viewHolder.itemView.findViewById(R.id.fotoUsuario_enviado_Chat);

    if (mensagem.getIdEnviadoDe().equals(usuarioAtual.getUid())){

        if (usuarioLogado != null && usuarioLogado.getUriCaminhoFotoPetUsuario() != null){
            Picasso.get().load(usuarioLogado.getUriCaminhoFotoPetUsuario()).priority(Picasso.Priority.HIGH).into(fotoUsuarioMensagemEnviado);
        }else{
            Picasso.get().load(R.drawable.cadastroimage).priority(Picasso.Priority.HIGH).into(fotoUsuarioMensagemEnviado);
        }

    mensagemEnviada.setText(mensagem.getMensagem());

        }else{

            if (usuario != null && usuario.getUriCaminhoFotoPetUsuario() != null){
                Picasso.get().load(usuario.getUriCaminhoFotoPetUsuario()).priority(Picasso.Priority.HIGH).into(fotoUsuarioMensagemEnviado);
            }else{
                Picasso.get().load(R.drawable.cadastroimage).priority(Picasso.Priority.HIGH).into(fotoUsuarioMensagemEnviado);
            }

            mensagemEnviada.setText(mensagem.getMensagem());
        }
}

        @Override
        public int getLayout() {
            return mensagem.getIdEnviadoDe().equals(usuarioAtual.getUid()) ? R.layout.adapter_mensagem_enviada : R.layout.adapter_mensagem_recebida;
        }

    }
}
