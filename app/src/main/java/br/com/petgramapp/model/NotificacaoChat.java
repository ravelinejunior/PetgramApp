package br.com.petgramapp.model;

public class NotificacaoChat extends Mensagem {

    private String fromName;

    public NotificacaoChat() {

    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }
}
