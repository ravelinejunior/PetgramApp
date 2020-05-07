package br.com.petgramapp.model;

public class Notificacao {

    public Notificacao() {
    }

    public Notificacao(String idUsuario, String comentarioFeito, String idPostagem, Boolean isPostado) {
        this.idUsuario = idUsuario;
        this.comentarioFeito = comentarioFeito;
        this.idPostagem = idPostagem;
        this.isPostado = isPostado;
    }

    private String idUsuario;
    private String comentarioFeito;
    private String idPostagem;
    private Boolean isPostado;
    private FotoPostada fotoPostada;

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getComentarioFeito() {
        return comentarioFeito;
    }

    public void setComentarioFeito(String comentarioFeito) {
        this.comentarioFeito = comentarioFeito;
    }

    public String getIdPostagem() {
        return idPostagem;
    }

    public void setIdPostagem(String idPostagem) {
        this.idPostagem = idPostagem;
    }

    public Boolean getIsPostado() {
        return isPostado;
    }

    public void setIsPostado(Boolean isPostado) {
        this.isPostado = isPostado;
    }

    public FotoPostada getFotoPostada() {
        return fotoPostada;
    }

    public void setFotoPostada(FotoPostada fotoPostada) {
        this.fotoPostada = fotoPostada;
    }
}
