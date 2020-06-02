package br.com.petgramapp.model;

public class MensagemJam {

    private String id;
    private String mensagem;
    private String imagemEnviada;
    private String dataEnvio;
    private String dataRecebido;
    private Long timeStamp;

    public MensagemJam() {

    }

    public MensagemJam(String id, String mensagem, String imagemEnviada) {
        this.id = id;
        this.mensagem = mensagem;
        this.imagemEnviada = imagemEnviada;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getImagemEnviada() {
        return imagemEnviada;
    }

    public void setImagemEnviada(String imagemEnviada) {
        this.imagemEnviada = imagemEnviada;
    }

    public String getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(String dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    public String getDataRecebido() {
        return dataRecebido;
    }

    public void setDataRecebido(String dataRecebido) {
        this.dataRecebido = dataRecebido;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
