package br.com.petgramapp.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Contact implements Parcelable {

    public static final Creator<Contact> CREATOR = new Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };
    private String idUsuario;
    private String nomeUsuario;
    private String lastMensagem;
    private String uriCaminhoFotoPetUsuario;
    private Long timestamp;

    public Contact() {

    }

    public Contact(String idUsuario, String nomeUsuario, String lastMensagem, String uriCaminhoFotoPetUsuario, Long timestamp) {
        this.idUsuario = idUsuario;
        this.nomeUsuario = nomeUsuario;
        this.lastMensagem = lastMensagem;
        this.uriCaminhoFotoPetUsuario = uriCaminhoFotoPetUsuario;
        this.timestamp = timestamp;
    }

    protected Contact(Parcel in) {
        idUsuario = in.readString();
        nomeUsuario = in.readString();
        lastMensagem = in.readString();
        uriCaminhoFotoPetUsuario = in.readString();
        if (in.readByte() == 0) {
            timestamp = null;
        } else {
            timestamp = in.readLong();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(idUsuario);
        dest.writeString(nomeUsuario);
        dest.writeString(lastMensagem);
        dest.writeString(uriCaminhoFotoPetUsuario);
        if (timestamp == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(timestamp);
        }
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public String getLastMensagem() {
        return lastMensagem;
    }

    public void setLastMensagem(String lastMensagem) {
        this.lastMensagem = lastMensagem;
    }

    public String getUriCaminhoFotoPetUsuario() {
        return uriCaminhoFotoPetUsuario;
    }

    public void setUriCaminhoFotoPetUsuario(String uriCaminhoFotoPetUsuario) {
        this.uriCaminhoFotoPetUsuario = uriCaminhoFotoPetUsuario;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int describeContents() {
        return 0;
    }


}

