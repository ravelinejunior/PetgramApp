package br.com.petgramapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.petgramapp.helper.ConfiguracaoFirebase;

public class Conversas implements Parcelable, Serializable {


    private String idRemetente;
    private String idDestinatario;
    private String ultimaMensagem;
    private String dataEnvio;
    private Long timeStamp;
    private Usuario usuario;
    private List<Usuario> usuarioUnicoList = new ArrayList<>();
    public static final Creator<Conversas> CREATOR = new Creator<Conversas>() {
        @Override
        public Conversas createFromParcel(Parcel in) {
            return new Conversas(in);
        }

        @Override
        public Conversas[] newArray(int size) {
            return new Conversas[size];
        }
    };
    private String isGroup;


    public Conversas(String idRemetente, String idDestinatario, String ultimaMensagem, String dataEnvio, Long timeStamp, Usuario usuario) {
        this.idRemetente = idRemetente;
        this.idDestinatario = idDestinatario;
        this.ultimaMensagem = ultimaMensagem;
        this.dataEnvio = dataEnvio;
        this.timeStamp = timeStamp;
        this.usuario = usuario;
    }
    private GrupoJam grupoJam;

    public Conversas() {
        this.setIsGroup("false");
    }

    protected Conversas(Parcel in) {
        idRemetente = in.readString();
        idDestinatario = in.readString();
        ultimaMensagem = in.readString();
        dataEnvio = in.readString();
        if (in.readByte() == 0) {
            timeStamp = null;
        } else {
            timeStamp = in.readLong();
        }
        usuario = in.readParcelable(Usuario.class.getClassLoader());
        usuarioUnicoList = in.createTypedArrayList(Usuario.CREATOR);
        isGroup = in.readString();
    }

    public String getIdRemetente() {
        return idRemetente;
    }

    public void setIdRemetente(String idRemetente) {
        this.idRemetente = idRemetente;
    }

    public String getIdDestinatario() {
        return idDestinatario;
    }

    public void setIdDestinatario(String idDestinatario) {
        this.idDestinatario = idDestinatario;
    }

    public String getUltimaMensagem() {
        return ultimaMensagem;
    }

    public void setUltimaMensagem(String ultimaMensagem) {
        this.ultimaMensagem = ultimaMensagem;
    }

    public String getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(String dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getIsGroup() {
        return isGroup;
    }

    public void setIsGroup(String isGroup) {
        this.isGroup = isGroup;
    }

    public GrupoJam getGrupoJam() {
        return grupoJam;
    }

    public void setGrupoJam(GrupoJam grupoJam) {
        this.grupoJam = grupoJam;
    }

    public void salvarConversa() {
        FirebaseFirestore firebaseFirestore = ConfiguracaoFirebase.getFirebaseFirestore();
        Map<String, Object> hash = new HashMap<>();
        hash.put("idRemetente", this.idRemetente);
        hash.put("idDestinatario", this.idDestinatario);
        hash.put("ultimaMensagem", this.ultimaMensagem);
        hash.put("dataEnvio", this.dataEnvio);
        hash.put("timeStamp", this.timeStamp);
        hash.put("usuario", this.usuario);

        firebaseFirestore.collection("Talks")
                .document("Conversas")
                .collection(this.idRemetente)
                .document(this.idDestinatario).
                set(this);

    }

    public void salvarConversaOutroUsuario(Usuario usuario) {
        FirebaseFirestore firebaseFirestore = ConfiguracaoFirebase.getFirebaseFirestore();
        setUsuario(usuario);

        firebaseFirestore.collection("Talks")
                .document("Conversas")
                .collection(this.idDestinatario)
                .document(this.idRemetente)
                .set(this);

    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(idRemetente);
        dest.writeString(idDestinatario);
        dest.writeString(ultimaMensagem);
        dest.writeString(dataEnvio);
        if (timeStamp == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(timeStamp);
        }
        dest.writeParcelable(usuario, flags);
        dest.writeTypedList(usuarioUnicoList);
        dest.writeString(isGroup);
    }
}


