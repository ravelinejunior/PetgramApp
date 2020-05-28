package br.com.petgramapp.model;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import br.com.petgramapp.helper.ConfiguracaoFirebase;

public class Mensagem {

    public FirebaseFirestore firebaseFirestore;
    private String mensagem;
    private String idEnviadoDe;
    private String idEnviadoPara;
    private Long timestamp;

    public Mensagem() {

    }

    public Mensagem(String mensagem, String idEnviadoDe, String idEnviadoPara, Long timestamp) {
        this.mensagem = mensagem;
        this.idEnviadoDe = idEnviadoDe;
        this.idEnviadoPara = idEnviadoPara;
        this.timestamp = timestamp;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getIdEnviadoDe() {
        return idEnviadoDe;
    }

    public void setIdEnviadoDe(String idEnviadoDe) {
        this.idEnviadoDe = idEnviadoDe;
    }

    public String getIdEnviadoPara() {
        return idEnviadoPara;
    }

    public void setIdEnviadoPara(String idEnviadoPara) {
        this.idEnviadoPara = idEnviadoPara;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void enviarMensagem(String mensagemQuemEnviouId,String mensagemQuemRecebeId,Usuario usuario,Mensagem mensagem,Usuario usuLogado ){
        firebaseFirestore = ConfiguracaoFirebase.getFirebaseFirestore();
        CollectionReference mensagemCollection = firebaseFirestore
                .collection("Chat")
                .document(mensagemQuemEnviouId)
                .collection(mensagemQuemRecebeId);

        Map<String,Object> map = new HashMap<>();
        map.put("mensagem",getMensagem());
        map.put("idEnviadoDe",getIdEnviadoDe());
        map.put("idEnviadoPara",getIdEnviadoPara());
        map.put("timestamp",getTimestamp());

        mensagemCollection.add(map).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.i("salvarMensagemEnviada","Id mensagem: "+documentReference.getId());
                Contact contato = new Contact();
                contato.setIdUsuario(idEnviadoPara);
                contato.setNomeUsuario(usuario.getNomePetUsuario());
                contato.setUriCaminhoFotoPetUsuario(usuario.getUriCaminhoFotoPetUsuario());
                contato.setTimestamp(mensagem.timestamp);
                contato.setLastMensagem(mensagem.mensagem);


                firebaseFirestore.collection("LastMensagens")
                        .document(idEnviadoDe)
                        .collection("contatos")
                        .document(idEnviadoPara)
                        .set(contato);

                if (!usuario.isOnline()){

                    NotificacaoChat notificacaoChat = new NotificacaoChat();
                    notificacaoChat.setFromName(usuLogado.getNomePetUsuario());
                    notificacaoChat.setIdEnviadoDe(mensagem.getIdEnviadoDe());
                    notificacaoChat.setIdEnviadoPara(mensagem.getIdEnviadoPara());
                    notificacaoChat.setTimestamp(mensagem.getTimestamp());
                    notificacaoChat.setMensagem(mensagem.getMensagem());

                    firebaseFirestore.collection("Notificacoes")
                            .document(usuario.getToken())
                            .set(notificacaoChat);

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("salvarMensagemEnviada","Erro salvar mensagem: "+e.getMessage());
            }
        });

         mensagemCollection = firebaseFirestore
                .collection("Chat")
                .document(mensagemQuemRecebeId)
                .collection(mensagemQuemEnviouId);

         Map<String,Object> map2 = new HashMap<>();
        map2.put("mensagem",getMensagem());
        map2.put("idEnviadoDe",getIdEnviadoDe());
        map2.put("idEnviadoPara",getIdEnviadoPara());
        map2.put("timestamp",getTimestamp());

        mensagemCollection.add(map2).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.i("salvarMensagemEnviada","Id mensagem: "+documentReference.getId());

                Contact contato = new Contact();
                contato.setIdUsuario(idEnviadoPara);
                contato.setNomeUsuario(usuario.getNomePetUsuario());
                contato.setUriCaminhoFotoPetUsuario(usuario.getUriCaminhoFotoPetUsuario());
                contato.setTimestamp(mensagem.timestamp);
                contato.setLastMensagem(mensagem.mensagem);


                firebaseFirestore.collection("LastMensagens")
                        .document(getIdEnviadoPara())
                        .collection("contatos")
                        .document(getIdEnviadoDe())
                        .set(contato);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("salvarMensagemEnviada","Erro salvar mensagem: "+e.getMessage());
            }
        });

    }

    public void receberMensagem(String mensagemQuemEnviouId,String mensagemQuemRecebeId){
        firebaseFirestore = ConfiguracaoFirebase.getFirebaseFirestore();
        CollectionReference mensagemCollection = firebaseFirestore
                .collection("Chat")
                .document(mensagemQuemRecebeId)
                .collection(mensagemQuemEnviouId);

        Map<String,Object> map = new HashMap<>();
        map.put("mensagem",getMensagem());
        map.put("idEnviadoDe",getIdEnviadoDe());
        map.put("idEnviadoPara",getIdEnviadoPara());
        map.put("timestamp",getTimestamp());

        mensagemCollection.add(map).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.i("salvarMensagemEnviada","Id mensagem: "+documentReference.getId());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("salvarMensagemEnviada","Erro salvar mensagem: "+e.getMessage());
            }
        });

    }
}
