package br.com.petgramapp.model;

public class NotificacoesJam extends MensagemJam {

    private String fromName;

    public NotificacoesJam() {

    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }
}
