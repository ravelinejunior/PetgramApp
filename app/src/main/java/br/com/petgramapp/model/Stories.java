package br.com.petgramapp.model;

import java.io.Serializable;

public class Stories implements Serializable {
    private long dataFim;
    private long dataInicio;
    private String idUsuario;
    private String idStories;
    private String urlStoriesFoto;

    public Stories() {

    }

    public Stories(long dataFim, long dataInicio, String idUsuario, String idStories, String urlStoriesFoto) {
        this.dataFim = dataFim;
        this.dataInicio = dataInicio;
        this.idUsuario = idUsuario;
        this.idStories = idStories;
        this.urlStoriesFoto = urlStoriesFoto;
    }

    public long getDataFim() {
        return dataFim;
    }

    public void setDataFim(long dataFim) {
        this.dataFim = dataFim;
    }

    public long getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(long dataInicio) {
        this.dataInicio = dataInicio;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getIdStories() {
        return idStories;
    }

    public void setIdStories(String idStories) {
        this.idStories = idStories;
    }

    public String getUrlStoriesFoto() {
        return urlStoriesFoto;
    }

    public void setUrlStoriesFoto(String urlStoriesFoto) {
        this.urlStoriesFoto = urlStoriesFoto;
    }
}
