package br.com.petgramapp.model;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import br.com.petgramapp.helper.ConfiguracaoFirebase;


public class Video {
    private String idVideo;
    private String idUsuarioPostou;
    private String videoPostado;
    private String dataPostada;
    private String descricaoVideoPostado;
    private Usuario usuario;



    public Video() {

        DatabaseReference databaseReference = ConfiguracaoFirebase.getReferenciaDatabase();
        DatabaseReference videoPostRef = databaseReference.child("Video");
        String idVideo = videoPostRef.push().getKey();
        setIdVideo(idVideo);

    }

    //utilizar estratégia FenOut de espalhamento
    public boolean salvarVideoPostada() {

        //objeto para atualização
        Map objeto = new HashMap();
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getReferenciaDatabase();

        String combinacaodeId = "/"+getIdVideo();
        objeto.put("/Video"+combinacaodeId+"/videoPostado",getVideoPostado());
        objeto.put("/Video"+combinacaodeId+"/idPostagem",getIdVideo());
        objeto.put("/Video"+combinacaodeId+"/idUsuarioPostou",getIdUsuarioPostou());
        objeto.put("/Video"+combinacaodeId+"/dataPostada",getDataPostada());

        objeto.put("/Video"+combinacaodeId+"/usuario/nomePetUsuario",getUsuario().getNomePetUsuario());
        objeto.put("/Video"+combinacaodeId+"/usuario/emailPetUsuario",getUsuario().getEmailPetUsuario());
        objeto.put("/Video"+combinacaodeId+"/usuario/uriCaminhoFotoPetUsuario",getUsuario().getUriCaminhoFotoPetUsuario());


        firebaseRef.updateChildren(objeto);
        return true;
    }

    public boolean salvarVideoFireStore() {

        //objeto para atualização
        Map objeto = new HashMap();
        FirebaseFirestore firebaseFirestore = ConfiguracaoFirebase.getFirebaseFirestore();

        objeto.put("videoPostado",getVideoPostado());
        objeto.put("idVideoPostado",getIdVideo());
        objeto.put("idUsuarioPostou",getIdUsuarioPostou());
        objeto.put("dataPostada",getDataPostada());
        objeto.put("nomePetUsuario",getUsuario().getNomePetUsuario());
        objeto.put("emailPetUsuario",getUsuario().getEmailPetUsuario());


        firebaseFirestore.collection("Video")
                .document(getIdVideo())
                .set(objeto);


        return true;
    }

    public String getIdVideo() {
        return idVideo;
    }

    public void setIdVideo(String idVideo) {
        this.idVideo = idVideo;
    }

    public String getIdUsuarioPostou() {
        return idUsuarioPostou;
    }

    public void setIdUsuarioPostou(String idUsuarioPostou) {
        this.idUsuarioPostou = idUsuarioPostou;
    }

    public String getVideoPostado() {
        return videoPostado;
    }

    public void setVideoPostado(String videoPostado) {
        this.videoPostado = videoPostado;
    }

    public String getDataPostada() {
        return dataPostada;
    }

    public void setDataPostada(String dataPostada) {
        this.dataPostada = dataPostada;
    }

    public String getDescricaoVideoPostado() {
        return descricaoVideoPostado;
    }

    public void setDescricaoVideoPostado(String descricaoVideoPostado) {
        this.descricaoVideoPostado = descricaoVideoPostado;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
