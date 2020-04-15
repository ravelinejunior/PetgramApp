package br.com.petgramapp.model;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;

import br.com.petgramapp.helper.ConfiguracaoFirebase;

public class Usuario implements Serializable {
    public Usuario() {

    }

    public Usuario(String id, String nomePetUsuario, String emailPetUsuario, String descricaoPetUsuario, String uriCaminhoFotoPetUsuario) {

        this.id = id;
        this.nomePetUsuario = nomePetUsuario;
        this.emailPetUsuario = emailPetUsuario;
        this.descricaoPetUsuario = descricaoPetUsuario;
        this.uriCaminhoFotoPetUsuario = uriCaminhoFotoPetUsuario;
    }

    Random random = new Random();
    private String id;
    private String nomePetUsuario;
    private String nomePetUsuarioUp;
    private String emailPetUsuario;
    private String idadePetUsuario;
    private String descricaoPetUsuario;
    private String sexoPetUsuario;
    private String senhaPetUsuario;
    private String confirmacaoSenhaPetUsuario;
    private String uriCaminhoFotoPetUsuario;
    private int fotos = 0;
    private int clientes = random.nextInt(3500);
    private int fas = random.nextInt(3500);

    public void atualizarFotosPostadas(){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getReferenciaDatabase();
        DatabaseReference usuariosRef = firebaseRef.child("usuarios").child(getId());
        HashMap<String,Object> fotosPostadas = new HashMap<>();
        fotosPostadas.put("fotos",getFotos());
        usuariosRef.updateChildren(fotosPostadas);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNomePetUsuario() {
        return nomePetUsuario;
    }

    public void setNomePetUsuario(String nomePetUsuario) {
        this.nomePetUsuario = nomePetUsuario;
    }

    public String getNomePetUsuarioUp() {
        this.nomePetUsuarioUp = getNomePetUsuario().toLowerCase();
        return nomePetUsuarioUp;
    }

    public void setNomePetUsuarioUp(String nomePetUsuarioUp) {
        this.nomePetUsuarioUp = nomePetUsuarioUp.toLowerCase();
    }

    public int getFotos() {
        return fotos;
    }

    public void setFotos(int fotos) {
        this.fotos = fotos;
    }

    public int getClientes() {
        return clientes;
    }

    public void setClientes(int clientes) {
        this.clientes = clientes;
    }

    public int getFas() {
        return fas;
    }

    public void setFas(int fas) {
        this.fas = fas;
    }

    public String getEmailPetUsuario() {
        return emailPetUsuario;
    }

    public void setEmailPetUsuario(String emailPetUsuario) {
        this.emailPetUsuario = emailPetUsuario;
    }

    public String getIdadePetUsuario() {
        return idadePetUsuario;
    }

    public void setIdadePetUsuario(String idadePetUsuario) {
        this.idadePetUsuario = idadePetUsuario;
    }

    public String getDescricaoPetUsuario() {
        return descricaoPetUsuario;
    }

    public void setDescricaoPetUsuario(String descricaoPetUsuario) {
        this.descricaoPetUsuario = descricaoPetUsuario;
    }

    public String getSexoPetUsuario() {
        return sexoPetUsuario.toLowerCase();
    }

    public void setSexoPetUsuario(String sexoPetUsuario) {
        this.sexoPetUsuario = sexoPetUsuario.toLowerCase();
    }

    @Exclude
    public String getSenhaPetUsuario() {
        return senhaPetUsuario;
    }

    public void setSenhaPetUsuario(String senhaPetUsuario) {
        this.senhaPetUsuario = senhaPetUsuario;
    }

    public String getConfirmacaoSenhaPetUsuario() {
        return confirmacaoSenhaPetUsuario;
    }

    public void setConfirmacaoSenhaPetUsuario(String confirmacaoSenhaPetUsuario) {
        this.confirmacaoSenhaPetUsuario = confirmacaoSenhaPetUsuario;
    }

    public String getUriCaminhoFotoPetUsuario() {
        return uriCaminhoFotoPetUsuario;
    }

    public void setUriCaminhoFotoPetUsuario(String uriCaminhoFotoPetUsuario) {
        this.uriCaminhoFotoPetUsuario = uriCaminhoFotoPetUsuario;
    }

    public boolean salvarUsuario(){
        DatabaseReference databaseReference = ConfiguracaoFirebase.getReferenciaDatabase();
        DatabaseReference usuariosRef = databaseReference.child("usuarios").child(getId());

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("id",getId());
        hashMap.put("nomePetUsuario",getNomePetUsuario());
        hashMap.put("nomePetUsuarioUp",getNomePetUsuarioUp());
        hashMap.put("emailPetUsuario",getEmailPetUsuario());
        hashMap.put("idadePetUsuario",getIdadePetUsuario());
        hashMap.put("descricaoPetUsuario",getDescricaoPetUsuario());
        hashMap.put("sexoPetUsuario",getSexoPetUsuario());
        hashMap.put("senhaPetUsuario",getSenhaPetUsuario());
        hashMap.put("uriCaminhoFotoPetUsuario","https://firebasestorage.googleapis.com/v0/b/petgramapp.appspot.com/o/placeholder.png?alt=media&token=4c92c48f-bcf9-4643-a620-6495c29d1e73");

        usuariosRef.setValue(hashMap);

        return true;
    }
}
