package br.com.petgramapp.model;

public class FirebaseNotification {

    private String to;
    private NotificacaoDadosFirebase notification;

    public FirebaseNotification(){

    }
    public FirebaseNotification(String to,NotificacaoDadosFirebase notification){
        this.to = to;
        this.notification = notification;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public NotificacaoDadosFirebase getNotification() {
        return notification;
    }

    public void setNotification(NotificacaoDadosFirebase notification) {
        this.notification = notification;
    }
}
