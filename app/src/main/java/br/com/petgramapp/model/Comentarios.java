package br.com.petgramapp.model;

import java.io.Serializable;

public class Comentarios implements Serializable {

    private String comentario;
    private String idQuemPublicou;
    private Usuario usuario;

    public Comentarios() {

    }

    public Comentarios(String comentario, String idQuemPublicou, Usuario usuario) {
        this.comentario = comentario;
        this.idQuemPublicou = idQuemPublicou;
        this.usuario = usuario;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public String getIdQuemPublicou() {
        return idQuemPublicou;
    }

    public void setIdQuemPublicou(String idQuemPublicou) {
        this.idQuemPublicou = idQuemPublicou;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
