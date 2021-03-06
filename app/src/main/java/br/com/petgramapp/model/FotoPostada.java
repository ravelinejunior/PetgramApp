package br.com.petgramapp.model;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.helper.UsuarioFirebase;

public class FotoPostada {

    public FotoPostada() {

        DatabaseReference databaseReference = ConfiguracaoFirebase.getReferenciaDatabase();
        DatabaseReference imagemPostadaRef = databaseReference.child("Posts");
        String idImagemPostada = imagemPostadaRef.push().getKey();
        setIdPostagem(idImagemPostada);

    }


    private String idPostagem;
    private String idUsuarioPostou;
    private String imagemPostada;
    private String dataPostada;
    private String descricaoImagemPostada;
    private Usuario usuario;

    public String getIdPostagem() {
        return idPostagem;
    }

    public void setIdPostagem(String idPostagem) {
        this.idPostagem = idPostagem;
    }

    public String getIdUsuarioPostou() {
        return idUsuarioPostou;
    }

    public void setIdUsuarioPostou(String idUsuarioPostou) {
        this.idUsuarioPostou = idUsuarioPostou;
    }

    public String getImagemPostada() {
        return imagemPostada;
    }

    public void setImagemPostada(String imagemPostada) {
        this.imagemPostada = imagemPostada;
    }

    public String getDescricaoImagemPostada() {
        return descricaoImagemPostada;
    }

    public void setDescricaoImagemPostada(String descricaoImagemPostada) {
        this.descricaoImagemPostada = descricaoImagemPostada;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getDataPostada() {
        return dataPostada;
    }

    public void setDataPostada(String dataPostada) {
        this.dataPostada = dataPostada;
    }

    //utilizar estratégia FenOut de espalhamento
    public boolean salvarFotoPostada() {

        //objeto para atualização
        Map objeto = new HashMap();
        Usuario usuarioLogado = UsuarioFirebase.getUsuarioLogado();
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getReferenciaDatabase();

        String combinacaodeId = "/"+getIdPostagem();
        objeto.put("/Posts"+combinacaodeId+"/imagemPostada",getImagemPostada());
        objeto.put("/Posts"+combinacaodeId+"/idPostagem",getIdPostagem());
        objeto.put("/Posts"+combinacaodeId+"/idUsuarioPostou",getIdUsuarioPostou());
        objeto.put("/Posts"+combinacaodeId+"/descricaoImagemPostada",getDescricaoImagemPostada());
        objeto.put("/Posts"+combinacaodeId+"/dataPostada",getDataPostada());

        objeto.put("/Posts"+combinacaodeId+"/usuario/nomePetUsuario",getUsuario().getNomePetUsuario());
        objeto.put("/Posts"+combinacaodeId+"/usuario/emailPetUsuario",getUsuario().getEmailPetUsuario());
        objeto.put("/Posts"+combinacaodeId+"/usuario/uriCaminhoFotoPetUsuario",getUsuario().getUriCaminhoFotoPetUsuario());


        firebaseRef.updateChildren(objeto);
        return true;
        }

    public boolean salvarFotoPostadaFireStore() {

        //objeto para atualização
        Map objeto = new HashMap();
        FirebaseFirestore firebaseFirestore = ConfiguracaoFirebase.getFirebaseFirestore();

        objeto.put("imagemPostada",getImagemPostada());
        objeto.put("idPostagem",getIdPostagem());
        objeto.put("idUsuarioPostou",getIdUsuarioPostou());
        objeto.put("descricaoImagemPostada",getDescricaoImagemPostada());
        objeto.put("dataPostada",getDataPostada());

        objeto.put("nomePetUsuario",getUsuario().getNomePetUsuario());
        objeto.put("emailPetUsuario",getUsuario().getEmailPetUsuario());
        objeto.put("uriCaminhoFotoPetUsuario",getUsuario().getUriCaminhoFotoPetUsuario());

        firebaseFirestore.collection("Posts")
                .document(getIdPostagem())
                .set(objeto);


        return true;
    }



}
