package br.com.petgramapp.model;

public class HomeFeed {
    private String nomePetUsuario;
    private String uriFotoUsuario;
    private String idUsuario;
    private String descricaoFotoPostada;
    private String fotoPostada;
    private String publicadoPor;
    private String idFotoPostada;
    private Usuario usuario;

    public HomeFeed() {

    }

    public HomeFeed(String nomePetUsuario, String uriFotoUsuario, String descricaoFotoPostada, String fotoPostada) {
        this.nomePetUsuario = nomePetUsuario;
        this.uriFotoUsuario = uriFotoUsuario;
        this.descricaoFotoPostada = descricaoFotoPostada;
        this.fotoPostada = fotoPostada;
    }

    public HomeFeed(String nomePetUsuario, String uriFotoUsuario, String descricaoFotoPostada, String fotoPostada, Usuario usuario) {
        this.nomePetUsuario = nomePetUsuario;
        this.uriFotoUsuario = uriFotoUsuario;
        this.descricaoFotoPostada = descricaoFotoPostada;
        this.fotoPostada = fotoPostada;
        this.usuario = usuario;
    }

    public String getPublicadoPor() {
        return publicadoPor;
    }

    public void setPublicadoPor(String publicadoPor) {
        this.publicadoPor = publicadoPor;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getIdFotoPostada() {
        return idFotoPostada;
    }

    public void setIdFotoPostada(String idFotoPostada) {
        this.idFotoPostada = idFotoPostada;
    }

    public String getNomePetUsuario() {
        return nomePetUsuario;
    }

    public void setNomePetUsuario(String nomePetUsuario) {
        this.nomePetUsuario = nomePetUsuario;
    }

    public String getUriFotoUsuario() {
        return uriFotoUsuario;
    }

    public void setUriFotoUsuario(String uriFotoUsuario) {
        this.uriFotoUsuario = uriFotoUsuario;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getDescricaoFotoPostada() {
        return descricaoFotoPostada;
    }

    public void setDescricaoFotoPostada(String descricaoFotoPostada) {
        this.descricaoFotoPostada = descricaoFotoPostada;
    }

    public String getFotoPostada() {
        return fotoPostada;
    }

    public void setFotoPostada(String fotoPostada) {
        this.fotoPostada = fotoPostada;
    }
}
